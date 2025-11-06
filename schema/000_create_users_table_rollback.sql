-- Rollback: 000_create_users_table.sql
-- Description: Rollback script to drop users table
-- Date: 2025-11-04
-- WARNING: This will cascade delete all accounts and related data

-- Drop constraints first (if they exist)
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_currency_code;
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_country_code;
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_email_format;

-- Drop the users table (will cascade to accounts table)
DROP TABLE IF EXISTS users;
