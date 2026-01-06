#!/bin/bash
# Build all Docker images and push to AWS ECR
# Usage: ./build-and-push-all.sh <environment> [region]
# Example: ./build-and-push-all.sh dev us-east-1

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check parameters
if [ -z "$1" ]; then
  echo -e "${RED}Error: Environment is required${NC}"
  echo "Usage: ./build-and-push-all.sh <environment> [region]"
  echo "Example: ./build-and-push-all.sh dev us-east-1"
  echo ""
  echo "Environments: dev, staging, prod"
  exit 1
fi

ENVIRONMENT=$1
AWS_REGION=${2:-us-east-1}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Get current date and git commit SHA for tagging
DATE_TAG=$(date +%Y%m%d)
GIT_SHA=$(git rev-parse --short HEAD 2>/dev/null || echo "local")
ECR_TAG="${ENVIRONMENT}-${DATE_TAG}-${GIT_SHA}"

# Get AWS Account ID
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text 2>/dev/null || echo "")

if [ -z "$AWS_ACCOUNT_ID" ]; then
  echo -e "${RED}Error: Could not determine AWS Account ID${NC}"
  echo "Please configure AWS CLI with: aws configure"
  exit 1
fi

ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Build and Push All Services to ECR${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "Environment: ${ENVIRONMENT}"
echo "Region: ${AWS_REGION}"
echo "Tag: ${ECR_TAG}"
echo "Registry: ${ECR_REGISTRY}"
echo ""

# Start time
START_TIME=$(date +%s)

# Step 1: Login to ECR
echo -e "${YELLOW}Step 1: Logging into ECR...${NC}"
"${SCRIPT_DIR}/ecr-login.sh" "${AWS_REGION}" "${AWS_ACCOUNT_ID}"
echo ""

# Step 2: Build all images
echo -e "${YELLOW}Step 2: Building all Docker images...${NC}"
"${SCRIPT_DIR}/build-all.sh" "latest"
echo ""

# Step 3: Push all images to ECR
echo -e "${YELLOW}Step 3: Pushing all images to ECR...${NC}"
echo ""

SERVICES=("eureka-server" "api-gateway" "auth-service" "receipt-service" "listing-service" "cart-service" "settlement-service" "analytics-service" "admin-service" "user-service" "chat-service" "customer-service")

SUCCESS_COUNT=0
FAIL_COUNT=0
FAILED_SERVICES=()

for SERVICE in "${SERVICES[@]}"; do
  echo -e "${YELLOW}Pushing ${SERVICE}...${NC}"

  if "${SCRIPT_DIR}/push-to-ecr.sh" "${SERVICE}" "latest" "${ECR_TAG}" "${AWS_REGION}" "${AWS_ACCOUNT_ID}" > "/tmp/push-${SERVICE}.log" 2>&1; then
    echo -e "${GREEN}✓ ${SERVICE} pushed successfully${NC}"
    ((SUCCESS_COUNT++))
  else
    echo -e "${RED}✗ ${SERVICE} push failed${NC}"
    echo -e "${YELLOW}Check logs: /tmp/push-${SERVICE}.log${NC}"
    ((FAIL_COUNT++))
    FAILED_SERVICES+=("${SERVICE}")
  fi

  echo ""
done

# End time
END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Push Summary${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}✓ Successful: ${SUCCESS_COUNT}${NC}"
echo -e "${RED}✗ Failed: ${FAIL_COUNT}${NC}"
echo "Duration: ${DURATION}s"
echo ""

if [ ${FAIL_COUNT} -gt 0 ]; then
  echo -e "${RED}Failed services:${NC}"
  for SERVICE in "${FAILED_SERVICES[@]}"; do
    echo -e "  - ${SERVICE}"
  done
  exit 1
else
  echo -e "${GREEN}All services built and pushed successfully!${NC}"
  echo ""
  echo "All images are tagged as:"
  echo "  - ${ECR_TAG}"
  echo "  - latest"
  echo ""
  echo "Example image URI:"
  echo "  ${ECR_REGISTRY}/anshshare/auth-service:${ECR_TAG}"
fi
