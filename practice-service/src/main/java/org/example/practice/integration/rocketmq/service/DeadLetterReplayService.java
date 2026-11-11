package org.example.practice.integration.rocketmq.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.example.practice.integration.rocketmq.config.RocketMqProperties;
import org.example.practice.integration.rocketmq.model.DeadLetterMessage;
import org.example.practice.integration.rocketmq.model.DlqReplayResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class DeadLetterReplayService {

    private final DeadLetterStore deadLetterStore;
    private final OrderMessageProducer producer;
    private final RocketMqProperties properties;
    private final RocketMqMetrics metrics;

    public DeadLetterReplayService(
            DeadLetterStore deadLetterStore,
            OrderMessageProducer producer,
            RocketMqProperties properties,
            RocketMqMetrics metrics) {
        this.deadLetterStore = deadLetterStore;
        this.producer = producer;
        this.properties = properties;
        this.metrics = metrics;
    }

    /**
     * 同一死信只允许手工重放一次，避免重复点击造成重复投递。
     */
    public synchronized DlqReplayResponse replay(String recordId) {
        DeadLetterMessage message = deadLetterStore.find(recordId)
                .orElseThrow(() -> new RocketMqNotFoundException("死信不存在: " + recordId));
        if (message.replayed()) {
            throw new RocketMqConflictException("死信已重放，请勿重复提交: " + recordId);
        }

        log.info("开始重放死信 recordId={}, dlqMessageId={}, sourceGroup={}, key={}",
                recordId, message.messageId(), message.consumerGroup(), message.key());
        SendReceipt receipt = producer.replay(message);
        if (!deadLetterStore.markReplayed(recordId)) {
            throw new RocketMqConflictException("死信重放状态更新失败: " + recordId);
        }
        metrics.increment(RocketMqMetrics.Metric.DEAD_LETTER_REPLAYED);
        log.info("死信重放完成 recordId={}, dlqMessageId={}, newMessageId={}, topic={}, sourceGroup={}",
                recordId, message.messageId(), receipt.getMessageId(),
                properties.getTopic(), message.consumerGroup());
        return new DlqReplayResponse(
                recordId,
                message.messageId(),
                receipt.getMessageId().toString(),
                properties.getTopic(),
                message.tag(),
                "REPLAYED",
                Instant.now());
    }
}
