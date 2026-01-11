#!/bin/bash

# This script adds database connection overrides to docker-compose.local.yml
# Services need to connect to 'postgres' container, not 'localhost'

FILE="docker-compose.local.yml"

# Define services that need database connection
services=("auth-service" "receipt-service" "listing-service" "cart-service" "settlement-service" "analytics-service" "admin-service" "user-service" "chat-service" "customer-service" "wishhub-service")

# Create temporary file
temp_file=$(mktemp)

# Process the file
in_service=false
service_name=""

while IFS= read -r line; do
    echo "$line" >> "$temp_file"
    
    # Check if we're entering a service definition
    for svc in "${services[@]}"; do
        if [[ "$line" =~ ^[[:space:]]*$svc:[[:space:]]*$ ]]; then
            in_service=true
            service_name=$svc
            break
        fi
    done
    
    # If we find SPRING_PROFILES_ACTIVE line, add database overrides
    if [[ "$in_service" == true ]] && [[ "$line" =~ SPRING_PROFILES_ACTIVE ]]; then
        # Add database connection overrides
        db_name="${service_name//-/_}_db"
        echo "      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/${db_name}" >> "$temp_file"
        echo "      - SPRING_DATASOURCE_USERNAME=postgres" >> "$temp_file"
        echo "      - SPRING_DATASOURCE_PASSWORD=manasp1977" >> "$temp_file"
        echo "      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/" >> "$temp_file"
        in_service=false
    fi
done < "$FILE"

# Replace original file
mv "$temp_file" "$FILE"

echo "Updated docker-compose.local.yml with database connection overrides"
