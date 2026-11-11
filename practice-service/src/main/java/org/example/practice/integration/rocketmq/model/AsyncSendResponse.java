package org.example.practice.integration.rocketmq.model;

public record AsyncSendResponse(
        String eventId,
        String orderId,
        String status,
        String topic,
        String tag,
        Integer delaySeconds,
        String failConsumerGroup) {
}
