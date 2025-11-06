-- Rollback: 005_create_indexes.sql
-- Description: Drop additional performance indexes
-- Date: 2025-11-04

-- Drop accounts table indexes
DROP INDEX IF EXISTS idx_accounts_updated ON accounts;
DROP INDEX IF EXISTS idx_accounts_created_deleted ON accounts;
DROP INDEX IF EXISTS idx_accounts_creation_type ON accounts;
DROP INDEX IF EXISTS idx_accounts_provider ON accounts;
DROP INDEX IF EXISTS idx_accounts_type_deleted ON accounts;
DROP INDEX IF EXISTS idx_accounts_user_deleted_type ON accounts;

-- Drop users table indexes
DROP INDEX IF EXISTS idx_users_country_active ON users;
DROP INDEX IF EXISTS idx_users_state_active ON users;
DROP INDEX IF EXISTS idx_users_active_last_login ON users;
DROP INDEX IF EXISTS idx_users_active_created ON users;

-- Drop full-text indexes if they were created
-- ALTER TABLE users DROP INDEX ft_idx_user_names;
-- ALTER TABLE accounts DROP INDEX ft_idx_account_name;
