# Database Schema Quick Reference

## Table Overview

### users
- Primary user table with Firebase authentication
- Fields: id, firebase_id, email, firstname, lastname, phone, state, country, currency, active, first_login, last_login, created_at, updated_at, role_id

### accounts
- Financial accounts owned by users
- Fields: id, user_id (FK), name, type, balance_cents, provider, account_id, creation_type, deleted, created, updated
- Account Types: CASH_EQUIVALENT, TAXABLE, TAX_DEFERRED, TAX_FREE
- Creation Types: MANUAL, PULLED

## Views

### v_user_account_summary
Complete user profile with account summary and balances by type.

### v_account_details
Account information joined with user details for reporting.

### v_user_activity_stats
User engagement metrics and activity status tracking.

### v_account_distribution_by_state
Geographic analysis of accounts and balances.

## Stored Procedures

### sp_get_user_portfolio_summary(user_id)
Returns complete portfolio breakdown for a user.

**Example:**
```sql
CALL sp_get_user_portfolio_summary(123);
```

### sp_soft_delete_user(user_id, OUT accounts_deleted)
Deactivate user and soft delete all associated accounts.

**Example:**
```sql
CALL sp_soft_delete_user(123, @count);
SELECT @count;
```

### sp_create_account(user_id, name, type, balance_cents, provider, account_id, creation_type, OUT new_account_id)
Create new account with comprehensive validation.

**Example:**
```sql
CALL sp_create_account(123, 'Savings', 'CASH_EQUIVALENT', 100000, 'Chase', 'ACC123', 'MANUAL', @id);
```

### sp_update_account_balance(account_id, user_id, new_balance_cents)
Update account balance with ownership validation.

**Example:**
```sql
CALL sp_update_account_balance(1, 123, 150000);
```

### sp_get_accounts_by_type(user_id, account_type)
Retrieve all accounts of specific type for a user.

**Example:**
```sql
CALL sp_get_accounts_by_type(123, 'TAX_DEFERRED');
```

## Functions

### fn_calculate_net_worth(user_id) → DECIMAL
Calculate total net worth across all active accounts.

**Example:**
```sql
SELECT fn_calculate_net_worth(123);
```

### fn_get_balance_by_type(user_id, account_type) → DECIMAL
Get total balance for specific account type.

**Example:**
```sql
SELECT fn_get_balance_by_type(123, 'TAXABLE');
```

### fn_count_user_accounts(user_id) → INT
Count active accounts for user.

**Example:**
```sql
SELECT fn_count_user_accounts(123);
```

### fn_calculate_tax_advantaged(user_id) → DECIMAL
Calculate combined tax-deferred and tax-free balance.

**Example:**
```sql
SELECT fn_calculate_tax_advantaged(123);
```

### fn_get_user_currency(user_id) → VARCHAR(3)
Get user's preferred currency code.

**Example:**
```sql
SELECT fn_get_user_currency(123);
```

### fn_is_user_active(user_id) → BOOLEAN
Check if user account is active.

**Example:**
```sql
SELECT fn_is_user_active(123);
```

### fn_calculate_diversity_score(user_id) → INT
Calculate portfolio diversity score (0-100, 25 points per account type).

**Example:**
```sql
SELECT fn_calculate_diversity_score(123);
```

### fn_format_currency(amount_cents, currency_code) → VARCHAR
Format amount with currency symbol.

**Example:**
```sql
SELECT fn_format_currency(123456, 'USD'); -- Returns: $1,234.56
```

## Common Query Patterns

### Get User Portfolio Dashboard
```sql
SELECT 
    email,
    fn_calculate_net_worth(id) AS net_worth,
    fn_get_balance_by_type(id, 'CASH_EQUIVALENT') AS cash,
    fn_get_balance_by_type(id, 'TAXABLE') AS taxable,
    fn_get_balance_by_type(id, 'TAX_DEFERRED') AS tax_deferred,
    fn_get_balance_by_type(id, 'TAX_FREE') AS tax_free,
    fn_calculate_diversity_score(id) AS diversity
FROM users
WHERE id = 123;
```

### Find High Net Worth Users
```sql
SELECT 
    u.email,
    fn_calculate_net_worth(u.id) AS net_worth,
    fn_count_user_accounts(u.id) AS accounts
FROM users u
WHERE u.active = 1
HAVING net_worth > 100000
ORDER BY net_worth DESC;
```

### Account Type Distribution
```sql
SELECT 
    type,
    COUNT(*) AS count,
    SUM(balance_cents) / 100.0 AS total_balance
FROM accounts
WHERE deleted = 0
GROUP BY type;
```

### User Activity Report
```sql
SELECT 
    activity_status,
    COUNT(*) AS user_count,
    AVG(total_assets_dollars) AS avg_assets
FROM v_user_activity_stats
GROUP BY activity_status;
```

## Data Validation Rules

### Users Table
- Email must be valid format (checked by constraint)
- Country must be in approved list (USA, CAN, GBR, AUS, IND, DEU, FRA, JPN, CHN, BRA)
- Currency must be in approved list (USD, CAD, GBP, EUR, AUD, INR, JPY, CNY, BRL)

### Accounts Table
- Account type must be: CASH_EQUIVALENT, TAXABLE, TAX_DEFERRED, TAX_FREE
- Creation type must be: MANUAL, PULLED
- Balance must be non-negative
- Foreign key enforces user existence

## Performance Tips

1. Use views for complex reporting queries
2. Leverage composite indexes for multi-column filters
3. Use stored procedures for multi-step operations
4. Use functions for calculated fields in SELECT statements
5. Always filter by `deleted = 0` for accounts
6. Always filter by `active = 1` for users
