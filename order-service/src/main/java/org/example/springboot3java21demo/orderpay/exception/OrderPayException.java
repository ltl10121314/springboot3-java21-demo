package org.example.springboot3java21demo.orderpay.exception;

public class OrderPayException extends RuntimeException {

    private final int code;
    private final int httpStatus;

    public OrderPayException(int code, int httpStatus, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public static OrderPayException badRequest(String message) {
        return new OrderPayException(400, 400, message);
    }

    public static OrderPayException notFound(String message) {
        return new OrderPayException(404, 404, message);
    }

    public static OrderPayException conflict(String message) {
        return new OrderPayException(409, 409, message);
    }

    public int getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
