#!/bin/bash
# Create ECS Task Definitions for all microservices

set -e

AWS_ACCOUNT_ID=778830035443
AWS_REGION=us-east-1
RDS_ENDPOINT="anshshare-db.cadk8wwwq70w.us-east-1.rds.amazonaws.com"
TASK_EXECUTION_ROLE="arn:aws:iam::778830035443:role/anshshare-infrastructure-ECSTaskExecutionRole-lfPxc21oBWMB"

echo "Creating ECS Task Definitions..."
echo ""

# Service configuration: name:port:memory
SERVICES=(
  "eureka-server:8761:128"
  "api-gateway:8080:128"
  "auth-service:8081:128"
  "receipt-service:8082:128"
  "listing-service:8083:128"
  "cart-service:8084:128"
  "settlement-service:8085:128"
  "analytics-service:8086:128"
  "admin-service:8087:128"
  "user-service:8088:128"
  "chat-service:8089:128"
  "customer-service:8090:128"
)

for SERVICE_CONFIG in "${SERVICES[@]}"; do
  IFS=':' read -r SERVICE_NAME PORT MEMORY <<< "$SERVICE_CONFIG"

  echo "Creating task definition for $SERVICE_NAME..."

  # Create task definition JSON
  cat > /tmp/${SERVICE_NAME}-task.json <<EOF
{
  "family": "${SERVICE_NAME}",
  "networkMode": "bridge",
  "requiresCompatibilities": ["EC2"],
  "cpu": "256",
  "memory": "${MEMORY}",
  "executionRoleArn": "${TASK_EXECUTION_ROLE}",
  "containerDefinitions": [
    {
      "name": "${SERVICE_NAME}",
      "image": "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/anshshare/${SERVICE_NAME}:latest",
      "cpu": 256,
      "memory": ${MEMORY},
      "essential": true,
      "portMappings": [
        {
          "containerPort": ${PORT},
          "hostPort": ${PORT},
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "aws"
        },
        {
          "name": "SPRING_DATASOURCE_URL",
          "value": "jdbc:postgresql://${RDS_ENDPOINT}:5432/${SERVICE_NAME//-/_}_db"
        },
        {
          "name": "SPRING_DATASOURCE_USERNAME",
          "value": "anshshare_admin"
        },
        {
          "name": "SPRING_DATASOURCE_PASSWORD",
          "value": "AnshShare2024!"
        },
        {
          "name": "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE",
          "value": "http://44.200.50.2:8761/eureka/"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/anshshare/${SERVICE_NAME}",
          "awslogs-region": "${AWS_REGION}",
          "awslogs-stream-prefix": "ecs",
          "awslogs-create-group": "true"
        }
      }
    }
  ]
}
EOF

  # Register task definition
  aws ecs register-task-definition \
    --cli-input-json file:///tmp/${SERVICE_NAME}-task.json \
    --region ${AWS_REGION} > /dev/null

  echo "✓ Task definition created for ${SERVICE_NAME}"
  rm /tmp/${SERVICE_NAME}-task.json
done

echo ""
echo "✅ All task definitions created successfully!"
echo ""
echo "Task definitions registered:"
for SERVICE_CONFIG in "${SERVICES[@]}"; do
  IFS=':' read -r SERVICE_NAME PORT MEMORY <<< "$SERVICE_CONFIG"
  echo "  - ${SERVICE_NAME}"
done
