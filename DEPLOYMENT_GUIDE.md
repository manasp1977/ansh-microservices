# AnshShare Microservices Deployment Guide

## Overview

This guide provides comprehensive deployment instructions for the AnshShare microservices architecture across different environments.

## Table of Contents

1. [Deployment Options](#deployment-options)
2. [Local Development Deployment](#local-development-deployment)
3. [Docker Deployment](#docker-deployment)
4. [Kubernetes Deployment](#kubernetes-deployment)
5. [Production Checklist](#production-checklist)
6. [Monitoring & Logging](#monitoring--logging)
7. [Backup & Recovery](#backup--recovery)
8. [Troubleshooting](#troubleshooting)

## Deployment Options

| Environment | Use Case | Complexity | Cost |
|-------------|----------|------------|------|
| Local (Maven) | Development, Testing | Low | Free |
| Docker Compose | Local staging, Demo | Medium | Free |
| Kubernetes (Minikube) | Local K8s testing | Medium | Free |
| Kubernetes (Cloud) | Production | High | $$ |
| Cloud Native (AWS ECS, etc.) | Production | High | $$$ |

## Local Development Deployment

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- 8GB RAM minimum

### Setup Steps

See [TESTING_GUIDE.md](TESTING_GUIDE.md) for detailed local setup instructions.

**Quick Start:**

```bash
# 1. Create databases
psql -U postgres -f setup/create_databases.sql

# 2. Build all services
mvn clean install

# 3. Start services (use separate terminals)
cd eureka-server && mvn spring-boot:run &
sleep 30
cd auth-service && mvn spring-boot:run &
cd receipt-service && mvn spring-boot:run &
cd listing-service && mvn spring-boot:run &
cd cart-service && mvn spring-boot:run &
cd settlement-service && mvn spring-boot:run &
cd analytics-service && mvn spring-boot:run &
cd admin-service && mvn spring-boot:run &
cd api-gateway && mvn spring-boot:run &
```

## Docker Deployment

### Step 1: Create Dockerfiles

**auth-service/Dockerfile** (same pattern for all services):

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8081
```

### Step 2: Create Docker Compose File

**docker-compose.yml**:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14-alpine
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./setup/init-databases.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  eureka-server:
    build: ./eureka-server
    ports:
      - "8761:8761"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761"]
      interval: 30s
      timeout: 10s
      retries: 5

  auth-service:
    build: ./auth-service
    depends_on:
      postgres:
        condition: service_healthy
      eureka-server:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/auth_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
    ports:
      - "8081:8081"

  receipt-service:
    build: ./receipt-service
    depends_on:
      postgres:
        condition: service_healthy
      eureka-server:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/receipt_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
    ports:
      - "8082:8082"

  listing-service:
    build: ./listing-service
    depends_on:
      postgres:
        condition: service_healthy
      eureka-server:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/listing_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
    ports:
      - "8083:8083"

  cart-service:
    build: ./cart-service
    depends_on:
      postgres:
        condition: service_healthy
      eureka-server:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/cart_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
    ports:
      - "8084:8084"

  settlement-service:
    build: ./settlement-service
    depends_on:
      postgres:
        condition: service_healthy
      eureka-server:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/settlement_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
    ports:
      - "8085:8085"

  analytics-service:
    build: ./analytics-service
    depends_on:
      eureka-server:
        condition: service_healthy
    environment:
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
    ports:
      - "8086:8086"

  admin-service:
    build: ./admin-service
    depends_on:
      eureka-server:
        condition: service_healthy
    environment:
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
    ports:
      - "8087:8087"

  api-gateway:
    build: ./api-gateway
    depends_on:
      eureka-server:
        condition: service_healthy
    environment:
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
      JWT_SECRET: ${JWT_SECRET}
    ports:
      - "8080:8080"

volumes:
  postgres-data:
```

**setup/init-databases.sql**:

```sql
CREATE DATABASE auth_db;
CREATE DATABASE receipt_db;
CREATE DATABASE listing_db;
CREATE DATABASE cart_db;
CREATE DATABASE settlement_db;
```

### Step 3: Build and Run

```bash
# Build all Docker images
docker-compose build

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Step 4: Verify Deployment

```bash
# Check all containers running
docker-compose ps

# Verify Eureka
curl http://localhost:8761

# Test API Gateway
curl http://localhost:8080/api/auth/login
```

## Kubernetes Deployment

### Prerequisites

- kubectl
- Kubernetes cluster (Minikube, GKE, EKS, AKS)
- Helm (optional but recommended)

### Step 1: Create Kubernetes Manifests

**k8s/namespace.yaml**:

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: anshare
```

**k8s/postgres-deployment.yaml**:

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: postgres-pvc
  namespace: anshare
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: anshare
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
      - name: postgres
        image: postgres:14-alpine
        env:
        - name: POSTGRES_USER
          value: postgres
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: postgres-secret
              key: password
        ports:
        - containerPort: 5432
        volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql/data
      volumes:
      - name: postgres-storage
        persistentVolumeClaim:
          claimName: postgres-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: postgres
  namespace: anshare
spec:
  selector:
    app: postgres
  ports:
  - port: 5432
    targetPort: 5432
```

**k8s/eureka-deployment.yaml**:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: eureka-server
  namespace: anshare
spec:
  replicas: 1
  selector:
    matchLabels:
      app: eureka-server
  template:
    metadata:
      labels:
        app: eureka-server
    spec:
      containers:
      - name: eureka-server
        image: anshare/eureka-server:latest
        ports:
        - containerPort: 8761
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: k8s
---
apiVersion: v1
kind: Service
metadata:
  name: eureka-server
  namespace: anshare
spec:
  selector:
    app: eureka-server
  ports:
  - port: 8761
    targetPort: 8761
  type: ClusterIP
```

**k8s/auth-service-deployment.yaml**:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
  namespace: anshare
spec:
  replicas: 2
  selector:
    matchLabels:
      app: auth-service
  template:
    metadata:
      labels:
        app: auth-service
    spec:
      containers:
      - name: auth-service
        image: anshare/auth-service:latest
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_DATASOURCE_URL
          value: jdbc:postgresql://postgres:5432/auth_db
        - name: SPRING_DATASOURCE_USERNAME
          value: postgres
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: postgres-secret
              key: password
        - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
          value: http://eureka-server:8761/eureka/
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: auth-service
  namespace: anshare
spec:
  selector:
    app: auth-service
  ports:
  - port: 8081
    targetPort: 8081
  type: ClusterIP
```

**k8s/api-gateway-deployment.yaml**:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
  namespace: anshare
spec:
  replicas: 2
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
      - name: api-gateway
        image: anshare/api-gateway:latest
        ports:
        - containerPort: 8080
        env:
        - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
          value: http://eureka-server:8761/eureka/
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: secret
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: api-gateway
  namespace: anshare
spec:
  selector:
    app: api-gateway
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

### Step 2: Create Secrets

```bash
# Create namespace
kubectl apply -f k8s/namespace.yaml

# Create postgres secret
kubectl create secret generic postgres-secret \
  --from-literal=password='your-secure-password' \
  -n anshare

# Create JWT secret
kubectl create secret generic jwt-secret \
  --from-literal=secret='your-jwt-secret-base64-encoded' \
  -n anshare
```

### Step 3: Build and Push Docker Images

```bash
# Build all images
docker build -t anshare/eureka-server:latest ./eureka-server
docker build -t anshare/api-gateway:latest ./api-gateway
docker build -t anshare/auth-service:latest ./auth-service
docker build -t anshare/receipt-service:latest ./receipt-service
docker build -t anshare/listing-service:latest ./listing-service
docker build -t anshare/cart-service:latest ./cart-service
docker build -t anshare/settlement-service:latest ./settlement-service
docker build -t anshare/analytics-service:latest ./analytics-service
docker build -t anshare/admin-service:latest ./admin-service

# Push to registry (use your registry)
docker tag anshare/eureka-server:latest your-registry/eureka-server:latest
docker push your-registry/eureka-server:latest
# ... repeat for all services
```

### Step 4: Deploy to Kubernetes

```bash
# Deploy infrastructure
kubectl apply -f k8s/postgres-deployment.yaml
kubectl apply -f k8s/eureka-deployment.yaml

# Wait for Eureka to be ready
kubectl wait --for=condition=ready pod -l app=eureka-server -n anshare --timeout=300s

# Deploy services
kubectl apply -f k8s/auth-service-deployment.yaml
kubectl apply -f k8s/receipt-service-deployment.yaml
kubectl apply -f k8s/listing-service-deployment.yaml
kubectl apply -f k8s/cart-service-deployment.yaml
kubectl apply -f k8s/settlement-service-deployment.yaml
kubectl apply -f k8s/analytics-service-deployment.yaml
kubectl apply -f k8s/admin-service-deployment.yaml

# Deploy API Gateway
kubectl apply -f k8s/api-gateway-deployment.yaml
```

### Step 5: Verify Deployment

```bash
# Check all pods
kubectl get pods -n anshare

# Check services
kubectl get svc -n anshare

# Get API Gateway external IP
kubectl get svc api-gateway -n anshare

# View logs
kubectl logs -f deployment/auth-service -n anshare
```

## Production Checklist

### Security

- [ ] Enable HTTPS/TLS for all services
- [ ] Use Spring Cloud Config for secrets management
- [ ] Implement API rate limiting at gateway
- [ ] Add CORS configuration
- [ ] Enable database encryption at rest
- [ ] Use secure passwords for all databases
- [ ] Implement network policies (Kubernetes)
- [ ] Add Web Application Firewall (WAF)

### High Availability

- [ ] Run at least 2 replicas of each service
- [ ] Configure pod anti-affinity (Kubernetes)
- [ ] Set up database replication
- [ ] Configure load balancer health checks
- [ ] Implement circuit breaker (Resilience4j)
- [ ] Add retry logic for Feign clients

### Performance

- [ ] Configure connection pooling
- [ ] Add Redis cache layer
- [ ] Enable database indexing
- [ ] Configure JVM heap size appropriately
- [ ] Add CDN for static assets
- [ ] Implement database read replicas

### Monitoring & Logging

- [ ] Add Spring Boot Actuator endpoints
- [ ] Configure Prometheus metrics
- [ ] Set up Grafana dashboards
- [ ] Implement distributed tracing (Zipkin/Jaeger)
- [ ] Configure centralized logging (ELK stack)
- [ ] Set up alerting (Prometheus Alertmanager)

### Backup & Recovery

- [ ] Configure automated database backups
- [ ] Test restore procedures
- [ ] Document disaster recovery plan
- [ ] Set up off-site backup storage
- [ ] Configure point-in-time recovery

### CI/CD

- [ ] Set up automated builds (Jenkins/GitLab CI)
- [ ] Implement automated testing
- [ ] Configure blue-green deployment
- [ ] Add canary releases
- [ ] Implement rollback procedures

## Monitoring & Logging

### Spring Boot Actuator

Add to each service **pom.xml**:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Add to **application.yml**:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### Prometheus Configuration

**prometheus.yml**:

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'eureka-server'
    static_configs:
      - targets: ['eureka-server:8761']
  - job_name: 'auth-service'
    static_configs:
      - targets: ['auth-service:8081']
  # ... add all services
```

### Grafana Dashboards

Import Spring Boot dashboard (ID: 4701) for monitoring:
- JVM metrics
- HTTP requests
- Database connections
- Feign client calls

## Backup & Recovery

### Database Backup

```bash
# Automated daily backup script
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups"

databases=("auth_db" "receipt_db" "listing_db" "cart_db" "settlement_db")

for db in "${databases[@]}"; do
    pg_dump -U postgres -h localhost $db | gzip > $BACKUP_DIR/${db}_${DATE}.sql.gz
done

# Keep only last 30 days
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete
```

### Restore Procedure

```bash
# Restore single database
gunzip -c /backups/auth_db_20240115_120000.sql.gz | psql -U postgres -d auth_db

# Restore all databases
for backup in /backups/*_latest.sql.gz; do
    db=$(basename $backup | cut -d_ -f1)
    gunzip -c $backup | psql -U postgres -d $db
done
```

## Troubleshooting

### Common Issues

#### 1. Services Not Registering with Eureka

**Symptom**: Services start but don't appear in Eureka dashboard

**Solution**:
```bash
# Check network connectivity
curl http://eureka-server:8761

# Check Eureka client configuration
kubectl logs deployment/auth-service -n anshare | grep Eureka
```

#### 2. Database Connection Pool Exhausted

**Symptom**: "Connection pool exhausted" errors

**Solution**:
```yaml
# Increase pool size in application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
```

#### 3. High Latency on Feign Calls

**Symptom**: Slow response times

**Solution**:
```yaml
# Increase Feign timeouts
feign:
  client:
    config:
      default:
        connectTimeout: 10000
        readTimeout: 10000
```

---

**Deployment Guide Complete!** Ready for production deployment.
