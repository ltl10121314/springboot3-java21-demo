package org.example.springboot3java21demo.payment.constant;

/**
 * 支付域错误码。
 *
 * <p>约定：4xxx 为客户端可纠正的错误，5xxx 为需要服务端补偿或人工介入的错误。
 * 其中 5001（结果未知）是资金安全的关键分支，必须走「查询/补偿」而不是「重试或置失败」。</p>
 */
public enum PaymentErrorCode {

    PARAM_INVALID(400, "参数校验失败"),
    RATE_LIMITED(429, "请求过于频繁，请稍后重试"),
    DUPLICATE_REQUEST(409, "重复请求，请使用原 requestId 查询结果"),

    STOCK_NOT_ENOUGH(4001, "库存不足"),
    ORDER_STATE_ILLEGAL(4002, "订单状态不允许该操作"),

    CHANNEL_RESULT_UNKNOWN(5001, "支付结果未知，请稍后查询"),
    CHANNEL_REJECTED(5002, "支付渠道拒绝该笔交易"),
    CHANNEL_UNAVAILABLE(5003, "支付渠道暂不可用"),
    SYSTEM_ERROR(5000, "系统异常，请稍后重试");

    private final int code;
    private final String message;

    PaymentErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
