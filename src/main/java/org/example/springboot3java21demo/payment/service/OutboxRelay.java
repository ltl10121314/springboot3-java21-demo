package org.example.springboot3java21demo.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot3java21demo.payment.constant.PaymentConstants;
import org.example.springboot3java21demo.payment.domain.OutboxEvent;
import org.example.springboot3java21demo.payment.infra.outbox.OutboxPublisher;
import org.example.springboot3java21demo.payment.repository.OutboxRepository;
import org.example.springboot3java21demo.payment.support.PaymentMetrics;
import org.example.springboot3java21demo.payment.support.TraceContext;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * 本地消息表中继：把「已经和业务一起落库」的事件投递到 MQ/RPC。
 *
 * <p>投递语义是 at-least-once：投递成功后立刻标记 SENT；如果标记前进程崩溃，
 * 下一轮会重复投递，因此消费端必须用 eventId 去重。靠这个组合，
 * 「本地事务」和「跨系统消息」之间不需要分布式事务也能达到最终一致。</p>
 *
 * <p>失败按指数退避重试，超过上限进入 DEAD 并触发告警——这就把「静默丢消息」
 * 变成了「可监控、可人工补偿」的确定性状态。</p>
 */
@Slf4j
public class OutboxRelay {

    private static final int BATCH_SIZE = PaymentConstants.OUTBOX_BATCH_SIZE;
    private static final int MAX_RETRY = PaymentConstants.OUTBOX_MAX_RETRY;
    private static final long BASE_BACKOFF_MILLIS = 1000L;
    private static final long MAX_BACKOFF_MILLIS = 60_000L;

    private final OutboxRepository outboxRepository;
    private final OutboxPublisher publisher;
    private final PaymentMetrics metrics;

    public OutboxRelay(OutboxRepository outboxRepository, OutboxPublisher publisher, PaymentMetrics metrics) {
        this.outboxRepository = outboxRepository;
        this.publisher = publisher;
        this.metrics = metrics;
    }

    /**
     * 投递一轮，返回成功投递条数。生产用 @Scheduled / XXL-JOB 触发，多实例可并行（FOR UPDATE SKIP LOCKED）。
     */
    @Scheduled(fixedDelayString = "${payment.outbox.relay-interval-ms:2000}")
    public int relayOnce() {
        long now = System.currentTimeMillis();
        List<OutboxEvent> pending = outboxRepository.findPending(now, BATCH_SIZE);
        int delivered = 0;
        for (OutboxEvent event : pending) {
            delivered += deliver(event, now) ? 1 : 0;
        }
        if (delivered > 0) {
            log.info("[{}] 本地消息表投递完成，本轮 {} 条", TraceContext.currentOrCreate(), delivered);
        }
        return delivered;
    }

    private boolean deliver(OutboxEvent event, long now) {
        try {
            publisher.publish(event);
            outboxRepository.save(event.markSent(now));
            return true;
        } catch (Exception ex) {
            long backoff = Math.min(BASE_BACKOFF_MILLIS << Math.min(event.retryCount(), 6), MAX_BACKOFF_MILLIS);
            OutboxEvent retried = event.markRetry(now, backoff, MAX_RETRY);
            outboxRepository.save(retried);
            metrics.recordOutboxRetry();
            if (retried.status() == OutboxEvent.Status.DEAD) {
                log.error("[{}] 事件投递重试耗尽，转入 DEAD 等待人工处理 eventId={} type={}",
                        event.traceId(), event.eventId(), event.eventType(), ex);
            } else {
                log.warn("[{}] 事件投递失败，{}ms 后第 {} 次重试 eventId={}",
                        event.traceId(), backoff, retried.retryCount(), event.eventId());
            }
            return false;
        }
    }
}
