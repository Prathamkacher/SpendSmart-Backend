-- V1__create_categories_table.sql
-- Category-Service schema for SpendSmart

CREATE TABLE IF NOT EXISTS categories (
    category_id    BIGINT          NOT NULL AUTO_INCREMENT,
    user_id        BIGINT          NOT NULL,
    name           VARCHAR(100)    NOT NULL,
    type           ENUM('EXPENSE', 'INCOME') NOT NULL,
    icon           VARCHAR(50)     NULL,
    color_code     VARCHAR(7)      NULL DEFAULT '#6366F1',
    budget_limit   DECIMAL(15, 2)  NULL,
    is_default     BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at     DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (category_id),

    -- Unique constraint: no duplicate category names per user+type
    UNIQUE INDEX uq_user_name_type (user_id, name, type),

    -- Performance indexes
    INDEX idx_categories_user_id (user_id),
    INDEX idx_categories_user_type (user_id, type),
    INDEX idx_categories_is_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
