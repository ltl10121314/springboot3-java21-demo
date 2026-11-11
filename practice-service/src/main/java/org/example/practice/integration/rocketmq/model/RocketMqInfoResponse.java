package org.example.practice.integration.rocketmq.model;

import org.example.practice.integration.rocketmq.config.RocketMqProperties;

public record RocketMqInfoResponse(
        boolean enabled,
        String endpoint,
        String topic,
        String delayTopic,
        String businessConsumerGroup,
        String businessTags,
        String auditConsumerGroup,
        String auditTags,
        String deadLetterConsumerGroup,
        String businessDeadLetterTopic,
        String auditDeadLetterTopic) {

    public static RocketMqInfoResponse from(RocketMqProperties properties) {
        return new RocketMqInfoResponse(
                properties.isEnabled(),
                properties.getEndpoint(),
                properties.getTopic(),
                properties.getDelayTopic(),
                properties.getBusinessConsumerGroup(),
                properties.getBusinessTags(),
                properties.getAuditConsumerGroup(),
                properties.getAuditTags(),
                properties.getDeadLetterConsumerGroup(),
                properties.getBusinessDeadLetterTopic(),
                properties.getAuditDeadLetterTopic());
    }
}
