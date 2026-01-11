#!/bin/bash
set -e

DB_PASSWORD="AnshShare2024!"
STACK_NAME="anshshare-rds-uat"
TEMPLATE_FILE="aws/cloudformation/rds-postgres-uat.yaml"
REGION="us-east-2"
INSTANCE_ID="i-01e398e89cc02e3d0"

echo "Getting VPC and subnet information..."
VPC_ID=$(aws ec2 describe-instances --region $REGION --instance-ids $INSTANCE_ID --query 'Reservations[0].Instances[0].VpcId' --output text)
SG_ID=$(aws ec2 describe-instances --region $REGION --instance-ids $INSTANCE_ID --query 'Reservations[0].Instances[0].SecurityGroups[0].GroupId' --output text)
SUBNETS=$(aws ec2 describe-subnets --region $REGION --filters "Name=vpc-id,Values=$VPC_ID" --query 'Subnets[*].SubnetId' --output text)
SUBNET_1=$(echo $SUBNETS | awk '{print $1}')
SUBNET_2=$(echo $SUBNETS | awk '{print $2}')

echo "Deployment Configuration:"
echo "  VPC: $VPC_ID"
echo "  Subnet 1: $SUBNET_1"
echo "  Subnet 2: $SUBNET_2"
echo "  Security Group: $SG_ID"
echo ""

echo "Deploying RDS CloudFormation stack..."
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

echo ""
echo "RDS deployment initiated!"
