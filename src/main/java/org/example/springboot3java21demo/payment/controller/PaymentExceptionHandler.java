package org.example.springboot3java21demo.payment.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot3java21demo.payment.exception.PaymentException;
import org.example.springboot3java21demo.payment.model.ApiResponse;
import org.example.springboot3java21demo.payment.support.TraceContext;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 支付接口异常处理
 */
@Slf4j
@RestControllerAdvice(basePackages = "org.example.springboot3java21demo.payment.controller")
public class PaymentExceptionHandler {

    /**
     * 支付域异常：错误码直接透出，客户端可据此决定是否重试。
     * 响应体里带 traceId，便于把用户报障和后台日志对上。
     */
    @ExceptionHandler(PaymentException.class)
    public ApiResponse<Void> handlePaymentException(PaymentException ex) {
        log.warn("[{}] 支付业务异常 code={} message={}",
                TraceContext.current(), ex.getErrorCode().getCode(), ex.getMessage());
        return ApiResponse.fail(ex.getErrorCode().getCode(), ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex) {
        return ApiResponse.fail(400, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ApiResponse<Void> handleIllegalState(IllegalStateException ex) {
        return ApiResponse.fail(500, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception ex) {
        log.error("支付接口异常", ex);
        return ApiResponse.fail(500, "支付失败，请稍后重试");
    }
}
