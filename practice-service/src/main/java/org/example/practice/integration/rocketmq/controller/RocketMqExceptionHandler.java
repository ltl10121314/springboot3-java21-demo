package org.example.practice.integration.rocketmq.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.practice.integration.rocketmq.model.RocketMqErrorResponse;
import org.example.practice.integration.rocketmq.service.RocketMqBadRequestException;
import org.example.practice.integration.rocketmq.service.RocketMqConflictException;
import org.example.practice.integration.rocketmq.service.RocketMqNotFoundException;
import org.example.practice.integration.rocketmq.service.RocketMqSendException;
import org.example.practice.integration.rocketmq.service.RocketMqUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * RocketMQ 接口异常处理。
 * <p>
 * 领域异常上虽然标了 {@link org.springframework.web.bind.annotation.ResponseStatus}，
 * 但 Spring Boot 默认错误体不会输出异常信息，客户端只能看到 400/500 而看不到原因。
 * 这里统一把异常信息写进响应体，便于调用方定位问题。
 */
@Slf4j
@RestControllerAdvice(basePackages = "org.example.practice.integration.rocketmq.controller")
public class RocketMqExceptionHandler {

    @ExceptionHandler(RocketMqBadRequestException.class)
    public ResponseEntity<RocketMqErrorResponse> handleBadRequest(RocketMqBadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * 参数校验失败时把各字段的校验提示拼进响应，避免只返回一个空泛的 400。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RocketMqErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = "请求参数校验失败";
        }
        return build(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(RocketMqNotFoundException.class)
    public ResponseEntity<RocketMqErrorResponse> handleNotFound(RocketMqNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RocketMqConflictException.class)
    public ResponseEntity<RocketMqErrorResponse> handleConflict(RocketMqConflictException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(RocketMqUnavailableException.class)
    public ResponseEntity<RocketMqErrorResponse> handleUnavailable(RocketMqUnavailableException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(RocketMqSendException.class)
    public ResponseEntity<RocketMqErrorResponse> handleSendFailure(RocketMqSendException ex) {
        log.error("RocketMQ 发送异常 message={}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(errorBody(HttpStatus.BAD_GATEWAY, ex.getMessage()));
    }

    private ResponseEntity<RocketMqErrorResponse> build(HttpStatus status, String message) {
        log.warn("RocketMQ 接口异常 status={}, message={}", status.value(), message);
        return ResponseEntity.status(status).body(errorBody(status, message));
    }

    private RocketMqErrorResponse errorBody(HttpStatus status, String message) {
        return new RocketMqErrorResponse(
                Instant.now(), status.value(), status.getReasonPhrase(), message);
    }
}
