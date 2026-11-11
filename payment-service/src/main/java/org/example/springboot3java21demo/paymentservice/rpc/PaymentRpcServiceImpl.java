package org.example.springboot3java21demo.paymentservice.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.example.springboot3java21demo.orderpay.rpc.PaymentRpcFacade;
import org.example.springboot3java21demo.orderpay.rpc.PaymentRpcRequest;
import org.example.springboot3java21demo.orderpay.rpc.PaymentRpcResponse;
import org.example.springboot3java21demo.paymentservice.config.NacosPaymentConfig;
import org.example.springboot3java21demo.paymentservice.domain.PaymentRecord;
import org.example.springboot3java21demo.paymentservice.domain.PaymentStatus;
import org.example.springboot3java21demo.paymentservice.gateway.PaymentGateway;
import org.example.springboot3java21demo.paymentservice.repository.PaymentJdbcRepository;
import org.example.springboot3java21demo.common.trace.TraceContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@DubboService(interfaceClass = PaymentRpcFacade.class)
public class PaymentRpcServiceImpl implements PaymentRpcFacade {

    private static final Duration LOCK_TTL = Duration.ofSeconds(30);
    private static final Duration RESULT_TTL = Duration.ofHours(24);
    private static final String LOCK_PREFIX = "payment-service:lock:";
    private static final String RESULT_PREFIX = "payment-service:result:";

    private final PaymentJdbcRepository paymentRepository;
    private final StringRedisTemplate redisTemplate;
    private final PaymentGateway paymentGateway;
    private final NacosPaymentConfig nacosPaymentConfig;
    private final ObjectMapper objectMapper;

    public PaymentRpcServiceImpl(
            PaymentJdbcRepository paymentRepository,
            StringRedisTemplate redisTemplate,
            PaymentGateway paymentGateway,
            NacosPaymentConfig nacosPaymentConfig,
            ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.redisTemplate = redisTemplate;
        this.paymentGateway = paymentGateway;
        this.nacosPaymentConfig = nacosPaymentConfig;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentRpcResponse pay(PaymentRpcRequest request) {
        String traceId = request == null ? null : request.getTraceId();
        try (TraceContext.Scope ignored = TraceContext.openIfPresent(traceId)) {
            try {
                return payInScope(request);
            } catch (RuntimeException ex) {
                log.error("支付 RPC 请求失败 requestId={}, orderNo={}, paymentNo={}",
                        request == null ? null : request.getRequestId(),
                        request == null ? null : request.getOrderNo(),
                        request == null ? null : request.getPaymentNo(), ex);
                throw ex;
            }
        }
    }

    private PaymentRpcResponse payInScope(PaymentRpcRequest request) {
        long startedAt = System.nanoTime();
        log.info("支付微服务处理开始 requestId={}, orderNo={}, paymentNo={}, channel={}",
                request == null ? null : request.getRequestId(),
                request == null ? null : request.getOrderNo(),
                request == null ? null : request.getPaymentNo(),
                request == null ? null : request.getChannel());
        validate(request);
        String channel = request.getChannel().trim().toUpperCase(Locale.ROOT);
        log.info("支付请求参数校验通过 orderNo={}, requestId={}, amount={}, channel={}",
                request.getOrderNo(), request.getRequestId(), request.getAmount(), channel);
        if (!nacosPaymentConfig.channelAllowed(channel)) {
            log.warn("支付渠道未启用 channel={}, allowed={}", channel, nacosPaymentConfig.allowedChannels());
            return new PaymentRpcResponse(
                    request.getRequestId(), request.getOrderNo(), request.getPaymentNo(),
                    "FAILED", channel, null,
                    "支付渠道未启用: " + channel, false, null);
        }

        PaymentRecord existing = paymentRepository.findByRequestId(request.getRequestId()).orElse(null);
        if (existing != null) {
            log.info("支付请求幂等命中 requestId={}, paymentNo={}, status={}",
                    request.getRequestId(), existing.paymentNo(), existing.status());
            return toResponse(existing, true);
        }

        PaymentRecord latestByOrder = paymentRepository.findLatestByOrderNo(request.getOrderNo()).orElse(null);
        if (latestByOrder != null && latestByOrder.status() != PaymentStatus.FAILED) {
            log.info("订单已有有效支付尝试，返回现有结果 orderNo={}, paymentNo={}, status={}",
                    request.getOrderNo(), latestByOrder.paymentNo(), latestByOrder.status());
            return toResponse(latestByOrder, true);
        }

        String lockKey = LOCK_PREFIX + request.getRequestId();
        String lockToken = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockToken, LOCK_TTL);
        if (!Boolean.TRUE.equals(acquired)) {
            PaymentRecord concurrent = paymentRepository.findByRequestId(request.getRequestId()).orElse(null);
            if (concurrent != null) {
                return toResponse(concurrent, true);
            }
            return new PaymentRpcResponse(
                    request.getRequestId(), request.getOrderNo(), request.getPaymentNo(),
                    "PAYING", channel, null, "相同 requestId 正在处理", true, null);
        }

        try {
            PaymentRecord concurrent = paymentRepository.findByRequestId(request.getRequestId()).orElse(null);
            if (concurrent != null) {
                return toResponse(concurrent, true);
            }
            Instant now = Instant.now();
            PaymentRecord payment = new PaymentRecord(
                    request.getPaymentNo(),
                    request.getRequestId(),
                    request.getOrderNo(),
                    request.getAmount(),
                    channel,
                    PaymentStatus.PROCESSING,
                    null,
                    null,
                    0,
                    now,
                    now);
            paymentRepository.insert(payment);
            log.info("支付流水已创建 paymentNo={}, orderNo={}", payment.paymentNo(), payment.orderNo());

            PaymentGateway.ChargeResult result;
            try {
                result = paymentGateway.charge(
                        payment.paymentNo(), payment.orderNo(), payment.amount(), channel);
            } catch (RuntimeException ex) {
                log.error("支付渠道调用异常，按结果未知处理 paymentNo={}, orderNo={}",
                        payment.paymentNo(), payment.orderNo(), ex);
                result = PaymentGateway.ChargeResult.unknown(
                        "CHANNEL_CALL_EXCEPTION", "支付渠道调用异常，结果待确认");
            }
            PaymentRpcResponse response = settle(payment, result);
            cache(response);
            log.info("支付微服务处理完成 paymentNo={}, status={}, duplicate={}, elapsedMs={}",
                    response.getPaymentNo(), response.getStatus(), response.isDuplicate(),
                    elapsedMillis(startedAt));
            return response;
        } catch (RuntimeException ex) {
            if (paymentRepository.findByRequestId(request.getRequestId()).isEmpty()) {
                releaseLock(lockKey, lockToken);
            }
            throw ex;
        }
    }

    @Override
    public PaymentRpcResponse query(String requestId) {
        log.info("查询支付记录开始 requestId={}", requestId);
        try {
            PaymentRecord payment = paymentRepository.findByRequestId(requestId)
                    .orElseThrow(() -> new IllegalArgumentException("支付记录不存在: " + requestId));
            log.info("查询支付记录完成 requestId={}, paymentNo={}, status={}",
                    requestId, payment.paymentNo(), payment.status());
            return toResponse(payment, true);
        } catch (RuntimeException ex) {
            log.error("查询支付记录失败 requestId={}", requestId, ex);
            throw ex;
        }
    }

    private PaymentRpcResponse settle(PaymentRecord payment, PaymentGateway.ChargeResult result) {
        Instant now = Instant.now();
        if (result.success()) {
            complete(payment.paymentNo(), PaymentStatus.SUCCESS,
                    result.channelTradeNo(), null, now);
            return new PaymentRpcResponse(
                    payment.requestId(), payment.orderNo(), payment.paymentNo(),
                    "SUCCESS", payment.channel(), result.channelTradeNo(),
                    "支付成功", false, now);
        }
        if (result.unknown()) {
            complete(payment.paymentNo(), PaymentStatus.UNKNOWN,
                    null, result.message(), now);
            return new PaymentRpcResponse(
                    payment.requestId(), payment.orderNo(), payment.paymentNo(),
                    "UNKNOWN", payment.channel(), null,
                    result.message(), false, null);
        }
        complete(payment.paymentNo(), PaymentStatus.FAILED,
                null, result.message(), now);
        return new PaymentRpcResponse(
                payment.requestId(), payment.orderNo(), payment.paymentNo(),
                "FAILED", payment.channel(), null,
                result.message(), false, null);
    }

    private void complete(
            String paymentNo,
            PaymentStatus status,
            String channelTradeNo,
            String failReason,
            Instant now) {
        if (!paymentRepository.complete(paymentNo, status, channelTradeNo, failReason, now)) {
            throw new IllegalStateException("支付流水状态更新失败: " + paymentNo);
        }
    }

    private PaymentRpcResponse toResponse(PaymentRecord payment, boolean duplicate) {
        String message = switch (payment.status()) {
            case PROCESSING -> "支付处理中";
            case SUCCESS -> "支付成功";
            case FAILED -> payment.failReason() == null ? "支付失败" : payment.failReason();
            case UNKNOWN -> "支付结果未知，正在确认";
        };
        return new PaymentRpcResponse(
                payment.requestId(),
                payment.orderNo(),
                payment.paymentNo(),
                payment.status().name(),
                payment.channel(),
                payment.channelTradeNo(),
                message,
                duplicate,
                payment.status() == PaymentStatus.SUCCESS ? payment.updatedAt() : null);
    }

    private void cache(PaymentRpcResponse response) {
        try {
            redisTemplate.opsForValue().set(
                    RESULT_PREFIX + response.getRequestId(),
                    objectMapper.writeValueAsString(response),
                    RESULT_TTL);
        } catch (Exception ex) {
            log.warn("缓存支付结果失败 requestId={}", response.getRequestId(), ex);
        }
    }

    private void releaseLock(String lockKey, String token) {
        String script = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                  return redis.call('del', KEYS[1])
                end
                return 0
                """;
        redisTemplate.execute(
                new org.springframework.data.redis.core.script.DefaultRedisScript<>(script, Long.class),
                List.of(lockKey), token);
    }

    private void validate(PaymentRpcRequest request) {
        if (request == null
                || !StringUtils.hasText(request.getRequestId())
                || !StringUtils.hasText(request.getOrderNo())
                || !StringUtils.hasText(request.getPaymentNo())
                || !StringUtils.hasText(request.getChannel())
                || request.getAmount() == null
                || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("支付请求参数不完整");
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
