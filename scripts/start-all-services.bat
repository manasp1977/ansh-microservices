@echo off
REM Windows batch script to start all microservices
REM Run this from ansh-microservices directory

echo ========================================
echo  AnshShare Microservices Startup
echo ========================================
echo.

echo Step 1: Starting Eureka Server (Service Discovery)...
start "Eureka Server" cmd /k "cd eureka-server && mvn spring-boot:run"

echo Waiting 30 seconds for Eureka to start...
timeout /t 30 /nobreak

echo.
echo Step 2: Starting Business Services...

start "Auth Service" cmd /k "cd auth-service && mvn spring-boot:run"
timeout /t 5 /nobreak

start "Receipt Service" cmd /k "cd receipt-service && mvn spring-boot:run"
timeout /t 5 /nobreak

start "Listing Service" cmd /k "cd listing-service && mvn spring-boot:run"
timeout /t 5 /nobreak

start "Cart Service" cmd /k "cd cart-service && mvn spring-boot:run"
timeout /t 5 /nobreak

start "Settlement Service" cmd /k "cd settlement-service && mvn spring-boot:run"
timeout /t 5 /nobreak

start "Analytics Service" cmd /k "cd analytics-service && mvn spring-boot:run"
timeout /t 5 /nobreak

start "Admin Service" cmd /k "cd admin-service && mvn spring-boot:run"
timeout /t 5 /nobreak

echo.
echo Step 3: Starting API Gateway...
echo Waiting 10 seconds for services to register...
timeout /t 10 /nobreak

start "API Gateway" cmd /k "cd api-gateway && mvn spring-boot:run"

echo.
echo ========================================
echo  All services are starting...
echo ========================================
echo.
echo Check each window for startup status
echo.
echo Eureka Dashboard: http://localhost:8761
echo API Gateway: http://localhost:8080
echo.
echo Wait 1-2 minutes for all services to register with Eureka
echo.
pause
