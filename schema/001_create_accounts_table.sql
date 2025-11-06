-- Migration: 001_create_accounts_table.sql
-- Description: Create accounts table for managing user financial accounts
-- Date: 2025-11-04
-- Author: System
-- Dependencies: Requires users table to exist

-- Create accounts table
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique account identifier',
    user_id BIGINT NOT NULL COMMENT 'Foreign key to users table',
    name VARCHAR(255) NOT NULL COMMENT 'Account display name',
    type VARCHAR(50) NOT NULL COMMENT 'Account type: CASH_EQUIVALENT, TAXABLE, TAX_DEFERRED, TAX_FREE',
    balance_cents INT NOT NULL DEFAULT 0 COMMENT 'Account balance in cents to avoid floating-point errors',
    provider VARCHAR(255) COMMENT 'Financial institution name (e.g., Chase Bank, Vanguard)',
    account_id VARCHAR(255) COMMENT 'Provider-specific account identifier',
    creation_type VARCHAR(50) NOT NULL COMMENT 'How account was created: MANUAL or PULLED',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Soft delete flag: 0=active, 1=deleted',
    created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record last update timestamp',
    
    -- Foreign key constraint
    CONSTRAINT fk_accounts_user_id 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    
    -- Indexes for performance
    INDEX idx_user_id (user_id) COMMENT 'Index for filtering by user',
    INDEX idx_type (type) COMMENT 'Index for filtering by account type',
    INDEX idx_deleted (deleted) COMMENT 'Index for filtering active/deleted accounts',
    INDEX idx_created (created) COMMENT 'Index for sorting by creation date',
    INDEX idx_user_type (user_id, type) COMMENT 'Composite index for user-specific type queries',
    INDEX idx_user_deleted (user_id, deleted) COMMENT 'Composite index for active accounts by user'
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Financial accounts owned by users';

-- Add constraint to validate account types
ALTER TABLE accounts ADD CONSTRAINT chk_account_type 
    CHECK (type IN ('CASH_EQUIVALENT', 'TAXABLE', 'TAX_DEFERRED', 'TAX_FREE'));

-- Add constraint to validate creation types
ALTER TABLE accounts ADD CONSTRAINT chk_creation_type 
    CHECK (creation_type IN ('MANUAL', 'PULLED'));

-- Add constraint to ensure balance is non-negative
ALTER TABLE accounts ADD CONSTRAINT chk_balance_non_negative 
    CHECK (balance_cents >= 0);
