package org.example.springboot3java21demo.paymentservice.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseMappingAnnotationTest {

    @Test
    @DisplayName("支付流水字段映射到 op_payment")
    void mapsPaymentRecordColumns() throws NoSuchFieldException {
        assertThat(PaymentRecord.class.getAnnotation(TableName.class).value())
                .isEqualTo("op_payment");
        assertThat(PaymentRecord.class.getDeclaredField("paymentNo")
                .getAnnotation(TableId.class).value())
                .isEqualTo("payment_no");
        assertThat(PaymentRecord.class.getDeclaredField("channelTradeNo")
                .getAnnotation(TableField.class).value())
                .isEqualTo("channel_trade_no");
    }
}
