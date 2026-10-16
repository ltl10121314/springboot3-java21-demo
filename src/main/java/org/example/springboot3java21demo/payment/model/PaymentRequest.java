package org.example.springboot3java21demo.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 支付请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    /**
     * 客户端生成的幂等键，同一笔业务重试时保持不变
     */
    private String requestId;

    /**
     * 业务订单号
     */
    private String orderNo;

    /**
     * 付款用户
     */
    private String userId;

    /**
     * 支付金额，单位：元
     */
    private BigDecimal amount;

    /**
     * 支付渠道，例如 ALIPAY、WECHAT
     */
    private String channel;

    /**
     * 商品 SKU，与 quantity 一起构成库存扣减维度。为空表示该笔支付不涉及库存。
     */
    private String skuId;

    /**
     * 购买数量，为空时按 1 处理
     */
    private Integer quantity;

    /**
     * 备注
     */
    private String remark;
}
