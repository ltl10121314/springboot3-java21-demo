package org.example.springboot3java21demo.payment.infra.outbox;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot3java21demo.payment.domain.OutboxEvent;

/**
 * 演示用投递实现：只打日志（带 traceId），生产替换为 MQ 生产者。
 */
@Slf4j
public class LoggingOutboxPublisher implements OutboxPublisher {

    @Override
    public void publish(OutboxEvent event) {
        log.info("[{}] 投递领域事件 type={} aggregateId={} eventId={} payload={}",
                event.traceId(), event.eventType(), event.aggregateId(), event.eventId(), event.payload());
    }
}
