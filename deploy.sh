#!/bin/bash

# Interactive Deployment Script for Ansh Microservices
# Supports local, uat, and prod environments

set -e

echo "================================================"
echo "  Ansh Microservices - Deployment Script"
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

print_warning() {
    echo -e "\033[0;33m[WARNING]\033[0m $1"
}

# Prompt for environment selection
echo "Select deployment environment:"
echo "  1) local   - Local development environment"
echo "  2) uat     - UAT environment (AWS EC2)"
echo "  3) prod    - Production environment (Future)"
echo ""
read -p "Enter choice [1-3]: " env_choice

case $env_choice in
    1)
        ENVIRONMENT="local"
        COMPOSE_FILE="docker-compose.local.yml"
        SPRING_PROFILE="local"
        print_info "Selected: LOCAL environment"
        ;;
    2)
        ENVIRONMENT="uat"
        COMPOSE_FILE="docker-compose.uat.yml"
        SPRING_PROFILE="uat"
        print_info "Selected: UAT environment"
        ;;
    3)
        ENVIRONMENT="prod"
        COMPOSE_FILE="docker-compose.prod.yml"
        SPRING_PROFILE="prod"
        print_info "Selected: PRODUCTION environment"
        print_warning "Deploying to PRODUCTION. This action requires extra caution!"
        read -p "Are you sure you want to deploy to PRODUCTION? (yes/no): " confirm
        if [ "$confirm" != "yes" ]; then
            print_error "Deployment cancelled."
            exit 1
        fi
        ;;
    *)
        print_error "Invalid choice. Exiting."
        exit 1
        ;;
esac

echo ""
print_info "Environment: $ENVIRONMENT"
print_info "Docker Compose File: $COMPOSE_FILE"
print_info "Spring Profile: $SPRING_PROFILE"
echo ""

# Check if docker-compose file exists
if [ ! -f "$COMPOSE_FILE" ]; then
    print_error "Docker Compose file not found: $COMPOSE_FILE"
    exit 1
fi

# Prompt for deployment action
echo "Select deployment action:"
echo "  1) Build and deploy services"
echo "  2) Deploy existing images"
echo "  3) Stop services"
echo "  4) View logs"
echo "  5) Rebuild specific service"
echo ""
read -p "Enter choice [1-5]: " action_choice

case $action_choice in
    1)
        print_info "Building and deploying all services..."

        # Build all services
        print_info "Building Maven projects..."
        mvn clean package -DskipTests

        if [ $? -ne 0 ]; then
            print_error "Maven build failed. Exiting."
            exit 1
        fi

        print_success "Maven build completed successfully"

        # Build Docker images
        print_info "Building Docker images..."

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

        for service in "${services[@]}"; do
            print_info "Building $service..."
            docker build -t anshshare/$service:latest -f $service/Dockerfile $service

            if [ $? -ne 0 ]; then
                print_error "Docker build failed for $service. Exiting."
                exit 1
            fi
        done

        print_success "All Docker images built successfully"

        # Deploy with docker-compose
        print_info "Deploying services with Docker Compose..."
        docker-compose -f $COMPOSE_FILE up -d

        if [ $? -ne 0 ]; then
            print_error "Docker Compose deployment failed. Exiting."
            exit 1
        fi

        print_success "Deployment completed successfully!"
        print_info "Services are starting up. Use 'docker-compose -f $COMPOSE_FILE logs -f' to view logs."
        ;;

    2)
        print_info "Deploying existing images..."
        docker-compose -f $COMPOSE_FILE up -d

        if [ $? -ne 0 ]; then
            print_error "Docker Compose deployment failed."
            exit 1
        fi

        print_success "Deployment completed successfully!"
        ;;

    3)
        print_info "Stopping services..."
        docker-compose -f $COMPOSE_FILE down

        if [ $? -ne 0 ]; then
            print_error "Failed to stop services."
            exit 1
        fi

        print_success "Services stopped successfully!"
        ;;

    4)
        print_info "Viewing logs..."
        docker-compose -f $COMPOSE_FILE logs -f
        ;;

    5)
        echo ""
        read -p "Enter service name to rebuild (e.g., auth-service): " service_name

        print_info "Rebuilding $service_name..."

        # Build Maven project
        print_info "Building Maven project for $service_name..."
        mvn clean package -DskipTests -pl $service_name -am

        if [ $? -ne 0 ]; then
            print_error "Maven build failed for $service_name."
            exit 1
        fi

        # Build Docker image
        print_info "Building Docker image for $service_name..."
        docker build -t anshshare/$service_name:latest -f $service_name/Dockerfile $service_name

        if [ $? -ne 0 ]; then
            print_error "Docker build failed for $service_name."
            exit 1
        fi

        # Restart the service
        print_info "Restarting $service_name..."
        docker-compose -f $COMPOSE_FILE up -d --no-deps --build $service_name

        if [ $? -ne 0 ]; then
            print_error "Failed to restart $service_name."
            exit 1
        fi

        print_success "$service_name rebuilt and restarted successfully!"
        ;;

    *)
        print_error "Invalid choice. Exiting."
        exit 1
        ;;
esac

echo ""
print_success "Script execution completed!"
echo ""

# Display helpful information
if [ "$action_choice" = "1" ] || [ "$action_choice" = "2" ]; then
    echo "================================================"
    echo "  Useful Commands"
    echo "================================================"
    echo ""
    echo "View logs:          docker-compose -f $COMPOSE_FILE logs -f"
    echo "View specific logs: docker-compose -f $COMPOSE_FILE logs -f <service-name>"
    echo "Stop services:      docker-compose -f $COMPOSE_FILE down"
    echo "Restart service:    docker-compose -f $COMPOSE_FILE restart <service-name>"
    echo "View status:        docker-compose -f $COMPOSE_FILE ps"
    echo ""

    if [ "$ENVIRONMENT" = "local" ]; then
        echo "API Gateway: http://localhost:8080"
        echo "Eureka:      http://localhost:8761"
    elif [ "$ENVIRONMENT" = "uat" ]; then
        echo "API Gateway: http://3.16.24.182:8080"
        echo "Frontend:    https://www.splitsavers.com"
    fi

    echo ""
fi
