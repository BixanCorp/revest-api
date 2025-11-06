-- Rollback: 003_create_stored_procedures.sql
-- Description: Drop all created stored procedures
-- Date: 2025-11-04

DROP PROCEDURE IF EXISTS sp_get_accounts_by_type;
DROP PROCEDURE IF EXISTS sp_update_account_balance;
DROP PROCEDURE IF EXISTS sp_create_account;
DROP PROCEDURE IF EXISTS sp_soft_delete_user;
DROP PROCEDURE IF EXISTS sp_get_user_portfolio_summary;
