package org.example.practice.integration.rocketmq.common;

public final class RocketMqConstants {

    public static final String DEAD_LETTER_TOPIC_PREFIX = "%DLQ%";
    public static final String FAIL_CONSUMER_GROUP_PROPERTY = "practice.fail-consumer-group";
    public static final String FAIL_ALL_CONSUMER_GROUPS = "BOTH";
    public static final String REPLAYED_FROM_DLQ_PROPERTY = "practice.replayed-from-dlq";

    private RocketMqConstants() {
    }

    public static boolean shouldSimulateFailure(String failConsumerGroup, String currentConsumerGroup) {
        return failConsumerGroup != null
                && (FAIL_ALL_CONSUMER_GROUPS.equalsIgnoreCase(failConsumerGroup)
                || currentConsumerGroup.equals(failConsumerGroup));
    }
}
