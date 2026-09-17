package com.ecommerce.inventoryservice.controller;

import com.ecommerce.inventoryservice.dto.InventoryRequest;
import com.ecommerce.inventoryservice.dto.InventoryResponse;
import com.ecommerce.inventoryservice.dto.StockReserveRequest;
import com.ecommerce.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // Create a new inventory item
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse createInventory(@RequestBody InventoryRequest inventoryRequest) {
        return inventoryService.createInventory(inventoryRequest);
    }

    // Fetch all inventory items or check stock status for specific SKU codes
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> getInventory(@RequestParam(required = false) List<String> skuCode) {
        if (skuCode != null && !skuCode.isEmpty()) {
            return inventoryService.isInStock(skuCode);
        }
        return inventoryService.getAllInventory();
    }

    // Fetch single inventory item details by SKU code
    @GetMapping("/{skuCode}")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse getInventoryBySkuCode(@PathVariable String skuCode) {
        return inventoryService.getInventoryBySkuCode(skuCode);
    }

    // Update stock quantity for a SKU code
    @PutMapping("/{skuCode}")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse updateStock(@PathVariable String skuCode, @RequestParam Integer quantity) {
        return inventoryService.updateStock(skuCode, quantity);
    }

    // Reserve stock for an order
    @PostMapping("/reserve")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse reserveStock(@RequestBody StockReserveRequest reserveRequest) {
        return inventoryService.reserveStock(reserveRequest);
    }

    // Delete inventory item by SKU code
    @DeleteMapping("/{skuCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInventory(@PathVariable String skuCode) {
        inventoryService.deleteInventory(skuCode);
    }
}