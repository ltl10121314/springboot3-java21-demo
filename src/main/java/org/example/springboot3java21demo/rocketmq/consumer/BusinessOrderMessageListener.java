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
 * 订单业务消费者：处理 ORDER_CREATED 和 ORDER_PAID。
 */
@Slf4j
@Component
public class BusinessOrderMessageListener implements MessageListener {

    private final RocketMqProperties properties;
    private final OrderMessageConsumptionService consumptionService;

    public BusinessOrderMessageListener(
            RocketMqProperties properties,
            OrderMessageConsumptionService consumptionService) {
        this.properties = properties;
        this.consumptionService = consumptionService;
    }

    @Override
    public ConsumeResult consume(MessageView message) {
        try {
            OrderEvent event = consumptionService.record(
                    properties.getBusinessConsumerGroup(), message);
            log.info("业务消费成功 group={}, topic={}, tag={}, key={}, eventId={}, type={}, attempt={}",
                    properties.getBusinessConsumerGroup(),
                    message.getTopic(),
                    message.getTag().orElse(null),
                    message.getKeys(),
                    event.eventId(),
                    event.eventType(),
                    message.getDeliveryAttempt());
            return ConsumeResult.SUCCESS;
        } catch (Exception e) {
            // FAILURE 会触发重试；RocketMQ 5.x 服务端负责重试和死信策略。
            log.error("业务消费失败 group={}, messageId={}, attempt={}",
                    properties.getBusinessConsumerGroup(), message.getMessageId(),
                    message.getDeliveryAttempt(), e);
            return ConsumeResult.FAILURE;
        }
    }
}
