-- V1__create_expenses_table.sql
-- Expense-Service schema for SpendSmart

CREATE TABLE IF NOT EXISTS expenses (
    expense_id     BIGINT          NOT NULL AUTO_INCREMENT,
    user_id        BIGINT          NOT NULL,
    category_id    BIGINT          NULL,
    title          VARCHAR(100)    NOT NULL,
    amount         DECIMAL(15, 2)  NOT NULL,
    currency       VARCHAR(3)      NOT NULL DEFAULT 'INR',
    type           ENUM('EXPENSE', 'SPLIT') NOT NULL DEFAULT 'EXPENSE',
    payment_method ENUM('CASH', 'CARD', 'UPI', 'BANK', 'WALLET') NOT NULL DEFAULT 'CASH',
    date           DATE            NOT NULL,
    notes          VARCHAR(500)    NULL,
    receipt_url    VARCHAR(500)    NULL,
    is_recurring   BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at     DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (expense_id),

    -- Performance indexes
    INDEX idx_expenses_user_id (user_id),
    INDEX idx_expenses_category_id (category_id),
    INDEX idx_expenses_date (date),
    INDEX idx_expenses_user_date (user_id, date),
    INDEX idx_expenses_user_type (user_id, type),
    INDEX idx_expenses_user_category (user_id, category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
