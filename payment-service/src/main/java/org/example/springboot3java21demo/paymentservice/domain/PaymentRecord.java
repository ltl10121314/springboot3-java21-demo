package org.example.springboot3java21demo.paymentservice.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 支付流水表 {@code op_payment}。一行代表一次支付尝试。
 *
 * @param paymentNo 平台支付单号，支付表主键
 * @param requestId 支付请求幂等键，同一请求只能产生一条支付流水
 * @param orderNo 关联的订单号
 * @param amount 本次支付的金额
 * @param channel 支付渠道，如 ALIPAY、WECHAT
 * @param status 支付处理状态
 * @param channelTradeNo 支付渠道返回的第三方交易号，渠道未返回时为空
 * @param failReason 支付明确失败时的原因，成功或未知时通常为空
 * @param version 乐观锁版本号，支付状态每次变更时递增
 * @param createdAt 支付流水创建时间
 * @param updatedAt 支付流水最后更新时间
 */
@TableName("op_payment")
public record PaymentRecord(
        @TableId(value = "payment_no", type = IdType.INPUT) String paymentNo,
        @TableField("request_id") String requestId,
        @TableField("order_no") String orderNo,
        @TableField("amount") BigDecimal amount,
        @TableField("channel") String channel,
        @TableField("status") PaymentStatus status,
        @TableField("channel_trade_no") String channelTradeNo,
        @TableField("fail_reason") String failReason,
        @TableField("version") long version,
        @TableField("created_at") Instant createdAt,
        @TableField("updated_at") Instant updatedAt) {
}
