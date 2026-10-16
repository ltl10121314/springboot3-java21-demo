package org.example.springboot3java21demo.payment.domain;

import java.math.BigDecimal;

/**
 * 渠道对账单明细（渠道侧的事实）。
 *
 * @param paymentNo      平台支付流水号（对账主键，下单时就传给渠道）
 * @param channelTradeNo 渠道交易号
 * @param bizDate        账期 yyyy-MM-dd
 * @param amount         渠道侧金额
 * @param success        渠道侧是否成功
 * @param channelStatus  渠道原始状态
 */
public record ChannelTradeBill(
        String paymentNo,
        String channelTradeNo,
        String bizDate,
        BigDecimal amount,
        boolean success,
        String channelStatus) {
}
