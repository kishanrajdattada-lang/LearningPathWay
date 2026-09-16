-- Core Inventory Table
CREATE TABLE IF NOT EXISTS t_inventory (
    id BIGSERIAL PRIMARY KEY,
    sku_code VARCHAR(100) NOT NULL UNIQUE,
    quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index for Rapid SKU Stock Lookups
CREATE INDEX idx_inventory_sku_code ON t_inventory(sku_code);