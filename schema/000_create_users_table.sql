-- Migration: 000_create_users_table.sql
-- Description: Create users table for managing application users
-- Date: 2025-11-04
-- Author: System
-- Dependencies: None (base table)

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Unique user identifier',
    firebase_id VARCHAR(255) UNIQUE NOT NULL COMMENT 'Firebase authentication UID',
    email VARCHAR(255) UNIQUE NOT NULL COMMENT 'User email address',
    firstname VARCHAR(255) COMMENT 'User first name',
    lastname VARCHAR(255) COMMENT 'User last name',
    phone VARCHAR(63) COMMENT 'User phone number',
    state VARCHAR(63) NOT NULL COMMENT 'User state (e.g., CA, NY)',
    country VARCHAR(3) NOT NULL DEFAULT 'USA' COMMENT 'User country code (ISO 3166-1 alpha-3)',
    currency VARCHAR(3) DEFAULT 'USD' COMMENT 'User preferred currency (ISO 4217)',
    active TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'User account status: 0=inactive, 1=active',
    first_login DATETIME NULL COMMENT 'Timestamp of first login',
    last_login DATETIME NULL COMMENT 'Timestamp of most recent login',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation timestamp',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record last update timestamp',
    role_id INT NULL COMMENT 'User role identifier (future use)',
    
    -- Indexes for performance
    INDEX idx_firebase_id (firebase_id) COMMENT 'Index for Firebase authentication lookup',
    INDEX idx_email (email) COMMENT 'Index for email lookup',
    INDEX idx_active (active) COMMENT 'Index for filtering active users',
    INDEX idx_state (state) COMMENT 'Index for filtering by state',
    INDEX idx_country (country) COMMENT 'Index for filtering by country',
    INDEX idx_created_at (created_at) COMMENT 'Index for sorting by creation date',
    INDEX idx_last_login (last_login) COMMENT 'Index for user activity analysis'
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Application users with Firebase authentication';

-- Add constraint to ensure email format
ALTER TABLE users ADD CONSTRAINT chk_email_format 
    CHECK (email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$');

-- Add constraint to validate country codes (common codes)
ALTER TABLE users ADD CONSTRAINT chk_country_code 
    CHECK (country IN ('USA', 'CAN', 'GBR', 'AUS', 'IND', 'DEU', 'FRA', 'JPN', 'CHN', 'BRA'));

-- Add constraint to validate currency codes
ALTER TABLE users ADD CONSTRAINT chk_currency_code 
    CHECK (currency IN ('USD', 'CAD', 'GBP', 'EUR', 'AUD', 'INR', 'JPY', 'CNY', 'BRL'));
