# RDS PostgreSQL Setup - Summary

## What Was Created

### CloudFormation Template
**File**: `aws/cloudformation/rds-postgres-uat.yaml`

Creates:
- RDS PostgreSQL 15.10 instance (db.t3.micro)
- DB Subnet Group (2 subnets in different AZs)
- Security Group (allows EC2 → RDS on port 5432)
- 20 GB GP3 storage with auto-scaling to 100 GB
- Automated backups (7-day retention)
- CloudWatch log exports

### Deployment Scripts

1. **`aws/scripts/setup-rds-complete.sh`** (Recommended)
   - One-command automated setup
   - Deploys RDS, creates databases, updates configs
   - Handles everything automatically

2. **`aws/scripts/deploy-rds-uat.sh`**
   - Deploys RDS CloudFormation stack
   - Auto-discovers VPC and subnets from EC2
   - Creates second subnet if needed

3. **`aws/scripts/init-rds-databases.sh`**
   - Creates all 11 databases in RDS
   - Tests connection before proceeding
   - Verifies database creation

4. **`aws/scripts/update-uat-config-for-rds.sh`**
   - Updates `config/environments/uat.properties`
   - Changes db.host to RDS endpoint
   - Creates backup of original config

5. **`aws/scripts/remove-postgres-from-docker-compose.sh`**
   - Removes postgres service from docker-compose.uat.yml
   - Cleans up depends_on clauses
   - Removes postgres_data volume
   - Creates backup of original file

### Documentation

1. **`aws/RDS_DEPLOYMENT_GUIDE.md`** - Comprehensive guide
   - Complete deployment process
   - Architecture diagrams
   - Configuration details
   - Monitoring and troubleshooting
   - Backup and restore procedures
   - Cost management

2. **`aws/QUICK_START_RDS.md`** - Quick reference
   - One-page setup guide
   - Essential commands
   - Troubleshooting checklist

3. **`aws/scripts/README.md`** - Scripts documentation
   - Detailed script descriptions
   - Usage examples
   - Troubleshooting tips

4. **`aws/README.md`** - Updated main README
   - Added RDS section
   - Quick setup instructions
   - Management commands

## How to Use

### Option 1: Automated Setup (Recommended)

```bash
bash aws/scripts/setup-rds-complete.sh
```

Wait ~15 minutes, then restart services on EC2.

### Option 2: Manual Setup

```bash
# 1. Deploy RDS
bash aws/scripts/deploy-rds-uat.sh

# 2. Wait for RDS to be available (check AWS Console)

# 3. Create databases
bash aws/scripts/init-rds-databases.sh

# 4. Update configuration
bash aws/scripts/update-uat-config-for-rds.sh

# 5. Remove PostgreSQL container
bash aws/scripts/remove-postgres-from-docker-compose.sh

# 6. Restart services on EC2
ssh ec2-user@<EC2_IP>
cd ansh-microservices
docker-compose -f docker-compose.uat.yml down
docker-compose -f docker-compose.uat.yml up -d
```

## Key Benefits

### Data Persistence
- Data survives EC2 instance stop/start/termination
- No more data loss when EC2 is restarted

### Automated Backups
- Daily automated backups (7-day retention)
- Point-in-time recovery
- Manual snapshots on demand

### Better Performance
- Dedicated database instance
- Optimized PostgreSQL configuration
- Separate from application server

### Easier Management
- AWS-managed patching
- CloudWatch monitoring
- Automated maintenance windows

### Scalability
- Auto-scaling storage (20-100 GB)
- Easy instance type upgrades
- Read replicas (if needed)

## Configuration Changes

### Before (Containerized PostgreSQL)
```yaml
# docker-compose.uat.yml
services:
  postgres:
    image: postgres:15-alpine
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
```

```properties
# config/environments/uat.properties
db.host=postgres
db.port=5432
```

### After (RDS PostgreSQL)
```yaml
# docker-compose.uat.yml
services:
  # postgres service removed
  # Services connect directly to RDS
```

```properties
# config/environments/uat.properties
db.host=anshshare-db-uat.xxxxx.us-east-2.rds.amazonaws.com
db.port=5432
```

## RDS Instance Details

- **Instance ID**: anshshare-db-uat
- **Engine**: PostgreSQL 15.10
- **Instance Class**: db.t3.micro (2 vCPU, 1 GB RAM)
- **Storage**: 20 GB GP3 (auto-scales to 100 GB)
- **Multi-AZ**: No (single AZ for cost savings)
- **Public Access**: No (private subnet only)
- **Backup Retention**: 7 days
- **Encryption**: Yes (AWS KMS)
- **Deletion Protection**: No (for UAT)

## Databases Created

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

## Cost Breakdown

### With AWS Free Tier (First 12 Months)
- db.t3.micro: **$0/month** (750 hours free)
- 20 GB storage: **~$2.30/month**
- Backups (7 days): **Free** (first 20 GB)
- **Total: ~$2-5/month**

### Without Free Tier
- db.t3.micro: **~$15/month**
- 20 GB storage: **~$2.30/month**
- Backups (7 days): **Free** (first 20 GB)
- **Total: ~$17-20/month**

## Cost Management

### Stop RDS When Not Needed
```bash
# Stop (max 7 days)
aws rds stop-db-instance \
    --db-instance-identifier anshshare-db-uat \
    --region us-east-2

# Start
aws rds start-db-instance \
    --db-instance-identifier anshshare-db-uat \
    --region us-east-2
```

### Create Snapshots Before Deletion
```bash
aws rds create-db-snapshot \
    --db-instance-identifier anshshare-db-uat \
    --db-snapshot-identifier final-backup-$(date +%Y%m%d) \
    --region us-east-2
```

## Monitoring

### CloudWatch Metrics
- CPU Utilization
- Database Connections
- Free Storage Space
- Read/Write Latency
- Network Throughput

### CloudWatch Logs
- PostgreSQL logs
- Error logs
- Slow query logs

### Access Logs
```bash
aws logs tail /aws/rds/instance/anshshare-db-uat/postgresql \
    --follow \
    --region us-east-2
```

## Backup Strategy

### Automated Backups
- **Schedule**: Daily at 3:00 AM UTC
- **Retention**: 7 days
- **Point-in-time Recovery**: Yes

### Manual Snapshots
- Before major changes
- Before deployments
- Weekly recommended

### Restore Process
1. Identify snapshot to restore
2. Create new RDS instance from snapshot
3. Update configuration with new endpoint
4. Test thoroughly
5. Switch over

## Security

### Network Security
- Private subnet (no public access)
- Security group limits access to EC2 only
- VPC isolation

### Data Security
- Encryption at rest (AWS KMS)
- SSL/TLS available for connections
- IAM database authentication (optional)

### Access Control
- Master user: anshshare_admin
- Application users created by Flyway
- Password rotation recommended

## Troubleshooting

### RDS Not Accessible
1. Check RDS status: "available"
2. Verify security group allows EC2 → RDS:5432
3. Test from EC2: `psql -h <endpoint> -U anshshare_admin`

### Services Can't Connect
1. Check config/environments/uat.properties
2. Verify RDS endpoint in db.host
3. Check service logs: `docker logs auth-service`
4. Test network: `nc -zv <endpoint> 5432`

### Flyway Migrations Fail
1. Check database exists: `\l` in psql
2. Verify migration files in resources/db/migration
3. Check flyway_schema_history table
4. Re-run migrations if safe

## Rollback Instructions

### Delete RDS and Revert
```bash
# 1. Delete RDS stack
aws cloudformation delete-stack \
    --stack-name anshshare-rds-uat \
    --region us-east-2

# 2. Restore backups
cp docker-compose.uat.yml.backup docker-compose.uat.yml
cp config/environments/uat.properties.backup config/environments/uat.properties

# 3. Restart services
docker-compose -f docker-compose.uat.yml down
docker-compose -f docker-compose.uat.yml up -d
```

## Next Steps

1. **Deploy to UAT**: Run the setup script
2. **Test thoroughly**: Verify data persistence
3. **Monitor costs**: Check AWS Cost Explorer
4. **Plan backups**: Schedule manual snapshots
5. **Production ready**: Use similar setup for prod

## Support Resources

- [RDS Deployment Guide](aws/RDS_DEPLOYMENT_GUIDE.md)
- [Quick Start Guide](aws/QUICK_START_RDS.md)
- [Scripts Documentation](aws/scripts/README.md)
- [AWS RDS Documentation](https://docs.aws.amazon.com/rds/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/15/)

## Success Checklist

After setup, verify:

- [ ] RDS instance status is "available"
- [ ] All 11 databases created
- [ ] Configuration updated with RDS endpoint
- [ ] PostgreSQL removed from docker-compose
- [ ] Services restarted on EC2
- [ ] Services can connect to RDS
- [ ] Flyway migrations successful
- [ ] Application accessible and working
- [ ] Data persists after EC2 restart
- [ ] Backups configured correctly
- [ ] CloudWatch monitoring enabled
- [ ] Costs within expected range

---

**Created**: 2026-01-11
**For**: AnshShare Microservices UAT Environment
**Purpose**: Data persistence and better database management
