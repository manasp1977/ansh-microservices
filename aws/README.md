# AWS Deployment Guide

This directory contains AWS deployment configurations for AnshShare microservices.

## Quick Start - Free Tier Deployment

**Cost: $0/month** (first 12 months with AWS Free Tier)

```bash
# Run complete deployment
cd aws/scripts
./deploy-free-tier.sh
```

This will:
1. Create ECR repositories
2. Build all Docker images
3. Push images to ECR
4. Provide manual setup instructions

## Directory Structure

```
aws/
├── cloudformation/
│   └── free-tier/           # Free tier CloudFormation templates
│       ├── README.md        # Free tier deployment guide
│       └── Manual setup instructions
├── scripts/
│   ├── create-ecr-repos.sh  # Create ECR repositories
│   ├── deploy-free-tier.sh  # Complete free tier deployment
│   ├── start-service.sh     # Start an ECS service
│   ├── stop-service.sh      # Stop an ECS service
│   └── list-running-services.sh  # List running services
└── README.md                # This file
```

## Deployment Options

### Option A: Free Tier (Recommended for Development)

**Cost:** $0/month (first 12 months)
**Services:** 10-12 services simultaneously (64MB each)
**Resources:**
- 1x t2.micro EC2 instance
- 1x db.t2.micro RDS PostgreSQL
- No load balancer (direct EC2 access)

**Deploy:**
```bash
cd aws/scripts
./deploy-free-tier.sh
```

**See:** `cloudformation/free-tier/README.md`

### Option B: Production (For Real Applications)

**Cost:** $800-1500/month
**Services:** All 12 services with auto-scaling
**Resources:**
- ECS Fargate cluster
- Multi-AZ RDS
- Application Load Balancer
- Auto-scaling groups

**Coming soon:** Production CloudFormation templates

## Prerequisites

### Required Tools

```bash
# AWS CLI
aws --version  # Should be 2.x or higher

# Docker
docker --version

# Git (for versioning)
git --version
```

### AWS Setup

```bash
# Configure AWS CLI
aws configure

# Verify credentials
aws sts get-caller-identity
```

## Infrastructure Components

### Free Tier Architecture

```
┌─────────────────────────────────────┐
│           VPC (10.0.0.0/16)        │
│                                     │
│  ┌──────────────┐  ┌─────────────┐│
│  │ Public Subnet│  │Private Subnet│
│  │              │  │              ││
│  │  EC2 t2.micro│  │RDS db.t2.micro│
│  │  (1GB RAM)   │  │              ││
│  │  ├─ eureka   │  │ PostgreSQL   │
│  │  ├─ gateway  │  │ 8 databases  ││
│  │  ├─ auth     │  │              │
│  │  ├─ listing  │  │              ││
│  │  ├─ cart     │  │              │
│  │  └─ + 7 more │  │              ││
│  │  (64MB each) │  │              │
│  └──────────────┘  └──────────────┘│
│         │                           │
│    Internet Gateway                 │
└────────┼────────────────────────────┘
         │
     Internet
```

### Services

With 64MB per service, you can run 10-12 services simultaneously!

**Memory Allocation (64MB each):**
1. eureka-server (64MB) - Service Discovery
2. api-gateway (64MB) - API Gateway
3. auth-service (64MB) - Authentication
4. listing-service (64MB) - Product Listings
5. cart-service (64MB) - Shopping Cart
6. receipt-service (64MB) - Receipts
7. settlement-service (64MB) - Settlements
8. user-service (64MB) - User Profiles
9. analytics-service (64MB) - Analytics
10. admin-service (64MB) - Administration
11. chat-service (64MB) - Chat/Messaging
12. customer-service (64MB) - Customer Support

**Total: ~768MB used + ~256MB system = 1GB t2.micro**

## Deployment Steps

### 1. Create ECR Repositories

```bash
cd aws/scripts
./create-ecr-repos.sh us-east-1
```

### 2. Build and Push Docker Images

```bash
cd ../../docker/scripts
./build-and-push-all.sh dev us-east-1
```

### 3. Create AWS Infrastructure

**Free Tier Approach:**
- Use AWS Console or AWS CLI
- Follow guide in `cloudformation/free-tier/README.md`

**Production Approach:**
- Use CloudFormation templates (coming soon)

### 4. Deploy Services

After infrastructure is ready:

```bash
# Services will auto-start from ECS task definitions
# Monitor in AWS Console → ECS → Clusters
```

## Service Management

### Start Additional Service

```bash
cd aws/scripts
./start-service.sh listing-service
```

### Stop Service

```bash
./stop-service.sh listing-service
```

### List Running Services

```bash
./list-running-services.sh
```

## Accessing Services

### Get EC2 Public IP

```bash
aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=anshshare-ecs-instance" \
  --query 'Reservations[0].Instances[0].PublicIpAddress' \
  --output text
```

### Service URLs

Replace `<EC2-IP>` with your EC2 instance public IP:

- **Eureka Dashboard:** `http://<EC2-IP>:8761`
- **API Gateway:** `http://<EC2-IP>:8080`
- **Auth Service:** `http://<EC2-IP>:8081`
- **Other Services:** `http://<EC2-IP>:808X`

## Monitoring

### View Logs

```bash
# CloudWatch Logs
aws logs tail /ecs/anshshare/auth-service --follow

# All log groups
aws logs describe-log-groups \
  --log-group-name-prefix /ecs/anshshare
```

### Check Service Status

```bash
# ECS services
aws ecs list-services --cluster anshshare-cluster

# Service details
aws ecs describe-services \
  --cluster anshshare-cluster \
  --services auth-service
```

### Database Status

```bash
# RDS instance status
aws rds describe-db-instances \
  --db-instance-identifier anshshare-db

# Connect to database
psql -h <RDS-ENDPOINT> -U anshshare_app -d auth_db
```

## Cost Monitoring

### Stay Within Free Tier

```bash
# Check EC2 usage
aws ec2 describe-instances --query 'Reservations[].Instances[].LaunchTime'

# Check RDS usage
aws rds describe-db-instances \
  --query 'DBInstances[].InstanceCreateTime'
```

### Monthly Costs

**Free Tier (First 12 Months):**
- EC2: $0 (750 hours/month free)
- RDS: $0 (750 hours/month free)
- Data Transfer: $0 (1GB/month free)
- **Total: $0/month**

**After Free Tier:**
- EC2 t2.micro: ~$8.50/month
- RDS db.t2.micro: ~$15/month
- Data Transfer: ~$1/month
- **Total: ~$25/month**

## Troubleshooting

### Services Not Starting

**Check ECS tasks:**
```bash
aws ecs list-tasks --cluster anshshare-cluster
aws ecs describe-tasks --cluster anshshare-cluster --tasks <task-arn>
```

**Check logs:**
```bash
aws logs tail /ecs/anshshare/auth-service --since 10m
```

### Database Connection Issues

**Test from EC2:**
```bash
# SSH into EC2 instance
ssh -i your-key.pem ec2-user@<EC2-IP>

# Test PostgreSQL connection
psql -h <RDS-ENDPOINT> -U anshshare_app -d auth_db
```

### Out of Memory

**Check memory usage:**
```bash
./list-running-services.sh
```

**Stop non-essential services:**
```bash
./stop-service.sh analytics-service
./stop-service.sh settlement-service
```

## Cleanup

### Delete All Resources

```bash
# Delete CloudFormation stack (if used)
aws cloudformation delete-stack --stack-name anshshare-free-tier

# Or delete resources manually:
# 1. Stop all ECS services
# 2. Delete ECS cluster
# 3. Delete RDS instance
# 4. Delete VPC and subnets

# Delete ECR repositories
for repo in eureka-server api-gateway auth-service; do
  aws ecr delete-repository \
    --repository-name "anshshare/${repo}" \
    --force
done
```

## Security Best Practices

1. **Use AWS Secrets Manager** for database credentials
2. **Enable CloudWatch logging** for all services
3. **Use security groups** to restrict access
4. **Enable RDS encryption** at rest
5. **Use IAM roles** instead of access keys
6. **Enable MFA** on AWS account
7. **Regular security updates** on EC2 instances

## Next Steps

1. **Deploy free tier** following this guide
2. **Test all services** via EC2 public IP
3. **Monitor costs** in AWS Cost Explorer
4. **Scale to production** when ready (use production templates)

## Support

For issues or questions:
- Check AWS CloudWatch Logs
- Review ECS task definitions
- Check security group rules
- Verify ECR image availability
- Review this documentation

## Additional Resources

- [AWS Free Tier](https://aws.amazon.com/free/)
- [ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [RDS Documentation](https://docs.aws.amazon.com/rds/)
- [ECR Documentation](https://docs.aws.amazon.com/ecr/)

## RDS PostgreSQL for UAT (Recommended)

### Why Use RDS?

If you're using EC2 for UAT deployment, we recommend using RDS PostgreSQL instead of a containerized database:

**Benefits:**
- ✓ **Data Persistence**: Data survives EC2 restarts/terminations
- ✓ **Automated Backups**: 7-day retention with point-in-time recovery  
- ✓ **Better Performance**: Dedicated database instance
- ✓ **Easy Scaling**: Auto-scaling storage (20-100 GB)
- ✓ **Monitoring**: CloudWatch metrics included

**Cost:**
- **Free Tier**: ~$2-5/month (first 12 months)
- **Regular**: ~$17-20/month
- db.t3.micro + 20 GB storage

### Quick Setup

**One command to set up everything:**

```bash
bash aws/scripts/setup-rds-complete.sh
```

This will:
1. Deploy RDS PostgreSQL instance
2. Create all 11 databases
3. Update UAT configuration
4. Remove PostgreSQL from docker-compose

**Time:** ~15 minutes (includes 10 min for RDS to become available)

### Manual Setup

If you prefer step-by-step control:

```bash
# 1. Deploy RDS (10 min wait)
bash aws/scripts/deploy-rds-uat.sh

# 2. Initialize databases
bash aws/scripts/init-rds-databases.sh

# 3. Update configuration
bash aws/scripts/update-uat-config-for-rds.sh

# 4. Remove PostgreSQL container
bash aws/scripts/remove-postgres-from-docker-compose.sh
```

### After Setup

On your EC2 instance, restart services:

```bash
ssh -i ~/.ssh/anshshare-key.pem ec2-user@<EC2_IP>
cd ansh-microservices
docker-compose -f docker-compose.uat.yml down
docker-compose -f docker-compose.uat.yml up -d
```

### RDS Management

**Stop RDS to save money:**
```bash
aws rds stop-db-instance \
    --db-instance-identifier anshshare-db-uat \
    --region us-east-2
```

**Start RDS:**
```bash
aws rds start-db-instance \
    --db-instance-identifier anshshare-db-uat \
    --region us-east-2
```

**Create backup:**
```bash
aws rds create-db-snapshot \
    --db-instance-identifier anshshare-db-uat \
    --db-snapshot-identifier uat-backup-$(date +%Y%m%d) \
    --region us-east-2
```

### More Information

- **Quick Start**: [QUICK_START_RDS.md](QUICK_START_RDS.md)
- **Full Guide**: [RDS_DEPLOYMENT_GUIDE.md](RDS_DEPLOYMENT_GUIDE.md)
- **Scripts**: [scripts/README.md](scripts/README.md)

