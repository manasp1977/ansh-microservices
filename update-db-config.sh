#!/bin/bash
# Update PostgreSQL configuration for Mac

USERNAME=$(whoami)

echo "Updating PostgreSQL username to: $USERNAME"

# Update each service's application.yml
for service in auth-service receipt-service listing-service cart-service settlement-service analytics-service; do
  CONFIG_FILE="$service/src/main/resources/application.yml"
  if [ -f "$CONFIG_FILE" ]; then
    echo "Updating $CONFIG_FILE..."
    sed -i.bak "s/username: postgres/username: $USERNAME/g" "$CONFIG_FILE"
    sed -i.bak "s/password: postgres/password: /g" "$CONFIG_FILE"
  fi
done

echo "Database configuration updated!"
