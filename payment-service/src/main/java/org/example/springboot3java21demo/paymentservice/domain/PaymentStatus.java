package org.example.springboot3java21demo.paymentservice.domain;

public enum PaymentStatus {
    /** 支付流水已创建，正在调用支付渠道或等待渠道结果。 */
    PROCESSING,
    /** 支付渠道明确返回成功。 */
    SUCCESS,
    /** 支付渠道明确返回失败，允许业务侧按失败处理。 */
    FAILED,
    /** 网络异常、超时等原因导致结果未知，不能直接判定失败。 */
    UNKNOWN
}
