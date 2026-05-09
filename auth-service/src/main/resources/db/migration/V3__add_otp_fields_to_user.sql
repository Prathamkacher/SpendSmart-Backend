-- V3__add_otp_fields_to_user.sql
-- Add fields for handling forgot password OTP

ALTER TABLE users
ADD COLUMN reset_otp VARCHAR(6) NULL,
ADD COLUMN reset_otp_expiry DATETIME NULL;
