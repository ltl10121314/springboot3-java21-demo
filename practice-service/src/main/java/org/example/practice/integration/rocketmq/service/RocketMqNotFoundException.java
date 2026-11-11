package org.example.practice.integration.rocketmq.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RocketMqNotFoundException extends RuntimeException {

    public RocketMqNotFoundException(String message) {
        super(message);
    }
}
