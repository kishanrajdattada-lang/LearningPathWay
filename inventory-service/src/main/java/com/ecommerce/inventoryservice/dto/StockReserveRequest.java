package com.ecommerce.inventoryservice.dto;

public record StockReserveRequest(
        String skuCode,
        Integer quantity
) {}
