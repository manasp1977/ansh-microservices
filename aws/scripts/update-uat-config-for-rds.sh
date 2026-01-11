#!/bin/bash

# Update UAT configuration to use RDS instead of containerized PostgreSQL
# This script updates the uat.properties file with the RDS endpoint

set -e

STACK_NAME="anshshare-rds-uat"
REGION="us-east-2"
CONFIG_FILE="config/environments/uat.properties"

echo "========================================="
echo "Update UAT Configuration for RDS"
echo "========================================="
echo ""

# Get RDS endpoint from CloudFormation stack
echo "Fetching RDS endpoint..."
RDS_ENDPOINT=$(aws cloudformation describe-stacks \
    --region $REGION \
    --stack-name $STACK_NAME \
    --query 'Stacks[0].Outputs[?OutputKey==`RDSEndpoint`].OutputValue' \
    --output text 2>/dev/null || echo "")

if [ -z "$RDS_ENDPOINT" ] || [ "$RDS_ENDPOINT" = "None" ]; then
    echo "ERROR: Could not find RDS endpoint from stack: $STACK_NAME"
    echo "Make sure the RDS stack has been deployed successfully"
    exit 1
fi

echo "✓ RDS Endpoint: $RDS_ENDPOINT"
echo ""

# Backup current configuration
echo "Creating backup of current configuration..."
cp $CONFIG_FILE ${CONFIG_FILE}.backup
echo "✓ Backup created: ${CONFIG_FILE}.backup"
echo ""

# Update configuration file
echo "Updating $CONFIG_FILE..."

# Update db.host to use RDS endpoint
sed -i "s|^db\.host=.*|db.host=$RDS_ENDPOINT|g" $CONFIG_FILE

echo "✓ Configuration updated"
echo ""

echo "Changes made:"
echo "  db.host: postgres -> $RDS_ENDPOINT"
echo ""

echo "========================================="
echo "✓ Configuration Update Complete!"
echo "========================================="
echo ""
echo "Next steps:"
echo "1. Review the updated configuration in $CONFIG_FILE"
echo "2. Update docker-compose.uat.yml to remove postgres service"
echo "3. Rebuild and redeploy services"
echo ""
