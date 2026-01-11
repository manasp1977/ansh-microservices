# EC2 Deployment Steps for RDS Migration

## ✅ Local Setup Complete

The following has been completed on your local machine:

- ✅ RDS instance deployed and available
- ✅ Configuration updated (uat.properties)
- ✅ Docker Compose updated (removed PostgreSQL)
- ✅ Changes committed and pushed to GitHub

## 🚀 EC2 Deployment Instructions

### Step 1: SSH to EC2

```bash
ssh -i ~/.ssh/anshshare-key.pem ec2-user@3.16.24.182
```

### Step 2: Pull Latest Changes

```bash
cd ansh-microservices
git pull origin develop
```

Expected output:
```
remote: Enumerating objects: X, done.
remote: Counting objects: 100% (X/X), done.
...
Updating f232473..f3ced7e
Fast-forward
 config/environments/uat.properties | 2 +-
 docker-compose.uat.yml | 24 ---------
 ...
```

### Step 3: Initialize RDS Databases

```bash
chmod +x init-rds-on-ec2.sh
./init-rds-on-ec2.sh
```

This script will:
1. Install PostgreSQL client (if not present)
2. Connect to RDS
3. Create all 11 databases

Expected output:
```
=========================================
Initialize RDS Databases from EC2
=========================================

Installing PostgreSQL client...
Testing connection to RDS...
✓ Connection successful

Creating databases...
  Creating auth_db... created ✓
  Creating receipt_db... created ✓
  Creating listing_db... created ✓
  Creating cart_db... created ✓
  Creating settlement_db... created ✓
  Creating analytics_db... created ✓
  Creating admin_db... created ✓
  Creating user_db... created ✓
  Creating chat_db... created ✓
  Creating customer_db... created ✓
  Creating wishhub_db... created ✓

=========================================
✓ Database Initialization Complete!
=========================================
```

### Step 4: Stop Current Services

```bash
docker-compose -f docker-compose.uat.yml down
```

Expected output:
```
Stopping settlement-service ... done
Stopping analytics-service ... done
Stopping admin-service ... done
...
Removing settlement-service ... done
Removing analytics-service ... done
...
Removing network ansh-microservices_anshshare-network
```

### Step 5: Clean Up Old Volumes

```bash
docker volume prune -f
```

This removes the old PostgreSQL data volume since we're now using RDS.

### Step 6: Start Services with RDS

```bash
docker-compose -f docker-compose.uat.yml up -d
```

Services will now connect to RDS instead of containerized PostgreSQL.

Expected output:
```
Creating network "ansh-microservices_anshshare-network" ... done
Creating eureka-server ... done
Creating api-gateway ... done
Creating auth-service ... done
Creating receipt-service ... done
...
```

### Step 7: Monitor Service Startup

```bash
# Check all services are running
docker-compose -f docker-compose.uat.yml ps

# Watch logs as services start
docker-compose -f docker-compose.uat.yml logs -f
```

Press `Ctrl+C` to stop watching logs.

### Step 8: Verify Database Connections

```bash
# Check auth-service connected and ran migrations
docker logs auth-service 2>&1 | grep -i "flyway.*success\|migration.*complet"

# Should see output like:
# Successfully applied X migrations to schema "public"
```

```bash
# Check other services
docker logs listing-service 2>&1 | grep -i "started.*application"
docker logs cart-service 2>&1 | grep -i "started.*application"
docker logs wishhub-service 2>&1 | grep -i "started.*application"
```

### Step 9: Test the Application

```bash
# Test from EC2
curl http://localhost:8080/api/auth/health

# Expected: HTTP 200 or similar healthy response
```

From your browser:
```
http://3.16.24.182:8080
```

### Step 10: Verify Data Persistence

1. **Create test data:**
   - Sign up with a new user
   - Create a wish in WishHub
   - Create a listing

2. **Test persistence:**
   ```bash
   # On EC2, stop the instance
   sudo shutdown -h now
   ```

3. **Restart from AWS Console:**
   - Go to AWS Console → EC2
   - Select instance → Actions → Start Instance
   - Wait 2-3 minutes

4. **Verify data:**
   - Access http://3.16.24.182:8080
   - Login with test user
   - Check that wishes and listings still exist ✓

## 🔍 Troubleshooting

### Services Won't Start

```bash
# Check logs for specific service
docker logs auth-service

# Common issues:
# 1. Can't connect to RDS
#    → Check security group allows EC2 → RDS on port 5432
# 2. Database doesn't exist
#    → Re-run init-rds-on-ec2.sh
# 3. Flyway migration failed
#    → Check migration files, may need to reset
```

### Database Connection Failed

```bash
# Test connection from EC2
psql -h anshshare-db-uat.cri426askc5s.us-east-2.rds.amazonaws.com \
     -U anshshare_admin \
     -d postgres

# If fails:
# 1. Check RDS is running in AWS Console
# 2. Verify security group rules
# 3. Check VPC/subnet configuration
```

### Service Logs Show Errors

```bash
# View full logs
docker logs auth-service --tail 100

# Common errors and fixes:
# - "Connection refused" → RDS not accessible, check security group
# - "Database does not exist" → Run init-rds-on-ec2.sh
# - "Authentication failed" → Check password in uat.properties
```

## 📊 Verification Checklist

After deployment, verify:

- [ ] All services show as "Up" in `docker-compose ps`
- [ ] No error logs in `docker logs <service>`
- [ ] Application accessible at http://3.16.24.182:8080
- [ ] Can sign up new user
- [ ] Can create wishes/listings
- [ ] Data persists after EC2 restart
- [ ] No PostgreSQL container running (should be removed)

```bash
# Quick verification
docker ps | grep postgres
# Should return nothing (no postgres container)

docker-compose -f docker-compose.uat.yml ps
# All services should show "Up"

curl http://localhost:8080/api/auth/health
# Should return 200 OK
```

## 🎯 Success Criteria

Your deployment is successful when:

1. ✅ All 11 databases created in RDS
2. ✅ All services running without errors
3. ✅ Application accessible and functional
4. ✅ Data persists after EC2 restart
5. ✅ No PostgreSQL container present

## 📝 Post-Deployment

### Monitor RDS

```bash
# Check RDS status
aws rds describe-db-instances \
    --db-instance-identifier anshshare-db-uat \
    --region us-east-2 \
    --query 'DBInstances[0].[DBInstanceStatus,Endpoint.Address]' \
    --output table

# View RDS logs
aws logs tail /aws/rds/instance/anshshare-db-uat/postgresql \
    --follow \
    --region us-east-2
```

### Create First Backup

```bash
aws rds create-db-snapshot \
    --db-instance-identifier anshshare-db-uat \
    --db-snapshot-identifier uat-initial-backup-$(date +%Y%m%d) \
    --region us-east-2
```

### Cost Monitoring

Check costs in AWS Console:
- Billing → Cost Explorer
- Set up budget alerts for $25/month

## 🔗 Related Documentation

- Full Guide: `RDS_DEPLOYMENT_COMPLETE.md`
- Troubleshooting: `aws/RDS_DEPLOYMENT_GUIDE.md`
- Checklist: `RDS_DEPLOYMENT_CHECKLIST.txt`

---

**RDS Endpoint**: `anshshare-db-uat.cri426askc5s.us-east-2.rds.amazonaws.com`
**Username**: `anshshare_admin`
**Password**: `AnshShare2024!`
**Database Count**: 11
**Status**: ✅ Ready for EC2 deployment
