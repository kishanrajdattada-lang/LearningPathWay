package com.ecommerce.order_service.dto;

import java.math.BigDecimal;

public record OrderItemRequest(
        String skuCode,
        BigDecimal price,
        Integer quantity
) {}