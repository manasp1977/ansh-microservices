-- AnshShare Microservices Database Setup Script
-- Run this script as PostgreSQL superuser (postgres)
-- Usage: psql -U postgres -f setup-databases.sql

-- Create databases
CREATE DATABASE auth_db;
CREATE DATABASE receipt_db;
CREATE DATABASE listing_db;
CREATE DATABASE cart_db;
CREATE DATABASE settlement_db;

-- Verify databases created
SELECT datname FROM pg_database WHERE datname IN ('auth_db', 'receipt_db', 'listing_db', 'cart_db', 'settlement_db');

-- Grant privileges (optional - if using different user)
-- GRANT ALL PRIVILEGES ON DATABASE auth_db TO your_user;
-- GRANT ALL PRIVILEGES ON DATABASE receipt_db TO your_user;
-- GRANT ALL PRIVILEGES ON DATABASE listing_db TO your_user;
-- GRANT ALL PRIVILEGES ON DATABASE cart_db TO your_user;
-- GRANT ALL PRIVILEGES ON DATABASE settlement_db TO your_user;
