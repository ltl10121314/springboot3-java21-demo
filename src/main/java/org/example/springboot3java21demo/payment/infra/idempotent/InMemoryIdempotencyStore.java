package org.example.springboot3java21demo.payment.infra.idempotent;

import org.example.springboot3java21demo.payment.model.PaymentResult;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 幂等存储的内存实现，用于单机演示与单元测试。
 * 生产替换为 {@link RedisIdempotencyStore}（多实例共享）。
 */
public class InMemoryIdempotencyStore implements IdempotencyStore {

    private static final String PROCESSING = "__PROCESSING__";

    private final ConcurrentHashMap<String, Object> store = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String requestId) {
        return store.putIfAbsent(requestId, PROCESSING) == null;
    }

    @Override
    public void saveResult(String requestId, PaymentResult result) {
        store.put(requestId, result);
    }

    @Override
    public Optional<PaymentResult> findResult(String requestId) {
        Object value = store.get(requestId);
        return value instanceof PaymentResult result ? Optional.of(result) : Optional.empty();
    }

    @Override
    public boolean isProcessing(String requestId) {
        return PROCESSING.equals(store.get(requestId));
    }

    @Override
    public void release(String requestId) {
        store.remove(requestId);
    }
}
