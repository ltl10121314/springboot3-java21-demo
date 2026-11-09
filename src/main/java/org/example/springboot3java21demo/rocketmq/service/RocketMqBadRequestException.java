package org.example.springboot3java21demo.rocketmq.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RocketMqBadRequestException extends RuntimeException {

    public RocketMqBadRequestException(String message) {
        super(message);
    }
}
