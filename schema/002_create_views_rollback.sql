-- Rollback: 002_create_views.sql
-- Description: Drop all created views
-- Date: 2025-11-04

DROP VIEW IF EXISTS v_account_distribution_by_state;
DROP VIEW IF EXISTS v_user_activity_stats;
DROP VIEW IF EXISTS v_account_details;
DROP VIEW IF EXISTS v_user_account_summary;
