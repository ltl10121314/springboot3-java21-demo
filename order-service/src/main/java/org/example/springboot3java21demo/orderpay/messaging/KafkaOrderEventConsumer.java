package org.example.springboot3java21demo.orderpay.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.springboot3java21demo.orderpay.domain.OrderEvent;
import org.example.springboot3java21demo.orderpay.domain.OrderRecord;
import org.example.springboot3java21demo.orderpay.repository.OrderJdbcRepository;
import org.example.springboot3java21demo.orderpay.search.OrderSearchService;
import org.example.springboot3java21demo.common.trace.TraceConstants;
import org.example.springboot3java21demo.common.trace.TraceContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaOrderEventConsumer {

    private final ObjectMapper objectMapper;
    private final OrderJdbcRepository orderRepository;
    private final OrderSearchService searchService;

    public KafkaOrderEventConsumer(
            ObjectMapper objectMapper,
            OrderJdbcRepository orderRepository,
            OrderSearchService searchService) {
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
        this.searchService = searchService;
    }

    @KafkaListener(
            topics = "${order-pay.kafka.topic:order-pay-events}",
            groupId = "${order-pay.kafka.group-id:order-pay-indexer}")
    public void consume(ConsumerRecord<String, String> record) {
        String transportTraceId = null;
        if (record.headers().lastHeader(TraceConstants.TRACE_ID_MDC_KEY) != null) {
            transportTraceId = new String(
                    record.headers().lastHeader(TraceConstants.TRACE_ID_MDC_KEY).value(),
                    java.nio.charset.StandardCharsets.UTF_8);
        }
        try (TraceContext.Scope ignored = TraceContext.openIfPresent(transportTraceId)) {
            try {
                log.debug("收到 Kafka 订单事件 partition={}, offset={}, key={}",
                        record.partition(), record.offset(), record.key());
                OrderEvent event = objectMapper.readValue(record.value(), OrderEvent.class);
                if (TraceContext.sanitize(transportTraceId) == null) {
                    try (TraceContext.Scope eventScope =
                                 TraceContext.openIfPresent(event.traceId())) {
                        process(record, event);
                    }
                } else {
                    process(record, event);
                }
            } catch (Exception ex) {
                log.error("消费 Kafka 订单事件失败 offset={}, value={}",
                        record.offset(), record.value(), ex);
                throw new IllegalStateException("订单事件索引失败", ex);
            }
        }
    }

    private void process(ConsumerRecord<String, String> record, OrderEvent event) {
        OrderRecord order = orderRepository.findByOrderNo(event.orderNo()).orElse(null);
        if (order == null) {
            log.warn("Kafka 事件对应订单不存在 eventId={}, orderNo={}",
                    event.eventId(), event.orderNo());
            return;
        }
        // 以 MySQL 最新事实为准写入索引，避免乱序事件把已支付订单覆盖回旧状态。
        searchService.index(order);
        log.info("Kafka 事件已同步 Elasticsearch eventId={}, type={}, orderNo={}, status={}",
                event.eventId(), event.eventType(), event.orderNo(), order.status());
    }
}
