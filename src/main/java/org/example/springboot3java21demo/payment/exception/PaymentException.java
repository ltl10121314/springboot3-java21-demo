package org.example.springboot3java21demo.payment.exception;

import org.example.springboot3java21demo.payment.constant.PaymentErrorCode;

/**
 * 支付域业务异常，携带可对外暴露的错误码。
 */
public class PaymentException extends RuntimeException {

    private final PaymentErrorCode errorCode;

    public PaymentException(PaymentErrorCode errorCode) {
        this(errorCode, errorCode.getMessage());
    }

    public PaymentException(PaymentErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PaymentException(PaymentErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public PaymentErrorCode getErrorCode() {
        return errorCode;
    }
}
