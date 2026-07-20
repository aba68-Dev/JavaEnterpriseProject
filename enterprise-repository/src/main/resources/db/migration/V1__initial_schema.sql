-- Flyway migration V1: initial schema
-- Enterprise Platform Database

CREATE TABLE IF NOT EXISTS categories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_category_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL,
    email         VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    phone         VARCHAR(20),
    status        VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_user_username UNIQUE (username),
    CONSTRAINT uq_user_email    UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT      NOT NULL,
    role    VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS products (
    id             BIGINT          AUTO_INCREMENT PRIMARY KEY,
    sku            VARCHAR(50)     NOT NULL,
    name           VARCHAR(200)    NOT NULL,
    description    TEXT,
    price          DECIMAL(12, 2)  NOT NULL,
    stock_quantity INT             NOT NULL DEFAULT 0,
    status         VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    category_id    BIGINT,
    created_at     DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version        BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT uq_product_sku    UNIQUE (sku),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_product_category ON products (category_id);
CREATE INDEX idx_product_status   ON products (status);

CREATE TABLE IF NOT EXISTS orders (
    id               BIGINT         AUTO_INCREMENT PRIMARY KEY,
    order_number     VARCHAR(36)    NOT NULL,
    user_id          BIGINT         NOT NULL,
    status           VARCHAR(30)    NOT NULL DEFAULT 'PENDING',
    total_amount     DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    shipping_address VARCHAR(500),
    notes            VARCHAR(500),
    created_at       DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version          BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT uq_order_number UNIQUE (order_number),
    CONSTRAINT fk_order_user   FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_order_user   ON orders (user_id);
CREATE INDEX idx_order_status ON orders (status);

CREATE TABLE IF NOT EXISTS order_items (
    id          BIGINT         AUTO_INCREMENT PRIMARY KEY,
    order_id    BIGINT         NOT NULL,
    product_id  BIGINT         NOT NULL,
    quantity    INT            NOT NULL,
    unit_price  DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_order_item_order   FOREIGN KEY (order_id)   REFERENCES orders   (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_order_item_order   ON order_items (order_id);
CREATE INDEX idx_order_item_product ON order_items (product_id);

-- Audit log for all domain events
CREATE TABLE IF NOT EXISTS audit_log (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    entity_type  VARCHAR(50)  NOT NULL,
    entity_id    BIGINT,
    action       VARCHAR(30)  NOT NULL,
    actor        VARCHAR(100),
    details      TEXT,
    created_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_audit_entity ON audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_actor  ON audit_log (actor);
