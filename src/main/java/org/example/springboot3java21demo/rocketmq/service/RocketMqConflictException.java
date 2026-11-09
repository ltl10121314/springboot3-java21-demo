package org.example.springboot3java21demo.rocketmq.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RocketMqConflictException extends RuntimeException {

    public RocketMqConflictException(String message) {
        super(message);
    }
}
