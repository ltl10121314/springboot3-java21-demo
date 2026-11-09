package org.example.springboot3java21demo.rocketmq.service;

import org.example.springboot3java21demo.rocketmq.model.ConsumedMessage;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 仅用于练习观察的内存消费记录，生产环境应写入数据库、搜索服务或可观测平台。
 */
@Component
public class ConsumedMessageStore {

    private final Map<String, ConcurrentLinkedDeque<ConsumedMessage>> messagesByGroup =
            new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public long nextSequence() {
        return sequence.incrementAndGet();
    }

    public void add(ConsumedMessage message, int limit) {
        ConcurrentLinkedDeque<ConsumedMessage> messages =
                messagesByGroup.computeIfAbsent(message.consumerGroup(), ignored -> new ConcurrentLinkedDeque<>());
        messages.addFirst(message);
        while (messages.size() > Math.max(limit, 1)) {
            messages.pollLast();
        }
    }

    public List<ConsumedMessage> findRecent(String consumerGroup, int limit) {
        int safeLimit = Math.max(limit, 1);
        if (consumerGroup != null && !consumerGroup.isBlank()) {
            ConcurrentLinkedDeque<ConsumedMessage> messages =
                    messagesByGroup.getOrDefault(consumerGroup, new ConcurrentLinkedDeque<>());
            return messages.stream().limit(safeLimit).toList();
        }
        return messagesByGroup.values().stream()
                .flatMap(ConcurrentLinkedDeque::stream)
                .sorted(Comparator.comparingLong(ConsumedMessage::sequence).reversed())
                .limit(safeLimit)
                .toList();
    }

    public void clear() {
        messagesByGroup.clear();
    }
}
