package com.ecommerce.order_service.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        String customerId,
        String status,
        BigDecimal totalAmount,
        Instant createdAt,
        List<OrderItemResponse> items
) {
    public record OrderItemResponse(
            Long id,
            String skuCode,
            BigDecimal price,
            Integer quantity
    ) {}
}
