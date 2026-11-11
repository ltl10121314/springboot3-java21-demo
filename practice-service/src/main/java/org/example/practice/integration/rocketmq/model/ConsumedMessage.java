package org.example.practice.integration.rocketmq.model;

import java.time.Instant;

/**
 * 消费结果快照，用于通过 HTTP 接口观察不同 ConsumerGroup 的消费情况。
 */
public record ConsumedMessage(
        long sequence,
        String consumerGroup,
        String messageId,
        String topic,
        String tag,
        String key,
        int deliveryAttempt,
        ConsumeStatus status,
        String payload,
        Instant consumedAt) {
}
