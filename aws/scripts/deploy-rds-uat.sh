#!/bin/bash

# Deploy RDS PostgreSQL for UAT environment
# This script creates an RDS instance for the UAT environment

set -e

STACK_NAME="anshshare-rds-uat"
TEMPLATE_FILE="aws/cloudformation/rds-postgres-uat.yaml"
REGION="us-east-2"

echo "========================================="
echo "AnshShare UAT - RDS Deployment Script"
echo "========================================="
echo ""

# Check if AWS CLI is configured
if ! aws sts get-caller-identity &>/dev/null; then
    echo "ERROR: AWS CLI is not configured or credentials are invalid"
    echo "Please run 'aws configure' first"
    exit 1
fi

echo "✓ AWS CLI configured"
echo ""

# Get EC2 instance information to extract VPC and subnet details
echo "Fetching EC2 instance information..."
INSTANCE_ID=$(aws ec2 describe-instances \
    --region $REGION \
    --filters "Name=tag:Name,Values=ansh-ec2" "Name=instance-state-name,Values=running" \
    --query 'Reservations[0].Instances[0].InstanceId' \
    --output text 2>/dev/null || echo "")

if [ -z "$INSTANCE_ID" ] || [ "$INSTANCE_ID" = "None" ]; then
    echo "ERROR: Could not find running EC2 instance with tag Name=ansh-ec2"
    echo "Please make sure your EC2 instance is running"
    exit 1
fi

echo "✓ Found EC2 instance: $INSTANCE_ID"

# Get VPC ID
VPC_ID=$(aws ec2 describe-instances \
    --region $REGION \
    --instance-ids $INSTANCE_ID \
    --query 'Reservations[0].Instances[0].VpcId' \
    --output text)

echo "✓ VPC ID: $VPC_ID"

# Get Security Group ID
SG_ID=$(aws ec2 describe-instances \
    --region $REGION \
    --instance-ids $INSTANCE_ID \
    --query 'Reservations[0].Instances[0].SecurityGroups[0].GroupId' \
    --output text)

echo "✓ Security Group ID: $SG_ID"

# Get subnets in the VPC (we need 2 in different AZs for RDS)
echo "Fetching available subnets..."
SUBNETS=$(aws ec2 describe-subnets \
    --region $REGION \
    --filters "Name=vpc-id,Values=$VPC_ID" \
    --query 'Subnets[*].[SubnetId,AvailabilityZone]' \
    --output text)

# Extract first two subnets
SUBNET_1=$(echo "$SUBNETS" | awk 'NR==1 {print $1}')
SUBNET_2=$(echo "$SUBNETS" | awk 'NR==2 {print $1}')

if [ -z "$SUBNET_1" ] || [ -z "$SUBNET_2" ]; then
    echo "ERROR: Could not find 2 subnets in VPC"
    echo "Creating additional subnet..."

    # Get VPC CIDR
    VPC_CIDR=$(aws ec2 describe-vpcs \
        --region $REGION \
        --vpc-ids $VPC_ID \
        --query 'Vpcs[0].CidrBlock' \
        --output text)

    # Create second subnet in different AZ
    AZS=$(aws ec2 describe-availability-zones \
        --region $REGION \
        --query 'AvailabilityZones[*].ZoneName' \
        --output text)

    AZ_2=$(echo $AZS | awk '{print $2}')

    SUBNET_2=$(aws ec2 create-subnet \
        --region $REGION \
        --vpc-id $VPC_ID \
        --cidr-block 10.0.2.0/24 \
        --availability-zone $AZ_2 \
        --query 'Subnet.SubnetId' \
        --output text)

    aws ec2 create-tags \
        --region $REGION \
        --resources $SUBNET_2 \
        --tags Key=Name,Value=anshshare-subnet-2

    echo "✓ Created subnet: $SUBNET_2"
fi

echo "✓ Subnet 1: $SUBNET_1"
echo "✓ Subnet 2: $SUBNET_2"
echo ""

# Prompt for database password
read -sp "Enter RDS master password (min 8 characters) [default: AnshShare2024!]: " DB_PASSWORD
echo ""
DB_PASSWORD=${DB_PASSWORD:-AnshShare2024!}

# Validate password length
if [ ${#DB_PASSWORD} -lt 8 ]; then
    echo "ERROR: Password must be at least 8 characters long"
    exit 1
fi

echo ""
echo "Deployment Configuration:"
echo "  Stack Name: $STACK_NAME"
echo "  Region: $REGION"
echo "  VPC ID: $VPC_ID"
echo "  Subnet 1: $SUBNET_1"
echo "  Subnet 2: $SUBNET_2"
echo "  EC2 Security Group: $SG_ID"
echo "  Environment: uat"
echo ""
read -p "Continue with deployment? (y/n) " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Deployment cancelled"
    exit 0
fi

echo ""
echo "Deploying RDS stack..."

# Deploy CloudFormation stack
aws cloudformation deploy \
    --region $REGION \
    --stack-name $STACK_NAME \
    --template-file $TEMPLATE_FILE \
    --parameter-overrides \
        VPCId=$VPC_ID \
        Subnet1Id=$SUBNET_1 \
        Subnet2Id=$SUBNET_2 \
        DBPassword=$DB_PASSWORD \
        EC2SecurityGroupId=$SG_ID \
        Environment=uat \
    --capabilities CAPABILITY_IAM \
    --no-fail-on-empty-changeset

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================="
    echo "✓ RDS Stack Deployed Successfully!"
    echo "========================================="
    echo ""

    # Get RDS endpoint
    RDS_ENDPOINT=$(aws cloudformation describe-stacks \
        --region $REGION \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`RDSEndpoint`].OutputValue' \
        --output text)

    echo "RDS Endpoint: $RDS_ENDPOINT"
    echo ""
    echo "Next steps:"
    echo "1. Wait for RDS instance to be available (5-10 minutes)"
    echo "2. Run './aws/scripts/init-rds-databases.sh' to create all databases"
    echo "3. Update environment configuration with RDS endpoint"
    echo ""
    echo "Connection details:"
    echo "  Host: $RDS_ENDPOINT"
    echo "  Port: 5432"
    echo "  Username: anshshare_admin"
    echo "  Password: (the password you entered)"
    echo ""
else
    echo ""
    echo "ERROR: RDS deployment failed"
    exit 1
fi
