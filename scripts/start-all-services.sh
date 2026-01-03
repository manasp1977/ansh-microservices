#!/bin/bash
# Linux/Mac script to start all microservices
# Run this from ansh-microservices directory

echo "========================================"
echo " AnshShare Microservices Startup"
echo "========================================"
echo ""

echo "Step 1: Starting Eureka Server (Service Discovery)..."
cd eureka-server
mvn spring-boot:run > ../logs/eureka.log 2>&1 &
EUREKA_PID=$!
cd ..

echo "Waiting 30 seconds for Eureka to start..."
sleep 30

echo ""
echo "Step 2: Starting Business Services..."

cd auth-service
mvn spring-boot:run > ../logs/auth.log 2>&1 &
cd ..
sleep 5

cd receipt-service
mvn spring-boot:run > ../logs/receipt.log 2>&1 &
cd ..
sleep 5

cd listing-service
mvn spring-boot:run > ../logs/listing.log 2>&1 &
cd ..
sleep 5

cd cart-service
mvn spring-boot:run > ../logs/cart.log 2>&1 &
cd ..
sleep 5

cd settlement-service
mvn spring-boot:run > ../logs/settlement.log 2>&1 &
cd ..
sleep 5

cd analytics-service
mvn spring-boot:run > ../logs/analytics.log 2>&1 &
cd ..
sleep 5

cd admin-service
mvn spring-boot:run > ../logs/admin.log 2>&1 &
cd ..
sleep 5

echo ""
echo "Step 3: Starting API Gateway..."
echo "Waiting 10 seconds for services to register..."
sleep 10

cd api-gateway
mvn spring-boot:run > ../logs/gateway.log 2>&1 &
cd ..

echo ""
echo "========================================"
echo " All services are starting..."
echo "========================================"
echo ""
echo "Logs are in logs/ directory"
echo ""
echo "Eureka Dashboard: http://localhost:8761"
echo "API Gateway: http://localhost:8080"
echo ""
echo "Wait 1-2 minutes for all services to register with Eureka"
echo ""
echo "To view logs: tail -f logs/*.log"
echo ""
