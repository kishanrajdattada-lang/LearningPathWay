# E-Commerce Microservices - API Reference & Payloads

This document contains a complete specification of all REST API endpoints across `order-service` (Port 8081) and `inventory-service` (Port 8082), including request/response JSON payloads, status codes, and `curl` test commands.

---

## Table of Contents
1. [Order Service APIs (Port 8081)](#1-order-service-apis-port-8081)
   - [Create Order](#1-create-order-post)
   - [Fetch All Orders](#2-fetch-all-orders-get)
   - [Fetch Order by Number](#3-fetch-order-by-number-get)
2. [Inventory Service APIs (Port 8082)](#2-inventory-service-apis-port-8082)
   - [Create Inventory Item](#1-create-inventory-item-post)
   - [Fetch All Inventory Items](#2-fetch-all-inventory-items-get)
   - [Check Stock for Specific SKUs](#3-check-stock-for-specific-skus-get)
   - [Fetch Inventory Item by SKU](#4-fetch-inventory-item-by-sku-get)
   - [Update Stock Quantity](#5-update-stock-quantity-put)
   - [Reserve Stock for Order](#6-reserve-stock-for-order-post)
   - [Delete Inventory Item](#7-delete-inventory-item-delete)

---

## 1. Order Service APIs (Port 8081)

### 1. Create Order (`POST`)
- **URL**: `http://localhost:8081/api/v1/orders`
- **Method**: `POST`
- **Content-Type**: `application/json`
- **Status Code**: `201 Created`

#### Request Payload:
```json
{
  "customerId": "CUST-1001",
  "items": [
    {
      "skuCode": "IPHONE-15-128GB",
      "price": 799.00,
      "quantity": 1
    },
    {
      "skuCode": "MACBOOK-AIR-M3",
      "price": 1299.00,
      "quantity": 1
    }
  ]
}
```

#### Response Payload:
```json
{
  "id": 1,
  "orderNumber": "ORD-A1B2C3D4",
  "customerId": "CUST-1001",
  "status": "PENDING",
  "totalAmount": 2098.00,
  "createdAt": "2026-09-17T18:30:00Z",
  "items": [
    {
      "id": 1,
      "skuCode": "IPHONE-15-128GB",
      "price": 799.00,
      "quantity": 1
    },
    {
      "id": 2,
      "skuCode": "MACBOOK-AIR-M3",
      "price": 1299.00,
      "quantity": 1
    }
  ]
}
```

#### cURL Command:
```bash
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-1001",
    "items": [
      {
        "skuCode": "IPHONE-15-128GB",
        "price": 799.00,
        "quantity": 1
      },
      {
        "skuCode": "MACBOOK-AIR-M3",
        "price": 1299.00,
        "quantity": 1
      }
    ]
  }'
```

---

### 2. Fetch All Orders (`GET`)
- **URL**: `http://localhost:8081/api/v1/orders`
- **Method**: `GET`
- **Status Code**: `200 OK`

#### Response Payload:
```json
[
  {
    "id": 1,
    "orderNumber": "ORD-A1B2C3D4",
    "customerId": "CUST-1001",
    "status": "PENDING",
    "totalAmount": 2098.00,
    "createdAt": "2026-09-17T18:30:00Z",
    "items": [
      {
        "id": 1,
        "skuCode": "IPHONE-15-128GB",
        "price": 799.00,
        "quantity": 1
      },
      {
        "id": 2,
        "skuCode": "MACBOOK-AIR-M3",
        "price": 1299.00,
        "quantity": 1
      }
    ]
  }
]
```

#### cURL Command:
```bash
curl -X GET http://localhost:8081/api/v1/orders
```

---

### 3. Fetch Order by Number (`GET`)
- **URL**: `http://localhost:8081/api/v1/orders/{orderNumber}`
- **Method**: `GET`
- **Status Code**: `200 OK`

#### Response Payload:
```json
{
  "id": 1,
  "orderNumber": "ORD-A1B2C3D4",
  "customerId": "CUST-1001",
  "status": "PENDING",
  "totalAmount": 2098.00,
  "createdAt": "2026-09-17T18:30:00Z",
  "items": [
    {
      "id": 1,
      "skuCode": "IPHONE-15-128GB",
      "price": 799.00,
      "quantity": 1
    }
  ]
}
```

#### cURL Command:
```bash
curl -X GET http://localhost:8081/api/v1/orders/ORD-A1B2C3D4
```

---

## 2. Inventory Service APIs (Port 8082)

### 1. Create Inventory Item (`POST`)
- **URL**: `http://localhost:8082/api/v1/inventory`
- **Method**: `POST`
- **Content-Type**: `application/json`
- **Status Code**: `201 Created`

#### Request Payload:
```json
{
  "skuCode": "IPHONE-15-128GB",
  "quantity": 100
}
```

#### Response Payload:
```json
{
  "id": 1,
  "skuCode": "IPHONE-15-128GB",
  "quantity": 100,
  "reservedQuantity": 0,
  "availableQuantity": 100,
  "isInStock": true
}
```

#### cURL Command:
```bash
curl -X POST http://localhost:8082/api/v1/inventory \
  -H "Content-Type: application/json" \
  -d '{
    "skuCode": "IPHONE-15-128GB",
    "quantity": 100
  }'
```

---

### 2. Fetch All Inventory Items (`GET`)
- **URL**: `http://localhost:8082/api/v1/inventory`
- **Method**: `GET`
- **Status Code**: `200 OK`

#### Response Payload:
```json
[
  {
    "id": 1,
    "skuCode": "IPHONE-15-128GB",
    "quantity": 100,
    "reservedQuantity": 0,
    "availableQuantity": 100,
    "isInStock": true
  },
  {
    "id": 2,
    "skuCode": "MACBOOK-AIR-M3",
    "quantity": 50,
    "reservedQuantity": 5,
    "availableQuantity": 45,
    "isInStock": true
  }
]
```

#### cURL Command:
```bash
curl -X GET http://localhost:8082/api/v1/inventory
```

---

### 3. Check Stock for Specific SKUs (`GET`)
- **URL**: `http://localhost:8082/api/v1/inventory?skuCode=IPHONE-15-128GB&skuCode=MACBOOK-AIR-M3`
- **Method**: `GET`
- **Status Code**: `200 OK`

#### Response Payload:
```json
[
  {
    "id": 1,
    "skuCode": "IPHONE-15-128GB",
    "quantity": 100,
    "reservedQuantity": 0,
    "availableQuantity": 100,
    "isInStock": true
  },
  {
    "id": 2,
    "skuCode": "MACBOOK-AIR-M3",
    "quantity": 50,
    "reservedQuantity": 5,
    "availableQuantity": 45,
    "isInStock": true
  }
]
```

#### cURL Command:
```bash
curl -X GET "http://localhost:8082/api/v1/inventory?skuCode=IPHONE-15-128GB&skuCode=MACBOOK-AIR-M3"
```

---

### 4. Fetch Inventory Item by SKU (`GET`)
- **URL**: `http://localhost:8082/api/v1/inventory/{skuCode}`
- **Method**: `GET`
- **Status Code**: `200 OK`

#### Response Payload:
```json
{
  "id": 1,
  "skuCode": "IPHONE-15-128GB",
  "quantity": 100,
  "reservedQuantity": 0,
  "availableQuantity": 100,
  "isInStock": true
}
```

#### cURL Command:
```bash
curl -X GET http://localhost:8082/api/v1/inventory/IPHONE-15-128GB
```

---

### 5. Update Stock Quantity (`PUT`)
- **URL**: `http://localhost:8082/api/v1/inventory/{skuCode}?quantity={newQuantity}`
- **Method**: `PUT`
- **Status Code**: `200 OK`

#### Response Payload:
```json
{
  "id": 1,
  "skuCode": "IPHONE-15-128GB",
  "quantity": 150,
  "reservedQuantity": 0,
  "availableQuantity": 150,
  "isInStock": true
}
```

#### cURL Command:
```bash
curl -X PUT "http://localhost:8082/api/v1/inventory/IPHONE-15-128GB?quantity=150"
```

---

### 6. Reserve Stock for Order (`POST`)
- **URL**: `http://localhost:8082/api/v1/inventory/reserve`
- **Method**: `POST`
- **Content-Type**: `application/json`
- **Status Code**: `200 OK`

#### Request Payload:
```json
{
  "skuCode": "IPHONE-15-128GB",
  "quantity": 2
}
```

#### Response Payload:
```json
{
  "id": 1,
  "skuCode": "IPHONE-15-128GB",
  "quantity": 150,
  "reservedQuantity": 2,
  "availableQuantity": 148,
  "isInStock": true
}
```

#### cURL Command:
```bash
curl -X POST http://localhost:8082/api/v1/inventory/reserve \
  -H "Content-Type: application/json" \
  -d '{
    "skuCode": "IPHONE-15-128GB",
    "quantity": 2
  }'
```

---

### 7. Delete Inventory Item (`DELETE`)
- **URL**: `http://localhost:8082/api/v1/inventory/{skuCode}`
- **Method**: `DELETE`
- **Status Code**: `204 No Content` (Empty body)

#### cURL Command:
```bash
curl -X DELETE http://localhost:8082/api/v1/inventory/IPHONE-15-128GB
```
