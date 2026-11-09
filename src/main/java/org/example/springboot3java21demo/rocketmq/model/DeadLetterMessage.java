package org.example.springboot3java21demo.rocketmq.model;

import java.time.Instant;

/**
 * 从 RocketMQ %DLQ% Topic 消费到的死信快照。
 */
public record DeadLetterMessage(
        long sequence,
        String recordId,
        String consumerGroup,
        String deadLetterTopic,
        String messageId,
        String tag,
        String key,
        int deliveryAttempt,
        String payload,
        Instant receivedAt,
        boolean replayed) {

    public DeadLetterMessage withReplayed(boolean value) {
        return new DeadLetterMessage(
                sequence,
                recordId,
                consumerGroup,
                deadLetterTopic,
                messageId,
                tag,
                key,
                deliveryAttempt,
                payload,
                receivedAt,
                value);
    }
}
