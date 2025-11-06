#!/bin/bash
# Master migration script - Apply all migrations in order
# Date: 2025-11-04
# Usage: ./apply_all_migrations.sh

set -e  # Exit on error

# Database connection details from environment variables
DB_HOST="${DB_HOST:-43.231.232.167}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-bixan_revest_dev}"
DB_USERNAME="${DB_USERNAME:-bixan_dbuser_dev}"

# Check if password is provided
if [ -z "$DB_PASSWORD" ]; then
    echo "Error: DB_PASSWORD environment variable is not set"
    echo "Usage: DB_PASSWORD=your_password ./apply_all_migrations.sh"
    exit 1
fi

# Color codes for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Revest Database Migration Tool${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "Database: ${DB_NAME}@${DB_HOST}:${DB_PORT}"
echo "User: ${DB_USERNAME}"
echo ""

# Function to apply a migration
apply_migration() {
    local migration_file=$1
    local migration_name=$(basename "$migration_file" .sql)
    
    echo -e "${BLUE}Applying migration: ${migration_name}${NC}"
    
    if mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USERNAME}" -p"${DB_PASSWORD}" "${DB_NAME}" < "$migration_file"; then
        echo -e "${GREEN}✓ ${migration_name} applied successfully${NC}"
        return 0
    else
        echo -e "${RED}✗ ${migration_name} failed${NC}"
        return 1
    fi
}

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Migration files in order
migrations=(
    "000_create_users_table.sql"
    "001_create_accounts_table.sql"
    "002_create_views.sql"
    "003_create_stored_procedures.sql"
    "004_create_functions.sql"
    "005_create_indexes.sql"
)

echo "Starting migrations..."
echo ""

# Apply each migration
success_count=0
for migration in "${migrations[@]}"; do
    migration_path="${SCRIPT_DIR}/${migration}"
    
    if [ -f "$migration_path" ]; then
        if apply_migration "$migration_path"; then
            ((success_count++))
        else
            echo -e "${RED}Migration failed. Stopping.${NC}"
            exit 1
        fi
    else
        echo -e "${RED}Migration file not found: ${migration}${NC}"
        exit 1
    fi
    echo ""
done

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}All migrations completed successfully!${NC}"
echo -e "${GREEN}${success_count}/${#migrations[@]} migrations applied${NC}"
echo -e "${GREEN}========================================${NC}"
