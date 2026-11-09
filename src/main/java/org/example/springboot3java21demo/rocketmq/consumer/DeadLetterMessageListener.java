package org.example.springboot3java21demo.rocketmq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.example.springboot3java21demo.rocketmq.common.RocketMqConstants;
import org.example.springboot3java21demo.rocketmq.common.RocketMqMessageSupport;
import org.example.springboot3java21demo.rocketmq.service.DeadLetterStore;
import org.example.springboot3java21demo.rocketmq.service.RocketMqMetrics;
import org.springframework.stereotype.Component;

/**
 * 消费业务 ConsumerGroup 对应的 %DLQ% Topic，并保存原始消息供查询和重放。
 */
@Slf4j
@Component
public class DeadLetterMessageListener implements MessageListener {

    private final DeadLetterStore deadLetterStore;
    private final RocketMqMetrics metrics;

    public DeadLetterMessageListener(DeadLetterStore deadLetterStore, RocketMqMetrics metrics) {
        this.deadLetterStore = deadLetterStore;
        this.metrics = metrics;
    }

    @Override
    public ConsumeResult consume(MessageView message) {
        String topic = message.getTopic();
        String consumerGroup = topic.startsWith(RocketMqConstants.DEAD_LETTER_TOPIC_PREFIX)
                ? topic.substring(RocketMqConstants.DEAD_LETTER_TOPIC_PREFIX.length())
                : "unknown";
        String payload = RocketMqMessageSupport.bodyText(message);
        try {
            deadLetterStore.save(
                    consumerGroup,
                    topic,
                    message.getMessageId().toString(),
                    message.getTag().orElse(null),
                    RocketMqMessageSupport.keys(message),
                    message.getDeliveryAttempt(),
                    payload);
            metrics.increment(RocketMqMetrics.Metric.DEAD_LETTER_RECEIVED);
            log.error("收到 RocketMQ 死信 sourceGroup={}, dlqTopic={}, messageId={}, tag={}, key={}, eventId={}, payloadBytes={}",
                    consumerGroup,
                    topic,
                    message.getMessageId(),
                    message.getTag().orElse(null),
                    RocketMqMessageSupport.keys(message),
                    message.getProperties().get("eventId"),
                    payload.getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
            return ConsumeResult.SUCCESS;
        } catch (Exception e) {
            log.error("死信快照保存失败 dlqTopic={}, messageId={}",
                    topic, message.getMessageId(), e);
            return ConsumeResult.FAILURE;
        }
    }
}
