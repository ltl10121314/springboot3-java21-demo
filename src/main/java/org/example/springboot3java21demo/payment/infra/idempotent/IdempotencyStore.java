package org.example.springboot3java21demo.payment.infra.idempotent;

import org.example.springboot3java21demo.payment.model.PaymentResult;

import java.util.Optional;

/**
 * 幂等存储端口。
 *
 * <p>语义：{@code tryAcquire} 必须是原子的分布式抢占（Redis 用 {@code SET key value NX EX}），
 * 抢到的线程才是这笔 requestId 的唯一执行者，其余线程直接读结果。这就是「同一笔支付不重复扣款」的入口闸门。</p>
 */
public interface IdempotencyStore {

    /**
     * 抢占幂等键
     *
     * @return true 表示本线程获得执行权
     */
    boolean tryAcquire(String requestId);

    /**
     * 写入最终结果（成功/失败/关单都算最终结果，会被后续请求直接复用）
     */
    void saveResult(String requestId, PaymentResult result);

    /**
     * 读取已经落定的结果
     */
    Optional<PaymentResult> findResult(String requestId);

    /**
     * 是否为「处理中」占位
     */
    boolean isProcessing(String requestId);

    /**
     * 删除幂等键。
     *
     * <p>只在「确认没有产生任何业务副作用」时调用（例如订单尚未创建就抛异常）。
     * 一旦调用了渠道，即使拿到的结果未知，也绝不能删除，否则客户端重试会造成二次扣款。</p>
     */
    void release(String requestId);
}
