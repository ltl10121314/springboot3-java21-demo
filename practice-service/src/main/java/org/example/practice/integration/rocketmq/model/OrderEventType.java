package org.example.practice.integration.rocketmq.model;

/**
 * 订单事件类型，同时作为 RocketMQ Tag 使用。
 */
public enum OrderEventType {

    ORDER_CREATED("订单已创建"),
    ORDER_PAID("订单已支付");

    private final String description;

    OrderEventType(String description) {
        this.description = description;
    }

    public String tag() {
        return name();
    }

    public String getDescription() {
        return description;
    }
}
