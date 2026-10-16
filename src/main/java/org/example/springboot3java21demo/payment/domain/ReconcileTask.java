package org.example.springboot3java21demo.payment.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 对账差异工单。
 *
 * <p>「可复盘」的落点：任何一条本地与渠道不一致的流水，都必须落成一张可追踪、可认领、可关闭的工单，
 * 而不是打一行日志了事。工单有类型、有证据（双方金额/状态）、有处理状态。</p>
 */
public record ReconcileTask(
        String taskId,
        String bizDate,
        String paymentNo,
        String orderNo,
        DiffType diffType,
        String localStatus,
        String channelStatus,
        BigDecimal localAmount,
        BigDecimal channelAmount,
        Status status,
        String detail,
        long createdAt,
        long updatedAt) {

    /**
     * 差异类型
     */
    public enum DiffType {
        /** 本地成功、渠道无此单：可能是渠道掉单，需要补推或退款 */
        LOCAL_SUCCESS_CHANNEL_MISSING,
        /** 渠道成功、本地无此单：长款，必须补记，否则用户付了钱没服务 */
        CHANNEL_SUCCESS_LOCAL_MISSING,
        /** 金额不一致：高危，通常是篡改或重复支付 */
        AMOUNT_MISMATCH,
        /** 本地失败、渠道成功：最危险的一类，库存和服务都已被错误释放 */
        LOCAL_FAILED_CHANNEL_SUCCESS,
        /** 本地仍处于未决状态（PAYING/UNKNOWN），补偿任务尚未闭环 */
        UNSETTLED_OVERDUE
    }

    /**
     * 工单状态
     */
    public enum Status {
        OPEN,
        CLOSED
    }

    public static ReconcileTask open(String bizDate,
                                     String paymentNo,
                                     String orderNo,
                                     DiffType diffType,
                                     String localStatus,
                                     String channelStatus,
                                     BigDecimal localAmount,
                                     BigDecimal channelAmount,
                                     String detail,
                                     long now) {
        return new ReconcileTask(UUID.randomUUID().toString(), bizDate, paymentNo, orderNo, diffType,
                localStatus, channelStatus, localAmount, channelAmount, Status.OPEN, detail, now, now);
    }
}
