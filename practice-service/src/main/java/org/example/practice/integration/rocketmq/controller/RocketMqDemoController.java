package org.example.practice.integration.rocketmq.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.example.practice.integration.rocketmq.config.RocketMqProperties;
import org.example.practice.integration.rocketmq.model.AsyncSendResponse;
import org.example.practice.integration.rocketmq.model.ConsumedMessage;
import org.example.practice.integration.rocketmq.model.DeadLetterMessage;
import org.example.practice.integration.rocketmq.model.DlqReplayResponse;
import org.example.practice.integration.rocketmq.model.OrderEvent;
import org.example.practice.integration.rocketmq.model.RocketMqInfoResponse;
import org.example.practice.integration.rocketmq.model.SendOrderRequest;
import org.example.practice.integration.rocketmq.model.SendOrderResponse;
import org.example.practice.integration.rocketmq.service.ConsumedMessageStore;
import org.example.practice.integration.rocketmq.service.DeadLetterReplayService;
import org.example.practice.integration.rocketmq.service.DeadLetterStore;
import org.example.practice.integration.rocketmq.service.OrderMessageProducer;
import org.example.practice.integration.rocketmq.service.RocketMqMetrics;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rocketmq")
@Slf4j
public class RocketMqDemoController {

    private final RocketMqProperties properties;
    private final OrderMessageProducer producer;
    private final ConsumedMessageStore consumedMessageStore;
    private final DeadLetterStore deadLetterStore;
    private final DeadLetterReplayService deadLetterReplayService;
    private final RocketMqMetrics metrics;

    public RocketMqDemoController(
            RocketMqProperties properties,
            OrderMessageProducer producer,
            ConsumedMessageStore consumedMessageStore,
            DeadLetterStore deadLetterStore,
            DeadLetterReplayService deadLetterReplayService,
            RocketMqMetrics metrics) {
        this.properties = properties;
        this.producer = producer;
        this.consumedMessageStore = consumedMessageStore;
        this.deadLetterStore = deadLetterStore;
        this.deadLetterReplayService = deadLetterReplayService;
        this.metrics = metrics;
    }

    @GetMapping("/info")
    public RocketMqInfoResponse info() {
        RocketMqInfoResponse response = RocketMqInfoResponse.from(properties);
        log.debug("查询 RocketMQ 配置 info={}", response);
        return response;
    }

    @PostMapping("/orders/sync")
    public SendOrderResponse sendSync(@Valid @RequestBody SendOrderRequest request) {
        OrderEvent event = OrderEvent.from(request);
        log.info("收到同步发送请求 orderId={}, type={}, delaySeconds={}, failConsumerGroup={}",
                request.orderId(), request.eventType(), request.delaySeconds(), request.failConsumerGroup());
        SendReceipt receipt = producer.send(event, request.failConsumerGroup(), request.delaySeconds());
        return SendOrderResponse.from(
                event,
                producer.resolveTopic(request.delaySeconds()),
                receipt,
                request.delaySeconds(),
                request.failConsumerGroup());
    }

    @PostMapping("/orders/async")
    public ResponseEntity<AsyncSendResponse> sendAsync(@Valid @RequestBody SendOrderRequest request) {
        OrderEvent event = OrderEvent.from(request);
        log.info("收到异步发送请求 orderId={}, type={}, eventId={}, delaySeconds={}, failConsumerGroup={}",
                request.orderId(), request.eventType(), event.eventId(),
                request.delaySeconds(), request.failConsumerGroup());
        producer.sendAsync(event, request.failConsumerGroup(), request.delaySeconds());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new AsyncSendResponse(
                event.eventId(),
                event.orderId(),
                "SENDING",
                producer.resolveTopic(request.delaySeconds()),
                event.eventType().tag(),
                request.delaySeconds(),
                request.failConsumerGroup()));
    }

    /**
     * 不传 consumerGroup 时返回所有组的最近消息。
     */
    @GetMapping("/messages")
    public List<ConsumedMessage> recentMessages(
            @RequestParam(required = false) String consumerGroup,
            @RequestParam(defaultValue = "20") int limit) {
        List<ConsumedMessage> messages = consumedMessageStore.findRecent(consumerGroup, limit);
        log.debug("查询消费记录 consumerGroup={}, limit={}, resultSize={}",
                consumerGroup, limit, messages.size());
        return messages;
    }

    @GetMapping("/dead-letters")
    public List<DeadLetterMessage> recentDeadLetters(
            @RequestParam(defaultValue = "20") int limit) {
        List<DeadLetterMessage> messages = deadLetterStore.findRecent(limit);
        log.debug("查询死信记录 limit={}, resultSize={}", limit, messages.size());
        return messages;
    }

    @PostMapping("/dead-letters/{recordId}/replay")
    public DlqReplayResponse replayDeadLetter(@PathVariable String recordId) {
        log.info("收到死信重放请求 recordId={}", recordId);
        return deadLetterReplayService.replay(recordId);
    }

    @GetMapping("/metrics")
    public Map<String, Long> metrics() {
        Map<String, Long> snapshot = metrics.snapshot();
        log.debug("查询 RocketMQ 指标 metrics={}", snapshot);
        return snapshot;
    }

    @DeleteMapping("/messages")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearRecentMessages() {
        int previousSize = consumedMessageStore.size();
        consumedMessageStore.clear();
        log.info("清空消费记录 previousSize={}", previousSize);
    }
}
