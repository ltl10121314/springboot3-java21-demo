package org.example.springboot3java21demo.orderpay.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchOrderDocument {
    private String orderNo;
    private String requestId;
    private String userId;
    private String skuId;
    private String skuName;
    private BigDecimal totalAmount;
    private int quantity;
    private String status;
    private String paymentNo;
    private String createdAt;
    private String updatedAt;
    private String paidAt;
}
