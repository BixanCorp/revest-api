-- Migration: 005_create_indexes.sql
-- Description: Create additional composite indexes for performance optimization
-- Date: 2025-11-04
-- Author: System
-- Dependencies: Requires users and accounts tables

-- Additional indexes on users table for common query patterns
CREATE INDEX IF NOT EXISTS idx_users_active_created ON users(active, created_at);
CREATE INDEX IF NOT EXISTS idx_users_active_last_login ON users(active, last_login);
CREATE INDEX IF NOT EXISTS idx_users_state_active ON users(state, active);
CREATE INDEX IF NOT EXISTS idx_users_country_active ON users(country, active);

-- Additional indexes on accounts table for performance
CREATE INDEX IF NOT EXISTS idx_accounts_user_deleted_type ON accounts(user_id, deleted, type);
CREATE INDEX IF NOT EXISTS idx_accounts_type_deleted ON accounts(type, deleted);
CREATE INDEX IF NOT EXISTS idx_accounts_provider ON accounts(provider);
CREATE INDEX IF NOT EXISTS idx_accounts_creation_type ON accounts(creation_type);

-- Index for date-based queries
CREATE INDEX IF NOT EXISTS idx_accounts_created_deleted ON accounts(created, deleted);
CREATE INDEX IF NOT EXISTS idx_accounts_updated ON accounts(updated);

-- Full-text search indexes (if needed for account names)
-- Note: Uncomment if full-text search is required
-- ALTER TABLE accounts ADD FULLTEXT INDEX ft_idx_account_name (name);
-- ALTER TABLE users ADD FULLTEXT INDEX ft_idx_user_names (firstname, lastname);
