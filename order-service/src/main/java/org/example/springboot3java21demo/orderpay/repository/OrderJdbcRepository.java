package org.example.springboot3java21demo.orderpay.repository;

import org.example.springboot3java21demo.orderpay.domain.OrderRecord;
import org.example.springboot3java21demo.orderpay.domain.OrderStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Repository
public class OrderJdbcRepository {

    private static final String COLUMNS = """
            order_no, request_id, user_id, sku_id, sku_name, unit_price, quantity,
            total_amount, status, payment_no, version, created_at, updated_at, paid_at
            """;

    private static final RowMapper<OrderRecord> ROW_MAPPER = OrderJdbcRepository::mapRow;

    private final NamedParameterJdbcTemplate jdbc;

    public OrderJdbcRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(OrderRecord order) {
        String sql = """
                INSERT INTO op_order
                    (order_no, request_id, user_id, sku_id, sku_name, unit_price, quantity,
                     total_amount, status, payment_no, version, created_at, updated_at, paid_at)
                VALUES
                    (:orderNo, :requestId, :userId, :skuId, :skuName, :unitPrice, :quantity,
                     :totalAmount, :status, :paymentNo, :version, :createdAt, :updatedAt, :paidAt)
                """;
        jdbc.update(sql, params(order));
    }

    public Optional<OrderRecord> findByOrderNo(String orderNo) {
        return findOne("""
                SELECT %s FROM op_order WHERE order_no = :value
                """.formatted(COLUMNS), "value", orderNo);
    }

    public Optional<OrderRecord> findByRequestId(String requestId) {
        return findOne("""
                SELECT %s FROM op_order WHERE request_id = :value
                """.formatted(COLUMNS), "value", requestId);
    }

    public boolean markPaying(String orderNo, long expectedVersion, String paymentNo, Instant now) {
        String sql = """
                UPDATE op_order
                   SET status = 'PAYING', payment_no = :paymentNo, version = version + 1,
                       updated_at = :now
                 WHERE order_no = :orderNo
                   AND status = 'CREATED'
                   AND version = :expectedVersion
                """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("orderNo", orderNo)
                .addValue("expectedVersion", expectedVersion)
                .addValue("paymentNo", paymentNo)
                .addValue("now", Timestamp.from(now))) == 1;
    }

    public boolean markPaid(String orderNo, long expectedVersion, Instant now) {
        String sql = """
                UPDATE op_order
                   SET status = 'PAID', version = version + 1,
                       updated_at = :now, paid_at = :now
                 WHERE order_no = :orderNo
                   AND status = 'PAYING'
                   AND version = :expectedVersion
                """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("orderNo", orderNo)
                .addValue("expectedVersion", expectedVersion)
                .addValue("now", Timestamp.from(now))) == 1;
    }

    public boolean revertToCreated(String orderNo, long expectedVersion, Instant now) {
        String sql = """
                UPDATE op_order
                   SET status = 'CREATED', payment_no = NULL, version = version + 1,
                       updated_at = :now
                 WHERE order_no = :orderNo
                   AND status = 'PAYING'
                   AND version = :expectedVersion
                """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("orderNo", orderNo)
                .addValue("expectedVersion", expectedVersion)
                .addValue("now", Timestamp.from(now))) == 1;
    }

    public boolean cancelCreated(String orderNo, Instant now) {
        return finishCreated(orderNo, "CANCELLED", now);
    }

    public boolean closeCreated(String orderNo, Instant now) {
        return finishCreated(orderNo, "CLOSED", now);
    }

    private boolean finishCreated(String orderNo, String targetStatus, Instant now) {
        String sql = """
                UPDATE op_order
                   SET status = :targetStatus, version = version + 1, updated_at = :now
                 WHERE order_no = :orderNo
                   AND status = 'CREATED'
                """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("orderNo", orderNo)
                .addValue("targetStatus", targetStatus)
                .addValue("now", Timestamp.from(now))) == 1;
    }

    private Optional<OrderRecord> findOne(String sql, String name, Object value) {
        return jdbc.query(sql, new MapSqlParameterSource(name, value), ROW_MAPPER)
                .stream()
                .findFirst();
    }

    private MapSqlParameterSource params(OrderRecord order) {
        return new MapSqlParameterSource()
                .addValue("orderNo", order.orderNo())
                .addValue("requestId", order.requestId())
                .addValue("userId", order.userId())
                .addValue("skuId", order.skuId())
                .addValue("skuName", order.skuName())
                .addValue("unitPrice", order.unitPrice())
                .addValue("quantity", order.quantity())
                .addValue("totalAmount", order.totalAmount())
                .addValue("status", order.status().name())
                .addValue("paymentNo", order.paymentNo())
                .addValue("version", order.version())
                .addValue("createdAt", Timestamp.from(order.createdAt()))
                .addValue("updatedAt", Timestamp.from(order.updatedAt()))
                .addValue("paidAt", order.paidAt() == null ? null : Timestamp.from(order.paidAt()));
    }

    private static OrderRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        Timestamp paidAt = rs.getTimestamp("paid_at");
        return new OrderRecord(
                rs.getString("order_no"),
                rs.getString("request_id"),
                rs.getString("user_id"),
                rs.getString("sku_id"),
                rs.getString("sku_name"),
                rs.getBigDecimal("unit_price"),
                rs.getInt("quantity"),
                rs.getBigDecimal("total_amount"),
                OrderStatus.valueOf(rs.getString("status")),
                rs.getString("payment_no"),
                rs.getLong("version"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                paidAt == null ? null : paidAt.toInstant());
    }
}
