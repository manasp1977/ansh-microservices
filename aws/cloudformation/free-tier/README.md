# AWS Free Tier CloudFormation Templates

This directory contains CloudFormation templates optimized for AWS Free Tier deployment.

## Free Tier Architecture

**Cost: $0/month for first 12 months** (within AWS free tier limits)

```
VPC (10.0.0.0/16)
├── Public Subnet (10.0.1.0/24)
│   └── t2.micro EC2 (1GB RAM - ECS Container Instance)
│       ├── eureka-server (64MB)
│       ├── api-gateway (64MB)
│       ├── auth-service (64MB)
│       ├── listing-service (64MB)
│       ├── cart-service (64MB)
│       ├── receipt-service (64MB)
│       ├── settlement-service (64MB)
│       ├── user-service (64MB)
│       ├── analytics-service (64MB)
│       ├── admin-service (64MB)
│       └── + 2 more services (64MB each)
│           Total: ~768MB + 256MB system overhead
└── Private Subnet (10.0.11.0/24)
    └── db.t2.micro RDS PostgreSQL
        └── All 8 databases
```

## Templates

1. **free-tier-master.yaml** - Master template that creates all resources
2. Manual deployment instructions (simplified approach)

## Prerequisites

- AWS Account with free tier available
- AWS CLI configured
- ECR repositories created and images pushed

## Deployment

### Option 1: One-Command Deployment (Recommended)

```bash
cd aws/scripts
./deploy-free-tier.sh
```

This script will:
1. Create ECR repositories
2. Build and push Docker images
3. Deploy CloudFormation stack
4. Output access information

### Option 2: Manual Deployment

```bash
# 1. Set your parameters
export AWS_REGION=us-east-1
export STACK_NAME=anshshare-free-tier
export KEY_PAIR_NAME=your-ec2-keypair

# 2. Deploy the stack
aws cloudformation create-stack \
  --stack-name ${STACK_NAME} \
  --template-body file://free-tier-master.yaml \
  --parameters \
    ParameterKey=KeyPairName,ParameterValue=${KEY_PAIR_NAME} \
  --capabilities CAPABILITY_IAM \
  --region ${AWS_REGION}

# 3. Wait for completion (15-20 minutes)
aws cloudformation wait stack-create-complete \
  --stack-name ${STACK_NAME} \
  --region ${AWS_REGION}

# 4. Get outputs
aws cloudformation describe-stacks \
  --stack-name ${STACK_NAME} \
  --query 'Stacks[0].Outputs' \
  --region ${AWS_REGION}
```

## What Gets Created

### Network Resources (FREE)
- 1 VPC
- 2 Subnets (1 public, 1 private)
- 1 Internet Gateway
- Route tables
- Security groups

### Compute Resources (FREE - 750 hours/month)
- 1x t2.micro EC2 instance
- ECS cluster (no charge)
- 3 ECS services (eureka, api-gateway, auth)

### Database Resources (FREE - 750 hours/month)
- 1x db.t2.micro RDS PostgreSQL
- 20GB storage
- Single-AZ
- 8 databases for all services

### Storage & Registry (FREE - under limits)
- ECR repositories (500MB total)
- EBS volumes (30GB total)

## Services Running

### All Services Can Run Simultaneously! (64MB each)

With 64MB per service, you can run **ALL 12 services** on a single t2.micro instance:

1. **eureka-server** - Service Discovery (64MB)
2. **api-gateway** - API Gateway (64MB)
3. **auth-service** - Authentication (64MB)
4. **listing-service** - Product Listings (64MB)
5. **cart-service** - Shopping Cart (64MB)
6. **receipt-service** - Receipts (64MB)
7. **settlement-service** - Settlements (64MB)
8. **user-service** - User Profiles (64MB)
9. **analytics-service** - Analytics (64MB)
10. **admin-service** - Administration (64MB)
11. **chat-service** - Chat/Messaging (64MB)
12. **customer-service** - Customer Support (64MB)

**Total Memory Usage:**
- 12 services × 64MB = 768MB
- System overhead = ~256MB
- **Total = ~1024MB (fits perfectly in t2.micro!)**

### Optional: Start/Stop Services

If you need more resources, you can still start/stop services individually:

```bash
# Stop non-essential services
./stop-service.sh analytics-service
./stop-service.sh customer-service

# Start when needed
./start-service.sh analytics-service
```

## Managing Services

### Start Additional Service
```bash
cd aws/scripts
./start-service.sh listing-service
```

### Stop Service to Free RAM
```bash
./stop-service.sh listing-service
```

### Check Running Services
```bash
./list-running-services.sh
```

## Accessing Services

After deployment, get the EC2 public IP:

```bash
aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=anshshare-ecs-instance" \
  --query 'Reservations[0].Instances[0].PublicIpAddress' \
  --output text
```

**Service URLs:**
- Eureka Dashboard: `http://<EC2-IP>:8761`
- API Gateway: `http://<EC2-IP>:8080`
- Auth Service: `http://<EC2-IP>:8081`

## Free Tier Limits

Monitor your usage to stay within free tier:

```bash
# Check EC2 hours
aws ce get-cost-and-usage \
  --time-period Start=2026-01-01,End=2026-01-31 \
  --granularity MONTHLY \
  --metrics UsageQuantity \
  --filter file://ec2-filter.json

# Check RDS hours
# Similar query for RDS

# Check data transfer
# Monitor outbound data transfer (1GB/month free)
```

## Cost After Free Tier Expires

After 12 months, expected costs:
- EC2 t2.micro: ~$8.50/month
- RDS db.t2.micro: ~$15/month
- Data transfer: ~$1/month
- **Total: ~$25/month**

## Troubleshooting

### Services Not Starting

**Check ECS logs:**
```bash
aws logs tail /ecs/anshshare/auth-service --follow
```

**Check EC2 instance:**
```bash
# SSH into instance
ssh -i your-key.pem ec2-user@<EC2-IP>

# Check Docker containers
docker ps

# Check ECS agent
sudo systemctl status ecs
```

### Database Connection Issues

**Test RDS connectivity:**
```bash
# From EC2 instance
psql -h <RDS-ENDPOINT> -U anshshare_app -d auth_db
```

### Out of Memory

**Free RAM by stopping services:**
```bash
./stop-service.sh settlement-service
./stop-service.sh analytics-service
```

## Cleanup

To delete all resources:

```bash
aws cloudformation delete-stack \
  --stack-name anshshare-free-tier \
  --region us-east-1
```

**Note:** This will delete everything except ECR repositories (manual cleanup required).

## Upgrading to Production

When ready to scale:

1. Use production CloudFormation templates in `aws/cloudformation/`
2. Switch to Fargate for auto-scaling
3. Enable Multi-AZ RDS
4. Add Application Load Balancer
5. Enable CloudWatch monitoring

See `../README.md` for production deployment guide.
