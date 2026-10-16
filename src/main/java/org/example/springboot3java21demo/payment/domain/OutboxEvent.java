package org.example.springboot3java21demo.payment.domain;

import java.util.UUID;

/**
 * 本地消息表记录（Transactional Outbox）。
 *
 * <p>业务落库和「要发出去的消息」写在同一个本地事务里，再由中继任务（relay）异步投递到 MQ/RPC。
 * 这样即使 MQ 抖动或进程崩溃，消息也不会丢：投递语义是 at-least-once，
 * 消费端用 {@code eventId} 去重即可得到 exactly-once 的效果。</p>
 */
public record OutboxEvent(
        String eventId,
        String aggregateType,
        String aggregateId,
        String eventType,
        String payload,
        Status status,
        int retryCount,
        long nextRetryAt,
        long createdAt,
        long updatedAt,
        String traceId) {

    public enum Status {
        /** 待投递 */
        PENDING,
        /** 已投递成功 */
        SENT,
        /** 重试耗尽，需要人工/告警介入 */
        DEAD
    }

    public static OutboxEvent pending(String aggregateType,
                                      String aggregateId,
                                      String eventType,
                                      String payload,
                                      String traceId,
                                      long now) {
        return new OutboxEvent(UUID.randomUUID().toString(), aggregateType, aggregateId, eventType,
                payload, Status.PENDING, 0, now, now, now, traceId);
    }

    public OutboxEvent markSent(long now) {
        return new OutboxEvent(eventId, aggregateType, aggregateId, eventType, payload,
                Status.SENT, retryCount, nextRetryAt, createdAt, now, traceId);
    }

    /**
     * 投递失败后按退避时间重排；超过最大重试次数进入 DEAD，触发告警。
     */
    public OutboxEvent markRetry(long now, long backoffMillis, int maxRetry) {
        int next = retryCount + 1;
        return new OutboxEvent(eventId, aggregateType, aggregateId, eventType, payload,
                next >= maxRetry ? Status.DEAD : Status.PENDING,
                next, now + backoffMillis, createdAt, now, traceId);
    }
}
