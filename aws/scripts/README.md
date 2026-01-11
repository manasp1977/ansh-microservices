# AWS Deployment Scripts

This directory contains scripts for deploying and managing the AnshShare microservices infrastructure on AWS.

## RDS PostgreSQL Scripts

### setup-rds-complete.sh
**Complete automated RDS setup for UAT environment**

Orchestrates the entire RDS deployment process:
1. Deploys RDS PostgreSQL instance
2. Waits for RDS to become available
3. Initializes all databases
4. Updates UAT configuration
5. Removes PostgreSQL from docker-compose

**Usage:**
```bash
bash aws/scripts/setup-rds-complete.sh
```

**Prerequisites:**
- AWS CLI configured
- PostgreSQL client (psql) installed
- EC2 instance running in UAT

---

### deploy-rds-uat.sh
**Deploy RDS PostgreSQL instance**

Creates an RDS PostgreSQL instance in the same VPC as your EC2 instance.

**Usage:**
```bash
bash aws/scripts/deploy-rds-uat.sh
```

**What it does:**
- Discovers VPC and subnet information from EC2
- Creates a second subnet if needed (RDS requires 2 AZs)
- Deploys CloudFormation stack for RDS
- Configures security groups

**Time:** ~10 minutes for RDS to become available

---

### init-rds-databases.sh
**Initialize databases in RDS**

Creates all required databases for the microservices.

**Usage:**
```bash
bash aws/scripts/init-rds-databases.sh
```

**Creates:**
- auth_db
- receipt_db
- listing_db
- cart_db
- settlement_db
- analytics_db
- admin_db
- user_db
- chat_db
- customer_db
- wishhub_db

---

### update-uat-config-for-rds.sh
**Update UAT configuration to use RDS**

Updates `config/environments/uat.properties` with the RDS endpoint.

**Usage:**
```bash
bash aws/scripts/update-uat-config-for-rds.sh
```

**Changes:**
- Updates `db.host` from `postgres` to RDS endpoint
- Creates backup of original configuration

---

### remove-postgres-from-docker-compose.sh
**Remove PostgreSQL container from docker-compose.uat.yml**

Since RDS is now used, the containerized PostgreSQL is no longer needed.

**Usage:**
```bash
bash aws/scripts/remove-postgres-from-docker-compose.sh
```

**Changes:**
- Removes postgres service
- Removes postgres from depends_on clauses
- Removes postgres_data volume
- Creates backup of original file

---

## Script Workflow

### Quick Setup (Recommended)
```bash
# Run complete setup
bash aws/scripts/setup-rds-complete.sh
```

### Manual Step-by-Step
```bash
# 1. Deploy RDS
bash aws/scripts/deploy-rds-uat.sh

# 2. Wait for RDS to be available (check AWS Console)
aws rds describe-db-instances \
    --db-instance-identifier anshshare-db-uat \
    --region us-east-2 \
    --query 'DBInstances[0].DBInstanceStatus'

# 3. Initialize databases
bash aws/scripts/init-rds-databases.sh

# 4. Update configuration
bash aws/scripts/update-uat-config-for-rds.sh

# 5. Remove PostgreSQL container
bash aws/scripts/remove-postgres-from-docker-compose.sh
```

## After Running Scripts

1. **SSH to EC2 instance:**
   ```bash
   ssh -i ~/.ssh/anshshare-key.pem ec2-user@<EC2_IP>
   ```

2. **Restart services:**
   ```bash
   cd ansh-microservices
   docker-compose -f docker-compose.uat.yml down
   docker-compose -f docker-compose.uat.yml up -d
   ```

3. **Verify database connections:**
   ```bash
   docker logs auth-service 2>&1 | grep -i "flyway\|database"
   ```

4. **Test the application:**
   - Access: `http://<EC2_IP>:8080`
   - Sign up and create test data
   - Stop EC2 instance
   - Start EC2 instance
   - Verify data persists (stored in RDS)

## Troubleshooting

### RDS Connection Issues

**Check RDS status:**
```bash
aws rds describe-db-instances \
    --db-instance-identifier anshshare-db-uat \
    --region us-east-2
```

**Test connection from EC2:**
```bash
ssh -i ~/.ssh/anshshare-key.pem ec2-user@<EC2_IP>
psql -h <rds-endpoint> -U anshshare_admin -d postgres
```

**Check security groups:**
```bash
# Get RDS security group
aws cloudformation describe-stacks \
    --stack-name anshshare-rds-uat \
    --region us-east-2 \
    --query 'Stacks[0].Outputs[?OutputKey==`RDSSecurityGroupId`].OutputValue'

# Check rules
aws ec2 describe-security-groups \
    --group-ids <sg-id> \
    --region us-east-2
```

### Service Connection Issues

**Check service logs:**
```bash
docker logs auth-service 2>&1 | tail -100
```

**Verify configuration:**
```bash
cat config/environments/uat.properties | grep db.host
```

**Test database from within container:**
```bash
docker exec -it auth-service sh
wget --spider --timeout=1 <rds-endpoint>:5432
```

### Rollback to Containerized PostgreSQL

```bash
# Delete RDS stack
aws cloudformation delete-stack \
    --stack-name anshshare-rds-uat \
    --region us-east-2

# Restore backups
cp docker-compose.uat.yml.backup docker-compose.uat.yml
cp config/environments/uat.properties.backup config/environments/uat.properties

# Restart services
docker-compose -f docker-compose.uat.yml down
docker-compose -f docker-compose.uat.yml up -d
```

## Cost Management

**Monthly costs:**
- db.t3.micro: ~$15/month (free for first 12 months with free tier)
- 20 GB storage: ~$2.30/month
- Backups (7 days): First 20 GB free
- **Total: ~$17-20/month** (or ~$2-5/month with free tier)

**Check current costs:**
```bash
aws ce get-cost-and-usage \
    --time-period Start=2026-01-01,End=2026-01-31 \
    --granularity MONTHLY \
    --metrics BlendedCost \
    --filter file://filter.json
```

**Stop RDS when not needed:**
```bash
# Stop RDS (max 7 days)
aws rds stop-db-instance \
    --db-instance-identifier anshshare-db-uat \
    --region us-east-2

# Start RDS
aws rds start-db-instance \
    --db-instance-identifier anshshare-db-uat \
    --region us-east-2
```

## Monitoring

**View CloudWatch logs:**
```bash
aws logs tail /aws/rds/instance/anshshare-db-uat/postgresql \
    --follow \
    --region us-east-2
```

**Check metrics:**
- AWS Console → RDS → anshshare-db-uat → Monitoring

**Key metrics:**
- CPU Utilization
- Database Connections
- Free Storage Space
- Read/Write Latency

## Backup and Restore

**Create manual snapshot:**
```bash
aws rds create-db-snapshot \
    --db-instance-identifier anshshare-db-uat \
    --db-snapshot-identifier anshshare-uat-backup-$(date +%Y%m%d-%H%M) \
    --region us-east-2
```

**List snapshots:**
```bash
aws rds describe-db-snapshots \
    --db-instance-identifier anshshare-db-uat \
    --region us-east-2
```

**Restore from snapshot:**
```bash
aws rds restore-db-instance-from-db-snapshot \
    --db-instance-identifier anshshare-db-uat-restored \
    --db-snapshot-identifier <snapshot-id> \
    --region us-east-2
```

## Additional Resources

- [RDS Deployment Guide](../RDS_DEPLOYMENT_GUIDE.md)
- [AWS RDS Documentation](https://docs.aws.amazon.com/rds/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/15/)

## Support

For issues or questions:
1. Check the [RDS Deployment Guide](../RDS_DEPLOYMENT_GUIDE.md)
2. Review CloudWatch logs
3. Check AWS RDS console for instance status
4. Verify security group configurations
