#!/bin/bash

# Complete RDS Setup for UAT Environment
# This script orchestrates the entire RDS deployment process

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "========================================="
echo "AnshShare UAT - Complete RDS Setup"
echo "========================================="
echo ""
echo "This script will:"
echo "  1. Deploy RDS PostgreSQL instance (~10 minutes)"
echo "  2. Initialize databases"
echo "  3. Update UAT configuration"
echo "  4. Remove PostgreSQL from docker-compose"
echo ""
echo "Prerequisites:"
echo "  ✓ AWS CLI configured"
echo "  ✓ PostgreSQL client (psql) installed"
echo "  ✓ EC2 instance running in UAT"
echo ""
read -p "Continue? (y/n) " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Setup cancelled"
    exit 0
fi

echo ""
echo "========================================="
echo "Step 1/4: Deploying RDS Instance"
echo "========================================="
echo ""

cd "$PROJECT_ROOT"

if ! bash "$SCRIPT_DIR/deploy-rds-uat.sh"; then
    echo ""
    echo "ERROR: RDS deployment failed"
    exit 1
fi

echo ""
echo "Waiting for RDS instance to become available..."
echo "This may take 5-10 minutes..."
echo ""

STACK_NAME="anshshare-rds-uat"
REGION="us-east-2"

# Wait for RDS to be available
MAX_WAIT=600  # 10 minutes
ELAPSED=0
INTERVAL=30

while [ $ELAPSED -lt $MAX_WAIT ]; do
    STATUS=$(aws rds describe-db-instances \
        --db-instance-identifier anshshare-db-uat \
        --region $REGION \
        --query 'DBInstances[0].DBInstanceStatus' \
        --output text 2>/dev/null || echo "not-found")

    if [ "$STATUS" = "available" ]; then
        echo "✓ RDS instance is available"
        break
    fi

    echo "  Current status: $STATUS (waiting ${INTERVAL}s...)"
    sleep $INTERVAL
    ELAPSED=$((ELAPSED + INTERVAL))
done

if [ "$STATUS" != "available" ]; then
    echo ""
    echo "WARNING: RDS instance did not become available within 10 minutes"
    echo "Please check AWS Console and run the remaining steps manually:"
    echo "  1. Wait for RDS to be available"
    echo "  2. Run: bash aws/scripts/init-rds-databases.sh"
    echo "  3. Run: bash aws/scripts/update-uat-config-for-rds.sh"
    echo "  4. Run: bash aws/scripts/remove-postgres-from-docker-compose.sh"
    exit 1
fi

echo ""
echo "========================================="
echo "Step 2/4: Initializing Databases"
echo "========================================="
echo ""

if ! bash "$SCRIPT_DIR/init-rds-databases.sh"; then
    echo ""
    echo "ERROR: Database initialization failed"
    echo "You can retry this step later with:"
    echo "  bash aws/scripts/init-rds-databases.sh"
    exit 1
fi

echo ""
echo "========================================="
echo "Step 3/4: Updating UAT Configuration"
echo "========================================="
echo ""

if ! bash "$SCRIPT_DIR/update-uat-config-for-rds.sh"; then
    echo ""
    echo "ERROR: Configuration update failed"
    exit 1
fi

echo ""
echo "========================================="
echo "Step 4/4: Removing PostgreSQL Container"
echo "========================================="
echo ""

if ! bash "$SCRIPT_DIR/remove-postgres-from-docker-compose.sh"; then
    echo ""
    echo "ERROR: Docker Compose update failed"
    exit 1
fi

echo ""
echo "========================================="
echo "✓ RDS Setup Complete!"
echo "========================================="
echo ""

# Get RDS endpoint
RDS_ENDPOINT=$(aws cloudformation describe-stacks \
    --region $REGION \
    --stack-name $STACK_NAME \
    --query 'Stacks[0].Outputs[?OutputKey==`RDSEndpoint`].OutputValue' \
    --output text)

echo "Summary:"
echo "  ✓ RDS instance deployed and available"
echo "  ✓ All 11 databases created"
echo "  ✓ UAT configuration updated"
echo "  ✓ PostgreSQL removed from docker-compose"
echo ""
echo "RDS Details:"
echo "  Endpoint: $RDS_ENDPOINT"
echo "  Port: 5432"
echo "  Username: anshshare_admin"
echo "  Databases: auth_db, receipt_db, listing_db, cart_db, settlement_db,"
echo "             analytics_db, admin_db, user_db, chat_db, customer_db, wishhub_db"
echo ""
echo "Next Steps:"
echo "1. SSH to your EC2 instance"
echo "2. Pull the latest docker images (if you rebuilt them)"
echo "3. Stop and restart services:"
echo "     cd ansh-microservices"
echo "     docker-compose -f docker-compose.uat.yml down"
echo "     docker-compose -f docker-compose.uat.yml up -d"
echo "4. Verify services can connect to RDS:"
echo "     docker logs auth-service 2>&1 | grep -i flyway"
echo "5. Test the application to ensure data persistence"
echo ""
echo "Backups created:"
echo "  - config/environments/uat.properties.backup"
echo "  - docker-compose.uat.yml.backup"
echo ""
echo "To monitor RDS:"
echo "  aws rds describe-db-instances --db-instance-identifier anshshare-db-uat --region us-east-2"
echo ""
echo "For more information, see: aws/RDS_DEPLOYMENT_GUIDE.md"
echo ""
