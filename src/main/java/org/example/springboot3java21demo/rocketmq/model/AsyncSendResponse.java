package org.example.springboot3java21demo.rocketmq.model;

public record AsyncSendResponse(
        String eventId,
        String orderId,
        String status,
        String topic,
        String tag) {
}
