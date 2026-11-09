package org.example.springboot3java21demo.rocketmq.model;

/**
 * 一次正常 Topic 消费尝试的结果。
 */
public enum ConsumeStatus {
    CONSUMED,
    DUPLICATE,
    RETRY
}
