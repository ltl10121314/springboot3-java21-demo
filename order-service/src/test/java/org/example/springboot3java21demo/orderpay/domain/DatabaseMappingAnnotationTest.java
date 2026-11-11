package org.example.springboot3java21demo.orderpay.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseMappingAnnotationTest {

    @Test
    @DisplayName("订单记录字段映射到 op_order")
    void mapsOrderRecordColumns() throws NoSuchFieldException {
        assertThat(OrderRecord.class.getAnnotation(TableName.class).value())
                .isEqualTo("op_order");
        assertThat(fieldAnnotation(OrderRecord.class, "orderNo", TableId.class).value())
                .isEqualTo("order_no");
        assertThat(fieldAnnotation(OrderRecord.class, "totalAmount", TableField.class).value())
                .isEqualTo("total_amount");
    }

    @Test
    @DisplayName("库存记录字段映射到 op_inventory")
    void mapsInventoryRecordColumns() throws NoSuchFieldException {
        assertThat(InventoryRecord.class.getAnnotation(TableName.class).value())
                .isEqualTo("op_inventory");
        assertThat(fieldAnnotation(InventoryRecord.class, "skuId", TableId.class).value())
                .isEqualTo("sku_id");
        assertThat(fieldAnnotation(InventoryRecord.class, "reservedStock", TableField.class).value())
                .isEqualTo("reserved_stock");
        assertThat(fieldAnnotation(InventoryRecord.class, "updatedAt", TableField.class).value())
                .isEqualTo("updated_at");
    }

    @Test
    @DisplayName("Outbox 记录字段映射到 op_outbox_event")
    void mapsOutboxRecordColumns() throws NoSuchFieldException {
        assertThat(OutboxRecord.class.getAnnotation(TableName.class).value())
                .isEqualTo("op_outbox_event");
        assertThat(fieldAnnotation(OutboxRecord.class, "id", TableId.class).value())
                .isEqualTo("id");
        assertThat(fieldAnnotation(OutboxRecord.class, "retryCount", TableField.class).value())
                .isEqualTo("retry_count");
        assertThat(fieldAnnotation(OutboxRecord.class, "lastError", TableField.class).value())
                .isEqualTo("last_error");
        assertThat(fieldAnnotation(OutboxRecord.class, "sentAt", TableField.class).value())
                .isEqualTo("sent_at");
    }

    private <A extends java.lang.annotation.Annotation> A fieldAnnotation(
            Class<?> type, String fieldName, Class<A> annotationType) throws NoSuchFieldException {
        A annotation = type.getDeclaredField(fieldName).getAnnotation(annotationType);
        assertThat(annotation).isNotNull();
        return annotation;
    }
}
