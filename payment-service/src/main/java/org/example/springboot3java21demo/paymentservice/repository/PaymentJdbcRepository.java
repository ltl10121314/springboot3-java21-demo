package org.example.springboot3java21demo.paymentservice.repository;

import org.example.springboot3java21demo.paymentservice.domain.PaymentRecord;
import org.example.springboot3java21demo.paymentservice.domain.PaymentStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Repository
public class PaymentJdbcRepository {

    private static final String COLUMNS = """
            payment_no, request_id, order_no, amount, channel, status,
            channel_trade_no, fail_reason, version, created_at, updated_at
            """;

    private static final RowMapper<PaymentRecord> ROW_MAPPER = (rs, rowNum) -> new PaymentRecord(
            rs.getString("payment_no"),
            rs.getString("request_id"),
            rs.getString("order_no"),
            rs.getBigDecimal("amount"),
            rs.getString("channel"),
            PaymentStatus.valueOf(rs.getString("status")),
            rs.getString("channel_trade_no"),
            rs.getString("fail_reason"),
            rs.getLong("version"),
            rs.getTimestamp("created_at").toInstant(),
            rs.getTimestamp("updated_at").toInstant());

    private final NamedParameterJdbcTemplate jdbc;

    public PaymentJdbcRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(PaymentRecord payment) {
        String sql = """
                INSERT INTO op_payment
                    (payment_no, request_id, order_no, amount, channel, status,
                     channel_trade_no, fail_reason, version, created_at, updated_at)
                VALUES
                    (:paymentNo, :requestId, :orderNo, :amount, :channel, :status,
                     :channelTradeNo, :failReason, :version, :createdAt, :updatedAt)
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("paymentNo", payment.paymentNo())
                .addValue("requestId", payment.requestId())
                .addValue("orderNo", payment.orderNo())
                .addValue("amount", payment.amount())
                .addValue("channel", payment.channel())
                .addValue("status", payment.status().name())
                .addValue("channelTradeNo", payment.channelTradeNo())
                .addValue("failReason", payment.failReason())
                .addValue("version", payment.version())
                .addValue("createdAt", Timestamp.from(payment.createdAt()))
                .addValue("updatedAt", Timestamp.from(payment.updatedAt())));
    }

    public Optional<PaymentRecord> findByRequestId(String requestId) {
        return findOne("""
                SELECT %s FROM op_payment WHERE request_id = :value
                """.formatted(COLUMNS), "value", requestId);
    }

    public Optional<PaymentRecord> findByPaymentNo(String paymentNo) {
        return findOne("""
                SELECT %s FROM op_payment WHERE payment_no = :value
                """.formatted(COLUMNS), "value", paymentNo);
    }

    public Optional<PaymentRecord> findLatestByOrderNo(String orderNo) {
        return jdbc.query("""
                SELECT %s FROM op_payment
                 WHERE order_no = :orderNo
                 ORDER BY created_at DESC
                 LIMIT 1
                """.formatted(COLUMNS), new MapSqlParameterSource("orderNo", orderNo), ROW_MAPPER)
                .stream()
                .findFirst();
    }

    public boolean complete(
            String paymentNo,
            PaymentStatus target,
            String channelTradeNo,
            String failReason,
            Instant now) {
        String sql = """
                UPDATE op_payment
                   SET status = :target,
                       channel_trade_no = :channelTradeNo,
                       fail_reason = :failReason,
                       version = version + 1,
                       updated_at = :now
                 WHERE payment_no = :paymentNo
                   AND status IN ('PROCESSING', 'UNKNOWN')
                """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("paymentNo", paymentNo)
                .addValue("target", target.name())
                .addValue("channelTradeNo", channelTradeNo)
                .addValue("failReason", failReason)
                .addValue("now", Timestamp.from(now))) == 1;
    }

    private Optional<PaymentRecord> findOne(String sql, String name, Object value) {
        return jdbc.query(sql, new MapSqlParameterSource(name, value), ROW_MAPPER)
                .stream()
                .findFirst();
    }
}
