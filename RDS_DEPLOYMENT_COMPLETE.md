# ✅ RDS Deployment Complete

## What Was Done

### 1. RDS Instance Created ✓
- **Instance ID**: anshshare-db-uat
- **Engine**: PostgreSQL 15.10
- **Instance Class**: db.t3.micro (2 vCPU, 1 GB RAM)
- **Storage**: 20 GB GP3 (auto-scales to 100 GB)
- **Endpoint**: `anshshare-db-uat.cri426askc5s.us-east-2.rds.amazonaws.com`
- **Port**: 5432
- **Username**: anshshare_admin
- **Password**: AnshShare2024!
- **Status**: ✅ Available

### 2. Configuration Updated ✓
- **File**: `config/environments/uat.properties`
- **Change**: Updated `db.host` from `postgres` to RDS endpoint
- **Backup**: `config/environments/uat.properties.backup`

### 3. Docker Compose Updated ✓
- **File**: `docker-compose.uat.yml`
- **Changes**:
  - Removed postgres service
  - Removed all postgres dependencies
  - Removed postgres_data volume
- **Backup**: `docker-compose.uat.yml.backup`

### 4. Database Initialization Script Created ✓
- **File**: `init-rds-on-ec2.sh`
- Ready to run on EC2 to create all 11 databases

## What Needs to Be Done on EC2

### Step 1: Commit and Push Changes

```bash
git add config/environments/uat.properties docker-compose.uat.yml
git commit -m "Switch UAT to RDS PostgreSQL"
git push
```

### Step 2: SSH to EC2

```bash
ssh -i ~/.ssh/anshshare-key.pem ec2-user@3.16.24.182
```

### Step 3: Pull Latest Changes

```bash
cd ansh-microservices
git pull
```

### Step 4: Initialize RDS Databases

```bash
chmod +x init-rds-on-ec2.sh
./init-rds-on-ec2.sh
```

This will:
- Install PostgreSQL client if needed
- Connect to RDS
- Create all 11 databases

### Step 5: Stop Current Services

```bash
docker-compose -f docker-compose.uat.yml down
```

### Step 6: Clean Up Old Containers and Volumes

```bash
docker-compose -f docker-compose.uat.yml rm -f
docker volume prune -f
```

### Step 7: Start Services with RDS

```bash
docker-compose -f docker-compose.uat.yml up -d
```

### Step 8: Verify Services

```bash
# Wait for services to start (30-60 seconds)
sleep 60

# Check service status
docker-compose -f docker-compose.uat.yml ps

# Check auth-service logs for successful Flyway migrations
docker logs auth-service 2>&1 | grep -i "flyway.*success\|migration.*success"

# Check other services
docker logs listing-service 2>&1 | tail -20
docker logs cart-service 2>&1 | tail -20
```

### Step 9: Test the Application

```bash
# Test health endpoint
curl http://localhost:8080/api/auth/health

# Or access from browser
# http://3.16.24.182:8080
```

### Step 10: Verify Data Persistence

1. Create a test user and some data
2. Stop the EC2 instance from AWS Console
3. Start the EC2 instance
4. Wait for services to start
5. Verify data still exists ✓

## RDS Details

### Connection Information
```
Host: anshshare-db-uat.cri426askc5s.us-east-2.rds.amazonaws.com
Port: 5432
Username: anshshare_admin
Password: AnshShare2024!
```

### Databases to be Created
1. auth_db - Authentication
2. receipt_db - Receipts
3. listing_db - Product listings
4. cart_db - Shopping cart
5. settlement_db - Settlements
6. analytics_db - Analytics
7. admin_db - Administration
8. user_db - User profiles
9. chat_db - Chat/messaging
10. customer_db - Customer support
11. wishhub_db - Wish lists

### Features
- **Automated Backups**: Daily at 3:00 AM UTC, 7-day retention
- **Encryption**: At rest using AWS KMS
- **Auto-scaling**: Storage grows from 20 GB to 100 GB as needed
- **Monitoring**: CloudWatch metrics and logs
- **Maintenance Window**: Sunday 4:00-5:00 AM UTC

## Cost

### With Free Tier (First 12 Months)
- db.t3.micro: **FREE** (750 hours/month)
- 20 GB storage: **~$2.30/month**
- Backups (7 days): **FREE** (first 20 GB)
- **Total: ~$2-5/month**

### Without Free Tier
- db.t3.micro: **~$15/month**
- 20 GB storage: **~$2.30/month**
- Backups (7 days): **FREE** (first 20 GB)
- **Total: ~$17-20/month**

## Managing RDS

### View RDS Status
```bash
aws rds describe-db-instances \
    --db-instance-identifier anshshare-db-uat \
    --region us-east-2 \
    --query 'DBInstances[0].DBInstanceStatus'
```

### Stop RDS (Save Money)
```bash
aws rds stop-db-instance \
    --db-instance-identifier anshshare-db-uat \
    --region us-east-2
```

### Start RDS
```bash
aws rds start-db-instance \
    --db-instance-identifier anshshare-db-uat \
    --region us-east-2
```

### Create Snapshot
```bash
aws rds create-db-snapshot \
    --db-instance-identifier anshshare-db-uat \
    --db-snapshot-identifier uat-backup-$(date +%Y%m%d-%H%M) \
    --region us-east-2
```

### View Snapshots
```bash
aws rds describe-db-snapshots \
    --db-instance-identifier anshshare-db-uat \
    --region us-east-2
```

### Monitoring
```bash
# View logs
aws logs tail /aws/rds/instance/anshshare-db-uat/postgresql \
    --follow \
    --region us-east-2

# Or check in AWS Console:
# RDS → Databases → anshshare-db-uat → Monitoring
```

## Troubleshooting

### Services Can't Connect
1. Check RDS status is "available"
2. Verify config/environments/uat.properties has correct endpoint
3. Check service logs: `docker logs auth-service`
4. Test from EC2: `psql -h <endpoint> -U anshshare_admin -d postgres`

### Flyway Migrations Fail
1. Verify databases exist: `\l` in psql
2. Check migration files exist in resources/db/migration
3. Check flyway_schema_history table
4. Re-run migrations by restarting service

### Connection Timeout
1. Security group allows EC2 → RDS on port 5432
2. RDS is in correct VPC
3. Network ACLs allow traffic

## Rollback (If Needed)

```bash
# 1. Delete RDS
aws cloudformation delete-stack \
    --stack-name anshshare-rds-uat \
    --region us-east-2

# 2. Restore configs
git checkout config/environments/uat.properties docker-compose.uat.yml

# 3. Restart services on EC2
docker-compose -f docker-compose.uat.yml down
docker-compose -f docker-compose.uat.yml up -d
```

## Next Steps

1. ✅ RDS deployed and available
2. ✅ Configuration updated
3. ✅ Docker Compose updated
4. ⏳ Commit and push changes (do this now)
5. ⏳ SSH to EC2 and pull changes
6. ⏳ Initialize databases on EC2
7. ⏳ Restart services
8. ⏳ Test application
9. ⏳ Verify data persistence

## Files Created/Modified

### Modified
- `config/environments/uat.properties` - Updated db.host
- `docker-compose.uat.yml` - Removed PostgreSQL
- `aws/scripts/deploy-rds-uat.sh` - Fixed EC2 tag name

### Created
- `deploy-rds-now.sh` - Quick deployment script
- `init-rds-on-ec2.sh` - Database initialization script
- Backups: `*.backup` files

### Backups
- `config/environments/uat.properties.backup`
- `docker-compose.uat.yml.backup`

---

**Deployment Date**: 2026-01-11
**RDS Endpoint**: anshshare-db-uat.cri426askc5s.us-east-2.rds.amazonaws.com
**Status**: ✅ Ready for EC2 deployment
