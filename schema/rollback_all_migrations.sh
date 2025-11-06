#!/bin/bash
# Master rollback script - Rollback all migrations in reverse order
# Date: 2025-11-04
# Usage: ./rollback_all_migrations.sh
# WARNING: This will drop all tables, views, procedures, and functions!

set -e  # Exit on error

# Database connection details from environment variables
DB_HOST="${DB_HOST:-43.231.232.167}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-bixan_revest_dev}"
DB_USERNAME="${DB_USERNAME:-bixan_dbuser_dev}"

# Check if password is provided
if [ -z "$DB_PASSWORD" ]; then
    echo "Error: DB_PASSWORD environment variable is not set"
    echo "Usage: DB_PASSWORD=your_password ./rollback_all_migrations.sh"
    exit 1
fi

# Color codes for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${RED}========================================${NC}"
echo -e "${RED}Revest Database Rollback Tool${NC}"
echo -e "${RED}========================================${NC}"
echo ""
echo -e "${YELLOW}WARNING: This will remove all database objects!${NC}"
echo "Database: ${DB_NAME}@${DB_HOST}:${DB_PORT}"
echo "User: ${DB_USERNAME}"
echo ""
read -p "Are you sure you want to proceed? (type 'yes' to confirm): " confirmation

if [ "$confirmation" != "yes" ]; then
    echo "Rollback cancelled."
    exit 0
fi

# Function to apply a rollback
apply_rollback() {
    local rollback_file=$1
    local rollback_name=$(basename "$rollback_file" _rollback.sql)
    
    echo -e "${BLUE}Rolling back: ${rollback_name}${NC}"
    
    if mysql -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USERNAME}" -p"${DB_PASSWORD}" "${DB_NAME}" < "$rollback_file"; then
        echo -e "${GREEN}✓ ${rollback_name} rolled back successfully${NC}"
        return 0
    else
        echo -e "${RED}✗ ${rollback_name} rollback failed${NC}"
        return 1
    fi
}

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Rollback files in REVERSE order
rollbacks=(
    "005_create_indexes_rollback.sql"
    "004_create_functions_rollback.sql"
    "003_create_stored_procedures_rollback.sql"
    "002_create_views_rollback.sql"
    "001_create_accounts_table_rollback.sql"
    "000_create_users_table_rollback.sql"
)

echo "Starting rollbacks..."
echo ""

# Apply each rollback
success_count=0
for rollback in "${rollbacks[@]}"; do
    rollback_path="${SCRIPT_DIR}/${rollback}"
    
    if [ -f "$rollback_path" ]; then
        if apply_rollback "$rollback_path"; then
            ((success_count++))
        else
            echo -e "${YELLOW}Rollback failed but continuing...${NC}"
        fi
    else
        echo -e "${YELLOW}Rollback file not found: ${rollback}${NC}"
    fi
    echo ""
done

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}All rollbacks completed!${NC}"
echo -e "${GREEN}${success_count}/${#rollbacks[@]} rollbacks applied${NC}"
echo -e "${GREEN}========================================${NC}"
