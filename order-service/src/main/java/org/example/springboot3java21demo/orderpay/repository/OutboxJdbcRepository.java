package org.example.springboot3java21demo.orderpay.repository;

import org.example.springboot3java21demo.orderpay.domain.OutboxRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class OutboxJdbcRepository {

    private static final RowMapper<OutboxRecord> ROW_MAPPER = (rs, rowNum) -> {
        Timestamp sentAt = rs.getTimestamp("sent_at");
        return new OutboxRecord(
                rs.getLong("id"),
                rs.getString("event_id"),
                rs.getString("aggregate_id"),
                rs.getString("event_type"),
                rs.getString("destination"),
                rs.getString("payload"),
                rs.getTimestamp("available_at").toInstant(),
                rs.getTimestamp("deliver_at").toInstant(),
                rs.getString("status"),
                rs.getInt("retry_count"),
                rs.getString("last_error"),
                rs.getTimestamp("created_at").toInstant(),
                sentAt == null ? null : sentAt.toInstant());
    };

    private final NamedParameterJdbcTemplate jdbc;

    public OutboxJdbcRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(
            String eventId,
            String aggregateId,
            String eventType,
            String destination,
            String payload,
            Instant availableAt,
            Instant deliverAt) {
        String sql = """
                INSERT INTO op_outbox_event
                    (event_id, aggregate_id, event_type, destination, payload,
                     available_at, deliver_at, status, retry_count, created_at)
                VALUES
                    (:eventId, :aggregateId, :eventType, :destination, :payload,
                     :availableAt, :deliverAt, 'PENDING', 0, :createdAt)
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("eventId", eventId)
                .addValue("aggregateId", aggregateId)
                .addValue("eventType", eventType)
                .addValue("destination", destination)
                .addValue("payload", payload)
                .addValue("availableAt", Timestamp.from(availableAt))
                .addValue("deliverAt", Timestamp.from(deliverAt))
                .addValue("createdAt", Timestamp.from(Instant.now())));
    }

    public List<OutboxRecord> findDue(int limit) {
        return jdbc.query("""
                SELECT id, event_id, aggregate_id, event_type, destination, payload,
                       available_at, deliver_at, status, retry_count,
                       last_error, created_at, sent_at
                  FROM op_outbox_event
                 WHERE status = 'PENDING'
                   AND available_at <= :now
                 ORDER BY id
                 LIMIT :limit
                """, new MapSqlParameterSource()
                .addValue("now", Timestamp.from(Instant.now()))
                .addValue("limit", limit), ROW_MAPPER);
    }

    public void markSent(long id) {
        jdbc.update("""
                UPDATE op_outbox_event
                   SET status = 'SENT', sent_at = :now, last_error = NULL
                 WHERE id = :id AND status = 'PENDING'
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("now", Timestamp.from(Instant.now())));
    }

    public void markRetry(long id, String error, long retryDelaySeconds) {
        String safeError = error == null ? "unknown" : error.substring(0, Math.min(error.length(), 1000));
        jdbc.update("""
                UPDATE op_outbox_event
                   SET retry_count = retry_count + 1,
                       last_error = :error,
                       available_at = :availableAt
                 WHERE id = :id AND status = 'PENDING'
                """, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("error", safeError)
                .addValue("availableAt", Timestamp.from(
                        Instant.now().plusSeconds(Math.max(1, retryDelaySeconds)))));
    }

    public long countPending() {
        Long count = jdbc.getJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM op_outbox_event WHERE status = 'PENDING'", Long.class);
        return count == null ? 0 : count;
    }
}
