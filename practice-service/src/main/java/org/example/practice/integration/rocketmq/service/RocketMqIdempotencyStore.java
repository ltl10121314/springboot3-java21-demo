package org.example.practice.integration.rocketmq.service;

import lombok.extern.slf4j.Slf4j;
import org.example.practice.integration.rocketmq.config.RocketMqProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 有界内存幂等表。生产环境应替换为 Redis 或数据库唯一约束。
 */
@Slf4j
@Component
public class RocketMqIdempotencyStore {

    private final Map<String, Instant> processedEvents;

    public RocketMqIdempotencyStore(RocketMqProperties properties) {
        int maxSize = Math.max(properties.getIdempotencyCacheSize(), 1);
        this.processedEvents = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Instant> eldest) {
                return size() > maxSize;
            }
        };
    }

    public synchronized boolean tryMarkProcessed(String consumerGroup, String eventId) {
        String key = consumerGroup + ":" + eventId;
        Instant previous = processedEvents.putIfAbsent(key, Instant.now());
        if (previous == null) {
            log.debug("幂等记录成功 group={}, eventId={}, cacheSize={}",
                    consumerGroup, eventId, processedEvents.size());
            return true;
        }
        log.warn("检测到重复消费 group={}, eventId={}, firstProcessedAt={}",
                consumerGroup, eventId, previous);
        return false;
    }

    public synchronized int size() {
        return processedEvents.size();
    }

    public synchronized void clear() {
        int previousSize = processedEvents.size();
        processedEvents.clear();
        log.info("清空 RocketMQ 幂等缓存 size={}", previousSize);
    }
}
