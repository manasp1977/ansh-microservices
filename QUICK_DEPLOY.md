# Quick Deployment Guide - EC2 Instance

Your EC2 instance is ready! Here's how to deploy your microservices.

## Instance Details
- **IP Address:** 3.21.126.175
- **Region:** us-east-2
- **Instance Type:** t3.small (2 vCPU, 2GB RAM)
- **Instance ID:** i-01e398e89cc02e3d0
- **Key Pair:** ec2-key-pair.pem

## Security Group ✅
All required ports are open:
- 22 (SSH)
- 8761 (Eureka)
- 8080 (API Gateway)
- 8081-8090 (All microservices)
- 5432 (PostgreSQL)

## Option 1: Automated Deployment (Recommended)

### Prerequisites
1. Make sure you have the `ec2-key-pair.pem` file in your project directory
2. All Docker images must be built locally (already done)

### Deploy Everything
```bash
# Make the script executable
chmod +x deploy-to-ec2.sh

# Run deployment
./deploy-to-ec2.sh
```

This script will:
1. Install Docker and Docker Compose on EC2
2. Transfer all configuration files
3. Transfer all 12 Docker images
4. Start all services with PostgreSQL

**Total time:** ~15-20 minutes (depending on network speed)

## Option 2: Manual Deployment

### Step 1: Connect to EC2
```bash
ssh -i ec2-key-pair.pem ec2-user@3.21.126.175
```

### Step 2: Install Docker
```bash
# Update system
sudo yum update -y

# Install Docker
sudo yum install -y docker

# Start Docker
sudo systemctl start docker
sudo systemctl enable docker

# Add user to docker group
sudo usermod -a -G docker ec2-user

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Log out and back in for group changes to take effect
exit
```

### Step 3: Transfer Files (From Your Local Machine)
```bash
# Transfer docker-compose.yml
scp -i ec2-key-pair.pem docker-compose.yml ec2-user@3.21.126.175:~/

# Transfer init-databases.sql
scp -i ec2-key-pair.pem init-databases.sql ec2-user@3.21.126.175:~/
```

### Step 4: Transfer Docker Images
For each service, run these commands from your local machine:

```bash
# Save images locally
docker save anshshare/eureka-server:latest | gzip > eureka-server.tar.gz
docker save anshshare/api-gateway:latest | gzip > api-gateway.tar.gz
docker save anshshare/auth-service:latest | gzip > auth-service.tar.gz
docker save anshshare/receipt-service:latest | gzip > receipt-service.tar.gz
docker save anshshare/listing-service:latest | gzip > listing-service.tar.gz
docker save anshshare/cart-service:latest | gzip > cart-service.tar.gz
docker save anshshare/settlement-service:latest | gzip > settlement-service.tar.gz
docker save anshshare/analytics-service:latest | gzip > analytics-service.tar.gz
docker save anshshare/admin-service:latest | gzip > admin-service.tar.gz
docker save anshshare/user-service:latest | gzip > user-service.tar.gz
docker save anshshare/chat-service:latest | gzip > chat-service.tar.gz
docker save anshshare/customer-service:latest | gzip > customer-service.tar.gz

# Transfer to EC2
scp -i ec2-key-pair.pem *.tar.gz ec2-user@3.21.126.175:~/

# On EC2, load the images
ssh -i ec2-key-pair.pem ec2-user@3.21.126.175
for img in *.tar.gz; do
  docker load < $img
  rm $img
done
exit
```

### Step 5: Start Services
```bash
# SSH to EC2
ssh -i ec2-key-pair.pem ec2-user@3.21.126.175

# Start all services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f
```

## Verify Deployment

### Check Eureka Dashboard
Open in browser: http://3.21.126.175:8761

You should see all 11 microservices registered.

### Check API Gateway
```bash
curl http://3.21.126.175:8080/actuator/health
```

### Check Individual Services
```bash
# Auth Service
curl http://3.21.126.175:8081/actuator/health

# Listing Service
curl http://3.21.126.175:8083/actuator/health

# Cart Service
curl http://3.21.126.175:8084/actuator/health
```

## Useful Commands

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f eureka-server
docker-compose logs -f api-gateway
docker-compose logs -f auth-service
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
# Stop all services
docker-compose down

# Stop but keep data
docker-compose stop
```

### Check Resource Usage
```bash
# Container stats
docker stats

# Disk usage
docker system df

# Instance resources
htop  # Install with: sudo yum install -y htop
```

## Troubleshooting

### Services not starting?
```bash
# Check logs
docker-compose logs [service-name]

# Check if ports are in use
netstat -tlnp | grep 8761

# Restart individual service
docker-compose restart [service-name]
```

### Out of memory?
t3.small has 2GB RAM. With all 12 services + PostgreSQL, you're using ~1.6GB.

To free memory:
```bash
# Stop non-essential services
docker-compose stop chat-service customer-service
```

### Database connection issues?
```bash
# Check PostgreSQL
docker-compose logs postgres

# Connect to PostgreSQL
docker exec -it anshshare-postgres psql -U anshshare_admin -d postgres

# List databases
\l

# Connect to specific database
\c auth_service_db
```

### Cannot connect from browser?
1. Verify security group allows your IP (currently allows 0.0.0.0/0)
2. Check if services are running: `docker-compose ps`
3. Check EC2 instance public IP hasn't changed

## Service Startup Order

Docker Compose handles this automatically:
1. PostgreSQL (with health check)
2. Eureka Server (with health check)
3. API Gateway (waits for Eureka)
4. All other services (wait for Eureka + PostgreSQL)

## Cost Estimate

**t3.small in us-east-2:**
- On-Demand: ~$0.0208/hour = ~$15/month (730 hours)
- 2 vCPU, 2GB RAM

**Data Transfer:**
- First 100GB out per month: Free
- Estimated: $0-1/month

**Total: ~$15-16/month**

## Next Steps

1. Test all API endpoints
2. Set up monitoring (CloudWatch, Prometheus)
3. Configure backups for PostgreSQL data
4. Set up CI/CD pipeline
5. Add SSL/TLS with Let's Encrypt

## Support

If you encounter issues:
1. Check CloudWatch Logs (if configured)
2. Check `docker-compose logs`
3. Verify all databases exist: `docker exec -it anshshare-postgres psql -U anshshare_admin -d postgres -c '\l'`

---

**Ready to deploy!** Use Option 1 (automated script) for the easiest deployment.
