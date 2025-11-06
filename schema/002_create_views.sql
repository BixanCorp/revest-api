-- Migration: 002_create_views.sql
-- Description: Create database views for common queries
-- Date: 2025-11-04
-- Author: System
-- Dependencies: Requires users and accounts tables

-- View: Active users with account summary
DROP VIEW IF EXISTS v_user_account_summary;

CREATE VIEW v_user_account_summary AS
SELECT 
    u.id AS user_id,
    u.email,
    u.firstname,
    u.lastname,
    CONCAT(u.firstname, ' ', u.lastname) AS full_name,
    u.state,
    u.country,
    u.currency,
    u.last_login,
    u.created_at AS user_created_at,
    COUNT(DISTINCT a.id) AS total_accounts,
    SUM(CASE WHEN a.type = 'CASH_EQUIVALENT' THEN 1 ELSE 0 END) AS cash_accounts,
    SUM(CASE WHEN a.type = 'TAXABLE' THEN 1 ELSE 0 END) AS taxable_accounts,
    SUM(CASE WHEN a.type = 'TAX_DEFERRED' THEN 1 ELSE 0 END) AS tax_deferred_accounts,
    SUM(CASE WHEN a.type = 'TAX_FREE' THEN 1 ELSE 0 END) AS tax_free_accounts,
    COALESCE(SUM(a.balance_cents), 0) AS total_balance_cents,
    COALESCE(SUM(a.balance_cents) / 100.0, 0) AS total_balance_dollars,
    COALESCE(SUM(CASE WHEN a.type = 'CASH_EQUIVALENT' THEN a.balance_cents ELSE 0 END) / 100.0, 0) AS cash_balance_dollars,
    COALESCE(SUM(CASE WHEN a.type = 'TAXABLE' THEN a.balance_cents ELSE 0 END) / 100.0, 0) AS taxable_balance_dollars,
    COALESCE(SUM(CASE WHEN a.type = 'TAX_DEFERRED' THEN a.balance_cents ELSE 0 END) / 100.0, 0) AS tax_deferred_balance_dollars,
    COALESCE(SUM(CASE WHEN a.type = 'TAX_FREE' THEN a.balance_cents ELSE 0 END) / 100.0, 0) AS tax_free_balance_dollars
FROM users u
LEFT JOIN accounts a ON u.id = a.user_id AND a.deleted = 0
WHERE u.active = 1
GROUP BY u.id, u.email, u.firstname, u.lastname, u.state, u.country, u.currency, u.last_login, u.created_at;

-- View: Account details with user information
DROP VIEW IF EXISTS v_account_details;

CREATE VIEW v_account_details AS
SELECT 
    a.id AS account_id,
    a.name AS account_name,
    a.type AS account_type,
    a.balance_cents,
    a.balance_cents / 100.0 AS balance_dollars,
    a.provider,
    a.account_id AS provider_account_id,
    a.creation_type,
    a.created AS account_created,
    a.updated AS account_updated,
    u.id AS user_id,
    u.email AS user_email,
    u.firstname AS user_firstname,
    u.lastname AS user_lastname,
    CONCAT(u.firstname, ' ', u.lastname) AS user_full_name,
    u.currency AS user_currency
FROM accounts a
INNER JOIN users u ON a.user_id = u.id
WHERE a.deleted = 0 AND u.active = 1;

-- View: User activity statistics
DROP VIEW IF EXISTS v_user_activity_stats;

CREATE VIEW v_user_activity_stats AS
SELECT 
    u.id AS user_id,
    u.email,
    CONCAT(u.firstname, ' ', u.lastname) AS full_name,
    u.state,
    u.country,
    u.created_at AS registration_date,
    u.first_login,
    u.last_login,
    DATEDIFF(CURDATE(), u.created_at) AS days_since_registration,
    CASE 
        WHEN u.last_login IS NULL THEN 'Never Logged In'
        WHEN DATEDIFF(CURDATE(), u.last_login) = 0 THEN 'Active Today'
        WHEN DATEDIFF(CURDATE(), u.last_login) <= 7 THEN 'Active This Week'
        WHEN DATEDIFF(CURDATE(), u.last_login) <= 30 THEN 'Active This Month'
        WHEN DATEDIFF(CURDATE(), u.last_login) <= 90 THEN 'Active This Quarter'
        ELSE 'Inactive'
    END AS activity_status,
    COUNT(DISTINCT a.id) AS total_accounts,
    COALESCE(SUM(a.balance_cents) / 100.0, 0) AS total_assets_dollars
FROM users u
LEFT JOIN accounts a ON u.id = a.user_id AND a.deleted = 0
WHERE u.active = 1
GROUP BY u.id, u.email, u.firstname, u.lastname, u.state, u.country, 
         u.created_at, u.first_login, u.last_login;

-- View: Account type distribution by state
DROP VIEW IF EXISTS v_account_distribution_by_state;

CREATE VIEW v_account_distribution_by_state AS
SELECT 
    u.state,
    u.country,
    COUNT(DISTINCT u.id) AS total_users,
    COUNT(DISTINCT a.id) AS total_accounts,
    SUM(CASE WHEN a.type = 'CASH_EQUIVALENT' THEN 1 ELSE 0 END) AS cash_accounts,
    SUM(CASE WHEN a.type = 'TAXABLE' THEN 1 ELSE 0 END) AS taxable_accounts,
    SUM(CASE WHEN a.type = 'TAX_DEFERRED' THEN 1 ELSE 0 END) AS tax_deferred_accounts,
    SUM(CASE WHEN a.type = 'TAX_FREE' THEN 1 ELSE 0 END) AS tax_free_accounts,
    COALESCE(SUM(a.balance_cents) / 100.0, 0) AS total_balance_dollars,
    COALESCE(AVG(a.balance_cents) / 100.0, 0) AS avg_account_balance_dollars
FROM users u
LEFT JOIN accounts a ON u.id = a.user_id AND a.deleted = 0
WHERE u.active = 1
GROUP BY u.state, u.country;
