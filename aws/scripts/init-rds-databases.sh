#!/bin/bash

# Initialize databases in RDS PostgreSQL for UAT environment
# This script creates all required databases for the microservices

set -e

STACK_NAME="anshshare-rds-uat"
REGION="us-east-2"

echo "========================================="
echo "AnshShare UAT - RDS Database Initialization"
echo "========================================="
echo ""

# Check if psql is installed
if ! command -v psql &> /dev/null; then
    echo "ERROR: psql is not installed"
    echo "Please install PostgreSQL client:"
    echo "  Ubuntu/Debian: sudo apt-get install postgresql-client"
    echo "  macOS: brew install postgresql"
    echo "  Windows: Download from https://www.postgresql.org/download/windows/"
    exit 1
fi

# Get RDS endpoint from CloudFormation stack
echo "Fetching RDS endpoint..."
RDS_ENDPOINT=$(aws cloudformation describe-stacks \
    --region $REGION \
    --stack-name $STACK_NAME \
    --query 'Stacks[0].Outputs[?OutputKey==`RDS Endpoint`].OutputValue' \
    --output text 2>/dev/null || echo "")

if [ -z "$RDS_ENDPOINT" ] || [ "$RDS_ENDPOINT" = "None" ]; then
    echo "ERROR: Could not find RDS endpoint from stack: $STACK_NAME"
    echo "Make sure the RDS stack has been deployed successfully"
    exit 1
fi

echo "✓ RDS Endpoint: $RDS_ENDPOINT"
echo ""

# Prompt for database password
read -sp "Enter RDS master password: " DB_PASSWORD
echo ""

if [ -z "$DB_PASSWORD" ]; then
    echo "ERROR: Password cannot be empty"
    exit 1
fi

# Test connection
echo "Testing database connection..."
export PGPASSWORD=$DB_PASSWORD

if ! psql -h $RDS_ENDPOINT -U anshshare_admin -d postgres -c "SELECT version();" &>/dev/null; then
    echo "ERROR: Could not connect to RDS"
    echo "Please verify:"
    echo "  1. RDS instance is available (check AWS Console)"
    echo "  2. Password is correct"
    echo "  3. Security group allows connections from your IP"
    exit 1
fi

echo "✓ Database connection successful"
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
        echo "created"
    fi
done

echo ""
echo "========================================="
echo "✓ Database Initialization Complete!"
echo "========================================="
echo ""
echo "Created databases:"
for db in "${DATABASES[@]}"; do
    echo "  - $db"
done
echo ""
echo "Connection details:"
echo "  Host: $RDS_ENDPOINT"
echo "  Port: 5432"
echo "  Username: anshshare_admin"
echo "  Password: (the password you entered)"
echo ""
echo "Next steps:"
echo "1. Update config/environments/uat.properties with RDS endpoint"
echo "2. Update docker-compose.uat.yml to remove postgres service"
echo "3. Redeploy services with new database configuration"
echo ""

# Unset password
unset PGPASSWORD
