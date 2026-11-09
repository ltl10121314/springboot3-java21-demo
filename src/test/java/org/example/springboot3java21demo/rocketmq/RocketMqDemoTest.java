package org.example.springboot3java21demo.rocketmq;

import org.example.springboot3java21demo.rocketmq.model.ConsumedMessage;
import org.example.springboot3java21demo.rocketmq.model.OrderEventType;
import org.example.springboot3java21demo.rocketmq.service.ConsumedMessageStore;
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

    private ConsumedMessage message(long sequence, String group, String tag) {
        return new ConsumedMessage(
                sequence, group, "msg-" + sequence, "practice-order-topic",
                tag, "order-" + sequence, 1, "{}", Instant.now());
    }
}
