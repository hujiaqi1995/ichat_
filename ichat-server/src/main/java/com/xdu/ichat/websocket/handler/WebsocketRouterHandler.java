package com.xdu.ichat.websocket.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.xdu.ichat.entity.vo.MsgVO;
import com.xdu.ichat.service.MsgService;
import com.xdu.ichat.utils.EnhancedThreadFactory;

import com.xdu.ichat.utils.JsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.xdu.ichat.utils.Constants.*;

/**
 * @author hujiaqi
 * @create 2020/6/26
 */
@Slf4j
@ChannelHandler.Sharable
@Component
public class WebsocketRouterHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final ConcurrentHashMap<Long, Channel> userChannel = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Channel, Long> channelUser = new ConcurrentHashMap<>();
    private ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(5, new EnhancedThreadFactory("ackCheckingThreadPool"));
    private static final AttributeKey<AtomicLong> TID_GENERATOR = AttributeKey.valueOf("tid_generator");
    private static final AttributeKey<ConcurrentHashMap> NON_ACKED_MAP = AttributeKey.valueOf("non_acked_map");

    private Gson gson = new Gson();

    @Value("${message.retryTimes}")
    private int retryTimes;

    @Autowired
    private MsgService msgService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            String msgJson = ((TextWebSocketFrame) frame).text();
            JsonObject messageJson = JsonUtil.parseString(msgJson);
            if (messageJson == null) {
                ctx.writeAndFlush(new TextWebSocketFrame("received: " + msgJson));
                return;
            }
            int type = JsonUtil.getInt(messageJson, "type");
            JsonObject data = JsonUtil.getJsonObject(messageJson, "data");
            if (data == null) {
                return;
            }
            JsonObject message = new JsonObject();
            switch (type) {
                case IM_HEARTBEAT:
                    long uid = JsonUtil.getLong(data, "uid");
                    long timeout = JsonUtil.getLong(data, "timeout");
                    log.info("[heartbeat]: uid = {} , current timeout is {} ms, channel = {}", uid, timeout, ctx.channel());
                    message.addProperty("type", IM_HEARTBEAT);
                    message.addProperty("timeout", timeout);
                    ctx.writeAndFlush(new TextWebSocketFrame(message.toString()));
                    break;
                case IM_ONLINE:
                    long loginUid = JsonUtil.getLong(data, "uid");
                    userChannel.put(loginUid, ctx.channel());
                    channelUser.put(ctx.channel(), loginUid);
                    ctx.channel().attr(TID_GENERATOR).set(new AtomicLong());
                    ctx.channel().attr(NON_ACKED_MAP).set(new ConcurrentHashMap<Long, JsonObject>());
                    log.info("[user bind]: uid = {} , channel = {}", loginUid, ctx.channel());
                    message.addProperty("type", IM_ONLINE);
                    message.addProperty("status", "success");
                    ctx.writeAndFlush(new TextWebSocketFrame(message.toString()));
                    break;
                case IM_QUERY_MESSAGE:
                    long ownerUid = JsonUtil.getLong(data, "ownerUid");
                    long otherUid = JsonUtil.getLong(data, "otherUid");
                    MsgVO msgVO = msgService.queryConversationMsg(ownerUid, otherUid);
                    if (msgVO != null) {
                        message.addProperty("type", IM_QUERY_MESSAGE);
                        message.addProperty("data", gson.toJson(msgVO));
                    }
                    ctx.writeAndFlush(new TextWebSocketFrame(message.toString()));
                    break;
                case IM_SEND_MESSAGE:
                    long senderUid = JsonUtil.getLong(data, "senderUid");
                    long recipientUid = JsonUtil.getLong(data, "recipientUid");
                    String content = JsonUtil.getString(data, "content");
                    int msgType = JsonUtil.getInt(data, "msgType");
                    MsgVO messageVo = msgService.sendNewMsg(senderUid, recipientUid, content, msgType);
                    if (messageVo != null) {
                        message.addProperty("type", IM_SEND_MESSAGE);
                        message.addProperty("data", gson.toJson(messageVo));
                    }
                    ctx.writeAndFlush(new TextWebSocketFrame(message.toString()));
                    break;
                case IM_QUERY_TOTAL_UNREAD:
                    long unreadOwnerUid = data.get("uid").getAsLong();
                    long totalUnread = msgService.queryTotalUnread(unreadOwnerUid);
                    message.addProperty("type", IM_QUERY_TOTAL_UNREAD);
                    JsonObject dataBack = new JsonObject();
                    dataBack.addProperty("unread", totalUnread);
                    message.add("data", dataBack);
                    ctx.writeAndFlush(new TextWebSocketFrame(message.toString()));
                    break;
                case IM_CLIENT_ACK:
                    long tid = data.get("tid").getAsLong();
                    ConcurrentHashMap<Long, JsonObject> nonAckedMap = ctx.channel().attr(NON_ACKED_MAP).get();
                    nonAckedMap.remove(tid);
                    break;
                default:

            }

        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("[channelActive]:remote address is {} ", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("[channelClosed]:remote address is {} ", ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("process error. uid is {},  channel info {}", channelUser.get(ctx.channel()), ctx.channel(), cause);
        ctx.channel().close();
    }

    public void pushMsg(long recipientUid, JsonObject message) {
        Channel channel = userChannel.get(recipientUid);
        if (channel != null && channel.isActive() && channel.isWritable()) {
            AtomicLong generator = channel.attr(TID_GENERATOR).get();
            long tid = generator.incrementAndGet();
            message.addProperty("tid", tid);
            channel.writeAndFlush(new TextWebSocketFrame(message.toString())).addListener(future -> {
                if (future.isSuccess()) {
                    addMsgToAckBuffer(channel, message);
                    log.warn("future has been successfully pushed. {}, channel: {}", message, channel);
                } else if (future.isCancelled()) {
                    log.warn("future has been cancelled. {}, channel: {}", message, channel);
                } else {
                    log.error("message write fail, {}, channel: {}", message, channel, future.cause());
                }
            });
        }
    }

    /**
     * 清除用户和socket映射的相关信息
     *
     * @param channel
     */
    public void cleanUserChannel(Channel channel) {
        long uid = channelUser.remove(channel);
        userChannel.remove(uid);
        log.info("[cleanChannel]:remove uid & channel info from gateway, uid is {}, channel is {}", uid, channel);
    }

    /**
     * 将推送的消息加入待ack列表
     *
     * @param channel
     * @param msgJson
     */
    public void addMsgToAckBuffer(Channel channel, JsonObject msgJson) {
        channel.attr(NON_ACKED_MAP).get().put(msgJson.get("tid").getAsLong(), msgJson);
        executorService.schedule(() -> {
            if (channel.isActive()) {
                checkAndResend(channel, msgJson);
            }
        }, 5000, TimeUnit.MILLISECONDS);
    }

    /**
     * 检查并重推
     *
     * @param channel
     * @param msgJson
     */
    private void checkAndResend(Channel channel, JsonObject msgJson) {
        long tid = msgJson.get("tid").getAsLong();
        int tryTimes = retryTimes;
        while (tryTimes > 0) {
            if (channel.attr(NON_ACKED_MAP).get().containsKey(tid)) {
                channel.writeAndFlush(new TextWebSocketFrame(msgJson.toString()));
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            tryTimes--;
        }
    }
}
