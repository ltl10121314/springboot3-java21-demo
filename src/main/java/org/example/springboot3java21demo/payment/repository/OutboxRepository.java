package org.example.springboot3java21demo.payment.repository;

import org.example.springboot3java21demo.payment.domain.OutboxEvent;

import java.util.List;

/**
 * 本地消息表仓储端口。
 *
 * <p>生产实现与业务表同库同事务写入；扫描待投递消息用
 * {@code SELECT ... WHERE status = 'PENDING' AND next_retry_at &lt;= now() ORDER BY id LIMIT 200 FOR UPDATE SKIP LOCKED}，
 * 多实例并发扫描不会互相阻塞。</p>
 */
public interface OutboxRepository {

    void save(OutboxEvent event);

    List<OutboxEvent> findPending(long now, int limit);

    List<OutboxEvent> findAll();

    long countByStatus(OutboxEvent.Status status);
}
