-- Insert Initial Stock Levels for Core SKUs
INSERT INTO t_inventory (sku_code, quantity, reserved_quantity)
VALUES 
    ('IPHONE-15-128GB', 50, 0),
    ('MACBOOK-AIR-M3', 25, 0),
    ('AIRPODS-PRO-V2', 100, 0),
    ('LOGITECH-MX-MASTER-3S', 0, 0); -- Out-of-stock item for edge case testing