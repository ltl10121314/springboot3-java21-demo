package org.example.practice.integration.rocketmq.service;

import lombok.extern.slf4j.Slf4j;
import org.example.practice.integration.rocketmq.config.RocketMqProperties;
import org.example.practice.integration.rocketmq.model.DeadLetterMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 保存最近消费到的死信，供 HTTP 查询和手工重放。
 */
@Slf4j
@Component
public class DeadLetterStore {

    private final Map<String, DeadLetterMessage> messagesByRecordId = new ConcurrentHashMap<>();
    private final Map<String, String> recordIdByMessageKey = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<String> recentRecordIds = new ConcurrentLinkedDeque<>();
    private final AtomicLong sequence = new AtomicLong();
    private final RocketMqProperties properties;

    public DeadLetterStore(RocketMqProperties properties) {
        this.properties = properties;
    }

    public synchronized DeadLetterMessage save(
            String consumerGroup,
            String deadLetterTopic,
            String messageId,
            String tag,
            String key,
            int deliveryAttempt,
            String payload) {
        String messageKey = deadLetterTopic + ":" + messageId;
        String existingRecordId = recordIdByMessageKey.get(messageKey);
        if (existingRecordId != null) {
            DeadLetterMessage existing = messagesByRecordId.get(existingRecordId);
            if (existing != null) {
                log.debug("忽略重复死信快照 dlqTopic={}, messageId={}, recordId={}",
                        deadLetterTopic, messageId, existingRecordId);
                return existing;
            }
        }

        String recordId = UUID.randomUUID().toString();
        DeadLetterMessage message = new DeadLetterMessage(
                sequence.incrementAndGet(),
                recordId,
                consumerGroup,
                deadLetterTopic,
                messageId,
                tag,
                key,
                deliveryAttempt,
                payload,
                java.time.Instant.now(),
                false);
        messagesByRecordId.put(recordId, message);
        recordIdByMessageKey.put(messageKey, recordId);
        recentRecordIds.addFirst(recordId);
        while (recentRecordIds.size() > Math.max(properties.getDeadLetterRecentLimit(), 1)) {
            String removedId = recentRecordIds.pollLast();
            if (removedId != null) {
                DeadLetterMessage removed = messagesByRecordId.remove(removedId);
                if (removed != null) {
                    recordIdByMessageKey.remove(removed.deadLetterTopic() + ":" + removed.messageId());
                }
            }
        }
        log.warn("死信快照已保存 recordId={}, group={}, dlqTopic={}, messageId={}, key={}, attempt={}",
                recordId, consumerGroup, deadLetterTopic, messageId, key, deliveryAttempt);
        return message;
    }

    public Optional<DeadLetterMessage> find(String recordId) {
        return Optional.ofNullable(messagesByRecordId.get(recordId));
    }

    public List<DeadLetterMessage> findRecent(int limit) {
        int safeLimit = Math.max(limit, 1);
        List<DeadLetterMessage> result = new ArrayList<>(safeLimit);
        for (String recordId : recentRecordIds) {
            DeadLetterMessage message = messagesByRecordId.get(recordId);
            if (message != null) {
                result.add(message);
                if (result.size() >= safeLimit) {
                    break;
                }
            }
        }
        return result;
    }

    public synchronized boolean markReplayed(String recordId) {
        DeadLetterMessage message = messagesByRecordId.get(recordId);
        if (message == null || message.replayed()) {
            return false;
        }
        messagesByRecordId.put(recordId, message.withReplayed(true));
        log.info("死信已标记为重放 recordId={}, messageId={}, group={}",
                recordId, message.messageId(), message.consumerGroup());
        return true;
    }
}
