#!/bin/bash
# Run this script on EC2 to initialize RDS databases

set -e

RDS_ENDPOINT="anshshare-db-uat.cri426askc5s.us-east-2.rds.amazonaws.com"
DB_PASSWORD="AnshShare2024!"

echo "========================================="
echo "Initialize RDS Databases from EC2"
echo "========================================="
echo ""

# Install PostgreSQL client if not present
if ! command -v psql &> /dev/null; then
    echo "Installing PostgreSQL client..."
    sudo yum install -y postgresql15
fi

export PGPASSWORD=$DB_PASSWORD

# Test connection
echo "Testing connection to RDS..."
if ! psql -h $RDS_ENDPOINT -U anshshare_admin -d postgres -c "SELECT version();" &>/dev/null; then
    echo "ERROR: Cannot connect to RDS"
    echo "Please check:"
    echo "  1. RDS instance is running"
    echo "  2. Security group allows EC2 -> RDS on port 5432"
    exit 1
fi

echo "✓ Connection successful"
echo ""

# Create databases
DATABASES=(
    "auth_db"
    "receipt_db"
    "listing_db"
    "cart_db"
    "settlement_db"
    "analytics_db"
    "admin_db"
    "user_db"
    "chat_db"
    "customer_db"
    "wishhub_db"
)

echo "Creating databases..."
for db in "${DATABASES[@]}"; do
    echo -n "  Creating $db... "

    # Check if database exists
    DB_EXISTS=$(psql -h $RDS_ENDPOINT -U anshshare_admin -d postgres \
        -tAc "SELECT 1 FROM pg_database WHERE datname='$db'")

    if [ "$DB_EXISTS" = "1" ]; then
        echo "already exists"
    else
        psql -h $RDS_ENDPOINT -U anshshare_admin -d postgres \
            -c "CREATE DATABASE $db;" &>/dev/null
        echo "created ✓"
    fi
done

echo ""
echo "========================================="
echo "✓ Database Initialization Complete!"
echo "========================================="
echo ""
echo "All databases created successfully."
echo "You can now restart the services."
echo ""

unset PGPASSWORD
