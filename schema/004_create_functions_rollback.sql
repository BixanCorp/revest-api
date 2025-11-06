-- Rollback: 004_create_functions.sql
-- Description: Drop all created functions
-- Date: 2025-11-04

DROP FUNCTION IF EXISTS fn_format_currency;
DROP FUNCTION IF EXISTS fn_calculate_diversity_score;
DROP FUNCTION IF EXISTS fn_is_user_active;
DROP FUNCTION IF EXISTS fn_get_user_currency;
DROP FUNCTION IF EXISTS fn_calculate_tax_advantaged;
DROP FUNCTION IF EXISTS fn_count_user_accounts;
DROP FUNCTION IF EXISTS fn_get_balance_by_type;
DROP FUNCTION IF EXISTS fn_calculate_net_worth;
