-- V1__create_budgets_table.sql
-- Budget-Service schema for SpendSmart

CREATE TABLE IF NOT EXISTS budgets (
    budget_id       BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    category_id     BIGINT          NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    limit_amount    DECIMAL(15, 2)  NOT NULL,
    currency        VARCHAR(3)      NOT NULL DEFAULT 'INR',
    period          ENUM('WEEKLY', 'MONTHLY', 'CUSTOM') NOT NULL DEFAULT 'MONTHLY',
    start_date      DATE            NOT NULL,
    end_date        DATE            NOT NULL,
    spent_amount    DECIMAL(15, 2)  NOT NULL DEFAULT 0.00,
    alert_threshold INT             NOT NULL DEFAULT 80,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (budget_id),

    -- Performance indexes
    INDEX idx_budgets_user_id (user_id),
    INDEX idx_budgets_user_active (user_id, is_active),
    INDEX idx_budgets_category (category_id),
    
    -- Constraint: Only one active budget per user per category
    UNIQUE INDEX uq_user_category_active (user_id, category_id, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
