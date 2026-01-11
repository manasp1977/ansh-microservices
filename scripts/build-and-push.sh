#!/bin/bash

# Build and Push Docker Images Script
# This script builds all microservices and pushes them to Docker Hub

set -e

echo "================================================"
echo "  Build and Push Docker Images"
echo "================================================"
echo ""

# Function to display colored messages
print_info() {
    echo -e "\033[0;34m[INFO]\033[0m $1"
}

print_success() {
    echo -e "\033[0;32m[SUCCESS]\033[0m $1"
}

print_error() {
    echo -e "\033[0;31m[ERROR]\033[0m $1"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if logged into Docker Hub
if ! docker info | grep -q "Username"; then
    print_info "You may need to log in to Docker Hub."
    read -p "Do you want to log in now? (y/n): " login_choice
    if [ "$login_choice" = "y" ]; then
        docker login
    fi
fi

# Prompt for push option
echo "Do you want to push images to Docker Hub after building?"
echo "  1) Build only (local images)"
echo "  2) Build and push to Docker Hub"
echo ""
read -p "Enter choice [1-2]: " push_choice

case $push_choice in
    1)
        PUSH_IMAGES=false
        print_info "Will build images locally only"
        ;;
    2)
        PUSH_IMAGES=true
        print_info "Will build and push images to Docker Hub"
        ;;
    *)
        print_error "Invalid choice. Exiting."
        exit 1
        ;;
esac

# Build all Maven projects
print_info "Building all Maven projects..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    print_error "Maven build failed. Exiting."
    exit 1
fi

print_success "Maven build completed successfully!"

# Define all services
services=(
    "eureka-server"
    "api-gateway"
    "auth-service"
    "receipt-service"
    "listing-service"
    "cart-service"
    "settlement-service"
    "analytics-service"
    "admin-service"
    "user-service"
    "chat-service"
    "customer-service"
    "wishhub-service"
)

echo ""
print_info "Building Docker images for ${#services[@]} services..."
echo ""

# Build and optionally push each service
failed_services=()
for service in "${services[@]}"; do
    print_info "Building $service..."

    # Check if Dockerfile exists
    if [ ! -f "$service/Dockerfile" ]; then
        print_error "Dockerfile not found for $service. Skipping."
        failed_services+=("$service")
        continue
    fi

    # Build Docker image
    docker build -t anshshare/$service:latest -f $service/Dockerfile $service

    if [ $? -ne 0 ]; then
        print_error "Docker build failed for $service."
        failed_services+=("$service")
        continue
    fi

    print_success "✓ Built $service"

    # Push to Docker Hub if requested
    if [ "$PUSH_IMAGES" = true ]; then
        print_info "Pushing $service to Docker Hub..."
        docker push anshshare/$service:latest

        if [ $? -ne 0 ]; then
            print_error "Failed to push $service to Docker Hub."
            failed_services+=("$service")
        else
            print_success "✓ Pushed $service"
        fi
    fi

    echo ""
done

# Summary
echo "================================================"
echo "  Build Summary"
echo "================================================"
echo ""

if [ ${#failed_services[@]} -eq 0 ]; then
    print_success "All services built successfully!"

    if [ "$PUSH_IMAGES" = true ]; then
        print_success "All images pushed to Docker Hub!"
    fi
else
    print_error "Some services failed:"
    for service in "${failed_services[@]}"; do
        echo "  - $service"
    done
    exit 1
fi

echo ""
print_info "Total services: ${#services[@]}"
print_info "Successful: $((${#services[@]} - ${#failed_services[@]}))"
print_info "Failed: ${#failed_services[@]}"
echo ""

if [ "$PUSH_IMAGES" = true ]; then
    echo "================================================"
    echo "  Next Steps"
    echo "================================================"
    echo ""
    echo "Images are now available in Docker Hub."
    echo ""
    echo "To deploy:"
    echo "  1. SSH into your server"
    echo "  2. Run: docker-compose -f docker-compose.uat.yml pull"
    echo "  3. Run: docker-compose -f docker-compose.uat.yml up -d"
    echo ""
fi

print_success "Script completed!"
