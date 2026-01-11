# RDS PostgreSQL Deployment Guide for UAT

This guide explains how to set up Amazon RDS PostgreSQL for the UAT environment, replacing the containerized PostgreSQL database running on EC2.

## Why Use RDS?

- **Data Persistence**: Data is retained even if EC2 instance is stopped or terminated
- **Automated Backups**: Daily automated backups with point-in-time recovery
- **Better Performance**: Dedicated database instance with optimized configuration
- **Easier Management**: Automated patching, monitoring, and maintenance
- **Scalability**: Easy to scale storage and compute resources

## Architecture

### Before (Containerized PostgreSQL)
```
EC2 Instance
├── Docker Containers
│   ├── PostgreSQL (all databases)
│   ├── Eureka Server
│   ├── API Gateway
│   └── Microservices (11 services)
```

### After (RDS PostgreSQL)
```
EC2 Instance                  RDS Instance
├── Docker Containers         ├── PostgreSQL 15.10
│   ├── Eureka Server         ├── auth_db
│   ├── API Gateway           ├── receipt_db
│   └── Microservices ------->├── listing_db
                              ├── cart_db
                              ├── settlement_db
                              ├── analytics_db
                              ├── admin_db
                              ├── user_db
                              ├── chat_db
                              ├── customer_db
                              └── wishhub_db
```

## Prerequisites

1. AWS CLI configured with appropriate credentials
2. PostgreSQL client (psql) installed locally
3. Existing EC2 instance running in UAT environment
4. SSH access to EC2 instance

## Deployment Steps

### Step 1: Deploy RDS Instance

Run the RDS deployment script:

```bash
cd /path/to/ansh-microservices
bash aws/scripts/deploy-rds-uat.sh
```

This script will:
- Fetch VPC and subnet information from your existing EC2 instance
- Create a second subnet if needed (RDS requires 2 subnets in different AZs)
- Deploy the RDS CloudFormation stack
- Configure security groups to allow EC2 -> RDS communication

**Note**: RDS deployment takes 5-10 minutes. Wait for the status to show "Available" in AWS Console.

### Step 2: Initialize Databases

Once RDS is available, create all the required databases:

```bash
bash aws/scripts/init-rds-databases.sh
```

This script will:
- Connect to the RDS instance
- Create all 11 databases (auth_db, receipt_db, listing_db, etc.)
- Verify database creation

### Step 3: Update UAT Configuration

Update the configuration to use RDS endpoint:

```bash
bash aws/scripts/update-uat-config-for-rds.sh
```

This script will:
- Fetch the RDS endpoint from CloudFormation
- Update `config/environments/uat.properties` with the RDS endpoint
- Create a backup of the original configuration

### Step 4: Remove PostgreSQL Container

Update docker-compose.uat.yml to remove the PostgreSQL service:

```bash
bash aws/scripts/remove-postgres-from-docker-compose.sh
```

This script will:
- Remove the postgres service from docker-compose.uat.yml
- Remove postgres from all depends_on clauses
- Remove the postgres_data volume
- Create a backup of the original file

### Step 5: Verify Configuration

Check that the configuration is correct:

```bash
# Check uat.properties
cat config/environments/uat.properties | grep db.host
# Should show: db.host=anshshare-db-uat.xxxxx.us-east-2.rds.amazonaws.com

# Check docker-compose.uat.yml
grep -A 5 "postgres:" docker-compose.uat.yml
# Should return nothing (postgres service removed)
```

### Step 6: Redeploy Services

On your EC2 instance, redeploy the services:

```bash
# SSH to EC2
ssh -i ~/.ssh/anshshare-key.pem ec2-user@<EC2_IP>

# Stop existing services
cd ansh-microservices
docker-compose -f docker-compose.uat.yml down

# Pull latest images (if you rebuilt)
docker-compose -f docker-compose.uat.yml pull

# Start services (PostgreSQL container will not start)
docker-compose -f docker-compose.uat.yml up -d

# Check logs
docker-compose -f docker-compose.uat.yml logs -f auth-service
```

### Step 7: Verify Database Connections

Check that services can connect to RDS:

```bash
# Check auth-service logs
docker logs auth-service 2>&1 | grep -i "database\|flyway\|postgres"

# Should see successful Flyway migrations and database connections
```

### Step 8: Test Application

1. Access the application via browser: `http://<EC2_IP>:8080`
2. Sign up a new user
3. Create some test data (listings, wishes, etc.)
4. Stop the EC2 instance
5. Start the EC2 instance again
6. Verify data is still present (persistent in RDS)

## RDS Configuration

The RDS instance is configured with:

- **Engine**: PostgreSQL 15.10
- **Instance Class**: db.t3.micro (eligible for free tier for 12 months)
- **Storage**: 20 GB GP3 (auto-scaling up to 100 GB)
- **Backup Retention**: 7 days
- **Backup Window**: 3:00 AM - 4:00 AM UTC
- **Maintenance Window**: Sunday 4:00 AM - 5:00 AM UTC
- **Encryption**: Enabled
- **Deletion Protection**: Disabled (for UAT)
- **Public Access**: Disabled (only accessible from EC2)

## Cost Estimate

- **db.t3.micro**: ~$15/month (or free tier for first 12 months)
- **Storage (20 GB)**: ~$2.30/month
- **Backup Storage**: First 20 GB free, then $0.095/GB/month
- **Total**: ~$17-20/month (or ~$2-5/month if using free tier)

## Security

1. **Network Isolation**: RDS is in private subnet, not publicly accessible
2. **Security Groups**: Only EC2 security group can access RDS on port 5432
3. **Encryption**: Data encrypted at rest using AWS KMS
4. **Backups**: Automated daily backups with 7-day retention
5. **SSL/TLS**: Connections can be encrypted (optional)

## Backup and Restore

### Automated Backups
- Daily automated backups during backup window (3:00-4:00 AM)
- 7-day retention period
- Point-in-time recovery available

### Manual Snapshots
```bash
# Create manual snapshot
aws rds create-db-snapshot \
    --db-instance-identifier anshshare-db-uat \
    --db-snapshot-identifier anshshare-uat-manual-backup-$(date +%Y%m%d) \
    --region us-east-2
```

### Restore from Snapshot
```bash
# List available snapshots
aws rds describe-db-snapshots \
    --db-instance-identifier anshshare-db-uat \
    --region us-east-2

# Restore from snapshot (creates new instance)
aws rds restore-db-instance-from-db-snapshot \
    --db-instance-identifier anshshare-db-uat-restored \
    --db-snapshot-identifier <snapshot-id> \
    --region us-east-2
```

## Monitoring

### CloudWatch Metrics
Access metrics in AWS Console > RDS > Databases > anshshare-db-uat > Monitoring

Key metrics to monitor:
- **CPUUtilization**: Should be < 80%
- **DatabaseConnections**: Track active connections
- **FreeStorageSpace**: Ensure sufficient space
- **ReadLatency/WriteLatency**: Monitor query performance

### CloudWatch Logs
PostgreSQL logs are exported to CloudWatch Logs:
- AWS Console > CloudWatch > Log Groups > /aws/rds/instance/anshshare-db-uat/postgresql

## Troubleshooting

### Cannot Connect to RDS

1. **Check Security Group**:
   ```bash
   aws ec2 describe-security-groups \
       --group-ids <rds-security-group-id> \
       --region us-east-2
   ```

2. **Check RDS Status**:
   ```bash
   aws rds describe-db-instances \
       --db-instance-identifier anshshare-db-uat \
       --region us-east-2 \
       --query 'DBInstances[0].DBInstanceStatus'
   ```

3. **Test from EC2**:
   ```bash
   # SSH to EC2
   ssh -i ~/.ssh/anshshare-key.pem ec2-user@<EC2_IP>

   # Install psql if not available
   sudo yum install -y postgresql15

   # Test connection
   psql -h <rds-endpoint> -U anshshare_admin -d postgres
   ```

### Flyway Migration Issues

If Flyway migrations fail:

1. Check database exists:
   ```sql
   \l  -- list all databases
   ```

2. Check migration history:
   ```sql
   SELECT * FROM flyway_schema_history;
   ```

3. Reset migrations (UAT only):
   ```sql
   DROP TABLE flyway_schema_history;
   -- Then restart service to re-run migrations
   ```

### High Storage Usage

1. Check database sizes:
   ```sql
   SELECT
       datname,
       pg_size_pretty(pg_database_size(datname)) AS size
   FROM pg_database
   ORDER BY pg_database_size(datname) DESC;
   ```

2. Check table sizes:
   ```sql
   SELECT
       schemaname,
       tablename,
       pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
   FROM pg_tables
   ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC
   LIMIT 10;
   ```

3. Enable auto-scaling (already configured, max 100 GB)

## Cleanup (Development Only)

To remove RDS and revert to containerized PostgreSQL:

```bash
# Delete RDS stack
aws cloudformation delete-stack \
    --stack-name anshshare-rds-uat \
    --region us-east-2

# Restore original docker-compose.uat.yml
cp docker-compose.uat.yml.backup docker-compose.uat.yml

# Restore original uat.properties
cp config/environments/uat.properties.backup config/environments/uat.properties
```

## Next Steps for Production

When deploying to production:

1. **Enable Deletion Protection**: Prevent accidental deletion
2. **Enable Multi-AZ**: High availability across multiple zones
3. **Increase Backup Retention**: 14-30 days
4. **Enable Performance Insights**: Advanced monitoring
5. **Use Larger Instance**: db.t3.small or db.t3.medium
6. **Enable SSL/TLS**: Require encrypted connections
7. **Implement Read Replicas**: For read-heavy workloads
8. **Set up CloudWatch Alarms**: Alert on critical metrics

## References

- [AWS RDS PostgreSQL Documentation](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_PostgreSQL.html)
- [RDS Best Practices](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_BestPractices.html)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/15/index.html)
