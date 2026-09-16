package com.ecommerce.order_service.dto;

import java.util.List;

public record OrderRequest(
        String customerId,
        List<OrderItemRequest> items
) {}