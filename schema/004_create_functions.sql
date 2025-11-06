-- Migration: 004_create_functions.sql
-- Description: Create database functions for calculations and utilities
-- Date: 2025-11-04
-- Author: System
-- Dependencies: Requires users and accounts tables

DELIMITER //

-- Function: Calculate user's net worth
DROP FUNCTION IF EXISTS fn_calculate_net_worth//

CREATE FUNCTION fn_calculate_net_worth(p_user_id BIGINT)
RETURNS DECIMAL(15,2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_net_worth DECIMAL(15,2);
    
    SELECT COALESCE(SUM(balance_cents) / 100.0, 0)
    INTO v_net_worth
    FROM accounts
    WHERE user_id = p_user_id AND deleted = 0;
    
    RETURN v_net_worth;
END//

-- Function: Get account balance by type
DROP FUNCTION IF EXISTS fn_get_balance_by_type//

CREATE FUNCTION fn_get_balance_by_type(
    p_user_id BIGINT,
    p_account_type VARCHAR(50)
)
RETURNS DECIMAL(15,2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_balance DECIMAL(15,2);
    
    SELECT COALESCE(SUM(balance_cents) / 100.0, 0)
    INTO v_balance
    FROM accounts
    WHERE user_id = p_user_id 
        AND type = p_account_type 
        AND deleted = 0;
    
    RETURN v_balance;
END//

-- Function: Count user accounts
DROP FUNCTION IF EXISTS fn_count_user_accounts//

CREATE FUNCTION fn_count_user_accounts(p_user_id BIGINT)
RETURNS INT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_count INT;
    
    SELECT COUNT(*)
    INTO v_count
    FROM accounts
    WHERE user_id = p_user_id AND deleted = 0;
    
    RETURN v_count;
END//

-- Function: Calculate tax-advantaged balance
DROP FUNCTION IF EXISTS fn_calculate_tax_advantaged//

CREATE FUNCTION fn_calculate_tax_advantaged(p_user_id BIGINT)
RETURNS DECIMAL(15,2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_balance DECIMAL(15,2);
    
    SELECT COALESCE(SUM(balance_cents) / 100.0, 0)
    INTO v_balance
    FROM accounts
    WHERE user_id = p_user_id 
        AND type IN ('TAX_DEFERRED', 'TAX_FREE')
        AND deleted = 0;
    
    RETURN v_balance;
END//

-- Function: Get user's primary currency
DROP FUNCTION IF EXISTS fn_get_user_currency//

CREATE FUNCTION fn_get_user_currency(p_user_id BIGINT)
RETURNS VARCHAR(3)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_currency VARCHAR(3);
    
    SELECT currency
    INTO v_currency
    FROM users
    WHERE id = p_user_id AND active = 1;
    
    RETURN COALESCE(v_currency, 'USD');
END//

-- Function: Check if user is active
DROP FUNCTION IF EXISTS fn_is_user_active//

CREATE FUNCTION fn_is_user_active(p_user_id BIGINT)
RETURNS BOOLEAN
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_active BOOLEAN;
    
    SELECT active
    INTO v_active
    FROM users
    WHERE id = p_user_id;
    
    RETURN COALESCE(v_active, FALSE);
END//

-- Function: Calculate account diversity score (0-100)
DROP FUNCTION IF EXISTS fn_calculate_diversity_score//

CREATE FUNCTION fn_calculate_diversity_score(p_user_id BIGINT)
RETURNS INT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_cash_count INT DEFAULT 0;
    DECLARE v_taxable_count INT DEFAULT 0;
    DECLARE v_tax_deferred_count INT DEFAULT 0;
    DECLARE v_tax_free_count INT DEFAULT 0;
    DECLARE v_score INT DEFAULT 0;
    
    -- Count accounts by type
    SELECT 
        SUM(CASE WHEN type = 'CASH_EQUIVALENT' THEN 1 ELSE 0 END),
        SUM(CASE WHEN type = 'TAXABLE' THEN 1 ELSE 0 END),
        SUM(CASE WHEN type = 'TAX_DEFERRED' THEN 1 ELSE 0 END),
        SUM(CASE WHEN type = 'TAX_FREE' THEN 1 ELSE 0 END)
    INTO v_cash_count, v_taxable_count, v_tax_deferred_count, v_tax_free_count
    FROM accounts
    WHERE user_id = p_user_id AND deleted = 0;
    
    -- Calculate score (25 points per account type)
    IF v_cash_count > 0 THEN SET v_score = v_score + 25; END IF;
    IF v_taxable_count > 0 THEN SET v_score = v_score + 25; END IF;
    IF v_tax_deferred_count > 0 THEN SET v_score = v_score + 25; END IF;
    IF v_tax_free_count > 0 THEN SET v_score = v_score + 25; END IF;
    
    RETURN v_score;
END//

-- Function: Format currency amount
DROP FUNCTION IF EXISTS fn_format_currency//

CREATE FUNCTION fn_format_currency(
    p_amount_cents INT,
    p_currency_code VARCHAR(3)
)
RETURNS VARCHAR(50)
DETERMINISTIC
BEGIN
    DECLARE v_symbol VARCHAR(5);
    DECLARE v_amount DECIMAL(15,2);
    
    SET v_amount = p_amount_cents / 100.0;
    
    SET v_symbol = CASE p_currency_code
        WHEN 'USD' THEN '$'
        WHEN 'EUR' THEN '€'
        WHEN 'GBP' THEN '£'
        WHEN 'JPY' THEN '¥'
        WHEN 'INR' THEN '₹'
        WHEN 'CAD' THEN 'C$'
        WHEN 'AUD' THEN 'A$'
        ELSE CONCAT(p_currency_code, ' ')
    END;
    
    RETURN CONCAT(v_symbol, FORMAT(v_amount, 2));
END//

DELIMITER ;
