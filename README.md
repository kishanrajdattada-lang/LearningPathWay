================================================================================
          ORDER-SERVICE: TECHNICAL ARCHITECTURE & IMPLEMENTATION GUIDE
================================================================================
Project: order-service
Framework: Spring Boot 3
Language: Java 21
Database: PostgreSQL (order_db on Port 5432)


================================================================================
1. DOMAIN ENTITIES & DATABASE DESIGN
================================================================================

Overview:
The order-service domain is built around two core JPA entities: Order and OrderItem.
These handle order processing and order line-item tracking.

Entity Relationship Diagram (ASCII):
+------------------------------------+             +------------------------------------+
|               Order                |             |             OrderItem              |
+------------------------------------+             +------------------------------------+
| PK  id             : Long          | 1         * | PK  id        : Long               |
|     orderNumber    : String (UQ)   |------------<| FK  order_id  : Long               |
|     customerId     : String        |             |     skuCode   : String             |
|     status         : String        |             |     price     : BigDecimal         |
|     totalAmount    : BigDecimal    |             |     quantity  : Integer            |
|     createdAt      : Instant       |             +------------------------------------+
|     updatedAt      : Instant       |
+------------------------------------+

Here is the complete breakdown of entity attributes for both services based on your codebase:
1. Order Service Entities
   Order (t_orders)
   id (Long): Primary key, auto-generated.
   orderNumber (String): Unique business identifier generated for the order (e.g., ORD-XXXXX).
   customerId (String): ID of the customer placing the order.
   status (String): Current order state (e.g., PENDING).
   totalAmount (BigDecimal): Calculated total price for all items in the order.
   createdAt (Instant): Timestamp indicating when the order was created.
   updatedAt (Instant): Timestamp indicating when the order was last modified.
   orderItems (List<OrderItem>): One-to-Many relationship mapping all line items to this order.
   OrderItem (t_order_items)
   id (Long): Primary key, auto-generated.
   skuCode (String): Product code being purchased.
   price (BigDecimal): Unit price of the product.
   quantity (Integer): Number of units requested.
   order (Order): Many-to-One reference linking the line item back to its parent Order.
2. Inventory Service Entity
   Inventory (t_inventory)
   id (Long): Primary key, auto-generated.
   skuCode (String): Unique product identifier used to correlate with line items in order-service.
   quantity (Integer): Total physical stock available in the warehouse.
   reservedQuantity (Integer): Stock temporarily locked/held for orders currently in progress.

Entity Relationship Details:
- Type: One-To-Many (Order -> OrderItem)
- Ownership: OrderItem is the child/owning side containing the foreign key order_id
  (@ManyToOne with @JoinColumn(name = "order_id")).
- Cascade & Lifecycle:
  * CascadeType.ALL ensures that saving, updating, or deleting an Order cascades
    automatically to its OrderItem records.
  * orphanRemoval = true ensures that removing an item from the Java orderItems list
    deletes its row from the database.
  * @PrePersist and @PreUpdate handle timestamp management (createdAt, updatedAt)
    automatically.


================================================================================
2. DATA TRANSFER OBJECTS (DTOs)
================================================================================

Why DTOs Were Introduced:
We do not expose JPA @Entity classes directly via REST controllers for three major
architectural reasons:

1. Decoupling Database Schema from API Contracts: Entities mirror database tables.
   Changing database fields shouldn't break client API contracts.
2. Preventing Circular Reference Loops: Order points to OrderItem, and OrderItem points
   back to Order. Serializing entities directly to JSON causes infinite recursion
   (StackOverflowError).
3. Immutability & Safety: DTOs are implemented using Java 21 record types, making
   request/response payloads thread-safe and immutable.

Complete DTO Inventory:
We maintain 3 DTOs (including nested record definitions):
1. OrderRequest: The incoming API request body for creating a new order. Contains
   customerId and a list of OrderItemRequest.
2. OrderItemRequest: Represents an individual line item inside OrderRequest (skuCode,
   price, quantity).
3. OrderResponse: The outgoing API response payload. Exposes public-facing data
   (orderNumber, status, totalAmount, createdAt) along with a nested list of
   OrderItemResponse.


================================================================================
3. CONFIGURATION & SOURCE CODE
================================================================================

--------------------------------------------------------------------------------
A. Configuration: src/main/resources/application.yml
--------------------------------------------------------------------------------
server:
  port: 8081

spring:
  application:
    name: order-service
  datasource:
    url: jdbc:postgresql://localhost:5432/order_db
    username: postgres
    password: root@123
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    defer-datasource-initialization: true
  sql:
    init:
      mode: always


--------------------------------------------------------------------------------
B. Data Transfer Objects (DTOs)
--------------------------------------------------------------------------------
File: src/main/java/com/ecommerce/order_service/dto/OrderItemRequest.java
package com.ecommerce.order_service.dto;

import java.math.BigDecimal;

public record OrderItemRequest(
    String skuCode,
    BigDecimal price,
    Integer quantity
) {}

---

File: src/main/java/com/ecommerce/order_service/dto/OrderRequest.java
package com.ecommerce.order_service.dto;

import java.util.List;

public record OrderRequest(
    String customerId,
    List<OrderItemRequest> items
) {}

---

File: src/main/java/com/ecommerce/order_service/dto/OrderResponse.java
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


--------------------------------------------------------------------------------
C. Domain Entities
--------------------------------------------------------------------------------
File: src/main/java/com/ecommerce/order_service/model/OrderItem.java
package com.ecommerce.order_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "t_order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku_code", nullable = false)
    private String skuCode;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
}

---

File: src/main/java/com/ecommerce/order_service/model/Order.java
package com.ecommerce.order_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "t_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String status;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}


--------------------------------------------------------------------------------
D. Repository Layer
--------------------------------------------------------------------------------
File: src/main/java/com/ecommerce/order_service/repository/OrderRepository.java
package com.ecommerce.order_service.repository;

import com.ecommerce.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
}


--------------------------------------------------------------------------------
E. Service Layer
--------------------------------------------------------------------------------
File: src/main/java/com/ecommerce/order_service/service/OrderService.java
package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.*;
import com.ecommerce.order_service.model.Order;
import com.ecommerce.order_service.model.OrderItem;
import com.ecommerce.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
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


--------------------------------------------------------------------------------
F. REST Controller Layer
--------------------------------------------------------------------------------
File: src/main/java/com/ecommerce/order_service/controller/OrderController.java
package com.ecommerce.order_service.controller;

import com.ecommerce.order_service.dto.OrderRequest;
import com.ecommerce.order_service.dto.OrderResponse;
import com.ecommerce.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@RequestBody OrderRequest orderRequest) {
        return orderService.createOrder(orderRequest);
    }

    @GetMapping("/{orderNumber}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse getOrder(@PathVariable String orderNumber) {
        return orderService.getOrderByNumber(orderNumber);
    }
}


================================================================================
4. API ENDPOINTS & TESTING GUIDE
================================================================================

Endpoint Matrix:
+--------+---------------------------------+-----------------------------------+-------------+
| Method | Endpoint                        | Description                       | Status Code |
+--------+---------------------------------+-----------------------------------+-------------+
| POST   | /api/v1/orders                  | Create a new order with line items| 201 Created |
| GET    | /api/v1/orders                  | Fetch all orders as a List        | 200 OK      |
| GET    | /api/v1/orders/{orderNumber}    | Fetch order details by number     | 200 OK      |
| POST   | /api/v1/inventory               | Add a new inventory item          | 201 Created |
| GET    | /api/v1/inventory               | Fetch all inventory items as List | 200 OK      |
| GET    | /api/v1/inventory?skuCode=...   | Check stock for specific SKU codes| 200 OK      |
| GET    | /api/v1/inventory/{skuCode}     | Fetch single inventory item by SKU| 200 OK      |
| PUT    | /api/v1/inventory/{skuCode}     | Update total stock quantity       | 200 OK      |
| POST   | /api/v1/inventory/reserve       | Reserve stock quantity for order  | 200 OK      |
| DELETE | /api/v1/inventory/{skuCode}     | Delete inventory item by SKU      | 204 No Content
+--------+---------------------------------+-----------------------------------+-------------+

--- Order Service Examples ---

1. Create Order Request (POST):
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-1001",
    "items": [
      {
        "skuCode": "IPHONE-15-128GB",
        "price": 799.00,
        "quantity": 1
      }
    ]
  }'

2. Fetch All Orders (GET):
curl -X GET http://localhost:8081/api/v1/orders

3. Fetch Single Order Details (GET):
curl -X GET http://localhost:8081/api/v1/orders/ORD-XXXXXXXX

--- Inventory Service Examples ---

4. Create Inventory Item (POST):
curl -X POST http://localhost:8082/api/v1/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "skuCode": "IPHONE-15-128GB",
    "quantity": 100
  }'

5. Fetch All Inventory Items (GET):
curl -X GET http://localhost:8082/api/v1/inventory

6. Fetch Single Inventory Item (GET):
curl -X GET http://localhost:8082/api/v1/inventory/IPHONE-15-128GB

7. Update Stock Quantity (PUT):
curl -X PUT "http://localhost:8082/api/v1/inventory/IPHONE-15-128GB?quantity=150"

8. Reserve Stock for Order (POST):
curl -X POST http://localhost:8082/api/v1/inventory/reserve \
  -H "Content-Type: application/json" \
  -d '{
    "skuCode": "IPHONE-15-128GB",
    "quantity": 2
  }'

9. Delete Inventory Item (DELETE):
curl -X DELETE http://localhost:8082/api/v1/inventory/IPHONE-15-128GB
================================================================================
