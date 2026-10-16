package org.example.springboot3java21demo.payment;

import org.example.springboot3java21demo.payment.domain.OutboxEvent;
import org.example.springboot3java21demo.payment.infra.gateway.MockPaymentChannelGateway;
import org.example.springboot3java21demo.payment.infra.idempotent.InMemoryIdempotencyStore;
import org.example.springboot3java21demo.payment.infra.memory.InMemoryChannelBillRepository;
import org.example.springboot3java21demo.payment.infra.memory.InMemoryInventoryRepository;
import org.example.springboot3java21demo.payment.infra.memory.InMemoryOutboxRepository;
import org.example.springboot3java21demo.payment.infra.memory.InMemoryPaymentOrderRepository;
import org.example.springboot3java21demo.payment.infra.memory.InMemoryReconcileTaskRepository;
import org.example.springboot3java21demo.payment.infra.outbox.OutboxPublisher;
import org.example.springboot3java21demo.payment.service.OutboxRelay;
import org.example.springboot3java21demo.payment.service.PaymentTimeoutCompensator;
import org.example.springboot3java21demo.payment.service.impl.PaymentServiceImpl;
import org.example.springboot3java21demo.payment.service.impl.ReconcileServiceImpl;
import org.example.springboot3java21demo.payment.support.PaymentCircuitBreaker;
import org.example.springboot3java21demo.payment.support.PaymentMetrics;
import org.example.springboot3java21demo.payment.support.PaymentRateLimiter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试装配：用内存实现把整条支付链路接起来，不依赖 MySQL / Redis / MQ。
 *
 * <p>与 {@code PaymentConfig} 的装配方式保持一致，保证测试验证的就是生产用的那条链路。</p>
 */
class PaymentTestContext {

    final String skuId = "SKU-TEST-001";

    final InMemoryInventoryRepository inventory;
    final InMemoryPaymentOrderRepository orders = new InMemoryPaymentOrderRepository();
    final InMemoryOutboxRepository outbox = new InMemoryOutboxRepository();
    final InMemoryChannelBillRepository channelBills = new InMemoryChannelBillRepository();
    final InMemoryReconcileTaskRepository reconcileTasks = new InMemoryReconcileTaskRepository();
    final InMemoryIdempotencyStore idempotency = new InMemoryIdempotencyStore();
    final PaymentMetrics metrics = new PaymentMetrics();
    final MockPaymentChannelGateway gateway;
    final PaymentCircuitBreaker circuitBreaker;
    final PaymentRateLimiter rateLimiter;
    final RecordingOutboxPublisher publisher = new RecordingOutboxPublisher();

    final PaymentServiceImpl paymentService;
    final OutboxRelay outboxRelay;
    final PaymentTimeoutCompensator compensator;
    final ReconcileServiceImpl reconcileService;

    PaymentTestContext(long stockTotal) {
        this(stockTotal, 1.0d, 0.0d, 0L, 1_000_000L);
    }

    PaymentTestContext(long stockTotal,
                       double channelSuccessRate,
                       double channelUnknownRate,
                       long unsettledTimeoutMillis,
                       long rateLimitMaxRequests) {
        this(stockTotal, channelSuccessRate, channelUnknownRate, unsettledTimeoutMillis,
                new PaymentRateLimiter(60, rateLimitMaxRequests));
    }

    PaymentTestContext(long stockTotal,
                       double channelSuccessRate,
                       double channelUnknownRate,
                       long unsettledTimeoutMillis,
                       PaymentRateLimiter rateLimiter) {
        this.inventory = new InMemoryInventoryRepository().seed(skuId, stockTotal);
        this.gateway = new MockPaymentChannelGateway(channelSuccessRate, channelUnknownRate, 0L);
        this.circuitBreaker = new PaymentCircuitBreaker(3, 60_000L, 1);
        this.rateLimiter = rateLimiter;

        this.paymentService = new PaymentServiceImpl(orders, inventory, outbox, idempotency,
                gateway, circuitBreaker, rateLimiter, metrics);
        this.outboxRelay = new OutboxRelay(outbox, publisher, metrics);
        this.compensator = new PaymentTimeoutCompensator(orders, inventory, outbox, gateway, metrics,
                unsettledTimeoutMillis);
        this.reconcileService = new ReconcileServiceImpl(orders, channelBills, reconcileTasks);
    }

    /**
     * 记录投递过的 eventId，并可模拟前 N 次投递失败，用于验证 at-least-once + 重试。
     */
    static class RecordingOutboxPublisher implements OutboxPublisher {

        final List<String> deliveredEventIds = new CopyOnWriteArrayList<>();
        final Map<String, Integer> deliveryAttempts = new ConcurrentHashMap<>();
        final AtomicInteger failuresToInject = new AtomicInteger();

        @Override
        public void publish(OutboxEvent event) throws Exception {
            deliveryAttempts.merge(event.eventId(), 1, Integer::sum);
            if (failuresToInject.getAndUpdate(value -> Math.max(0, value - 1)) > 0) {
                throw new IllegalStateException("模拟 MQ 抖动");
            }
            deliveredEventIds.add(event.eventId());
        }

        void failNext(int times) {
            failuresToInject.set(times);
        }
    }
}
