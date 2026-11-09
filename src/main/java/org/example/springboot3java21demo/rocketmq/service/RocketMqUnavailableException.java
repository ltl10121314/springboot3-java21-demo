package org.example.springboot3java21demo.rocketmq.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class RocketMqUnavailableException extends RuntimeException {

    public RocketMqUnavailableException(String message) {
        super(message);
    }
}
