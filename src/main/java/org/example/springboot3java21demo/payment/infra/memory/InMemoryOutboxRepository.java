package org.example.springboot3java21demo.payment.infra.memory;

import org.example.springboot3java21demo.payment.domain.OutboxEvent;
import org.example.springboot3java21demo.payment.repository.OutboxRepository;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地消息表的内存实现。
 */
public class InMemoryOutboxRepository implements OutboxRepository {

    private final ConcurrentHashMap<String, OutboxEvent> events = new ConcurrentHashMap<>();

    @Override
    public void save(OutboxEvent event) {
        events.put(event.eventId(), event);
    }

    @Override
    public List<OutboxEvent> findPending(long now, int limit) {
        return events.values().stream()
                .filter(event -> event.status() == OutboxEvent.Status.PENDING)
                .filter(event -> event.nextRetryAt() <= now)
                .sorted(Comparator.comparingLong(OutboxEvent::createdAt))
                .limit(limit)
                .toList();
    }

    @Override
    public List<OutboxEvent> findAll() {
        return events.values().stream()
                .sorted(Comparator.comparingLong(OutboxEvent::createdAt))
                .toList();
    }

    @Override
    public long countByStatus(OutboxEvent.Status status) {
        return events.values().stream().filter(event -> event.status() == status).count();
    }
}
