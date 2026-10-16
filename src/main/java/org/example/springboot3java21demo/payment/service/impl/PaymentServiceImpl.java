package org.example.springboot3java21demo.payment.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot3java21demo.payment.constant.PaymentConstants;
import org.example.springboot3java21demo.payment.constant.PaymentErrorCode;
import org.example.springboot3java21demo.payment.domain.PaymentOrder;
import org.example.springboot3java21demo.payment.domain.PaymentStatus;
import org.example.springboot3java21demo.payment.domain.StockLine;
import org.example.springboot3java21demo.payment.exception.PaymentException;
import org.example.springboot3java21demo.payment.infra.gateway.PaymentChannelGateway;
import org.example.springboot3java21demo.payment.infra.idempotent.IdempotencyStore;
import org.example.springboot3java21demo.payment.model.PaymentRequest;
import org.example.springboot3java21demo.payment.model.PaymentResult;
import org.example.springboot3java21demo.payment.repository.InventoryRepository;
import org.example.springboot3java21demo.payment.repository.OutboxRepository;
import org.example.springboot3java21demo.payment.repository.PaymentOrderRepository;
import org.example.springboot3java21demo.payment.service.PaymentService;
import org.example.springboot3java21demo.payment.support.PaymentCircuitBreaker;
import org.example.springboot3java21demo.payment.support.PaymentEvents;
import org.example.springboot3java21demo.payment.support.PaymentMetrics;
import org.example.springboot3java21demo.payment.support.PaymentRateLimiter;
import org.example.springboot3java21demo.payment.support.PaymentStateMachine;
import org.example.springboot3java21demo.payment.support.TraceContext;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/**
 * 支付核心编排。
 *
 * <p>一次 pay 调用串起四条主线：</p>
 * <pre>
 *   限流 ──▶ 幂等闸门 ──▶ 库存预扣 ──▶ 状态机 PAYING ──▶ 渠道调用（熔断保护）
 *                                                         │
 *                         ┌───────────────────────────────┼──────────────────────────────┐
 *                         ▼                               ▼                              ▼
 *                      SUCCESS                          FAILED                        UNKNOWN
 *               确认库存 + 发成功事件            释放库存 + 发失败事件          冻结库存 + 发未知事件
 * </pre>
 *
 * <p>高可用：限流快速失败、渠道熔断降级、全流程无阻塞等待、状态外置可水平扩展。
 * 不超卖：tryReserve 原子条件更新是库存唯一入口，成功才 confirm，失败才 release。
 * 数据一致性：状态机禁止非法流转、乐观锁串行化单笔更新、本地消息表保证跨系统最终一致。
 * 可复盘：traceId 贯穿，每次状态变更都伴随 outbox 事件落库，可回放出完整轨迹。</p>
 */
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private static final DateTimeFormatter BIZ_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final PaymentOrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final OutboxRepository outboxRepository;
    private final IdempotencyStore idempotencyStore;
    private final PaymentChannelGateway channelGateway;
    private final PaymentCircuitBreaker circuitBreaker;
    private final PaymentRateLimiter rateLimiter;
    private final PaymentMetrics metrics;

    public PaymentServiceImpl(PaymentOrderRepository orderRepository,
                              InventoryRepository inventoryRepository,
                              OutboxRepository outboxRepository,
                              IdempotencyStore idempotencyStore,
                              PaymentChannelGateway channelGateway,
                              PaymentCircuitBreaker circuitBreaker,
                              PaymentRateLimiter rateLimiter,
                              PaymentMetrics metrics) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
        this.outboxRepository = outboxRepository;
        this.idempotencyStore = idempotencyStore;
        this.channelGateway = channelGateway;
        this.circuitBreaker = circuitBreaker;
        this.rateLimiter = rateLimiter;
        this.metrics = metrics;
    }

    @Override
    public PaymentResult pay(PaymentRequest request) {
        validate(request);
        String traceId = TraceContext.currentOrCreate();
        metrics.recordRequest();

        // 第一道闸门：限流。超额直接 429 快速失败，不排队（排队会把 RT 放大到拖垮线程池）
        if (!rateLimiter.tryAcquire()) {
            metrics.recordRateLimited();
            log.warn("[{}] 支付请求被限流 requestId={}", traceId, request.getRequestId());
            throw new PaymentException(PaymentErrorCode.RATE_LIMITED);
        }

        long start = System.nanoTime();
        try {
            return doPay(request, traceId);
        } finally {
            metrics.recordLatency(System.nanoTime() - start);
        }
    }

    @Override
    public PaymentResult query(String requestId) {
        if (!StringUtils.hasText(requestId)) {
            throw new PaymentException(PaymentErrorCode.PARAM_INVALID, "requestId不能为空");
        }
        // 数据库是事实源，先查库；幂等缓存只是加速层
        Optional<PaymentOrder> order = orderRepository.findByRequestId(requestId);
        if (order.isPresent()) {
            return toResult(order.get(), true);
        }
        Optional<PaymentResult> cached = idempotencyStore.findResult(requestId);
        if (cached.isPresent()) {
            return copyWithDuplicate(cached.get(), true);
        }
        if (idempotencyStore.isProcessing(requestId)) {
            return PaymentResult.builder()
                    .requestId(requestId)
                    .status(PaymentStatus.PAYING.name())
                    .duplicate(true)
                    .message("支付处理中，请稍后用相同 requestId 查询")
                    .traceId(TraceContext.currentOrCreate())
                    .build();
        }
        return PaymentResult.builder()
                .requestId(requestId)
                .status(PaymentConstants.STATUS_NOT_FOUND)
                .duplicate(false)
                .message("未找到对应的支付记录")
                .traceId(TraceContext.currentOrCreate())
                .build();
    }

    private PaymentResult doPay(PaymentRequest request, String traceId) {
        long now = System.currentTimeMillis();
        String requestId = request.getRequestId();

        // 第二道闸门：分布式幂等。抢不到键说明已有请求在执行或已完成
        if (!idempotencyStore.tryAcquire(requestId)) {
            return reuseExisting(request, traceId);
        }

        PaymentOrder order;
        try {
            order = loadOrCreate(request, traceId, now);
        } catch (RuntimeException ex) {
            // 此时还没有任何业务副作用，释放幂等键让客户端可以立刻重试
            idempotencyStore.release(requestId);
            throw ex;
        }

        if (order.status().isTerminal()) {
            PaymentResult result = toResult(order, true);
            idempotencyStore.saveResult(requestId, result);
            return result;
        }

        // 第三道闸门：原子预扣库存。失败即关单，绝不会出现超卖
        if (order.hasStock()
                && !inventoryRepository.tryReserve(order.stockLine().skuId(), order.stockLine().quantity())) {
            metrics.recordStockRejected();
            PaymentOrder closed = advance(order, PaymentStatus.CLOSED, "库存不足", now);
            PaymentResult result = toResult(closed, false);
            idempotencyStore.saveResult(requestId, result);
            log.warn("[{}] 库存不足，关单 paymentNo={} skuId={}", traceId, order.paymentNo(),
                    order.stockLine().skuId());
            throw new PaymentException(PaymentErrorCode.STOCK_NOT_ENOUGH);
        }

        PaymentOrder paying = advance(order, PaymentStatus.PAYING, null, now);

        try {
            PaymentOrder settled = settle(paying, traceId, now);
            PaymentResult result = toResult(settled, false);
            idempotencyStore.saveResult(requestId, result);
            return result;
        } catch (RuntimeException ex) {
            // 关键：这里绝不能释放幂等键。可能已经调到渠道了，重试有二次扣款风险，
            // 正确做法是把当前状态缓存下来，让客户端查询、让补偿任务闭环。
            PaymentOrder latest = orderRepository.findByRequestId(requestId).orElse(paying);
            idempotencyStore.saveResult(requestId, toResult(latest, false));
            log.error("[{}] 支付结算阶段异常，已保留幂等键等待补偿 paymentNo={} status={}",
                    traceId, latest.paymentNo(), latest.status(), ex);
            throw ex;
        }
    }

    /**
     * 渠道调用 + 三态落库
     */
    private PaymentOrder settle(PaymentOrder paying, String traceId, long now) {
        PaymentChannelGateway.Result channelResult = callChannel(paying, traceId);

        if (channelResult.success()) {
            PaymentOrder settled = advance(paying, PaymentStatus.SUCCESS, null, now);
            confirmStock(paying, traceId, now);
            emit(paying, PaymentConstants.EVENT_PAYMENT_SUCCESS, channelResult.channelTradeNo(), traceId, now);
            metrics.recordSuccess();
            log.info("[{}] 支付成功 paymentNo={} orderNo={}", traceId, paying.paymentNo(), paying.orderNo());
            return settled;
        }

        if (channelResult.unknown()) {
            // 熔断降级/超时：钱可能已经扣了，库存必须继续冻结，交给补偿任务查渠道确认真实结果
            PaymentOrder unknown = advance(paying, PaymentStatus.UNKNOWN, channelResult.message(), now);
            emit(paying, PaymentConstants.EVENT_PAYMENT_UNKNOWN, channelResult.code(), traceId, now);
            metrics.recordUnknown();
            log.warn("[{}] 支付结果未知，等待补偿 paymentNo={} code={}", traceId, paying.paymentNo(),
                    channelResult.code());
            return unknown;
        }

        PaymentOrder failed = advance(paying, PaymentStatus.FAILED, channelResult.message(), now);
        releaseStock(paying, traceId, now);
        emit(paying, PaymentConstants.EVENT_PAYMENT_FAILED, channelResult.code(), traceId, now);
        metrics.recordFailed();
        log.info("[{}] 支付被渠道拒绝 paymentNo={} code={}", traceId, paying.paymentNo(), channelResult.code());
        return failed;
    }

    private PaymentChannelGateway.Result callChannel(PaymentOrder order, String traceId) {
        if (!circuitBreaker.allowRequest()) {
            metrics.recordCircuitRejected();
            // 熔断时不能判失败：没调到渠道 不等于 没扣款，只能标记未知后由补偿查询
            return PaymentChannelGateway.Result.unknown("CIRCUIT_OPEN", "渠道熔断中，结果待确认");
        }
        try {
            PaymentChannelGateway.Result result = channelGateway.pay(new PaymentChannelGateway.PayCommand(
                    order.paymentNo(), order.orderNo(), order.amount(), order.channel(), traceId));
            if (result.unknown()) {
                circuitBreaker.recordFailure();
            } else {
                // 渠道明确拒绝属于业务结果，不代表渠道故障，不应计入熔断
                circuitBreaker.recordSuccess();
            }
            return result;
        } catch (RuntimeException ex) {
            circuitBreaker.recordFailure();
            log.error("[{}] 渠道调用异常，降级为结果未知 paymentNo={}", traceId, order.paymentNo(), ex);
            return PaymentChannelGateway.Result.unknown("CHANNEL_EXCEPTION", ex.getMessage());
        }
    }

    private void confirmStock(PaymentOrder order, String traceId, long now) {
        if (!order.hasStock()) {
            return;
        }
        boolean confirmed = inventoryRepository.confirm(order.stockLine().skuId(), order.stockLine().quantity());
        if (!confirmed) {
            // 钱已收、货没扣：最严重的一类不一致，必须落对账差异事件让人工介入，绝不能静默吞掉
            log.error("[{}] 支付成功但库存确认失败，需要人工介入 paymentNo={} skuId={}",
                    traceId, order.paymentNo(), order.stockLine().skuId());
            emit(order, PaymentConstants.EVENT_RECONCILE_DIFF, "STOCK_CONFIRM_FAILED", traceId, now);
        }
    }

    private void releaseStock(PaymentOrder order, String traceId, long now) {
        if (!order.hasStock()) {
            return;
        }
        boolean released = inventoryRepository.release(order.stockLine().skuId(), order.stockLine().quantity());
        if (!released) {
            log.error("[{}] 库存释放失败，需要人工介入 paymentNo={} skuId={}",
                    traceId, order.paymentNo(), order.stockLine().skuId());
            emit(order, PaymentConstants.EVENT_RECONCILE_DIFF, "STOCK_RELEASE_FAILED", traceId, now);
            return;
        }
        emit(order, PaymentConstants.EVENT_STOCK_RELEASED, order.stockLine().skuId(), traceId, now);
    }

    /**
     * 状态机推进 + 乐观锁抢占，冲突时有界重试（补偿任务可能同时在推进同一单）
     */
    private PaymentOrder advance(PaymentOrder order, PaymentStatus target, String reason, long now) {
        return PaymentStateMachine.advance(orderRepository, order, target, reason, now);
    }

    private PaymentOrder loadOrCreate(PaymentRequest request, String traceId, long now) {
        Optional<PaymentOrder> existing = orderRepository.findByRequestId(request.getRequestId());
        if (existing.isPresent()) {
            return existing.get();
        }
        PaymentOrder created = PaymentOrder.create(
                newPaymentNo(),
                request.getRequestId(),
                request.getOrderNo(),
                request.getUserId(),
                request.getAmount(),
                request.getChannel(),
                StockLine.of(request.getSkuId(), request.getQuantity()),
                LocalDate.now().format(BIZ_DATE_FORMATTER),
                now,
                traceId);
        // 唯一索引兜底：并发下即使两个线程都判断为「不存在」，也只有一条能真正插入
        return orderRepository.insertIfAbsent(created);
    }

    private PaymentResult reuseExisting(PaymentRequest request, String traceId) {
        Optional<PaymentResult> cached = idempotencyStore.findResult(request.getRequestId());
        if (cached.isPresent()) {
            return copyWithDuplicate(cached.get(), true);
        }
        return PaymentResult.builder()
                .requestId(request.getRequestId())
                .orderNo(request.getOrderNo())
                .status(PaymentStatus.PAYING.name())
                .duplicate(true)
                .message("支付处理中，请稍后用相同 requestId 查询")
                .amount(request.getAmount())
                .channel(request.getChannel())
                .traceId(traceId)
                .build();
    }

    private void emit(PaymentOrder order, String eventType, String detail, String traceId, long now) {
        PaymentEvents.emit(outboxRepository, order, eventType, detail, traceId, now);
    }

    private PaymentResult toResult(PaymentOrder order, boolean duplicate) {
        return PaymentResult.builder()
                .requestId(order.requestId())
                .orderNo(order.orderNo())
                .paymentNo(order.paymentNo())
                .status(order.status().name())
                .duplicate(duplicate)
                .message(messageOf(order))
                .paidAt(order.status() == PaymentStatus.SUCCESS ? order.updatedAt() : null)
                .amount(order.amount())
                .channel(order.channel())
                .traceId(order.traceId())
                .build();
    }

    private String messageOf(PaymentOrder order) {
        return switch (order.status()) {
            case INIT -> "支付单已创建";
            case PAYING -> "支付处理中";
            case SUCCESS -> "支付成功";
            case FAILED -> "支付失败：" + (order.failReason() == null ? "渠道拒绝" : order.failReason());
            case UNKNOWN -> "支付结果未知，正在与渠道确认";
            case CLOSED -> "支付关闭：" + (order.failReason() == null ? "未发起支付" : order.failReason());
        };
    }

    private PaymentResult copyWithDuplicate(PaymentResult source, boolean duplicate) {
        return PaymentResult.builder()
                .requestId(source.getRequestId())
                .orderNo(source.getOrderNo())
                .paymentNo(source.getPaymentNo())
                .status(source.getStatus())
                .duplicate(duplicate)
                .message(source.getMessage())
                .paidAt(source.getPaidAt())
                .amount(source.getAmount())
                .channel(source.getChannel())
                .traceId(source.getTraceId())
                .build();
    }

    private String newPaymentNo() {
        return "PAY" + System.currentTimeMillis()
                + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private void validate(PaymentRequest request) {
        if (request == null) {
            throw new PaymentException(PaymentErrorCode.PARAM_INVALID, "支付请求不能为空");
        }
        if (!StringUtils.hasText(request.getRequestId())) {
            throw new PaymentException(PaymentErrorCode.PARAM_INVALID, "requestId不能为空");
        }
        if (!StringUtils.hasText(request.getOrderNo())) {
            throw new PaymentException(PaymentErrorCode.PARAM_INVALID, "orderNo不能为空");
        }
        if (!StringUtils.hasText(request.getUserId())) {
            throw new PaymentException(PaymentErrorCode.PARAM_INVALID, "userId不能为空");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException(PaymentErrorCode.PARAM_INVALID, "支付金额必须大于0");
        }
        if (!StringUtils.hasText(request.getChannel())) {
            throw new PaymentException(PaymentErrorCode.PARAM_INVALID, "channel不能为空");
        }
        if (StringUtils.hasText(request.getSkuId())
                && request.getQuantity() != null && request.getQuantity() <= 0) {
            throw new PaymentException(PaymentErrorCode.PARAM_INVALID, "quantity必须大于0");
        }
    }
}
