package org.example.practice.integration.rocketmq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.example.practice.integration.rocketmq.common.RocketMqConstants;
import org.example.practice.integration.rocketmq.common.RocketMqMessageSupport;
import org.example.practice.integration.rocketmq.config.RocketMqProperties;
import org.example.practice.integration.rocketmq.model.ConsumeStatus;
import org.example.practice.integration.rocketmq.model.OrderEvent;
import org.example.practice.integration.rocketmq.service.OrderMessageConsumptionService;
import org.example.practice.integration.rocketmq.service.RocketMqIdempotencyStore;
import org.example.practice.integration.rocketmq.service.RocketMqMetrics;
import org.example.springboot3java21demo.common.trace.TraceConstants;
import org.example.springboot3java21demo.common.trace.TraceContext;
import org.springframework.stereotype.Component;

/**
 * 订单业务消费者：处理 ORDER_CREATED 和 ORDER_PAID。
 */
@Slf4j
@Component
public class BusinessOrderMessageListener implements MessageListener {

    private final RocketMqProperties properties;
    private final OrderMessageConsumptionService consumptionService;
    private final RocketMqIdempotencyStore idempotencyStore;
    private final RocketMqMetrics metrics;

    public BusinessOrderMessageListener(
            RocketMqProperties properties,
            OrderMessageConsumptionService consumptionService,
            RocketMqIdempotencyStore idempotencyStore,
            RocketMqMetrics metrics) {
        this.properties = properties;
        this.consumptionService = consumptionService;
        this.idempotencyStore = idempotencyStore;
        this.metrics = metrics;
    }

    @Override
    public ConsumeResult consume(MessageView message) {
        try (TraceContext.Scope ignored = TraceContext.openIfPresent(
                message.getProperties().get(TraceConstants.TRACE_ID_MDC_KEY))) {
            return consumeInScope(message);
        }
    }

    private ConsumeResult consumeInScope(MessageView message) {
        long startedAt = System.nanoTime();
        metrics.increment(RocketMqMetrics.Metric.CONSUME_ATTEMPT);
        String consumerGroup = properties.getBusinessConsumerGroup();
        log.info("业务消费开始 group={}, messageId={}, tag={}, key={}, deliveryAttempt={}",
                consumerGroup,
                message.getMessageId(),
                message.getTag().orElse(null),
                RocketMqMessageSupport.keys(message),
                message.getDeliveryAttempt());
        try {
            OrderMessageConsumptionService.DecodedMessage decoded = consumptionService.decode(message);
            OrderEvent event = decoded.event();
            String failConsumerGroup = message.getProperties()
                    .get(RocketMqConstants.FAIL_CONSUMER_GROUP_PROPERTY);
            if (RocketMqConstants.shouldSimulateFailure(failConsumerGroup, consumerGroup)) {
                consumptionService.record(consumerGroup, message, decoded, ConsumeStatus.RETRY);
                metrics.increment(RocketMqMetrics.Metric.CONSUME_RETRY);
                log.warn("业务消费模拟失败，返回 FAILURE 等待 RocketMQ 重试 group={}, messageId={}, eventId={}, deliveryAttempt={}",
                        consumerGroup, message.getMessageId(), event.eventId(), message.getDeliveryAttempt());
                return ConsumeResult.FAILURE;
            }

            if (!idempotencyStore.tryMarkProcessed(consumerGroup, event.eventId())) {
                consumptionService.record(consumerGroup, message, decoded, ConsumeStatus.DUPLICATE);
                metrics.increment(RocketMqMetrics.Metric.CONSUME_DUPLICATE);
                log.warn("业务重复消息已忽略 group={}, messageId={}, eventId={}, deliveryAttempt={}",
                        consumerGroup, message.getMessageId(), event.eventId(), message.getDeliveryAttempt());
                return ConsumeResult.SUCCESS;
            }

            consumptionService.record(consumerGroup, message, decoded, ConsumeStatus.CONSUMED);
            metrics.increment(RocketMqMetrics.Metric.CONSUME_SUCCESS);
            log.info("业务消费成功 group={}, topic={}, tag={}, key={}, eventId={}, type={}, attempt={}, elapsedMs={}",
                    consumerGroup,
                    message.getTopic(),
                    message.getTag().orElse(null),
                    RocketMqMessageSupport.keys(message),
                    event.eventId(),
                    event.eventType(),
                    message.getDeliveryAttempt(),
                    elapsedMillis(startedAt));
            return ConsumeResult.SUCCESS;
        } catch (Exception e) {
            // FAILURE 会触发重试；RocketMQ 5.x 服务端负责重试和死信策略。
            metrics.increment(RocketMqMetrics.Metric.CONSUME_RETRY);
            log.error("业务消费异常，返回 FAILURE group={}, messageId={}, attempt={}, elapsedMs={}",
                    consumerGroup, message.getMessageId(), message.getDeliveryAttempt(),
                    elapsedMillis(startedAt), e);
            return ConsumeResult.FAILURE;
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
