# Database Schema Migration Scripts

This directory contains incremental database schema migration files for the Revest API.

## Naming Convention

Migration files follow the pattern: `{version}_{description}.sql`
- **version**: Three-digit number (e.g., 001, 002, 003)
- **description**: Snake_case description of the change

Rollback files: `{version}_{description}_rollback.sql`

## Migration Files

### 000_create_users_table.sql
**Date:** 2025-11-04  
**Description:** Creates the users table for managing application users with Firebase authentication.  
**Dependencies:** None (base table)

**Features:**
- User profile management with Firebase authentication
- Email and Firebase UID uniqueness constraints
- Location tracking (state, country)
- Currency preference
- Login timestamp tracking
- Email format validation
- Country and currency code validation

**Rollback:** Use `000_create_users_table_rollback.sql`

---

### 001_create_accounts_table.sql
**Date:** 2025-11-04  
**Description:** Creates the accounts table for managing user financial accounts.  
**Dependencies:** Requires `users` table to exist.

**Features:**
- User financial account management
- Soft delete support
- Balance stored in cents (avoids floating-point errors)
- Foreign key to users table with CASCADE delete
- Support for multiple account types: CASH_EQUIVALENT, TAXABLE, TAX_DEFERRED, TAX_FREE
- Creation type tracking: MANUAL or PULLED
- Comprehensive indexing for performance

**Rollback:** Use `001_create_accounts_table_rollback.sql`

---

### 002_create_views.sql
**Date:** 2025-11-04  
**Description:** Creates database views for common reporting and analytics queries.  
**Dependencies:** Requires `users` and `accounts` tables

**Views Created:**
- `v_user_account_summary` - User profile with account counts and balances by type
- `v_account_details` - Account information joined with user details
- `v_user_activity_stats` - User activity metrics and engagement status
- `v_account_distribution_by_state` - Geographic distribution of accounts and balances

**Rollback:** Use `002_create_views_rollback.sql`

---

### 003_create_stored_procedures.sql
**Date:** 2025-11-04  
**Description:** Creates stored procedures for complex operations.  
**Dependencies:** Requires `users` and `accounts` tables

**Procedures Created:**
- `sp_get_user_portfolio_summary` - Get complete portfolio breakdown for a user
- `sp_soft_delete_user` - Deactivate user and soft delete all accounts
- `sp_create_account` - Create account with validation
- `sp_update_account_balance` - Update account balance with validation
- `sp_get_accounts_by_type` - Get all accounts of specific type for user

**Rollback:** Use `003_create_stored_procedures_rollback.sql`

---

### 004_create_functions.sql
**Date:** 2025-11-04  
**Description:** Creates utility functions for calculations and data retrieval.  
**Dependencies:** Requires `users` and `accounts` tables

**Functions Created:**
- `fn_calculate_net_worth` - Calculate user's total net worth
- `fn_get_balance_by_type` - Get total balance for specific account type
- `fn_count_user_accounts` - Count active accounts for user
- `fn_calculate_tax_advantaged` - Calculate total tax-advantaged balance
- `fn_get_user_currency` - Get user's preferred currency
- `fn_is_user_active` - Check if user account is active
- `fn_calculate_diversity_score` - Calculate portfolio diversity (0-100)
- `fn_format_currency` - Format amount with currency symbol

**Rollback:** Use `004_create_functions_rollback.sql`

---

### 005_create_indexes.sql
**Date:** 2025-11-04  
**Description:** Creates additional composite indexes for query performance optimization.  
**Dependencies:** Requires `users` and `accounts` tables

**Indexes Created:**
- Composite indexes for common query patterns
- Date-based query optimization indexes
- Optional full-text search indexes (commented)

**Rollback:** Use `005_create_indexes_rollback.sql`

## Applying Migrations

### Manual Application
```bash
# Apply migration
mysql -h <host> -u <username> -p <database> < schema/001_create_accounts_table.sql

# Rollback if needed
mysql -h <host> -u <username> -p <database> < schema/001_create_accounts_table_rollback.sql
```

### Using Environment Variables
```bash
# Set connection details
export DB_HOST=43.231.232.167
export DB_PORT=3306
export DB_NAME=bixan_revest_dev
export DB_USERNAME=bixan_dbuser_dev
export DB_PASSWORD=your_password

# Apply migration
mysql -h ${DB_HOST} -P ${DB_PORT} -u ${DB_USERNAME} -p${DB_PASSWORD} ${DB_NAME} < schema/001_create_accounts_table.sql
```

## Auto-Creation

**Note:** The application automatically creates tables on startup if they don't exist. These migration scripts are provided for:
- Manual database setup
- Production deployment control
- Database version tracking
- Explicit migration management
- Rollback capabilities

## Best Practices

1. **Never modify existing migration files** - Create new migrations for changes
2. **Test migrations on development environment first**
3. **Always create corresponding rollback scripts**
4. **Document dependencies between migrations**
5. **Keep migrations small and focused**
6. **Use transactions where appropriate**

## Migration History

| Version | Date       | Description                        | Status   |
|---------|------------|------------------------------------|----------|
| 000     | 2025-11-04 | Create users table                 | Applied  |
| 001     | 2025-11-04 | Create accounts table              | Applied  |
| 002     | 2025-11-04 | Create views                       | Applied  |
| 003     | 2025-11-04 | Create stored procedures           | Applied  |
| 004     | 2025-11-04 | Create functions                   | Applied  |
| 005     | 2025-11-04 | Create additional indexes          | Applied  |

## Usage Examples

### Using Views
```sql
-- Get all active users with their portfolio summary
SELECT * FROM v_user_account_summary;

-- Get account details for a specific user
SELECT * FROM v_account_details WHERE user_id = 123;

-- Analyze user activity
SELECT * FROM v_user_activity_stats WHERE activity_status = 'Active Today';

-- Geographic distribution analysis
SELECT * FROM v_account_distribution_by_state ORDER BY total_balance_dollars DESC;
```

### Using Stored Procedures
```sql
-- Get portfolio summary for a user
CALL sp_get_user_portfolio_summary(123);

-- Create a new account with validation
CALL sp_create_account(123, 'My Savings', 'CASH_EQUIVALENT', 100000, 'Chase', 'ACC-123', 'MANUAL', @new_id);
SELECT @new_id;

-- Update account balance
CALL sp_update_account_balance(1, 123, 150000);

-- Get all taxable accounts for a user
CALL sp_get_accounts_by_type(123, 'TAXABLE');

-- Soft delete a user and all accounts
CALL sp_soft_delete_user(123, @deleted_count);
SELECT @deleted_count;
```

### Using Functions
```sql
-- Calculate net worth for a user
SELECT fn_calculate_net_worth(123) AS net_worth;

-- Get balance by account type
SELECT fn_get_balance_by_type(123, 'TAX_DEFERRED') AS retirement_balance;

-- Count user's accounts
SELECT fn_count_user_accounts(123) AS total_accounts;

-- Calculate tax-advantaged balance
SELECT fn_calculate_tax_advantaged(123) AS tax_advantaged;

-- Get user's currency
SELECT fn_get_user_currency(123) AS currency;

-- Check if user is active
SELECT fn_is_user_active(123) AS is_active;

-- Calculate portfolio diversity score
SELECT fn_calculate_diversity_score(123) AS diversity_score;

-- Format currency
SELECT fn_format_currency(123456, 'USD') AS formatted_amount;

-- Combined query example
SELECT 
    u.email,
    fn_calculate_net_worth(u.id) AS net_worth,
    fn_count_user_accounts(u.id) AS accounts,
    fn_calculate_diversity_score(u.id) AS diversity,
    fn_get_user_currency(u.id) AS currency
FROM users u
WHERE u.active = 1;
```
