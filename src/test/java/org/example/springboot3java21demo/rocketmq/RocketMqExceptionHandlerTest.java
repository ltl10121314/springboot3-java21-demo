package org.example.springboot3java21demo.rocketmq;

import org.example.springboot3java21demo.rocketmq.controller.RocketMqExceptionHandler;
import org.example.springboot3java21demo.rocketmq.model.RocketMqErrorResponse;
import org.example.springboot3java21demo.rocketmq.service.RocketMqBadRequestException;
import org.example.springboot3java21demo.rocketmq.service.RocketMqNotFoundException;
import org.example.springboot3java21demo.rocketmq.service.RocketMqSendException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class RocketMqExceptionHandlerTest {

    private final RocketMqExceptionHandler handler = new RocketMqExceptionHandler();

    @Test
    void badRequestExceptionMessageIsExposedInResponseBody() {
        ResponseEntity<RocketMqErrorResponse> response = handler.handleBadRequest(
                new RocketMqBadRequestException("failConsumerGroup 仅支持业务组、审计组或 BOTH"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message())
                .isEqualTo("failConsumerGroup 仅支持业务组、审计组或 BOTH");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void domainExceptionsAreMappedToTheirHttpStatus() {
        assertThat(handler.handleNotFound(new RocketMqNotFoundException("死信不存在: 1"))
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(handler.handleSendFailure(new RocketMqSendException("发送失败", new RuntimeException()))
                .getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    }
}
