-- ================================================================================
-- PGADMIN DATA INSERTION SCRIPT FOR E-COMMERCE MICROSERVICES
-- ================================================================================
-- Databases:
--   1. order_db (Database for order-service)
--   2. inventory_db (Database for inventory-service)
-- ================================================================================

--------------------------------------------------------------------------------
-- SECTION 1: INVENTORY SERVICE (Run this inside inventory_db)
--------------------------------------------------------------------------------

-- Create Table (if not auto-created by Spring Boot Hibernate)
CREATE TABLE IF NOT EXISTS t_inventory (
    id BIGSERIAL PRIMARY KEY,
    sku_code VARCHAR(100) NOT NULL UNIQUE,
    quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0
);

-- Insert Sample Inventory / Stock Data
INSERT INTO t_inventory (sku_code, quantity, reserved_quantity)
VALUES 
    ('IPHONE-15-128GB', 100, 0),
    ('MACBOOK-AIR-M3', 50, 5),
    ('AIRPODS-PRO-V2', 200, 10),
    ('LOGITECH-MX-MASTER-3S', 75, 0),
    ('SAMSUNG-GALAXY-S24', 30, 0)
ON CONFLICT (sku_code) 
DO UPDATE SET 
    quantity = EXCLUDED.quantity,
    reserved_quantity = EXCLUDED.reserved_quantity;


--------------------------------------------------------------------------------
-- SECTION 2: ORDER SERVICE (Run this inside order_db)
--------------------------------------------------------------------------------

-- Create Orders Table (if not auto-created by Spring Boot Hibernate)
CREATE TABLE IF NOT EXISTS t_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(100) NOT NULL UNIQUE,
    customer_id VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    total_amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create Order Items Table
CREATE TABLE IF NOT EXISTS t_order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    sku_code VARCHAR(100) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT fk_order_items_orders FOREIGN KEY (order_id) REFERENCES t_orders(id) ON DELETE CASCADE
);

-- Insert Sample Orders
INSERT INTO t_orders (id, order_number, customer_id, status, total_amount, created_at, updated_at)
VALUES 
    (1, 'ORD-88291', 'CUST-1001', 'CONFIRMED', 799.00, NOW() - INTERVAL '2 DAYS', NOW() - INTERVAL '2 DAYS'),
    (2, 'ORD-88292', 'CUST-1002', 'PENDING', 1299.50, NOW() - INTERVAL '1 DAY', NOW() - INTERVAL '1 DAY'),
    (3, 'ORD-88293', 'CUST-1001', 'CANCELLED', 199.99, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Reset Sequence for Orders ID
SELECT setval('t_orders_id_seq', (SELECT MAX(id) FROM t_orders));

-- Insert Sample Order Items
INSERT INTO t_order_items (id, order_id, sku_code, price, quantity)
VALUES 
    (1, 1, 'IPHONE-15-128GB', 799.00, 1),
    (2, 2, 'MACBOOK-AIR-M3', 1299.50, 1),
    (3, 3, 'AIRPODS-PRO-V2', 199.99, 1)
ON CONFLICT (id) DO NOTHING;

-- Reset Sequence for Order Items ID
SELECT setval('t_order_items_id_seq', (SELECT MAX(id) FROM t_order_items));
