#!/bin/bash
# Build all Docker images for microservices
# Usage: ./build-all.sh [tag]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

TAG=${1:-latest}
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

# All services to build
SERVICES=("eureka-server" "api-gateway" "auth-service" "receipt-service" "listing-service" "cart-service" "settlement-service" "analytics-service" "admin-service" "user-service" "chat-service" "customer-service")

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Building All AnshShare Microservices${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "Tag: ${TAG}"
echo "Total services: ${#SERVICES[@]}"
echo "Project root: ${PROJECT_ROOT}"
echo ""

# Counter for success/failure
SUCCESS_COUNT=0
FAIL_COUNT=0
FAILED_SERVICES=()

# Start time
START_TIME=$(date +%s)

# Build each service
for SERVICE in "${SERVICES[@]}"; do
  echo -e "${YELLOW}Building ${SERVICE}...${NC}"

  cd "${PROJECT_ROOT}"

  if docker build \
    -t "anshshare/${SERVICE}:${TAG}" \
    -f "${SERVICE}/Dockerfile" \
    . > "/tmp/build-${SERVICE}.log" 2>&1; then

    echo -e "${GREEN}✓ ${SERVICE} built successfully${NC}"
    ((SUCCESS_COUNT++))
  else
    echo -e "${RED}✗ ${SERVICE} build failed${NC}"
    echo -e "${YELLOW}Check logs: /tmp/build-${SERVICE}.log${NC}"
    ((FAIL_COUNT++))
    FAILED_SERVICES+=("${SERVICE}")
  fi

  echo ""
done

# End time
END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Build Summary${NC}"
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
  echo -e "${GREEN}All services built successfully!${NC}"
  echo ""
  echo "Docker images:"
  docker images | grep "anshshare/"
fi
