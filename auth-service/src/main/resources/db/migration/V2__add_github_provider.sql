-- V2__add_github_provider.sql
ALTER TABLE users MODIFY COLUMN provider ENUM('LOCAL', 'GOOGLE', 'GITHUB') NOT NULL DEFAULT 'LOCAL';
