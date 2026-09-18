package com.ecommerce.order_service.dto;

public record InventoryResponse(
        Long id,
        String skuCode,
        Integer quantity,
        Integer reservedQuantity,
        Integer availableQuantity,
        boolean isInStock
) {
    public InventoryResponse(String skuCode, boolean isInStock, Integer availableQuantity) {
        this(null, skuCode, null, null, availableQuantity, isInStock);
    }
}