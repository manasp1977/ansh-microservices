#!/bin/bash

EC2_HOST="ec2-user@ec2-3-16-24-182.us-east-2.compute.amazonaws.com"
KEY="~/Downloads/ec2-key-pair.pem"

# Services to deploy (excluding eureka-server which is healthy)
services="analytics-service receipt-service auth-service listing-service cart-service settlement-service user-service wishhub-service api-gateway admin-service customer-service chat-service"

echo "Copying JARs to EC2..."
for service in $services; do
  jar_file="$service/target/$service-1.0.0.jar"
  if [ -f "$jar_file" ]; then
    echo "Copying $service..."
    scp -i $KEY "$jar_file" "$EC2_HOST:~/" 2>/dev/null &
  fi
done
wait

echo "All JARs copied. Now rebuilding Docker images on EC2..."
