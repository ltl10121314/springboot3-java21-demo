package org.example.springboot3java21demo.payment.infra.outbox;

import org.example.springboot3java21demo.payment.domain.OutboxEvent;

/**
 * 消息投递端口。生产实现为 Kafka/RocketMQ 生产者，投递失败抛异常由中继任务重试。
 */
public interface OutboxPublisher {

    void publish(OutboxEvent event) throws Exception;
}
