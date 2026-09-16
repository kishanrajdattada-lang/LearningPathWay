-- Insert Sample Orders
INSERT INTO t_orders (order_number, customer_id, status, total_amount, created_at)
VALUES 
    ('ORD-88291', 'CUST-1001', 'CONFIRMED', 799.00, NOW() - INTERVAL '2 DAYS'),
    ('ORD-88292', 'CUST-1002', 'PENDING', 1299.50, NOW() - INTERVAL '1 DAY'),
    ('ORD-88293', 'CUST-1001', 'CANCELLED', 199.99, NOW());

-- Insert Sample Order Line Items
INSERT INTO t_order_items (order_id, sku_code, price, quantity)
VALUES 
    (1, 'IPHONE-15-128GB', 799.00, 1),
    (2, 'MACBOOK-AIR-M3', 1299.50, 1),
    (3, 'AIRPODS-PRO-V2', 199.99, 1);