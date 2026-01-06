#!/bin/bash
# Create ECR repositories for all microservices
# Usage: ./create-ecr-repos.sh [region]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

AWS_REGION=${1:-us-east-1}

SERVICES=("eureka-server" "api-gateway" "auth-service" "receipt-service" "listing-service" "cart-service" "settlement-service" "analytics-service" "admin-service" "user-service" "chat-service" "customer-service")

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Creating ECR Repositories${NC}"
echo -e "${BLUE}========================================${NC}"
echo "Region: ${AWS_REGION}"
echo "Repositories: ${#SERVICES[@]}"
echo ""

SUCCESS_COUNT=0
FAIL_COUNT=0

for SERVICE in "${SERVICES[@]}"; do
  REPO_NAME="anshshare/${SERVICE}"

  echo -e "${YELLOW}Creating repository: ${REPO_NAME}${NC}"

  if aws ecr create-repository \
    --repository-name "${REPO_NAME}" \
    --image-scanning-configuration scanOnPush=true \
    --region "${AWS_REGION}" \
    > /dev/null 2>&1; then

    echo -e "${GREEN}✓ Created ${REPO_NAME}${NC}"
    ((SUCCESS_COUNT++))

    # Set lifecycle policy to keep only recent images
    aws ecr put-lifecycle-policy \
      --repository-name "${REPO_NAME}" \
      --lifecycle-policy-text '{
        "rules": [{
          "rulePriority": 1,
          "description": "Keep last 10 images",
          "selection": {
            "tagStatus": "any",
            "countType": "imageCountMoreThan",
            "countNumber": 10
          },
          "action": {
            "type": "expire"
          }
        }]
      }' \
      --region "${AWS_REGION}" \
      > /dev/null 2>&1

  else
    # Check if it already exists
    if aws ecr describe-repositories \
      --repository-names "${REPO_NAME}" \
      --region "${AWS_REGION}" \
      > /dev/null 2>&1; then

      echo -e "${YELLOW}⚠ Repository ${REPO_NAME} already exists${NC}"
      ((SUCCESS_COUNT++))
    else
      echo -e "${RED}✗ Failed to create ${REPO_NAME}${NC}"
      ((FAIL_COUNT++))
    fi
  fi

  echo ""
done

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Summary${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}✓ Success: ${SUCCESS_COUNT}${NC}"
echo -e "${RED}✗ Failed: ${FAIL_COUNT}${NC}"
echo ""

if [ ${SUCCESS_COUNT} -gt 0 ]; then
  echo -e "${GREEN}ECR repositories are ready!${NC}"
  echo ""
  echo "Next steps:"
  echo "1. Build Docker images: cd ../../docker/scripts && ./build-all.sh"
  echo "2. Push to ECR: ./build-and-push-all.sh dev ${AWS_REGION}"
fi
