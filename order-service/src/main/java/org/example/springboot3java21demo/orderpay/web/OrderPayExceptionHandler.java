package org.example.springboot3java21demo.orderpay.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.RpcException;
import org.example.springboot3java21demo.common.web.ApiErrorLoggingFilter;
import org.example.springboot3java21demo.orderpay.exception.OrderPayException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice(basePackages = "org.example.springboot3java21demo.orderpay.web")
public class OrderPayExceptionHandler {

    @ExceptionHandler(OrderPayException.class)
    public ResponseEntity<ApiResult<Void>> handleBusiness(
            OrderPayException ex, HttpServletRequest request) {
        log.warn("订单支付业务异常 method={}, uri={}, code={}, status={}, message={}",
                request.getMethod(), request.getRequestURI(), ex.getCode(),
                ex.getHttpStatus(), ex.getMessage());
        markErrorLogged(request);
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiResult.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("订单支付参数校验失败 method={}, uri={}, errors={}",
                request.getMethod(), request.getRequestURI(), message);
        markErrorLogged(request);
        return ResponseEntity.badRequest().body(ApiResult.fail(400, message));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResult<Void>> handleMethodValidation(
            HandlerMethodValidationException ex, HttpServletRequest request) {
        String message = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(MessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("订单支付参数校验失败 method={}, uri={}, errors={}",
                request.getMethod(), request.getRequestURI(), message);
        markErrorLogged(request);
        return ResponseEntity.badRequest().body(ApiResult.fail(400, message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        log.warn("订单支付约束校验失败 method={}, uri={}, errors={}",
                request.getMethod(), request.getRequestURI(), message);
        markErrorLogged(request);
        return ResponseEntity.badRequest().body(ApiResult.fail(400, message));
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            ServletRequestBindingException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResult<Void>> handleBadRequest(
            Exception ex, HttpServletRequest request) {
        log.warn("订单支付请求参数无法解析 method={}, uri={}, exception={}, message={}",
                request.getMethod(), request.getRequestURI(),
                ex.getClass().getSimpleName(), ex.getMessage());
        markErrorLogged(request);
        return ResponseEntity.badRequest().body(ApiResult.fail(400, "请求参数格式错误"));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResult<Void>> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        log.warn("订单支付请求 Content-Type 不支持 method={}, uri={}, contentType={}",
                request.getMethod(), request.getRequestURI(), ex.getContentType());
        markErrorLogged(request);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResult.fail(415, "Content-Type 不支持"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResult<Void>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("订单支付请求业务参数错误 method={}, uri={}, message={}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        markErrorLogged(request);
        return ResponseEntity.badRequest().body(ApiResult.fail(400, ex.getMessage()));
    }

    @ExceptionHandler(RpcException.class)
    public ResponseEntity<ApiResult<Void>> handleRpcException(
            RpcException ex, HttpServletRequest request) {
        log.error("支付服务 Dubbo 调用失败 method={}, uri={}, code={}, message={}",
                request.getMethod(), request.getRequestURI(), ex.getCode(), ex.getMessage(), ex);
        markErrorLogged(request);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResult.fail(503, "支付服务暂不可用，请稍后重试"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleUnexpected(
            Exception ex, HttpServletRequest request) {
        log.error("订单支付接口异常 method={}, uri={}, exception={}, message={}",
                request.getMethod(), request.getRequestURI(),
                ex.getClass().getSimpleName(), ex.getMessage(), ex);
        markErrorLogged(request);
        return ResponseEntity.internalServerError()
                .body(ApiResult.fail(500, "系统处理失败，请稍后重试"));
    }

    private void markErrorLogged(HttpServletRequest request) {
        request.setAttribute(ApiErrorLoggingFilter.ERROR_LOGGED_ATTRIBUTE, Boolean.TRUE);
    }
}
