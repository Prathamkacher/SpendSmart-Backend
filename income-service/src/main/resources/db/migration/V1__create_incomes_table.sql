-- V1__create_incomes_table.sql
-- Income-Service schema for SpendSmart

CREATE TABLE IF NOT EXISTS incomes (
    income_id         BIGINT          NOT NULL AUTO_INCREMENT,
    user_id           BIGINT          NOT NULL,
    category_id       BIGINT          NULL,
    title             VARCHAR(100)    NOT NULL,
    amount            DECIMAL(15, 2)  NOT NULL,
    currency          VARCHAR(3)      NOT NULL DEFAULT 'INR',
    source            ENUM('SALARY', 'FREELANCE', 'BUSINESS', 'INVESTMENT', 'GIFT', 'OTHER') NOT NULL DEFAULT 'SALARY',
    date              DATE            NOT NULL,
    notes             VARCHAR(500)    NULL,
    is_recurring      BOOLEAN         NOT NULL DEFAULT FALSE,
    recurrence_period ENUM('MONTHLY', 'WEEKLY', 'YEARLY') NULL,
    created_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (income_id),

    -- Performance indexes
    INDEX idx_incomes_user_id (user_id),
    INDEX idx_incomes_source (source),
    INDEX idx_incomes_date (date),
    INDEX idx_incomes_user_date (user_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
