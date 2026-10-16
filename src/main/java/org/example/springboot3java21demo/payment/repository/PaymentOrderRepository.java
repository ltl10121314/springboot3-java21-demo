package org.example.springboot3java21demo.payment.repository;

import org.example.springboot3java21demo.payment.domain.PaymentOrder;
import org.example.springboot3java21demo.payment.domain.PaymentStatus;

import java.util.List;
import java.util.Optional;

/**
 * 支付单仓储端口。
 *
 * <p>生产实现落 MySQL，关键约束：</p>
 * <pre>
 *   UNIQUE KEY uk_request_id (request_id)   -- 幂等的最后一道防线
 *   UNIQUE KEY uk_payment_no (payment_no)   -- 渠道对账主键
 *   KEY idx_status_updated (status, updated_at) -- 补偿任务扫描
 * </pre>
 */
public interface PaymentOrderRepository {

    /**
     * 插入新支付单；若 requestId 已存在则返回已存在的那条（唯一索引冲突转为查询）。
     *
     * @return 真正落库的支付单（可能是别人先插入的）
     */
    PaymentOrder insertIfAbsent(PaymentOrder order);

    Optional<PaymentOrder> findByRequestId(String requestId);

    Optional<PaymentOrder> findByPaymentNo(String paymentNo);

    /**
     * 乐观锁更新：仅当库中 version 与 expect.version() 一致时才写入 next。
     * 实现对应 {@code UPDATE ... WHERE payment_no = ? AND version = ?}。
     *
     * @return true 表示抢到了这次状态推进
     */
    boolean compareAndSet(PaymentOrder expect, PaymentOrder next);

    /**
     * 扫描超时未决的单（PAYING / UNKNOWN），供补偿任务闭环
     */
    List<PaymentOrder> findUnsettledBefore(long deadline, int limit);

    /**
     * 对账用：按账期 + 状态拉取流水
     */
    List<PaymentOrder> findByBizDate(String bizDate);

    List<PaymentOrder> findByBizDateAndStatus(String bizDate, PaymentStatus status);
}
