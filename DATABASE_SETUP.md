# Database Setup Instructions

## Prerequisites
- PostgreSQL installed and running
- PostgreSQL client (psql) available

## Create Databases

### Option 1: Using psql command line
```bash
psql -U postgres -f create_databases.sql
```

### Option 2: Manual creation via psql
```bash
psql -U postgres

# Then run these commands:
CREATE DATABASE auth_db;
CREATE DATABASE receipt_db;
CREATE DATABASE listing_db;
CREATE DATABASE cart_db;
CREATE DATABASE settlement_db;
CREATE DATABASE analytics_db;

# Verify:
\l
```

### Option 3: Using pgAdmin or other PostgreSQL GUI
1. Connect to your PostgreSQL server
2. Create the following databases:
   - `auth_db`
   - `receipt_db`
   - `listing_db`
   - `cart_db`
   - `settlement_db`
   - `analytics_db`

## Verify Database Creation
```bash
psql -U postgres -c "\l"
```

You should see all 6 databases listed.

## Database Credentials
The default configuration uses:
- Username: postgres
- Password: postgres
- Host: localhost
- Port: 5432

Update each service's `application.yml` if your credentials differ.
