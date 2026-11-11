package org.example.practice.integration.rocketmq;

import org.example.practice.integration.rocketmq.common.RocketMqConstants;
import org.example.practice.integration.rocketmq.config.RocketMqProperties;
import org.example.practice.integration.rocketmq.model.ConsumeStatus;
import org.example.practice.integration.rocketmq.model.ConsumedMessage;
import org.example.practice.integration.rocketmq.model.OrderEventType;
import org.example.practice.integration.rocketmq.service.ConsumedMessageStore;
import org.example.practice.integration.rocketmq.service.DeadLetterStore;
import org.example.practice.integration.rocketmq.service.RocketMqIdempotencyStore;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RocketMqDemoTest {

    @Test
    void eventTypeIsUsedAsRocketMqTag() {
        assertThat(OrderEventType.ORDER_CREATED.tag()).isEqualTo("ORDER_CREATED");
        assertThat(OrderEventType.ORDER_PAID.tag()).isEqualTo("ORDER_PAID");
    }

    @Test
    void consumedMessagesAreIsolatedByConsumerGroup() {
        ConsumedMessageStore store = new ConsumedMessageStore();
        store.add(message(1, "business-group", "ORDER_CREATED"), 10);
        store.add(message(2, "audit-group", "ORDER_CREATED"), 10);
        store.add(message(3, "business-group", "ORDER_PAID"), 10);

        List<ConsumedMessage> businessMessages = store.findRecent("business-group", 10);
        List<ConsumedMessage> auditMessages = store.findRecent("audit-group", 10);

        assertThat(businessMessages)
                .extracting(ConsumedMessage::tag)
                .containsExactly("ORDER_PAID", "ORDER_CREATED");
        assertThat(auditMessages)
                .extracting(ConsumedMessage::tag)
                .containsExactly("ORDER_CREATED");
    }

    @Test
    void recentMessageStoreKeepsConfiguredLimit() {
        ConsumedMessageStore store = new ConsumedMessageStore();
        store.add(message(1, "group", "ORDER_CREATED"), 2);
        store.add(message(2, "group", "ORDER_CREATED"), 2);
        store.add(message(3, "group", "ORDER_CREATED"), 2);

        assertThat(store.findRecent("group", 10))
                .extracting(ConsumedMessage::sequence)
                .containsExactly(3L, 2L);
    }

    @Test
    void idempotencyStoreAcceptsOnlyFirstEventForEachConsumerGroup() {
        RocketMqProperties properties = new RocketMqProperties();
        RocketMqIdempotencyStore store = new RocketMqIdempotencyStore(properties);

        assertThat(store.tryMarkProcessed("business-group", "event-1")).isTrue();
        assertThat(store.tryMarkProcessed("business-group", "event-1")).isFalse();
        assertThat(store.tryMarkProcessed("audit-group", "event-1")).isTrue();
    }

    @Test
    void deadLetterStoreSupportsQueryAndOneTimeReplayMarker() {
        RocketMqProperties properties = new RocketMqProperties();
        DeadLetterStore store = new DeadLetterStore(properties);
        var saved = store.save(
                "business-group",
                "%DLQ%business-group",
                "dlq-1",
                "ORDER_CREATED",
                "order-1",
                3,
                "{}");

        assertThat(store.find(saved.recordId())).isPresent();
        assertThat(store.markReplayed(saved.recordId())).isTrue();
        assertThat(store.markReplayed(saved.recordId())).isFalse();
        assertThat(store.find(saved.recordId()).orElseThrow().replayed()).isTrue();
    }

    @Test
    void sameRocketMqMessageIdInDifferentDlqTopicsCreatesIndependentRecords() {
        RocketMqProperties properties = new RocketMqProperties();
        DeadLetterStore store = new DeadLetterStore(properties);

        var business = store.save(
                "business-group", "%DLQ%business-group", "same-message-id",
                "ORDER_CREATED", "order-1", 5, "{}");
        var audit = store.save(
                "audit-group", "%DLQ%audit-group", "same-message-id",
                "ORDER_CREATED", "order-1", 5, "{}");

        assertThat(business.recordId()).isNotEqualTo(audit.recordId());
        assertThat(store.findRecent(10))
                .extracting(message -> message.consumerGroup())
                .containsExactly("audit-group", "business-group");
    }

    @Test
    void failureInjectionMatchesConfiguredConsumerGroupOrBoth() {
        assertThat(RocketMqConstants.shouldSimulateFailure("business-group", "business-group")).isTrue();
        assertThat(RocketMqConstants.shouldSimulateFailure("business-group", "audit-group")).isFalse();
        assertThat(RocketMqConstants.shouldSimulateFailure("BOTH", "audit-group")).isTrue();
    }

    private ConsumedMessage message(long sequence, String group, String tag) {
        return new ConsumedMessage(
                sequence, group, "msg-" + sequence, "practice-order-topic",
                tag, "order-" + sequence, 1, ConsumeStatus.CONSUMED, "{}", Instant.now());
    }
}
