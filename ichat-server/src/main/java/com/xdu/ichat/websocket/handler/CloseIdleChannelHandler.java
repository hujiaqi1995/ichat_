package com.xdu.ichat.websocket.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author hujiaqi
 * @create 2020/6/26
 */
@ChannelHandler.Sharable
@Component
public class CloseIdleChannelHandler extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(CloseIdleChannelHandler.class);

    @Autowired
    private WebsocketRouterHandler websocketRouterHandler;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.ALL_IDLE) {
                logger.info("connector no receive ping packet from client,will close.,channel:{}", ctx.channel());
                websocketRouterHandler.cleanUserChannel(ctx.channel());
                ctx.close();
            }
        }
    }
}
