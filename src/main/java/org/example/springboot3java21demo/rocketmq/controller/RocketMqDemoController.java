package org.example.springboot3java21demo.rocketmq.controller;

import jakarta.validation.Valid;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.example.springboot3java21demo.rocketmq.config.RocketMqProperties;
import org.example.springboot3java21demo.rocketmq.model.AsyncSendResponse;
import org.example.springboot3java21demo.rocketmq.model.ConsumedMessage;
import org.example.springboot3java21demo.rocketmq.model.OrderEvent;
import org.example.springboot3java21demo.rocketmq.model.RocketMqInfoResponse;
import org.example.springboot3java21demo.rocketmq.model.SendOrderRequest;
import org.example.springboot3java21demo.rocketmq.model.SendOrderResponse;
import org.example.springboot3java21demo.rocketmq.service.ConsumedMessageStore;
import org.example.springboot3java21demo.rocketmq.service.OrderMessageProducer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rocketmq")
public class RocketMqDemoController {

    private final RocketMqProperties properties;
    private final OrderMessageProducer producer;
    private final ConsumedMessageStore consumedMessageStore;

    public RocketMqDemoController(
            RocketMqProperties properties,
            OrderMessageProducer producer,
            ConsumedMessageStore consumedMessageStore) {
        this.properties = properties;
        this.producer = producer;
        this.consumedMessageStore = consumedMessageStore;
    }

    @GetMapping("/info")
    public RocketMqInfoResponse info() {
        return RocketMqInfoResponse.from(properties);
    }

    @PostMapping("/orders/sync")
    public SendOrderResponse sendSync(@Valid @RequestBody SendOrderRequest request) {
        OrderEvent event = OrderEvent.from(request);
        SendReceipt receipt = producer.send(event);
        return SendOrderResponse.from(event, properties.getTopic(), receipt);
    }

    @PostMapping("/orders/async")
    public ResponseEntity<AsyncSendResponse> sendAsync(@Valid @RequestBody SendOrderRequest request) {
        OrderEvent event = OrderEvent.from(request);
        producer.sendAsync(event);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new AsyncSendResponse(
                event.eventId(),
                event.orderId(),
                "SENDING",
                properties.getTopic(),
                event.eventType().tag()));
    }

    /**
     * 不传 consumerGroup 时返回所有组的最近消息。
     */
    @GetMapping("/messages")
    public List<ConsumedMessage> recentMessages(
            @RequestParam(required = false) String consumerGroup,
            @RequestParam(defaultValue = "20") int limit) {
        return consumedMessageStore.findRecent(consumerGroup, limit);
    }

    @DeleteMapping("/messages")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearRecentMessages() {
        consumedMessageStore.clear();
    }
}
