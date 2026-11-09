package org.example.springboot3java21demo.rocketmq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.example.springboot3java21demo.rocketmq.config.RocketMqProperties;
import org.example.springboot3java21demo.rocketmq.model.OrderEvent;
import org.example.springboot3java21demo.rocketmq.service.OrderMessageConsumptionService;
import org.springframework.stereotype.Component;

/**
 * 审计消费者：独立 ConsumerGroup，只接收 ORDER_CREATED。
 */
@Slf4j
@Component
public class AuditOrderMessageListener implements MessageListener {

    private final RocketMqProperties properties;
    private final OrderMessageConsumptionService consumptionService;

    public AuditOrderMessageListener(
            RocketMqProperties properties,
            OrderMessageConsumptionService consumptionService) {
        this.properties = properties;
        this.consumptionService = consumptionService;
    }

    @Override
    public ConsumeResult consume(MessageView message) {
        try {
            OrderEvent event = consumptionService.record(
                    properties.getAuditConsumerGroup(), message);
            log.info("审计消费成功 group={}, topic={}, tag={}, orderId={}, eventId={}",
                    properties.getAuditConsumerGroup(),
                    message.getTopic(),
                    message.getTag().orElse(null),
                    event.orderId(),
                    event.eventId());
            return ConsumeResult.SUCCESS;
        } catch (Exception e) {
            log.error("审计消费失败 group={}, messageId={}",
                    properties.getAuditConsumerGroup(), message.getMessageId(), e);
            return ConsumeResult.FAILURE;
        }
    }
}
