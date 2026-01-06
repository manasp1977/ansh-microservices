#!/bin/bash
# Login to AWS ECR
# Usage: ./ecr-login.sh [region] [account-id]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default values
AWS_REGION=${1:-us-east-1}
AWS_ACCOUNT_ID=${2}

# If account ID not provided, try to get it from AWS CLI
if [ -z "$AWS_ACCOUNT_ID" ]; then
  echo -e "${YELLOW}Account ID not provided, fetching from AWS...${NC}"
  AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text 2>/dev/null || echo "")

  if [ -z "$AWS_ACCOUNT_ID" ]; then
    echo -e "${RED}Error: Could not determine AWS Account ID${NC}"
    echo "Please provide it as parameter or configure AWS CLI"
    echo "Usage: ./ecr-login.sh [region] [account-id]"
    exit 1
  fi
fi

ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

echo -e "${GREEN}Logging into AWS ECR${NC}"
echo "Region: ${AWS_REGION}"
echo "Account ID: ${AWS_ACCOUNT_ID}"
echo "Registry: ${ECR_REGISTRY}"
echo "----------------------------------------"

# Login to ECR
aws ecr get-login-password --region "${AWS_REGION}" | \
  docker login --username AWS --password-stdin "${ECR_REGISTRY}"

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✓ Successfully logged into ECR${NC}"

  # Export variables for other scripts
  export AWS_REGION
  export AWS_ACCOUNT_ID
  export ECR_REGISTRY

  echo ""
  echo "Environment variables set:"
  echo "  AWS_REGION=${AWS_REGION}"
  echo "  AWS_ACCOUNT_ID=${AWS_ACCOUNT_ID}"
  echo "  ECR_REGISTRY=${ECR_REGISTRY}"
else
  echo -e "${RED}✗ Failed to login to ECR${NC}"
  exit 1
fi
