# Manual AWS Deployment Guide for AnshShare Microservices

## 🎯 Overview

This guide provides step-by-step instructions to manually deploy your 12 microservices to AWS using the **cheapest possible configuration**.

**Important:** Your account does NOT have Free Tier eligibility, so we'll use the most cost-effective paid tier options.

## 💰 Cost Estimates

### Option 1: Full Cloud Deployment (~$23-25/month)
- EC2 t3.micro: ~$7/month
- RDS db.t3.micro: ~$15/month
- Data Transfer: ~$1/month
- **Total: ~$23-25/month**

### Option 2: Single Instance Deployment (~$7-10/month) **RECOMMENDED**
- EC2 t3.small (2GB RAM): ~$15/month OR t3.micro (1GB RAM): ~$7/month
- PostgreSQL installed on EC2 (no separate RDS)
- **Total: ~$7-15/month**

This guide focuses on **Option 2** (cheapest option).

---

## 📋 Prerequisites

### Required Tools
```bash
# AWS CLI
aws --version  # Should be 2.x

# Docker
docker --version

# Git
git --version
```

### AWS Account Setup
1. ✅ AWS Account created (778830035443)
2. ✅ AWS CLI configured with credentials
3. ✅ Docker images built locally (already done)

---

## 🚀 Deployment Steps

### Step 1: Create EC2 Key Pair

```bash
# Create new key pair
aws ec2 create-key-pair \
  --key-name anshshare-manual-key \
  --region us-east-1 \
  --query 'KeyMaterial' \
  --output text > anshshare-manual-key.pem

# Set permissions
chmod 400 anshshare-manual-key.pem

# Verify
ls -la anshshare-manual-key.pem
```

### Step 2: Create Security Group

```bash
# Create VPC (if you don't have default VPC)
VPC_ID=$(aws ec2 describe-vpcs --region us-east-1 --filters "Name=isDefault,Values=true" --query 'Vpcs[0].VpcId' --output text)
echo "Using VPC: $VPC_ID"

# Create Security Group
SG_ID=$(aws ec2 create-security-group \
  --group-name anshshare-sg \
  --description "Security group for AnshShare microservices" \
  --vpc-id $VPC_ID \
  --region us-east-1 \
  --query 'GroupId' \
  --output text)

echo "Security Group created: $SG_ID"

# Allow SSH (port 22)
aws ec2 authorize-security-group-ingress \
  --group-id $SG_ID \
  --protocol tcp \
  --port 22 \
  --cidr 0.0.0.0/0 \
  --region us-east-1

# Allow Eureka (port 8761)
aws ec2 authorize-security-group-ingress \
  --group-id $SG_ID \
  --protocol tcp \
  --port 8761 \
  --cidr 0.0.0.0/0 \
  --region us-east-1

# Allow API Gateway (port 8080)
aws ec2 authorize-security-group-ingress \
  --group-id $SG_ID \
  --protocol tcp \
  --port 8080 \
  --cidr 0.0.0.0/0 \
  --region us-east-1

# Allow all microservices (ports 8081-8090)
for PORT in {8081..8090}; do
  aws ec2 authorize-security-group-ingress \
    --group-id $SG_ID \
    --protocol tcp \
    --port $PORT \
    --cidr 0.0.0.0/0 \
    --region us-east-1
done

# Allow PostgreSQL (port 5432) - for internal use
aws ec2 authorize-security-group-ingress \
  --group-id $SG_ID \
  --protocol tcp \
  --port 5432 \
  --cidr 0.0.0.0/0 \
  --region us-east-1

echo "✓ Security group configured"
```

### Step 3: Launch EC2 Instance

```bash
# Get latest Amazon Linux 2023 AMI
AMI_ID=$(aws ec2 describe-images \
  --owners amazon \
  --filters "Name=name,Values=al2023-ami-2023.*-x86_64" "Name=state,Values=available" \
  --query 'Images | sort_by(@, &CreationDate) | [-1].ImageId' \
  --output text \
  --region us-east-1)

echo "Using AMI: $AMI_ID"

# Launch EC2 instance (t3.small for better performance, or t3.micro for cheaper)
# Recommended: t3.small (2GB RAM) = ~$15/month
# Budget option: t3.micro (1GB RAM) = ~$7/month (may run out of memory)

INSTANCE_ID=$(aws ec2 run-instances \
  --image-id $AMI_ID \
  --instance-type t3.small \
  --key-name anshshare-manual-key \
  --security-group-ids $SG_ID \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=anshshare-server}]' \
  --block-device-mappings 'DeviceName=/dev/xvda,Ebs={VolumeSize=30,VolumeType=gp3}' \
  --region us-east-1 \
  --query 'Instances[0].InstanceId' \
  --output text)

echo "Instance launched: $INSTANCE_ID"
echo "Waiting for instance to be running..."

# Wait for instance to be running
aws ec2 wait instance-running --instance-ids $INSTANCE_ID --region us-east-1

# Get public IP
PUBLIC_IP=$(aws ec2 describe-instances \
  --instance-ids $INSTANCE_ID \
  --region us-east-1 \
  --query 'Reservations[0].Instances[0].PublicIpAddress' \
  --output text)

echo "✓ Instance is running!"
echo "Public IP: $PUBLIC_IP"
echo ""
echo "Save this IP address: $PUBLIC_IP"
```

### Step 4: Setup EC2 Instance

```bash
# SSH into instance
ssh -i anshshare-manual-key.pem ec2-user@$PUBLIC_IP

# Once logged in, run the following commands:
```

**Run these commands on the EC2 instance:**

```bash
# Update system
sudo yum update -y

# Install Docker
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -a -G docker ec2-user

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Install PostgreSQL
sudo yum install -y postgresql15 postgresql15-server
sudo postgresql-setup --initdb
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Configure PostgreSQL
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'AnshShare2024!';"
sudo -u postgres psql -c "CREATE USER anshshare_admin WITH PASSWORD 'AnshShare2024!' CREATEDB;"

# Update PostgreSQL to allow network connections
sudo sed -i "s/#listen_addresses = 'localhost'/listen_addresses = '*'/g" /var/lib/pgsql/data/postgresql.conf
echo "host    all             all             0.0.0.0/0               md5" | sudo tee -a /var/lib/pgsql/data/pg_hba.conf
sudo systemctl restart postgresql

# Create databases for each service
for db in auth_service listing_service cart_service receipt_service settlement_service analytics_service admin_service user_service chat_service customer_service; do
  sudo -u postgres psql -c "CREATE DATABASE ${db}_db OWNER anshshare_admin;"
  echo "✓ Created ${db}_db"
done

# Verify databases
sudo -u postgres psql -l

echo "✓ EC2 instance setup complete!"
```

**Exit SSH and return to your local machine:**
```bash
exit
```

### Step 5: Push Docker Images to EC2

We'll save Docker images locally and copy them to EC2, then load them there.

**On your local machine:**

```bash
# Save all Docker images to tar files
echo "Saving Docker images..."
docker save anshshare/eureka-server:latest > eureka-server.tar
docker save anshshare/api-gateway:latest > api-gateway.tar
docker save anshshare/auth-service:latest > auth-service.tar
docker save anshshare/listing-service:latest > listing-service.tar
docker save anshshare/cart-service:latest > cart-service.tar
docker save anshshare/receipt-service:latest > receipt-service.tar
docker save anshshare/settlement-service:latest > settlement-service.tar
docker save anshshare/analytics-service:latest > analytics-service.tar
docker save anshshare/admin-service:latest > admin-service.tar
docker save anshshare/user-service:latest > user-service.tar
docker save anshshare/chat-service:latest > chat-service.tar
docker save anshshare/customer-service:latest > customer-service.tar

echo "✓ All images saved"

# Copy images to EC2 (replace $PUBLIC_IP with your actual IP)
echo "Copying images to EC2..."
for img in eureka-server api-gateway auth-service listing-service cart-service receipt-service settlement-service analytics-service admin-service user-service chat-service customer-service; do
  echo "Copying $img..."
  scp -i anshshare-manual-key.pem ${img}.tar ec2-user@$PUBLIC_IP:/home/ec2-user/
done

echo "✓ All images copied to EC2"

# Clean up local tar files
rm *.tar
```

### Step 6: Load Docker Images on EC2

**SSH back into EC2:**

```bash
ssh -i anshshare-manual-key.pem ec2-user@$PUBLIC_IP
```

**Load all Docker images:**

```bash
# Load all images
for img in eureka-server api-gateway auth-service listing-service cart-service receipt-service settlement-service analytics-service admin-service user-service chat-service customer-service; do
  echo "Loading $img..."
  docker load < /home/ec2-user/${img}.tar
  rm /home/ec2-user/${img}.tar
done

# Verify images are loaded
docker images | grep anshshare

echo "✓ All Docker images loaded"
```

### Step 7: Create Docker Compose File

**Still on EC2, create a docker-compose.yml file:**

```bash
cat > docker-compose.yml <<'EOF'
version: '3.8'

services:
  eureka-server:
    image: anshshare/eureka-server:latest
    container_name: eureka-server
    ports:
      - "8761:8761"
    environment:
      - SPRING_PROFILES_ACTIVE=aws
    restart: unless-stopped
    networks:
      - anshshare-network

  api-gateway:
    image: anshshare/api-gateway:latest
    container_name: api-gateway
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=aws
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
    depends_on:
      - eureka-server
    restart: unless-stopped
    networks:
      - anshshare-network

  auth-service:
    image: anshshare/auth-service:latest
    container_name: auth-service
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=aws
      - SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/auth_service_db
      - SPRING_DATASOURCE_USERNAME=anshshare_admin
      - SPRING_DATASOURCE_PASSWORD=AnshShare2024!
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
    depends_on:
      - eureka-server
    restart: unless-stopped
    network_mode: host

  listing-service:
    image: anshshare/listing-service:latest
    container_name: listing-service
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=aws
      - SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/listing_service_db
      - SPRING_DATASOURCE_USERNAME=anshshare_admin
      - SPRING_DATASOURCE_PASSWORD=AnshShare2024!
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
    network_mode: host
    restart: unless-stopped

  cart-service:
    image: anshshare/cart-service:latest
    container_name: cart-service
    ports:
      - "8084:8084"
    environment:
      - SPRING_PROFILES_ACTIVE=aws
      - SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/cart_service_db
      - SPRING_DATASOURCE_USERNAME=anshshare_admin
      - SPRING_DATASOURCE_PASSWORD=AnshShare2024!
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
    network_mode: host
    restart: unless-stopped

  receipt-service:
    image: anshshare/receipt-service:latest
    container_name: receipt-service
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=aws
      - SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/receipt_service_db
      - SPRING_DATASOURCE_USERNAME=anshshare_admin
      - SPRING_DATASOURCE_PASSWORD=AnshShare2024!
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
    network_mode: host
    restart: unless-stopped

  settlement-service:
    image: anshshare/settlement-service:latest
    container_name: settlement-service
    ports:
      - "8085:8085"
    environment:
      - SPRING_PROFILES_ACTIVE=aws
      - SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/settlement_service_db
      - SPRING_DATASOURCE_USERNAME=anshshare_admin
      - SPRING_DATASOURCE_PASSWORD=AnshShare2024!
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
    network_mode: host
    restart: unless-stopped

  analytics-service:
    image: anshshare/analytics-service:latest
    container_name: analytics-service
    ports:
      - "8086:8086"
    environment:
      - SPRING_PROFILES_ACTIVE=aws
      - SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/analytics_service_db
      - SPRING_DATASOURCE_USERNAME=anshshare_admin
      - SPRING_DATASOURCE_PASSWORD=AnshShare2024!
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
    network_mode: host
    restart: unless-stopped

  admin-service:
    image: anshshare/admin-service:latest
    container_name: admin-service
    ports:
      - "8087:8087"
    environment:
      - SPRING_PROFILES_ACTIVE=aws
      - SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/admin_service_db
      - SPRING_DATASOURCE_USERNAME=anshshare_admin
      - SPRING_DATASOURCE_PASSWORD=AnshShare2024!
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
    network_mode: host
    restart: unless-stopped

  user-service:
    image: anshshare/user-service:latest
    container_name: user-service
    ports:
      - "8088:8088"
    environment:
      - SPRING_PROFILES_ACTIVE=aws
      - SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/user_service_db
      - SPRING_DATASOURCE_USERNAME=anshshare_admin
      - SPRING_DATASOURCE_PASSWORD=AnshShare2024!
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
    network_mode: host
    restart: unless-stopped

  chat-service:
    image: anshshare/chat-service:latest
    container_name: chat-service
    ports:
      - "8089:8089"
    environment:
      - SPRING_PROFILES_ACTIVE=aws
      - SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/chat_service_db
      - SPRING_DATASOURCE_USERNAME=anshshare_admin
      - SPRING_DATASOURCE_PASSWORD=AnshShare2024!
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
    network_mode: host
    restart: unless-stopped

  customer-service:
    image: anshshare/customer-service:latest
    container_name: customer-service
    ports:
      - "8090:8090"
    environment:
      - SPRING_PROFILES_ACTIVE=aws
      - SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/customer_service_db
      - SPRING_DATASOURCE_USERNAME=anshshare_admin
      - SPRING_DATASOURCE_PASSWORD=AnshShare2024!
      - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
    network_mode: host
    restart: unless-stopped

networks:
  anshshare-network:
    driver: bridge
EOF

echo "✓ Docker Compose file created"
```

### Step 8: Start All Services

```bash
# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f

# To stop viewing logs, press Ctrl+C
```

### Step 9: Verify Deployment

**Check Eureka Dashboard:**
```bash
# From your local machine, open browser:
# http://<YOUR_PUBLIC_IP>:8761

# Or use curl from EC2:
curl http://localhost:8761
```

**Test API Gateway:**
```bash
curl http://localhost:8080/actuator/health
```

**Test Auth Service:**
```bash
curl http://localhost:8081/actuator/health
```

---

## 📊 Service URLs

Once deployed, access your services at:

- **Eureka Dashboard:** http://YOUR_PUBLIC_IP:8761
- **API Gateway:** http://YOUR_PUBLIC_IP:8080
- **Auth Service:** http://YOUR_PUBLIC_IP:8081
- **Receipt Service:** http://YOUR_PUBLIC_IP:8082
- **Listing Service:** http://YOUR_PUBLIC_IP:8083
- **Cart Service:** http://YOUR_PUBLIC_IP:8084
- **Settlement Service:** http://YOUR_PUBLIC_IP:8085
- **Analytics Service:** http://YOUR_PUBLIC_IP:8086
- **Admin Service:** http://YOUR_PUBLIC_IP:8087
- **User Service:** http://YOUR_PUBLIC_IP:8088
- **Chat Service:** http://YOUR_PUBLIC_IP:8089
- **Customer Service:** http://YOUR_PUBLIC_IP:8090

---

## 🛠️ Management Commands

### View Service Logs
```bash
# All services
docker-compose logs

# Specific service
docker-compose logs eureka-server
docker-compose logs auth-service

# Follow logs
docker-compose logs -f
```

### Restart Services
```bash
# Restart all
docker-compose restart

# Restart specific service
docker-compose restart auth-service
```

### Stop Services
```bash
# Stop all
docker-compose down

# Stop specific service
docker-compose stop auth-service
```

### Update a Service
```bash
# On local machine: rebuild image
docker build -t anshshare/auth-service:latest -f auth-service/Dockerfile .
docker save anshshare/auth-service:latest > auth-service.tar

# Copy to EC2
scp -i anshshare-manual-key.pem auth-service.tar ec2-user@$PUBLIC_IP:/home/ec2-user/

# On EC2: reload and restart
docker load < auth-service.tar
docker-compose up -d auth-service
```

---

## 💰 Cost Management

### Monthly Costs
- **t3.small (2GB RAM):** ~$15/month
- **t3.micro (1GB RAM):** ~$7/month (may run out of memory)
- **EBS Storage (30GB):** ~$2.40/month
- **Data Transfer:** ~$1/month
- **Total:** ~$10-18/month

### To Reduce Costs
1. Use t3.micro instead of t3.small (~$7/month vs ~$15/month)
2. Stop instance when not in use
3. Use spot instances (50-90% discount, but can be terminated)

### Stop Instance (to save money)
```bash
# Stop instance
aws ec2 stop-instances --instance-ids $INSTANCE_ID --region us-east-1

# Start instance later
aws ec2 start-instances --instance-ids $INSTANCE_ID --region us-east-1

# Get new public IP after restart
aws ec2 describe-instances --instance-ids $INSTANCE_ID --region us-east-1 --query 'Reservations[0].Instances[0].PublicIpAddress'
```

**Note:** Public IP changes when you stop/start. Use Elastic IP (free if instance is running) to keep same IP.

---

## 🔧 Troubleshooting

### Services Won't Start
```bash
# Check Docker logs
docker-compose logs <service-name>

# Check if PostgreSQL is running
sudo systemctl status postgresql

# Test database connection
psql -h localhost -U anshshare_admin -d auth_service_db
```

### Out of Memory
```bash
# Check memory usage
free -h

# Stop non-essential services
docker-compose stop analytics-service settlement-service customer-service

# Or upgrade to t3.small (2GB RAM)
```

### Can't Connect from Browser
```bash
# Check security group allows inbound traffic
aws ec2 describe-security-groups --group-ids $SG_ID --region us-east-1

# Check if ports are listening
sudo netstat -tlnp | grep -E ':(8761|8080|8081)'
```

### Database Connection Errors
```bash
# Check PostgreSQL is accepting connections
sudo -u postgres psql -c "SELECT version();"

# Verify database exists
sudo -u postgres psql -l | grep auth_service_db

# Check PostgreSQL logs
sudo tail -f /var/lib/pgsql/data/log/postgresql-*.log
```

---

## 🗑️ Cleanup (Delete Everything)

**To delete all resources and stop charges:**

```bash
# Stop and remove Docker containers
docker-compose down

# Terminate EC2 instance
aws ec2 terminate-instances --instance-ids $INSTANCE_ID --region us-east-1

# Wait for termination
aws ec2 wait instance-terminated --instance-ids $INSTANCE_ID --region us-east-1

# Delete Security Group
aws ec2 delete-security-group --group-id $SG_ID --region us-east-1

# Delete Key Pair
aws ec2 delete-key-pair --key-name anshshare-manual-key --region us-east-1
rm anshshare-manual-key.pem
```

---

## ✅ Quick Reference

### Essential Commands
```bash
# SSH to server
ssh -i anshshare-manual-key.pem ec2-user@$PUBLIC_IP

# View all services
docker-compose ps

# View logs
docker-compose logs -f

# Restart all services
docker-compose restart

# Stop all services
docker-compose down

# Start all services
docker-compose up -d
```

### Service Health Checks
```bash
curl http://localhost:8761  # Eureka
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # Auth Service
```

---

## 📚 Additional Resources

- **Docker Images:** All built and ready in your local Docker
- **Source Code:** C:\Users\k_man\IdeaProjects\ansh-microservices
- **AWS Account ID:** 778830035443
- **Region:** us-east-1

---

**You're all set! Follow these steps to deploy your microservices to AWS manually.**

Good luck with your deployment! 🚀
