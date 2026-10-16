package org.example.springboot3java21demo.payment.infra.idempotent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot3java21demo.payment.constant.PaymentConstants;
import org.example.springboot3java21demo.payment.model.PaymentResult;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;

/**
 * 基于 Redis 的幂等存储（多实例共享）。
 *
 * <p>核心是 {@code SET key value NX EX} 这一条原子命令：抢占成功者才真正发起支付。
 * 值有三种形态：{@code PROCESSING} 占位、结果 JSON、键不存在。</p>
 */
@Slf4j
@RequiredArgsConstructor
public class RedisIdempotencyStore implements IdempotencyStore {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public boolean tryAcquire(String requestId) {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                key(requestId), PaymentConstants.STATUS_PROCESSING, PaymentConstants.IDEMPOTENCY_TTL);
        return Boolean.TRUE.equals(acquired);
    }

    @Override
    public void saveResult(String requestId, PaymentResult result) {
        try {
            redisTemplate.opsForValue().set(key(requestId),
                    objectMapper.writeValueAsString(result), PaymentConstants.IDEMPOTENCY_TTL);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("序列化支付结果失败", ex);
        }
    }

    @Override
    public Optional<PaymentResult> findResult(String requestId) {
        String cached = redisTemplate.opsForValue().get(key(requestId));
        if (cached == null || PaymentConstants.STATUS_PROCESSING.equals(cached)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(cached, PaymentResult.class));
        } catch (JsonProcessingException ex) {
            log.error("反序列化支付结果失败, requestId={}, raw={}", requestId, cached, ex);
            return Optional.empty();
        }
    }

    @Override
    public boolean isProcessing(String requestId) {
        return PaymentConstants.STATUS_PROCESSING.equals(redisTemplate.opsForValue().get(key(requestId)));
    }

    @Override
    public void release(String requestId) {
        redisTemplate.delete(key(requestId));
    }

    private String key(String requestId) {
        return PaymentConstants.IDEMPOTENCY_KEY_PREFIX + requestId;
    }
}
