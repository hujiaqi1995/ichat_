package com.xdu.ichat.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xdu.ichat.websocket.handler.WebsocketRouterHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

/**
 * @author hujiaqi
 * @create 2020/6/26
 */

@Slf4j
@Component
public class MsgSubscriber implements MessageListener {

    @Autowired
    private WebsocketRouterHandler websocketRouterHandler;

    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String topic = stringRedisSerializer.deserialize(message.getChannel());
        String jsonMsg = stringRedisSerializer.deserialize(message.getBody());
        log.info("Message Received --> pattern: {}，topic:{}，message: {}", new String(pattern), topic, jsonMsg);

        JsonObject msgJson = JsonParser.parseString(jsonMsg).getAsJsonObject();
        Long otherUid = msgJson.get("other").getAsJsonObject().get("uid").getAsLong();
        JsonObject pushJson = new JsonObject();
        pushJson.addProperty("type", 4);
        pushJson.add("data", msgJson);
        websocketRouterHandler.pushMsg(otherUid, pushJson);
    }
}
