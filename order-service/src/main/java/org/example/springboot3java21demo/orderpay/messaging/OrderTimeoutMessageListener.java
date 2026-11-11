package org.example.springboot3java21demo.orderpay.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.example.springboot3java21demo.orderpay.domain.OrderTimeoutCommand;
import org.example.springboot3java21demo.orderpay.service.OrderApplicationService;
import org.example.springboot3java21demo.common.trace.TraceConstants;
import org.example.springboot3java21demo.common.trace.TraceContext;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OrderTimeoutMessageListener implements MessageListener {

    private final ObjectMapper objectMapper;
    private final OrderApplicationService orderApplicationService;

    public OrderTimeoutMessageListener(
            ObjectMapper objectMapper,
            OrderApplicationService orderApplicationService) {
        this.objectMapper = objectMapper;
        this.orderApplicationService = orderApplicationService;
    }

    @Override
    public ConsumeResult consume(MessageView message) {
        String transportTraceId = message.getProperties().get(TraceConstants.TRACE_ID_MDC_KEY);
        try (TraceContext.Scope ignored = TraceContext.openIfPresent(transportTraceId)) {
            try {
                log.debug("收到 RocketMQ 超时消息 messageId={}, deliveryAttempt={}",
                        message.getMessageId(), message.getDeliveryAttempt());
                ByteBuffer body = message.getBody().duplicate();
                byte[] bytes = new byte[body.remaining()];
                body.get(bytes);
                OrderTimeoutCommand command = objectMapper.readValue(
                        new String(bytes, StandardCharsets.UTF_8), OrderTimeoutCommand.class);
                if (TraceContext.sanitize(transportTraceId) == null) {
                    try (TraceContext.Scope eventScope =
                                 TraceContext.openIfPresent(command.traceId())) {
                        return closeOrder(message, command);
                    }
                }
                return closeOrder(message, command);
            } catch (Exception ex) {
                log.error("RocketMQ 超时消息消费失败 messageId={}", message.getMessageId(), ex);
                return ConsumeResult.FAILURE;
            }
        }
    }

    private ConsumeResult closeOrder(MessageView message, OrderTimeoutCommand command) {
        orderApplicationService.closeTimedOutOrder(command.orderNo());
        log.info("RocketMQ 超时消息消费成功 eventId={}, orderNo={}, messageId={}",
                command.eventId(), command.orderNo(), message.getMessageId());
        return ConsumeResult.SUCCESS;
    }
}
