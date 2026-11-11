package org.example.springboot3java21demo.orderpay.domain;

public enum OrderStatus {
    /** 订单已创建，库存已预占，等待支付。 */
    CREATED,
    /** 已锁定支付单号并调用支付服务，支付结果尚未最终确认。 */
    PAYING,
    /** 支付成功，预占库存已转为已售库存。 */
    PAID,
    /** 用户主动取消或创建阶段失败，预占库存已释放。 */
    CANCELLED,
    /** 超时未支付，由延迟关单任务关闭并释放预占库存。 */
    CLOSED;

    public boolean isTerminal() {
        return this == PAID || this == CANCELLED || this == CLOSED;
    }
}
