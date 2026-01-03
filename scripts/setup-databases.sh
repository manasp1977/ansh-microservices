#!/bin/bash
# Linux/Mac script to setup databases
# Prerequisites: PostgreSQL installed and psql in PATH

echo "Creating AnshShare databases..."

psql -U postgres -c "CREATE DATABASE auth_db;"
psql -U postgres -c "CREATE DATABASE receipt_db;"
psql -U postgres -c "CREATE DATABASE listing_db;"
psql -U postgres -c "CREATE DATABASE cart_db;"
psql -U postgres -c "CREATE DATABASE settlement_db;"

echo ""
echo "Verifying databases created..."
psql -U postgres -c "SELECT datname FROM pg_database WHERE datname IN ('auth_db', 'receipt_db', 'listing_db', 'cart_db', 'settlement_db');"

echo ""
echo "Database setup complete!"
