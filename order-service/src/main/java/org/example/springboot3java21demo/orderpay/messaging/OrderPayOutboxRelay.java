package org.example.springboot3java21demo.orderpay.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.springboot3java21demo.orderpay.config.OrderPayProperties;
import org.example.springboot3java21demo.orderpay.domain.OutboxRecord;
import org.example.springboot3java21demo.orderpay.repository.OutboxJdbcRepository;
import org.example.springboot3java21demo.orderpay.service.OrderEventOutbox;
import org.example.springboot3java21demo.common.trace.TraceConstants;
import org.example.springboot3java21demo.common.trace.TraceContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OrderPayOutboxRelay {

    private final OutboxJdbcRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectProvider<OrderPayRocketMqClient> rocketClientProvider;
    private final OrderPayProperties properties;
    private final ObjectMapper objectMapper;

    public OrderPayOutboxRelay(
            OutboxJdbcRepository outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectProvider<OrderPayRocketMqClient> rocketClientProvider,
            OrderPayProperties properties,
            ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.rocketClientProvider = rocketClientProvider;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${order-pay.outbox.relay-interval-ms:1000}")
    public void relay() {
        for (OutboxRecord event : outboxRepository.findDue(properties.getOutbox().getBatchSize())) {
            try (TraceContext.Scope ignored = TraceContext.openIfPresent(resolveTraceId(event))) {
                try {
                    log.debug("准备投递 Outbox 事件 eventId={}, type={}, destination={}, retryCount={}",
                            event.eventId(), event.eventType(), event.destination(), event.retryCount());
                    if (OrderEventOutbox.KAFKA.equals(event.destination())) {
                        ProducerRecord<String, String> record = new ProducerRecord<>(
                                properties.getKafka().getTopic(),
                                null,
                                event.aggregateId(),
                                event.payload());
                        String traceId = TraceContext.current();
                        if (traceId != null) {
                            record.headers().add(
                                    TraceConstants.TRACE_ID_MDC_KEY,
                                    traceId.getBytes(StandardCharsets.UTF_8));
                        }
                        kafkaTemplate.send(record).get(5, TimeUnit.SECONDS);
                    } else if (OrderEventOutbox.ROCKETMQ.equals(event.destination())) {
                        OrderPayRocketMqClient rocketClient = rocketClientProvider.getIfAvailable();
                        if (rocketClient == null) {
                            throw new IllegalStateException("RocketMQ 客户端未启用");
                        }
                        rocketClient.sendTimeoutCommand(event);
                    } else {
                        throw new IllegalArgumentException("未知 Outbox destination: " + event.destination());
                    }
                    outboxRepository.markSent(event.id());
                    log.info("Outbox 投递成功 eventId={}, type={}, destination={}",
                            event.eventId(), event.eventType(), event.destination());
                } catch (Exception ex) {
                    outboxRepository.markRetry(
                            event.id(),
                            ex.getMessage(),
                            properties.getOutbox().getRetryDelaySeconds());
                    log.error("Outbox 投递失败，稍后重试 eventId={}, destination={}, retryCount={}",
                            event.eventId(), event.destination(), event.retryCount() + 1, ex);
                }
            }
        }
    }

    private String resolveTraceId(OutboxRecord event) {
        try {
            JsonNode payload = objectMapper.readTree(event.payload());
            return TraceContext.sanitize(
                    payload.path(TraceConstants.TRACE_ID_MDC_KEY).asText(null));
        } catch (Exception ignored) {
            return null;
        }
    }
}
