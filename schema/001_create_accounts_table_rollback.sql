-- Rollback: 001_create_accounts_table.sql
-- Description: Rollback script to drop accounts table
-- Date: 2025-11-04

-- Drop constraints first (if they exist)
ALTER TABLE accounts DROP CONSTRAINT IF EXISTS chk_balance_non_negative;
ALTER TABLE accounts DROP CONSTRAINT IF EXISTS chk_creation_type;
ALTER TABLE accounts DROP CONSTRAINT IF EXISTS chk_account_type;
ALTER TABLE accounts DROP CONSTRAINT IF EXISTS fk_accounts_user_id;

-- Drop the accounts table
DROP TABLE IF EXISTS accounts;
