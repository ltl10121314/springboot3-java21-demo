package org.example.springboot3java21demo.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 支付结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResult {

    /**
     * 幂等键
     */
    private String requestId;

    /**
     * 业务订单号
     */
    private String orderNo;

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 状态：INIT、PAYING、SUCCESS、FAILED、UNKNOWN、CLOSED、NOT_FOUND
     */
    private String status;

    /**
     * 是否为重复请求
     */
    private boolean duplicate;

    /**
     * 结果说明
     */
    private String message;

    /**
     * 支付成功时间，毫秒时间戳
     */
    private Long paidAt;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付渠道
     */
    private String channel;

    /**
     * 全链路追踪 ID，排查问题时凭此串起所有日志
     */
    private String traceId;
}
