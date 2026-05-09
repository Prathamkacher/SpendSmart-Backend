-- V1__init_auth_schema.sql
-- SpendSmart Auth Service - Initial Schema

CREATE TABLE IF NOT EXISTS users (
    user_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(100)        NOT NULL,
    email         VARCHAR(150)        NOT NULL UNIQUE,
    password_hash VARCHAR(255),                         -- null for GOOGLE users
    currency      VARCHAR(10)         NOT NULL DEFAULT 'INR',
    timezone      VARCHAR(50)         NOT NULL DEFAULT 'Asia/Kolkata',
    avatar_url    VARCHAR(500),
    provider      ENUM('LOCAL','GOOGLE') NOT NULL DEFAULT 'LOCAL',
    is_active     TINYINT(1)          NOT NULL DEFAULT 1,
    monthly_budget DECIMAL(15,2)      NOT NULL DEFAULT 0.00,
    role          ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
    created_at    DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Index for faster email lookup (login)
CREATE INDEX idx_users_email    ON users(email);
CREATE INDEX idx_users_active   ON users(is_active);
CREATE INDEX idx_users_provider ON users(provider);

-- Refresh token table for logout/token rotation
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT      NOT NULL,
    token        VARCHAR(500) NOT NULL UNIQUE,
    expiry_date  DATETIME    NOT NULL,
    is_revoked   TINYINT(1)  NOT NULL DEFAULT 0,
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token   ON refresh_tokens(token);
CREATE INDEX idx_refresh_user_id ON refresh_tokens(user_id);