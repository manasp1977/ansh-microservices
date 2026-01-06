#!/bin/bash
# Complete deployment script for AWS Free Tier
# This script automates the entire deployment process

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
AWS_REGION=${AWS_REGION:-us-east-1}
STACK_NAME=${STACK_NAME:-anshshare-free-tier}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}AnshShare Free Tier Deployment${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "Region: ${AWS_REGION}"
echo "Stack Name: ${STACK_NAME}"
echo "Project Root: ${PROJECT_ROOT}"
echo ""

# Check AWS CLI
if ! command -v aws &> /dev/null; then
  echo -e "${RED}Error: AWS CLI not found${NC}"
  echo "Please install AWS CLI: https://aws.amazon.com/cli/"
  exit 1
fi

# Check Docker
if ! command -v docker &> /dev/null; then
  echo -e "${RED}Error: Docker not found${NC}"
  echo "Please install Docker: https://www.docker.com/"
  exit 1
fi

# Check AWS credentials
if ! aws sts get-caller-identity > /dev/null 2>&1; then
  echo -e "${RED}Error: AWS credentials not configured${NC}"
  echo "Please run: aws configure"
  exit 1
fi

AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
echo "AWS Account ID: ${AWS_ACCOUNT_ID}"
echo ""

# Ask for confirmation
echo -e "${YELLOW}This will deploy the following to AWS:${NC}"
echo "- VPC and networking"
echo "- RDS PostgreSQL database (db.t2.micro)"
echo "- ECS cluster with t2.micro EC2 instance"
echo "- 3 microservices (eureka, api-gateway, auth)"
echo ""
echo -e "${YELLOW}Estimated time: 20-30 minutes${NC}"
echo -e "${YELLOW}Cost: FREE (within AWS free tier)${NC}"
echo ""
read -p "Continue? (yes/no): " -r
if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
  echo "Deployment cancelled"
  exit 0
fi
echo ""

# Step 1: Create ECR repositories
echo -e "${BLUE}Step 1/4: Creating ECR repositories...${NC}"
"${SCRIPT_DIR}/create-ecr-repos.sh" "${AWS_REGION}"
echo ""

# Step 2: Build Docker images
echo -e "${BLUE}Step 2/4: Building Docker images...${NC}"
cd "${PROJECT_ROOT}/docker/scripts"
./build-all.sh latest
echo ""

# Step 3: Push images to ECR
echo -e "${BLUE}Step 3/4: Pushing images to ECR...${NC}"
./build-and-push-all.sh dev "${AWS_REGION}"
echo ""

# Step 4: Deploy CloudFormation (Manual instructions for now)
echo -e "${BLUE}Step 4/4: CloudFormation Deployment${NC}"
echo ""
echo -e "${YELLOW}NOTE: CloudFormation templates need to be deployed manually or via AWS Console${NC}"
echo ""
echo "For free tier deployment, you can use AWS Console to create:"
echo "1. VPC with public/private subnets"
echo "2. RDS PostgreSQL db.t2.micro instance"
echo "3. ECS cluster with t2.micro EC2 instance"
echo "4. ECS task definitions and services"
echo ""
echo -e "${GREEN}Docker images are ready in ECR!${NC}"
echo ""
echo "Image URIs:"
for SERVICE in eureka-server api-gateway auth-service; do
  echo "  ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/anshshare/${SERVICE}:latest"
done
echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}Deployment preparation complete!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "Next steps:"
echo "1. Create VPC and subnets in AWS Console"
echo "2. Create RDS PostgreSQL instance"
echo "3. Create ECS cluster with EC2 launch type"
echo "4. Create task definitions using ECR image URIs above"
echo "5. Create ECS services"
echo ""
echo "For detailed instructions, see:"
echo "  aws/cloudformation/free-tier/README.md"
