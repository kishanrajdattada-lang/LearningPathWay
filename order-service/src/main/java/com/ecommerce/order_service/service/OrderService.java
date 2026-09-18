package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.*;
import com.ecommerce.order_service.exception.InventoryServiceUnavailableException;
import com.ecommerce.order_service.exception.OutOfStockException;
import com.ecommerce.order_service.model.Order;
import com.ecommerce.order_service.model.OrderItem;
import com.ecommerce.order_service.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class OrderService {

    public OrderService(OrderRepository orderRepository, WebClient webClient) {
        this.orderRepository = orderRepository;
        this.webClient = webClient;
    }

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        // 1. Synchronously check stock availability & requested quantities via WebClient & Resilience4j
        boolean allInStock = checkStockAvailability(orderRequest.items());

        if (!allInStock) {
            throw new OutOfStockException("Product is out of stock, requested quantity exceeds available stock, or Inventory Service is unavailable.");
        }

        // 2. Save order to DB if items are in stock
        Order order = Order.builder()
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customerId(orderRequest.customerId())
                .status("PENDING")
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (var itemReq : orderRequest.items()) {
            OrderItem item = OrderItem.builder()
                    .skuCode(itemReq.skuCode())
                    .price(itemReq.price())
                    .quantity(itemReq.quantity())
                    .build();
            order.addOrderItem(item);

            BigDecimal itemTotal = itemReq.price().multiply(BigDecimal.valueOf(itemReq.quantity()));
            total = total.add(itemTotal);
        }

        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);

        return mapToResponse(savedOrder);
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackCheckStockAvailability")
    public boolean checkStockAvailability(List<OrderItemRequest> items) {
        List<String> skuCodes = items.stream()
                .map(OrderItemRequest::skuCode)
                .toList();

        InventoryResponse[] responseArray = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/inventory")
                        .queryParam("skuCode", skuCodes)
                        .build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        if (responseArray == null || responseArray.length == 0) {
            return false;
        }

        java.util.Map<String, Integer> inventoryMap = Arrays.stream(responseArray)
                .collect(java.util.stream.Collectors.toMap(
                        InventoryResponse::skuCode,
                        inv -> inv.availableQuantity() != null ? inv.availableQuantity() : 0,
                        (existing, replacement) -> existing
                ));

        for (OrderItemRequest item : items) {
            Integer available = inventoryMap.get(item.skuCode());
            if (available == null || available < item.quantity()) {
                log.warn("Insufficient stock for SKU {}. Requested: {}, Available: {}",
                        item.skuCode(), item.quantity(), available);
                return false;
            }
        }

        return true;
    }

    // Fallback method executed when Inventory Service is down, times out, or circuit opens
    public boolean fallbackCheckStockAvailability(List<OrderItemRequest> items, Throwable throwable) {
        log.error("Inventory Service is unavailable or unreachable! Cause: {}", throwable.getMessage());
        throw new InventoryServiceUnavailableException("Sorry, a technical error occurred at our end. Please try again later.");
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));
        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> new OrderResponse.OrderItemResponse(
                        item.getId(),
                        item.getSkuCode(),
                        item.getPrice(),
                        item.getQuantity()
                )).toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                items
        );
    }
}