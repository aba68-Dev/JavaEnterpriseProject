-- Flyway migration V2: seed reference data

INSERT INTO categories (name, description) VALUES
    ('Electronics',    'Electronic devices and accessories'),
    ('Clothing',       'Apparel and fashion items'),
    ('Books',          'Books, eBooks, and educational material'),
    ('Home & Kitchen', 'Household and kitchen products'),
    ('Sports',         'Sporting goods and outdoor equipment')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Default admin user (password: Admin@1234 — BCrypt hash)
INSERT INTO users (username, email, password_hash, full_name, status)
VALUES (
    'admin',
    'admin@enterprise.com',
    '$2a$12$eImiTXuWVxfM37uY4JANjQ==',
    'System Administrator',
    'ACTIVE'
) ON DUPLICATE KEY UPDATE status = 'ACTIVE';

-- Assign ROLE_ADMIN to the admin user
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN' FROM users WHERE username = 'admin'
ON DUPLICATE KEY UPDATE role = role;

-- Sample products
INSERT INTO products (sku, name, description, price, stock_quantity, status, category_id)
SELECT 'ELEC-001', 'Wireless Headphones', 'Premium noise-cancelling headphones', 199.99, 50, 'ACTIVE', c.id
FROM categories c WHERE c.name = 'Electronics'
ON DUPLICATE KEY UPDATE price = VALUES(price);

INSERT INTO products (sku, name, description, price, stock_quantity, status, category_id)
SELECT 'ELEC-002', 'Smart Watch', '4G-enabled smartwatch with health tracking', 349.99, 30, 'ACTIVE', c.id
FROM categories c WHERE c.name = 'Electronics'
ON DUPLICATE KEY UPDATE price = VALUES(price);

INSERT INTO products (sku, name, description, price, stock_quantity, status, category_id)
SELECT 'BOOK-001', 'Clean Code', 'A Handbook of Agile Software Craftsmanship by Robert C. Martin', 39.99, 100, 'ACTIVE', c.id
FROM categories c WHERE c.name = 'Books'
ON DUPLICATE KEY UPDATE price = VALUES(price);

INSERT INTO products (sku, name, description, price, stock_quantity, status, category_id)
SELECT 'BOOK-002', 'Design Patterns', 'Elements of Reusable Object-Oriented Software (GoF)', 49.99, 80, 'ACTIVE', c.id
FROM categories c WHERE c.name = 'Books'
ON DUPLICATE KEY UPDATE price = VALUES(price);
