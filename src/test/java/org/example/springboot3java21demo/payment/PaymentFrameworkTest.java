package org.example.springboot3java21demo.payment;

import org.example.springboot3java21demo.payment.constant.PaymentErrorCode;
import org.example.springboot3java21demo.payment.domain.ChannelTradeBill;
import org.example.springboot3java21demo.payment.domain.OutboxEvent;
import org.example.springboot3java21demo.payment.domain.PaymentOrder;
import org.example.springboot3java21demo.payment.domain.PaymentStatus;
import org.example.springboot3java21demo.payment.domain.ReconcileTask;
import org.example.springboot3java21demo.payment.domain.StockSnapshot;
import org.example.springboot3java21demo.payment.exception.PaymentException;
import org.example.springboot3java21demo.payment.model.PaymentRequest;
import org.example.springboot3java21demo.payment.model.PaymentResult;
import org.example.springboot3java21demo.payment.model.ReconcileReport;
import org.example.springboot3java21demo.payment.support.PaymentCircuitBreaker;
import org.example.springboot3java21demo.payment.support.PaymentRateLimiter;
import org.example.springboot3java21demo.payment.support.SlidingWindowRateLimiter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 支付框架的四条主线验收：不超卖、幂等、一致性闭环、高可用降级。
 */
class PaymentFrameworkTest {

    private static final DateTimeFormatter BIZ_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    @DisplayName("并发抢购不超卖：库存永远不会被扣成负数")
    void concurrentPaymentMustNotOversell() throws Exception {
        PaymentTestContext ctx = new PaymentTestContext(100);
        int attempts = 800;
        CountDownLatch startGate = new CountDownLatch(1);

        List<Callable<PaymentResult>> tasks = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            int index = i;
            tasks.add(() -> {
                startGate.await();
                return ctx.paymentService.pay(request("REQ-OVERSELL-" + index, ctx.skuId, 1));
            });
        }

        int success = 0;
        int stockRejected = 0;
        try (ExecutorService pool = Executors.newFixedThreadPool(64)) {
            List<Future<PaymentResult>> futures = tasks.stream().map(pool::submit).toList();
            startGate.countDown();
            for (Future<PaymentResult> future : futures) {
                try {
                    if (PaymentStatus.SUCCESS.name().equals(future.get(60, TimeUnit.SECONDS).getStatus())) {
                        success++;
                    }
                } catch (ExecutionException ex) {
                    if (ex.getCause() instanceof PaymentException paymentException
                            && paymentException.getErrorCode() == PaymentErrorCode.STOCK_NOT_ENOUGH) {
                        stockRejected++;
                    } else {
                        throw ex;
                    }
                }
            }
        }

        StockSnapshot stock = ctx.inventory.find(ctx.skuId).orElseThrow();
        int successCount = success;
        int rejectedCount = stockRejected;
        assertAll(
                () -> assertEquals(100, successCount, "只有 100 个库存能支付成功"),
                () -> assertEquals(700, rejectedCount, "其余请求必须因库存不足被拒绝"),
                () -> assertEquals(100, stock.sold(), "已售必须恰好等于总库存"),
                () -> assertEquals(0, stock.reserved(), "不允许有预扣残留"),
                () -> assertEquals(0, stock.available(), "可售库存归零"),
                () -> assertTrue(stock.available() >= 0, "可售库存绝不能为负"),
                () -> assertEquals(700, ctx.metrics.stockRejected())
        );
    }

    @Test
    @DisplayName("同一 requestId 并发重复提交，只会扣款一次")
    void sameRequestIdMustBeIdempotent() throws Exception {
        PaymentTestContext ctx = new PaymentTestContext(10);
        String requestId = "REQ-IDEMPOTENT-1";
        int concurrency = 64;
        CountDownLatch startGate = new CountDownLatch(1);

        List<Callable<PaymentResult>> tasks = new ArrayList<>();
        for (int i = 0; i < concurrency; i++) {
            tasks.add(() -> {
                startGate.await();
                return ctx.paymentService.pay(request(requestId, ctx.skuId, 2));
            });
        }

        List<PaymentResult> results = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(concurrency)) {
            List<Future<PaymentResult>> futures = tasks.stream().map(pool::submit).toList();
            startGate.countDown();
            for (Future<PaymentResult> future : futures) {
                results.add(future.get(60, TimeUnit.SECONDS));
            }
        }

        StockSnapshot stock = ctx.inventory.find(ctx.skuId).orElseThrow();
        long firstTimeExecutions = results.stream().filter(result -> !result.isDuplicate()).count();
        PaymentResult finalResult = ctx.paymentService.query(requestId);

        assertAll(
                () -> assertEquals(1, ctx.orders.size(), "同一 requestId 只允许一张支付单"),
                () -> assertEquals(1, ctx.gateway.payCallCount(), "渠道只能被调用一次"),
                () -> assertEquals(1, firstTimeExecutions, "只有一个请求是首次执行，其余都是重复请求"),
                () -> assertEquals(2, stock.sold(), "只扣一次数量"),
                () -> assertEquals(0, stock.reserved(), "不允许有预扣残留"),
                () -> assertEquals(PaymentStatus.SUCCESS.name(), finalResult.getStatus()),
                () -> assertTrue(finalResult.isDuplicate())
        );
        assertTrue(results.stream().allMatch(result ->
                        PaymentStatus.SUCCESS.name().equals(result.getStatus())
                                || PaymentStatus.PAYING.name().equals(result.getStatus())),
                "重复请求只会拿到成功或处理中，绝不能出现失败或二次扣款");
    }

    @Test
    @DisplayName("已终态的支付单重复提交，直接复用首次结果")
    void settledOrderReusesFirstResult() {
        PaymentTestContext ctx = new PaymentTestContext(10);
        PaymentResult first = ctx.paymentService.pay(request("REQ-REUSE-1", ctx.skuId, 1));
        PaymentResult second = ctx.paymentService.pay(request("REQ-REUSE-1", ctx.skuId, 1));

        StockSnapshot stock = ctx.inventory.find(ctx.skuId).orElseThrow();
        assertAll(
                () -> assertEquals(PaymentStatus.SUCCESS.name(), first.getStatus()),
                () -> assertFalse(first.isDuplicate()),
                () -> assertEquals(first.getPaymentNo(), second.getPaymentNo()),
                () -> assertTrue(second.isDuplicate()),
                () -> assertEquals(1, ctx.gateway.payCallCount()),
                () -> assertEquals(1, stock.sold())
        );
    }

    @Test
    @DisplayName("渠道明确拒绝时释放预扣库存，货可以继续卖")
    void channelRejectionReleasesStock() {
        // 成功率 0 / 未知率 0 表示渠道一定明确拒绝
        PaymentTestContext ctx = new PaymentTestContext(10, 0.0d, 0.0d, 0L, 1_000_000L);

        PaymentResult result = ctx.paymentService.pay(request("REQ-REJECT-1", ctx.skuId, 4));
        StockSnapshot stock = ctx.inventory.find(ctx.skuId).orElseThrow();
        List<String> eventTypes = ctx.outbox.findAll().stream().map(OutboxEvent::eventType).toList();

        assertAll(
                () -> assertEquals(PaymentStatus.FAILED.name(), result.getStatus()),
                () -> assertEquals(0, stock.sold()),
                () -> assertEquals(0, stock.reserved(), "失败必须释放预扣"),
                () -> assertEquals(10, stock.available(), "库存必须回到 10"),
                () -> assertEquals(2, ctx.outbox.countByStatus(OutboxEvent.Status.PENDING)),
                () -> assertTrue(eventTypes.contains("STOCK_RELEASED"), "必须留下库存释放事件"),
                () -> assertTrue(eventTypes.contains("PAYMENT_FAILED"), "必须留下支付失败事件")
        );
    }

    @Test
    @DisplayName("渠道结果未知时冻结库存，由补偿任务确认后闭环")
    void unknownResultIsClosedByCompensator() {
        // 未知率 1.0 表示渠道一定超时；超时时间设为 0 让补偿任务立刻能捞到
        PaymentTestContext ctx = new PaymentTestContext(10, 0.0d, 1.0d, 0L, 1_000_000L);

        PaymentResult result = ctx.paymentService.pay(request("REQ-UNKNOWN-1", ctx.skuId, 3));
        StockSnapshot frozen = ctx.inventory.find(ctx.skuId).orElseThrow();

        assertAll(
                () -> assertEquals(PaymentStatus.UNKNOWN.name(), result.getStatus()),
                () -> assertEquals(3, frozen.reserved(), "结果未知时必须保持冻结"),
                () -> assertEquals(0, frozen.sold(), "未经渠道确认绝不能记为已售"),
                () -> assertEquals(7, frozen.available())
        );

        // 渠道事后确认这笔其实成功了
        ctx.gateway.settleAsSuccess(result.getPaymentNo());
        int closed = ctx.compensator.compensateOnce();

        StockSnapshot settled = ctx.inventory.find(ctx.skuId).orElseThrow();
        PaymentResult afterCompensation = ctx.paymentService.query("REQ-UNKNOWN-1");
        assertAll(
                () -> assertEquals(1, closed),
                () -> assertEquals(PaymentStatus.SUCCESS.name(), afterCompensation.getStatus()),
                () -> assertEquals(3, settled.sold(), "补偿成功后要补记已售"),
                () -> assertEquals(0, settled.reserved()),
                () -> assertEquals(1, ctx.metrics.compensated())
        );
    }

    @Test
    @DisplayName("本地消息表保证最终一致：投递失败会自动重试直到成功")
    void outboxDeliversEventuallyEvenIfPublisherFails() throws Exception {
        PaymentTestContext ctx = new PaymentTestContext(10);
        for (int i = 0; i < 3; i++) {
            ctx.paymentService.pay(request("REQ-OUTBOX-" + i, ctx.skuId, 1));
        }
        assertEquals(3, ctx.outbox.countByStatus(OutboxEvent.Status.PENDING));

        int delivered = ctx.outboxRelay.relayOnce();
        assertEquals(3, delivered);
        assertEquals(0, ctx.outbox.countByStatus(OutboxEvent.Status.PENDING));
        assertEquals(3, ctx.outbox.countByStatus(OutboxEvent.Status.SENT));
        assertEquals(3, ctx.publisher.deliveredEventIds.size());

        // 再来一笔，注入一次投递失败
        ctx.paymentService.pay(request("REQ-OUTBOX-FAIL", ctx.skuId, 1));
        ctx.publisher.failNext(1);
        assertEquals(0, ctx.outboxRelay.relayOnce(), "本轮投递失败，不应算作成功");

        OutboxEvent retried = ctx.outbox.findAll().stream()
                .filter(event -> event.status() == OutboxEvent.Status.PENDING)
                .findFirst()
                .orElseThrow();
        assertAll(
                () -> assertEquals(1, retried.retryCount()),
                () -> assertTrue(retried.nextRetryAt() > System.currentTimeMillis() - 1000),
                () -> assertEquals(1L, ctx.metrics.snapshot().get("outboxRetried"))
        );

        // 退避时间过去后必须能补投成功（at-least-once）
        Thread.sleep(1_100L);
        assertEquals(1, ctx.outboxRelay.relayOnce());
        assertEquals(0, ctx.outbox.countByStatus(OutboxEvent.Status.PENDING));
        assertEquals(4, ctx.publisher.deliveredEventIds.size());
    }

    @Test
    @DisplayName("对账双向比对差异并生成可追踪工单")
    void reconcileDetectsBidirectionalDifferences() {
        PaymentTestContext ctx = new PaymentTestContext(10);
        PaymentResult matched = ctx.paymentService.pay(request("REQ-REC-1", ctx.skuId, 2));
        PaymentResult mismatched = ctx.paymentService.pay(request("REQ-REC-2", ctx.skuId, 3));

        // 本地失败、渠道成功的危险场景
        PaymentOrder failedLocally = syntheticOrder("PAY-SYNTH-FAILED", new BigDecimal("9.99"),
                PaymentStatus.FAILED);
        ctx.orders.insertIfAbsent(failedLocally);

        String bizDate = today();
        ctx.channelBills.saveAll(List.of(
                new ChannelTradeBill(matched.getPaymentNo(), "CH-1", bizDate, new BigDecimal("2.00"), true, "SUCCESS"),
                new ChannelTradeBill(mismatched.getPaymentNo(), "CH-2", bizDate, new BigDecimal("99.00"), true, "SUCCESS"),
                new ChannelTradeBill("PAY-SYNTH-FAILED", "CH-3", bizDate, new BigDecimal("9.99"), true, "SUCCESS"),
                new ChannelTradeBill("PAY-GHOST", "CH-4", bizDate, new BigDecimal("5.00"), true, "SUCCESS")
        ));

        ReconcileReport report = ctx.reconcileService.reconcile(bizDate);
        List<ReconcileTask> tasks = ctx.reconcileTasks.findByBizDate(bizDate);

        assertAll(
                () -> assertEquals(3, report.localTotal()),
                () -> assertEquals(4, report.channelTotal()),
                () -> assertEquals(1, report.matched(), "只有第一笔是完全一致的"),
                () -> assertEquals(3, report.diffCount()),
                () -> assertFalse(report.balanced()),
                () -> assertEquals(1, report.diffByType().get(ReconcileTask.DiffType.AMOUNT_MISMATCH.name())),
                () -> assertEquals(1, report.diffByType()
                        .get(ReconcileTask.DiffType.LOCAL_FAILED_CHANNEL_SUCCESS.name())),
                () -> assertEquals(1, report.diffByType()
                        .get(ReconcileTask.DiffType.CHANNEL_SUCCESS_LOCAL_MISSING.name())),
                () -> assertEquals(3, tasks.size()),
                () -> assertTrue(tasks.stream().allMatch(task -> task.status() == ReconcileTask.Status.OPEN))
        );
    }

    @Test
    @DisplayName("超过限流阈值时快速失败，不进入支付主流程")
    void rateLimiterRejectsExcessTraffic() {
        // 固定时钟，保证窗口不随真实时间滚动，测试结果完全确定
        PaymentRateLimiter fixedClockLimiter =
                new PaymentRateLimiter(60, 5, () -> 1_800_000_000L);
        PaymentTestContext ctx = new PaymentTestContext(10, 1.0d, 0.0d, 0L, fixedClockLimiter);

        int limited = 0;
        int passed = 0;
        for (int i = 0; i < 20; i++) {
            try {
                ctx.paymentService.pay(request("REQ-LIMIT-" + i, null, null));
                passed++;
            } catch (PaymentException ex) {
                assertEquals(PaymentErrorCode.RATE_LIMITED, ex.getErrorCode());
                limited++;
            }
        }

        int finalLimited = limited;
        int finalPassed = passed;
        assertAll(
                () -> assertEquals(5, finalPassed),
                () -> assertEquals(15, finalLimited),
                () -> assertEquals(15, ctx.metrics.rateLimited()),
                () -> assertEquals(5, ctx.orders.size(), "被限流的请求不产生任何业务副作用")
        );
    }

    @Test
    @DisplayName("限流窗口跨秒时不会误清当前窗口内的计数")
    void slidingWindowKeepsCountsWithinWindow() {
        AtomicLong clock = new AtomicLong(1_800_000_000L);
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(60, 3, clock::get);

        assertTrue(limiter.tryAcquire());
        assertTrue(limiter.tryAcquire());
        assertTrue(limiter.tryAcquire());
        assertFalse(limiter.tryAcquire(), "额度用尽必须拒绝");

        clock.incrementAndGet();
        assertFalse(limiter.tryAcquire(), "只跨了一秒，之前的计数仍在 60 秒窗口内");

        clock.addAndGet(60);
        assertTrue(limiter.tryAcquire(), "走出窗口后额度必须恢复");
    }

    @Test
    @DisplayName("渠道熔断后降级为结果未知而不是直接判失败")
    void circuitBreakerDegradesToUnknown() {
        // 未知率 1.0：每次调用都失败，连续 3 次后熔断器打开
        PaymentTestContext ctx = new PaymentTestContext(10, 0.0d, 1.0d, 60_000L, 1_000_000L);

        List<PaymentResult> results = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            results.add(ctx.paymentService.pay(request("REQ-CIRCUIT-" + i, null, null)));
        }

        assertAll(
                () -> assertEquals(PaymentCircuitBreaker.State.OPEN, ctx.circuitBreaker.state()),
                () -> assertEquals(2, ctx.metrics.circuitRejected()),
                () -> assertTrue(results.stream()
                                .allMatch(result -> PaymentStatus.UNKNOWN.name().equals(result.getStatus())),
                        "熔断降级必须标记为结果未知，绝不能判失败后释放库存"),
                () -> assertEquals(0, results.stream()
                        .filter(result -> PaymentStatus.FAILED.name().equals(result.getStatus()))
                        .count())
        );
    }

    @Test
    @DisplayName("状态机拒绝非法流转，终态不可被改写")
    void stateMachineRejectsIllegalTransition() {
        long now = System.currentTimeMillis();
        PaymentOrder order = PaymentOrder.create("PAY-SM-1", "REQ-SM-1", "ORD-SM-1", "U1",
                new BigDecimal("1.00"), "MOCK", null, today(), now, "trace-sm");

        PaymentOrder paying = order.transitionTo(PaymentStatus.PAYING, null, now);
        PaymentOrder success = paying.transitionTo(PaymentStatus.SUCCESS, null, now);

        assertAll(
                () -> assertEquals(PaymentStatus.INIT, order.status()),
                () -> assertEquals(1L, paying.version(), "每次流转 version 必须自增，用于乐观锁"),
                () -> assertEquals(2L, success.version()),
                () -> assertTrue(success.status().isTerminal()),
                () -> assertThrows(PaymentException.class,
                        () -> order.transitionTo(PaymentStatus.SUCCESS, null, now),
                        "INIT 不能跳过 PAYING 直接成功"),
                () -> assertThrows(PaymentException.class,
                        () -> success.transitionTo(PaymentStatus.FAILED, null, now),
                        "终态不允许再变更"),
                () -> assertNotNull(success.paymentNo()),
                () -> assertNotEquals(PaymentStatus.UNKNOWN, success.status())
        );
    }

    private static PaymentRequest request(String requestId, String skuId, Integer quantity) {
        return PaymentRequest.builder()
                .requestId(requestId)
                .orderNo("ORD-" + requestId)
                .userId("U-1001")
                .amount(new BigDecimal("2.00"))
                .channel("MOCK")
                .skuId(skuId)
                .quantity(quantity)
                .remark("unit-test")
                .build();
    }

    private static PaymentOrder syntheticOrder(String paymentNo, BigDecimal amount, PaymentStatus target) {
        long now = System.currentTimeMillis();
        PaymentOrder order = PaymentOrder.create(paymentNo, "REQ-" + paymentNo, "ORD-" + paymentNo, "U1",
                amount, "MOCK", null, today(), now, "trace-synthetic");
        return order.transitionTo(PaymentStatus.PAYING, null, now)
                .transitionTo(target, "synthetic", now);
    }

    private static String today() {
        return LocalDate.now().format(BIZ_DATE_FORMATTER);
    }
}
