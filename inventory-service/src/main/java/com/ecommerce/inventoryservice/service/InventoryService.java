package com.ecommerce.inventoryservice.service;

import com.ecommerce.inventoryservice.dto.InventoryRequest;
import com.ecommerce.inventoryservice.dto.InventoryResponse;
import com.ecommerce.inventoryservice.dto.StockReserveRequest;
import com.ecommerce.inventoryservice.model.Inventory;
import com.ecommerce.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public InventoryResponse createInventory(InventoryRequest inventoryRequest) {
        if (inventoryRepository.existsBySkuCode(inventoryRequest.skuCode())) {
            throw new IllegalArgumentException("Inventory item already exists for SKU code: " + inventoryRequest.skuCode());
        }

        Inventory inventory = Inventory.builder()
                .skuCode(inventoryRequest.skuCode())
                .quantity(inventoryRequest.quantity() != null ? inventoryRequest.quantity() : 0)
                .reservedQuantity(0)
                .build();

        Inventory savedInventory = inventoryRepository.save(inventory);
        return mapToResponse(savedInventory);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryBySkuCode(String skuCode) {
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException("Inventory item not found for SKU code: " + skuCode));
        return mapToResponse(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCodes) {
        return inventoryRepository.findBySkuCodeIn(skuCodes).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public InventoryResponse updateStock(String skuCode, Integer newQuantity) {
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException("Inventory item not found for SKU code: " + skuCode));

        if (newQuantity < inventory.getReservedQuantity()) {
            throw new IllegalArgumentException("New quantity cannot be less than reserved quantity: " + inventory.getReservedQuantity());
        }

        inventory.setQuantity(newQuantity);
        Inventory savedInventory = inventoryRepository.save(inventory);
        return mapToResponse(savedInventory);
    }

    @Transactional
    public InventoryResponse reserveStock(StockReserveRequest reserveRequest) {
        Inventory inventory = inventoryRepository.findBySkuCode(reserveRequest.skuCode())
                .orElseThrow(() -> new RuntimeException("Inventory item not found for SKU code: " + reserveRequest.skuCode()));

        int available = inventory.getQuantity() - inventory.getReservedQuantity();
        if (available < reserveRequest.quantity()) {
            throw new IllegalStateException("Insufficient stock for SKU code " + reserveRequest.skuCode() +
                    ". Available: " + available + ", Requested: " + reserveRequest.quantity());
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() + reserveRequest.quantity());
        Inventory savedInventory = inventoryRepository.save(inventory);
        return mapToResponse(savedInventory);
    }

    @Transactional
    public void deleteInventory(String skuCode) {
        if (!inventoryRepository.existsBySkuCode(skuCode)) {
            throw new RuntimeException("Inventory item not found for SKU code: " + skuCode);
        }
        inventoryRepository.deleteBySkuCode(skuCode);
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        int reserved = inventory.getReservedQuantity() != null ? inventory.getReservedQuantity() : 0;
        int available = inventory.getQuantity() - reserved;
        return new InventoryResponse(
                inventory.getId(),
                inventory.getSkuCode(),
                inventory.getQuantity(),
                reserved,
                available,
                available > 0
        );
    }
}