#!/bin/bash
# Tag and push Docker images to AWS ECR
# Usage: ./push-to-ecr.sh <service-name> <local-tag> <ecr-tag> [region] [account-id]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check parameters
if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ]; then
  echo -e "${RED}Error: Missing required parameters${NC}"
  echo "Usage: ./push-to-ecr.sh <service-name> <local-tag> <ecr-tag> [region] [account-id]"
  echo "Example: ./push-to-ecr.sh auth-service latest dev-20260104-abc123"
  exit 1
fi

SERVICE_NAME=$1
LOCAL_TAG=$2
ECR_TAG=$3
AWS_REGION=${4:-us-east-1}
AWS_ACCOUNT_ID=${5}

# Get AWS Account ID if not provided
if [ -z "$AWS_ACCOUNT_ID" ]; then
  AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text 2>/dev/null || echo "")

  if [ -z "$AWS_ACCOUNT_ID" ]; then
    echo -e "${RED}Error: Could not determine AWS Account ID${NC}"
    exit 1
  fi
fi

ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
ECR_REPO="${ECR_REGISTRY}/anshshare/${SERVICE_NAME}"
LOCAL_IMAGE="anshshare/${SERVICE_NAME}:${LOCAL_TAG}"

echo -e "${GREEN}Pushing ${SERVICE_NAME} to ECR${NC}"
echo "Local image: ${LOCAL_IMAGE}"
echo "ECR repository: ${ECR_REPO}:${ECR_TAG}"
echo "----------------------------------------"

# Check if local image exists
if ! docker images --format "{{.Repository}}:{{.Tag}}" | grep -q "^${LOCAL_IMAGE}$"; then
  echo -e "${RED}Error: Local image ${LOCAL_IMAGE} not found${NC}"
  echo "Please build the image first using build-service.sh or build-all.sh"
  exit 1
fi

# Ensure logged into ECR
echo "Ensuring ECR login..."
aws ecr get-login-password --region "${AWS_REGION}" | \
  docker login --username AWS --password-stdin "${ECR_REGISTRY}" > /dev/null 2>&1

# Tag for ECR
echo "Tagging image for ECR..."
docker tag "${LOCAL_IMAGE}" "${ECR_REPO}:${ECR_TAG}"

# Push to ECR
echo "Pushing to ECR..."
docker push "${ECR_REPO}:${ECR_TAG}"

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✓ Successfully pushed ${SERVICE_NAME}:${ECR_TAG} to ECR${NC}"
  echo ""
  echo "Image URI: ${ECR_REPO}:${ECR_TAG}"

  # Also tag and push as 'latest' if not already latest
  if [ "${ECR_TAG}" != "latest" ]; then
    echo ""
    echo "Also tagging as 'latest'..."
    docker tag "${LOCAL_IMAGE}" "${ECR_REPO}:latest"
    docker push "${ECR_REPO}:latest"
    echo -e "${GREEN}✓ Also pushed as latest${NC}"
  fi
else
  echo -e "${RED}✗ Failed to push ${SERVICE_NAME} to ECR${NC}"
  exit 1
fi
