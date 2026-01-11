# Quick Start: RDS PostgreSQL Setup for UAT

## One-Command Setup

```bash
bash aws/scripts/setup-rds-complete.sh
```

That's it! The script will handle everything automatically.

---

## What Happens

1. ✓ Deploys RDS PostgreSQL instance (~10 minutes)
2. ✓ Creates 11 databases
3. ✓ Updates configuration
4. ✓ Removes PostgreSQL container

---

## After Setup

### On Your Local Machine

Configuration files are automatically updated. Commit the changes:

```bash
git add config/environments/uat.properties docker-compose.uat.yml
git commit -m "Switch UAT to RDS PostgreSQL"
git push
```

### On Your EC2 Instance

SSH to EC2 and restart services:

```bash
ssh -i ~/.ssh/anshshare-key.pem ec2-user@<EC2_IP>

cd ansh-microservices
git pull
docker-compose -f docker-compose.uat.yml down
docker-compose -f docker-compose.uat.yml up -d
```

### Verify

```bash
# Check auth-service can connect to RDS
docker logs auth-service 2>&1 | grep -i "flyway.*success\|migration.*success"

# Test the application
curl http://<EC2_IP>:8080/api/auth/health
```

---

## RDS Details

After setup completes, you'll see:

```
RDS Endpoint: anshshare-db-uat.xxxxx.us-east-2.rds.amazonaws.com
Port: 5432
Username: anshshare_admin
Password: (what you entered during setup)

Databases:
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
```

---

## Benefits

✓ **Data Persistence**: Data survives EC2 restarts
✓ **Automated Backups**: 7-day backup retention
✓ **Better Performance**: Dedicated database instance
✓ **Easy Scaling**: Scale storage on demand
✓ **Monitoring**: CloudWatch metrics included

---

## Cost

- **Free Tier**: ~$2-5/month (first 12 months)
- **Regular**: ~$17-20/month
- **Breakdown**:
  - db.t3.micro: $15/mo (free with free tier)
  - 20 GB storage: $2.30/mo
  - Backups: First 20 GB free

---

## Stop RDS to Save Money

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

---

## Rollback

If you need to go back to containerized PostgreSQL:

```bash
# Delete RDS
aws cloudformation delete-stack \
    --stack-name anshshare-rds-uat \
    --region us-east-2

# Restore configs
cp docker-compose.uat.yml.backup docker-compose.uat.yml
cp config/environments/uat.properties.backup config/environments/uat.properties

# Restart services
docker-compose -f docker-compose.uat.yml down
docker-compose -f docker-compose.uat.yml up -d
```

---

## Manual Steps

If you prefer step-by-step control:

```bash
# 1. Deploy RDS
bash aws/scripts/deploy-rds-uat.sh

# 2. Initialize databases (after RDS is available)
bash aws/scripts/init-rds-databases.sh

# 3. Update configuration
bash aws/scripts/update-uat-config-for-rds.sh

# 4. Remove PostgreSQL container
bash aws/scripts/remove-postgres-from-docker-compose.sh
```

---

## Troubleshooting

### Can't connect to RDS

```bash
# Check RDS status
aws rds describe-db-instances \
    --db-instance-identifier anshshare-db-uat \
    --region us-east-2 \
    --query 'DBInstances[0].DBInstanceStatus'

# Should show: "available"
```

### Service won't start

```bash
# Check logs
docker logs auth-service

# Verify config
cat config/environments/uat.properties | grep db.host
```

---

## More Information

- Full Guide: [RDS_DEPLOYMENT_GUIDE.md](RDS_DEPLOYMENT_GUIDE.md)
- Scripts README: [scripts/README.md](scripts/README.md)
- AWS RDS Docs: https://docs.aws.amazon.com/rds/

---

## Support Checklist

Before asking for help, verify:

- [ ] AWS CLI is configured
- [ ] PostgreSQL client (psql) is installed
- [ ] EC2 instance is running
- [ ] RDS status is "available"
- [ ] Security groups allow EC2 → RDS (port 5432)
- [ ] Configuration files are updated
- [ ] Services have been restarted
