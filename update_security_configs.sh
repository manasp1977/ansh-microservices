#!/bin/bash

# Services to update
services=("cart-service" "settlement-service" "user-service" "wishhub-service" "chat-service" "api-gateway")

for service in "${services[@]}"; do
  config_file="$service/src/main/java/com/ansh/*/config/SecurityConfig.java"
  if ls $config_file 2>/dev/null; then
    for file in $config_file; do
      # Check if already has actuator
      if ! grep -q '"/actuator/\*\*"' "$file"; then
        # Add actuator permit before the first requestMatchers
        sed -i 's/\.requestMatchers(/\.requestMatchers("\/actuator\/\*\*").permitAll()\n                        .requestMatchers(/' "$file"
        echo "Updated: $file"
      else
        echo "Already updated: $file"
      fi
    done
  fi
done
