package com.ecommerce.inventoryservice.dto;

public record InventoryRequest(
        String skuCode,
        Integer quantity
) {}