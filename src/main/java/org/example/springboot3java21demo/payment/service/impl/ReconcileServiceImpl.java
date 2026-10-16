package org.example.springboot3java21demo.payment.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot3java21demo.payment.domain.ChannelTradeBill;
import org.example.springboot3java21demo.payment.domain.PaymentOrder;
import org.example.springboot3java21demo.payment.domain.PaymentStatus;
import org.example.springboot3java21demo.payment.domain.ReconcileTask;
import org.example.springboot3java21demo.payment.model.ReconcileReport;
import org.example.springboot3java21demo.payment.repository.ChannelBillRepository;
import org.example.springboot3java21demo.payment.repository.PaymentOrderRepository;
import org.example.springboot3java21demo.payment.repository.ReconcileTaskRepository;
import org.example.springboot3java21demo.payment.service.ReconcileService;
import org.example.springboot3java21demo.payment.support.TraceContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 对账实现：每天（或准实时）拿渠道账单和本地流水做双向比对。
 *
 * <p>单向比对只能发现「本地有、渠道没有」，会漏掉最危险的长款（用户付了钱、本地没单），
 * 所以必须双向：本地 → 渠道，渠道 → 本地，两边都要扫。</p>
 *
 * <p>产出不是一行日志，而是一张张带类型的工单：
 * 谁处理、处理到哪一步、什么时候关闭都是可查的，这才是「闭环」而不是「发现」。</p>
 */
@Slf4j
public class ReconcileServiceImpl implements ReconcileService {

    private final PaymentOrderRepository orderRepository;
    private final ChannelBillRepository channelBillRepository;
    private final ReconcileTaskRepository reconcileTaskRepository;

    public ReconcileServiceImpl(PaymentOrderRepository orderRepository,
                                ChannelBillRepository channelBillRepository,
                                ReconcileTaskRepository reconcileTaskRepository) {
        this.orderRepository = orderRepository;
        this.channelBillRepository = channelBillRepository;
        this.reconcileTaskRepository = reconcileTaskRepository;
    }

    @Override
    public ReconcileReport reconcile(String bizDate) {
        String traceId = TraceContext.currentOrCreate();
        long now = System.currentTimeMillis();

        List<PaymentOrder> localOrders = orderRepository.findByBizDate(bizDate);
        List<ChannelTradeBill> channelBills = channelBillRepository.findByBizDate(bizDate);
        Map<String, ChannelTradeBill> billByPaymentNo = channelBills.stream()
                .collect(Collectors.toMap(ChannelTradeBill::paymentNo, Function.identity(), (a, b) -> a));

        List<ReconcileTask> tasks = new ArrayList<>();
        Set<String> localPaymentNos = new HashSet<>();
        int matched = 0;

        for (PaymentOrder order : localOrders) {
            localPaymentNos.add(order.paymentNo());
            ChannelTradeBill bill = billByPaymentNo.get(order.paymentNo());
            ReconcileTask task = compare(order, bill, bizDate, now);
            if (task == null) {
                matched++;
            } else {
                tasks.add(task);
            }
        }

        // 反向扫描：渠道有单、本地没有，属于长款，必须补记
        for (ChannelTradeBill bill : channelBills) {
            if (bill.success() && !localPaymentNos.contains(bill.paymentNo())) {
                tasks.add(ReconcileTask.open(bizDate, bill.paymentNo(), null,
                        ReconcileTask.DiffType.CHANNEL_SUCCESS_LOCAL_MISSING,
                        "NOT_FOUND", bill.channelStatus(), null, bill.amount(),
                        "渠道成功但本地无对应支付单，需补记交易", now));
            }
        }

        tasks.forEach(reconcileTaskRepository::save);
        Map<String, Integer> diffByType = new HashMap<>();
        for (ReconcileTask task : tasks) {
            diffByType.merge(task.diffType().name(), 1, Integer::sum);
        }

        ReconcileReport report = new ReconcileReport(bizDate, localOrders.size(), channelBills.size(),
                matched, tasks.size(), diffByType, List.copyOf(tasks));
        if (report.balanced()) {
            log.info("[{}] 对账平账 bizDate={} 本地 {} 笔 / 渠道 {} 笔", traceId, bizDate,
                    localOrders.size(), channelBills.size());
        } else {
            log.error("[{}] 对账发现差异 bizDate={} 差异分布={}", traceId, bizDate, diffByType);
        }
        return report;
    }

    /**
     * 比对单笔，返回 null 表示一致
     */
    private ReconcileTask compare(PaymentOrder order, ChannelTradeBill bill, String bizDate, long now) {
        if (order.status() == PaymentStatus.SUCCESS) {
            if (bill == null) {
                return ReconcileTask.open(bizDate, order.paymentNo(), order.orderNo(),
                        ReconcileTask.DiffType.LOCAL_SUCCESS_CHANNEL_MISSING,
                        order.status().name(), "MISSING", order.amount(), null,
                        "本地成功但渠道无此单，可能是渠道掉单或对账单缺失", now);
            }
            if (!bill.success()) {
                return ReconcileTask.open(bizDate, order.paymentNo(), order.orderNo(),
                        ReconcileTask.DiffType.LOCAL_SUCCESS_CHANNEL_MISSING,
                        order.status().name(), bill.channelStatus(), order.amount(), bill.amount(),
                        "本地成功但渠道状态非成功", now);
            }
            if (bill.amount() != null && bill.amount().compareTo(order.amount()) != 0) {
                return ReconcileTask.open(bizDate, order.paymentNo(), order.orderNo(),
                        ReconcileTask.DiffType.AMOUNT_MISMATCH,
                        order.status().name(), bill.channelStatus(), order.amount(), bill.amount(),
                        "本地金额与渠道金额不一致", now);
            }
            return null;
        }

        if (order.status().isSettled() || order.status() == PaymentStatus.CLOSED) {
            if (bill != null && bill.success()) {
                // 本地判失败/关单，渠道却扣款成功：必须立即人工介入，钱已经收了
                return ReconcileTask.open(bizDate, order.paymentNo(), order.orderNo(),
                        ReconcileTask.DiffType.LOCAL_FAILED_CHANNEL_SUCCESS,
                        order.status().name(), bill.channelStatus(), order.amount(), bill.amount(),
                        "本地失败但渠道成功，需补记或退款", now);
            }
            return null;
        }

        // PAYING / UNKNOWN：补偿任务还没闭环，对账时必须暴露而不是掩盖
        return ReconcileTask.open(bizDate, order.paymentNo(), order.orderNo(),
                ReconcileTask.DiffType.UNSETTLED_OVERDUE,
                order.status().name(), bill == null ? "MISSING" : bill.channelStatus(),
                order.amount(), bill == null ? null : bill.amount(),
                "支付单长时间未决，补偿任务尚未闭环", now);
    }
}
