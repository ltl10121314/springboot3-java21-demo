package org.example.springboot3java21demo.rocketmq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RocketMQ 示例的完整配置。
 *
 * <p>Topic 是消息的一级分类，Tag 是 Topic 内的二级分类，
 * ConsumerGroup 决定多个消费者实例之间如何分摊消息。</p>
 */
@ConfigurationProperties(prefix = "app.rocketmq")
public class RocketMqProperties {

    private boolean enabled;
    private String endpoint = "127.0.0.1:8081";
    private String topic = "practice-order-topic";
    private String businessConsumerGroup = "practice-order-business-group";
    private String auditConsumerGroup = "practice-order-audit-group";
    private String businessTags = "ORDER_CREATED || ORDER_PAID";
    private String auditTags = "ORDER_CREATED";
    private int requestTimeoutMs = 3000;
    private int sendMaxAttempts = 3;
    private int consumeThreadCount = 2;
    private int recentMessageLimit = 100;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getBusinessConsumerGroup() {
        return businessConsumerGroup;
    }

    public void setBusinessConsumerGroup(String businessConsumerGroup) {
        this.businessConsumerGroup = businessConsumerGroup;
    }

    public String getAuditConsumerGroup() {
        return auditConsumerGroup;
    }

    public void setAuditConsumerGroup(String auditConsumerGroup) {
        this.auditConsumerGroup = auditConsumerGroup;
    }

    public String getBusinessTags() {
        return businessTags;
    }

    public void setBusinessTags(String businessTags) {
        this.businessTags = businessTags;
    }

    public String getAuditTags() {
        return auditTags;
    }

    public void setAuditTags(String auditTags) {
        this.auditTags = auditTags;
    }

    public int getRequestTimeoutMs() {
        return requestTimeoutMs;
    }

    public void setRequestTimeoutMs(int requestTimeoutMs) {
        this.requestTimeoutMs = requestTimeoutMs;
    }

    public int getSendMaxAttempts() {
        return sendMaxAttempts;
    }

    public void setSendMaxAttempts(int sendMaxAttempts) {
        this.sendMaxAttempts = sendMaxAttempts;
    }

    public int getConsumeThreadCount() {
        return consumeThreadCount;
    }

    public void setConsumeThreadCount(int consumeThreadCount) {
        this.consumeThreadCount = consumeThreadCount;
    }

    public int getRecentMessageLimit() {
        return recentMessageLimit;
    }

    public void setRecentMessageLimit(int recentMessageLimit) {
        this.recentMessageLimit = recentMessageLimit;
    }
}
