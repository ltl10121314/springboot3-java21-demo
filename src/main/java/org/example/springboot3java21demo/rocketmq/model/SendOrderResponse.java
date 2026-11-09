package org.example.springboot3java21demo.rocketmq.model;

import org.apache.rocketmq.client.apis.producer.SendReceipt;

import java.time.Instant;

public record SendOrderResponse(
        String eventId,
        String orderId,
        OrderEventType eventType,
        String topic,
        String tag,
        String messageId,
        String recallHandle,
        String status,
        Instant sentAt) {

    public static SendOrderResponse from(
            OrderEvent event,
            String topic,
            SendReceipt receipt) {
        return new SendOrderResponse(
                event.eventId(),
                event.orderId(),
                event.eventType(),
                topic,
                event.eventType().tag(),
                receipt.getMessageId().toString(),
                receipt.getRecallHandle(),
                "SENT",
                Instant.now());
    }
}
