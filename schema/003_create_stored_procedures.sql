-- Migration: 003_create_stored_procedures.sql
-- Description: Create stored procedures for common operations
-- Date: 2025-11-04
-- Author: System
-- Dependencies: Requires users and accounts tables

DELIMITER //

-- Procedure: Get user portfolio summary
DROP PROCEDURE IF EXISTS sp_get_user_portfolio_summary//

CREATE PROCEDURE sp_get_user_portfolio_summary(
    IN p_user_id BIGINT
)
BEGIN
    SELECT 
        u.id AS user_id,
        u.email,
        CONCAT(u.firstname, ' ', u.lastname) AS full_name,
        COUNT(DISTINCT a.id) AS total_accounts,
        COALESCE(SUM(a.balance_cents) / 100.0, 0) AS net_worth_dollars,
        COALESCE(SUM(CASE WHEN a.type = 'CASH_EQUIVALENT' THEN a.balance_cents ELSE 0 END) / 100.0, 0) AS cash_equivalent,
        COALESCE(SUM(CASE WHEN a.type = 'TAXABLE' THEN a.balance_cents ELSE 0 END) / 100.0, 0) AS taxable,
        COALESCE(SUM(CASE WHEN a.type = 'TAX_DEFERRED' THEN a.balance_cents ELSE 0 END) / 100.0, 0) AS tax_deferred,
        COALESCE(SUM(CASE WHEN a.type = 'TAX_FREE' THEN a.balance_cents ELSE 0 END) / 100.0, 0) AS tax_free,
        u.currency
    FROM users u
    LEFT JOIN accounts a ON u.id = a.user_id AND a.deleted = 0
    WHERE u.id = p_user_id AND u.active = 1
    GROUP BY u.id, u.email, u.firstname, u.lastname, u.currency;
END//

-- Procedure: Soft delete user and all accounts
DROP PROCEDURE IF EXISTS sp_soft_delete_user//

CREATE PROCEDURE sp_soft_delete_user(
    IN p_user_id BIGINT,
    OUT p_accounts_deleted INT
)
BEGIN
    DECLARE v_user_exists INT;
    
    -- Check if user exists and is active
    SELECT COUNT(*) INTO v_user_exists
    FROM users
    WHERE id = p_user_id AND active = 1;
    
    IF v_user_exists > 0 THEN
        -- Soft delete all user accounts
        UPDATE accounts
        SET deleted = 1, updated = CURRENT_TIMESTAMP
        WHERE user_id = p_user_id AND deleted = 0;
        
        SET p_accounts_deleted = ROW_COUNT();
        
        -- Deactivate user
        UPDATE users
        SET active = 0, updated_at = CURRENT_TIMESTAMP
        WHERE id = p_user_id;
        
        SELECT CONCAT('User ', p_user_id, ' deactivated. ', p_accounts_deleted, ' accounts deleted.') AS result;
    ELSE
        SET p_accounts_deleted = 0;
        SELECT 'User not found or already inactive' AS result;
    END IF;
END//

-- Procedure: Create account with validation
DROP PROCEDURE IF EXISTS sp_create_account//

CREATE PROCEDURE sp_create_account(
    IN p_user_id BIGINT,
    IN p_name VARCHAR(255),
    IN p_type VARCHAR(50),
    IN p_balance_cents INT,
    IN p_provider VARCHAR(255),
    IN p_account_id VARCHAR(255),
    IN p_creation_type VARCHAR(50),
    OUT p_new_account_id BIGINT
)
BEGIN
    DECLARE v_user_exists INT;
    DECLARE v_valid_type INT;
    DECLARE v_valid_creation_type INT;
    
    -- Validate user exists and is active
    SELECT COUNT(*) INTO v_user_exists
    FROM users
    WHERE id = p_user_id AND active = 1;
    
    -- Validate account type
    SET v_valid_type = CASE 
        WHEN p_type IN ('CASH_EQUIVALENT', 'TAXABLE', 'TAX_DEFERRED', 'TAX_FREE') THEN 1
        ELSE 0
    END;
    
    -- Validate creation type
    SET v_valid_creation_type = CASE 
        WHEN p_creation_type IN ('MANUAL', 'PULLED') THEN 1
        ELSE 0
    END;
    
    IF v_user_exists = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'User not found or inactive';
    ELSEIF v_valid_type = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid account type';
    ELSEIF v_valid_creation_type = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid creation type';
    ELSEIF p_balance_cents < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Balance cannot be negative';
    ELSE
        -- Insert the account
        INSERT INTO accounts (
            user_id, name, type, balance_cents, provider, 
            account_id, creation_type, deleted, created, updated
        ) VALUES (
            p_user_id, p_name, p_type, COALESCE(p_balance_cents, 0), p_provider,
            p_account_id, p_creation_type, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        );
        
        SET p_new_account_id = LAST_INSERT_ID();
        
        -- Return the created account
        SELECT * FROM accounts WHERE id = p_new_account_id;
    END IF;
END//

-- Procedure: Update account balance
DROP PROCEDURE IF EXISTS sp_update_account_balance//

CREATE PROCEDURE sp_update_account_balance(
    IN p_account_id BIGINT,
    IN p_user_id BIGINT,
    IN p_new_balance_cents INT
)
BEGIN
    DECLARE v_account_exists INT;
    
    -- Validate account exists, belongs to user, and is not deleted
    SELECT COUNT(*) INTO v_account_exists
    FROM accounts
    WHERE id = p_account_id 
        AND user_id = p_user_id 
        AND deleted = 0;
    
    IF v_account_exists = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Account not found or access denied';
    ELSEIF p_new_balance_cents < 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Balance cannot be negative';
    ELSE
        UPDATE accounts
        SET balance_cents = p_new_balance_cents,
            updated = CURRENT_TIMESTAMP
        WHERE id = p_account_id;
        
        SELECT 'Balance updated successfully' AS result,
               p_new_balance_cents AS new_balance_cents,
               p_new_balance_cents / 100.0 AS new_balance_dollars;
    END IF;
END//

-- Procedure: Get accounts by type for user
DROP PROCEDURE IF EXISTS sp_get_accounts_by_type//

CREATE PROCEDURE sp_get_accounts_by_type(
    IN p_user_id BIGINT,
    IN p_account_type VARCHAR(50)
)
BEGIN
    SELECT 
        id,
        name,
        type,
        balance_cents,
        balance_cents / 100.0 AS balance_dollars,
        provider,
        account_id,
        creation_type,
        created,
        updated
    FROM accounts
    WHERE user_id = p_user_id
        AND type = p_account_type
        AND deleted = 0
    ORDER BY created DESC;
END//

DELIMITER ;
