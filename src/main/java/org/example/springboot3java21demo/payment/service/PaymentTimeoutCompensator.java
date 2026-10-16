package org.example.springboot3java21demo.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot3java21demo.payment.constant.PaymentConstants;
import org.example.springboot3java21demo.payment.domain.PaymentOrder;
import org.example.springboot3java21demo.payment.domain.PaymentStatus;
import org.example.springboot3java21demo.payment.infra.gateway.PaymentChannelGateway;
import org.example.springboot3java21demo.payment.repository.InventoryRepository;
import org.example.springboot3java21demo.payment.repository.OutboxRepository;
import org.example.springboot3java21demo.payment.repository.PaymentOrderRepository;
import org.example.springboot3java21demo.payment.support.PaymentEvents;
import org.example.springboot3java21demo.payment.support.PaymentMetrics;
import org.example.springboot3java21demo.payment.support.PaymentStateMachine;
import org.example.springboot3java21demo.payment.support.TraceContext;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * 未决单补偿任务：把 UNKNOWN / PAYING 的单推进到终态。
 *
 * <p>这是「不超卖 + 不多扣」闭环里最关键的一环。渠道超时后我们不知道钱有没有扣，
 * 于是库存继续冻结、订单停在 UNKNOWN，由本任务反复向渠道查询：</p>
 * <ul>
 *   <li>渠道确认成功 → 补记 SUCCESS，库存 reserved 转 sold（用户付了钱，货必须给他）；</li>
 *   <li>渠道确认失败 → 补记 FAILED，释放库存（钱没扣，货放回去继续卖）；</li>
 *   <li>渠道仍未知 → 保持冻结，下一轮再查。绝不猜测，猜测的代价是超卖或客诉。</li>
 * </ul>
 */
@Slf4j
public class PaymentTimeoutCompensator {

    private static final int BATCH_SIZE = 200;

    private final PaymentOrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final OutboxRepository outboxRepository;
    private final PaymentChannelGateway channelGateway;
    private final PaymentMetrics metrics;
    private final long unsettledTimeoutMillis;

    public PaymentTimeoutCompensator(PaymentOrderRepository orderRepository,
                                     InventoryRepository inventoryRepository,
                                     OutboxRepository outboxRepository,
                                     PaymentChannelGateway channelGateway,
                                     PaymentMetrics metrics,
                                     long unsettledTimeoutMillis) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
        this.outboxRepository = outboxRepository;
        this.channelGateway = channelGateway;
        this.metrics = metrics;
        this.unsettledTimeoutMillis = unsettledTimeoutMillis;
    }

    /**
     * 补偿一轮，返回本轮闭环的单数
     */
    @Scheduled(fixedDelayString = "${payment.compensate.interval-ms:5000}")
    public int compensateOnce() {
        long now = System.currentTimeMillis();
        List<PaymentOrder> unsettled = orderRepository.findUnsettledBefore(now - unsettledTimeoutMillis, BATCH_SIZE);
        int closed = 0;
        for (PaymentOrder order : unsettled) {
            closed += close(order, now) ? 1 : 0;
        }
        return closed;
    }

    private boolean close(PaymentOrder order, long now) {
        String traceId = order.traceId() == null ? TraceContext.currentOrCreate() : order.traceId();
        PaymentChannelGateway.Result result = channelGateway.queryTrade(order.paymentNo());

        if (result.unknown()) {
            log.info("[{}] 渠道仍未决，继续冻结 paymentNo={} code={}", traceId, order.paymentNo(), result.code());
            return false;
        }

        if (result.success()) {
            PaymentOrder settled = PaymentStateMachine.advance(orderRepository, order,
                    PaymentStatus.SUCCESS, null, now);
            confirmStock(settled, traceId, now);
            PaymentEvents.emit(outboxRepository, settled, PaymentConstants.EVENT_PAYMENT_SUCCESS,
                    "COMPENSATED:" + result.channelTradeNo(), traceId, now);
            metrics.recordSuccess();
            metrics.recordCompensated();
            log.warn("[{}] 补偿完成：渠道确认成功，补记支付成功 paymentNo={}", traceId, order.paymentNo());
            return true;
        }

        PaymentOrder settled = PaymentStateMachine.advance(orderRepository, order,
                PaymentStatus.FAILED, result.message(), now);
        releaseStock(settled, traceId, now);
        PaymentEvents.emit(outboxRepository, settled, PaymentConstants.EVENT_PAYMENT_FAILED,
                "COMPENSATED:" + result.code(), traceId, now);
        metrics.recordFailed();
        metrics.recordCompensated();
        log.warn("[{}] 补偿完成：渠道确认失败，已释放库存 paymentNo={}", traceId, order.paymentNo());
        return true;
    }

    private void confirmStock(PaymentOrder order, String traceId, long now) {
        if (!order.hasStock()) {
            return;
        }
        if (!inventoryRepository.confirm(order.stockLine().skuId(), order.stockLine().quantity())) {
            log.error("[{}] 补偿时库存确认失败，需要人工介入 paymentNo={}", traceId, order.paymentNo());
            PaymentEvents.emit(outboxRepository, order, PaymentConstants.EVENT_RECONCILE_DIFF,
                    "COMPENSATE_STOCK_CONFIRM_FAILED", traceId, now);
        }
    }

    private void releaseStock(PaymentOrder order, String traceId, long now) {
        if (!order.hasStock()) {
            return;
        }
        if (!inventoryRepository.release(order.stockLine().skuId(), order.stockLine().quantity())) {
            log.error("[{}] 补偿时库存释放失败，需要人工介入 paymentNo={}", traceId, order.paymentNo());
            PaymentEvents.emit(outboxRepository, order, PaymentConstants.EVENT_RECONCILE_DIFF,
                    "COMPENSATE_STOCK_RELEASE_FAILED", traceId, now);
            return;
        }
        PaymentEvents.emit(outboxRepository, order, PaymentConstants.EVENT_STOCK_RELEASED,
                order.stockLine().skuId(), traceId, now);
    }
}
