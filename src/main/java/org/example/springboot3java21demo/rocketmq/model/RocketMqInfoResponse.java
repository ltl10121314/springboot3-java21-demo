package org.example.springboot3java21demo.rocketmq.model;

import org.example.springboot3java21demo.rocketmq.config.RocketMqProperties;

public record RocketMqInfoResponse(
        boolean enabled,
        String endpoint,
        String topic,
        String businessConsumerGroup,
        String businessTags,
        String auditConsumerGroup,
        String auditTags) {

    public static RocketMqInfoResponse from(RocketMqProperties properties) {
        return new RocketMqInfoResponse(
                properties.isEnabled(),
                properties.getEndpoint(),
                properties.getTopic(),
                properties.getBusinessConsumerGroup(),
                properties.getBusinessTags(),
                properties.getAuditConsumerGroup(),
                properties.getAuditTags());
    }
}
