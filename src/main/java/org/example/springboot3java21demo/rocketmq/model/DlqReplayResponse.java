package org.example.springboot3java21demo.rocketmq.model;

import java.time.Instant;

public record DlqReplayResponse(
        String deadLetterRecordId,
        String deadLetterMessageId,
        String newMessageId,
        String topic,
        String tag,
        String status,
        Instant replayedAt) {
}
