package org.example.practice.integration.rocketmq.common;

import org.apache.rocketmq.client.apis.message.MessageView;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public final class RocketMqMessageSupport {

    private RocketMqMessageSupport() {
    }

    public static byte[] bodyBytes(MessageView message) {
        ByteBuffer body = message.getBody().duplicate();
        byte[] bytes = new byte[body.remaining()];
        body.get(bytes);
        return bytes;
    }

    public static String bodyText(MessageView message) {
        return new String(bodyBytes(message), StandardCharsets.UTF_8);
    }

    public static String keys(MessageView message) {
        Collection<String> keys = message.getKeys();
        return keys == null ? "" : String.join(",", keys);
    }
}
