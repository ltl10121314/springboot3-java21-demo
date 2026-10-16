package org.example.springboot3java21demo.payment.infra.memory;

import org.example.springboot3java21demo.payment.domain.PaymentOrder;
import org.example.springboot3java21demo.payment.domain.PaymentStatus;
import org.example.springboot3java21demo.payment.repository.PaymentOrderRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 支付单仓储的内存实现。
 *
 * <p>insertIfAbsent 对应唯一索引 uk_request_id 的语义，
 * compareAndSet 对应带 version 条件的乐观锁更新语义，
 * 两者在并发下的行为与 MySQL 实现一致。</p>
 */
public class InMemoryPaymentOrderRepository implements PaymentOrderRepository {

    private final ConcurrentHashMap<String, PaymentOrder> byRequestId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PaymentOrder> byPaymentNo = new ConcurrentHashMap<>();

    @Override
    public PaymentOrder insertIfAbsent(PaymentOrder order) {
        PaymentOrder existing = byRequestId.putIfAbsent(order.requestId(), order);
        if (existing != null) {
            return existing;
        }
        byPaymentNo.put(order.paymentNo(), order);
        return order;
    }

    @Override
    public Optional<PaymentOrder> findByRequestId(String requestId) {
        return Optional.ofNullable(byRequestId.get(requestId));
    }

    @Override
    public Optional<PaymentOrder> findByPaymentNo(String paymentNo) {
        return Optional.ofNullable(byPaymentNo.get(paymentNo));
    }

    @Override
    public boolean compareAndSet(PaymentOrder expect, PaymentOrder next) {
        AtomicBoolean updated = new AtomicBoolean(false);
        byRequestId.compute(expect.requestId(), (key, current) -> {
            if (current != null && current.version() == expect.version() && current.status() == expect.status()) {
                updated.set(true);
                byPaymentNo.put(next.paymentNo(), next);
                return next;
            }
            return current;
        });
        return updated.get();
    }

    @Override
    public List<PaymentOrder> findUnsettledBefore(long deadline, int limit) {
        return byRequestId.values().stream()
                .filter(PaymentOrder::isUnsettled)
                .filter(order -> order.updatedAt() <= deadline)
                .sorted(Comparator.comparingLong(PaymentOrder::updatedAt))
                .limit(limit)
                .toList();
    }

    @Override
    public List<PaymentOrder> findByBizDate(String bizDate) {
        return byRequestId.values().stream()
                .filter(order -> bizDate.equals(order.bizDate()))
                .sorted(Comparator.comparingLong(PaymentOrder::createdAt))
                .toList();
    }

    @Override
    public List<PaymentOrder> findByBizDateAndStatus(String bizDate, PaymentStatus status) {
        List<PaymentOrder> result = new ArrayList<>();
        for (PaymentOrder order : byRequestId.values()) {
            if (bizDate.equals(order.bizDate()) && order.status() == status) {
                result.add(order);
            }
        }
        result.sort(Comparator.comparingLong(PaymentOrder::createdAt));
        return result;
    }

    public int size() {
        return byRequestId.size();
    }
}
