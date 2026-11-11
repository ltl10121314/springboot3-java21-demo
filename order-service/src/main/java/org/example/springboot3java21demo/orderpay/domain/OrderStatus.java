package org.example.springboot3java21demo.orderpay.domain;

public enum OrderStatus {
    CREATED,
    PAYING,
    PAID,
    CANCELLED,
    CLOSED;

    public boolean isTerminal() {
        return this == PAID || this == CANCELLED || this == CLOSED;
    }
}
