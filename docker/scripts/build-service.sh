#!/bin/bash
# Build a single Docker image for a microservice
# Usage: ./build-service.sh <service-name> [tag]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if service name is provided
if [ -z "$1" ]; then
  echo -e "${RED}Error: Service name is required${NC}"
  echo "Usage: ./build-service.sh <service-name> [tag]"
  echo "Example: ./build-service.sh auth-service latest"
  exit 1
fi

SERVICE_NAME=$1
TAG=${2:-latest}
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

# List of valid services
VALID_SERVICES=("eureka-server" "api-gateway" "auth-service" "receipt-service" "listing-service" "cart-service" "settlement-service" "analytics-service" "admin-service" "user-service" "chat-service" "customer-service")

# Check if service is valid
if [[ ! " ${VALID_SERVICES[@]} " =~ " ${SERVICE_NAME} " ]]; then
  echo -e "${RED}Error: Invalid service name '${SERVICE_NAME}'${NC}"
  echo "Valid services: ${VALID_SERVICES[@]}"
  exit 1
fi

# Check if Dockerfile exists
if [ ! -f "${PROJECT_ROOT}/${SERVICE_NAME}/Dockerfile" ]; then
  echo -e "${RED}Error: Dockerfile not found for ${SERVICE_NAME}${NC}"
  exit 1
fi

echo -e "${GREEN}Building Docker image for ${SERVICE_NAME}:${TAG}${NC}"
echo "Project root: ${PROJECT_ROOT}"
echo "----------------------------------------"

# Build the Docker image
cd "${PROJECT_ROOT}"
docker build \
  -t "anshshare/${SERVICE_NAME}:${TAG}" \
  -f "${SERVICE_NAME}/Dockerfile" \
  .

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✓ Successfully built ${SERVICE_NAME}:${TAG}${NC}"
  echo ""
  echo "Image details:"
  docker images "anshshare/${SERVICE_NAME}:${TAG}"
else
  echo -e "${RED}✗ Failed to build ${SERVICE_NAME}:${TAG}${NC}"
  exit 1
fi
