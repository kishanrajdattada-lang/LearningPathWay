-- Create Enum for Order Status
CREATE TYPE order_status AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED', 'FAILED');

-- Core Orders Table
CREATE TABLE IF NOT EXISTS t_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(100) NOT NULL UNIQUE,
    customer_id VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    total_amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Order Items Table (One-to-Many with t_orders)
CREATE TABLE IF NOT EXISTS t_order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    sku_code VARCHAR(100) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT fk_order_items_orders FOREIGN KEY (order_id) REFERENCES t_orders(id) ON DELETE CASCADE
);

-- Index for Faster Order Lookups
CREATE INDEX idx_orders_customer_id ON t_orders(customer_id);
CREATE INDEX idx_orders_order_number ON t_orders(order_number);