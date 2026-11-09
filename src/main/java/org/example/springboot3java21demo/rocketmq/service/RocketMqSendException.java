package org.example.springboot3java21demo.rocketmq.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class RocketMqSendException extends RuntimeException {

    public RocketMqSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
