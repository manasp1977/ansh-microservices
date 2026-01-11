# Ansh Microservices - Deployment Guide

This guide covers the complete deployment process for the Ansh microservices application across multiple environments.

## Quick Start

### Deploy to any environment in 3 steps:

1. **Choose your environment:**
   ```bash
   ./deploy.sh
   ```

2. **Select environment when prompted:**
   - `1` for local development
   - `2` for UAT (AWS EC2)
   - `3` for production (future)

3. **Choose deployment action:**
   - Build and deploy services
   - Deploy existing images
   - Stop services
   - View logs
   - Rebuild specific service

That's it! The script handles everything else.

## Environments

### Local Development
- **Purpose**: Local development and testing
- **Database**: PostgreSQL on localhost
- **Credentials**: postgres/manasp1977
- **Access**: http://localhost:8080

### UAT (User Acceptance Testing)
- **Purpose**: Testing before production
- **Infrastructure**: AWS EC2 (3.16.24.182)
- **Database**: PostgreSQL in Docker
- **Credentials**: anshshare_admin/AnshShare2024!
- **Access**: https://www.splitsavers.com

### Production
- **Purpose**: Live production environment
- **Infrastructure**: To be configured
- **Database**: RDS or dedicated PostgreSQL
- **Security**: Requires environment variables for all secrets

## Project Structure

```
ansh-microservices/
├── deploy.sh                        # Main deployment script
├── docker-compose.local.yml         # Local environment
├── docker-compose.uat.yml           # UAT environment
├── docker-compose.prod.yml          # Production environment
├── init-databases.sql               # UAT/Prod DB init
├── init-databases-local.sql         # Local DB init
├── ENVIRONMENT_SETUP.md             # Detailed environment guide
├── config/environments/             # Environment properties
└── scripts/                         # Utility scripts
```

## Environment-Specific Configuration

Each service automatically uses the correct configuration based on the `SPRING_PROFILES_ACTIVE` environment variable:

- **local**: Uses application-local.yml
- **uat**: Uses application-uat.yml
- **prod**: Uses application-prod.yml

### What's Different Between Environments?

| Configuration | Local | UAT | Production |
|--------------|-------|-----|-----------|
| Database Host | localhost | postgres (Docker) | External (RDS) |
| Database User | postgres | anshshare_admin | From env var |
| Database Password | manasp1977 | AnshShare2024! | From env var |
| Eureka URL | localhost:8761 | eureka-server:8761 | From env var |
| CORS Origins | localhost:* | splitsavers.com + S3 | splitsavers.com only |
| JWT Secret | Default | Default | Required env var |
| Logging Level | DEBUG | DEBUG | WARN |
| SQL Logging | Enabled | Disabled | Disabled |

## Services

The application consists of the following microservices:

| Service | Port | Purpose |
|---------|------|---------|
| Eureka Server | 8761 | Service discovery |
| API Gateway | 8080 | Entry point, routing, CORS |
| Auth Service | 8081 | Authentication & authorization |
| Receipt Service | 8082 | Receipt management |
| Listing Service | 8083 | Product listings |
| Cart Service | 8084 | Shopping cart |
| Settlement Service | 8085 | Payment settlements |
| Analytics Service | 8086 | Analytics & reporting |
| Admin Service | 8087 | Admin operations |
| User Service | 8088 | User profiles |
| Customer Service | 8089 | Customer support |
| Chat Service | 8090 | Real-time chat |
| WishHub Service | 8091 | Wish lists |

## Common Commands

### Deploy Backend

```bash
# Interactive deployment (recommended)
./deploy.sh

# Manual deployment to specific environment
docker-compose -f docker-compose.local.yml up -d    # Local
docker-compose -f docker-compose.uat.yml up -d      # UAT
docker-compose -f docker-compose.prod.yml up -d     # Production
```

### Deploy Frontend

```bash
cd ../ansh-ui-new
./deploy-frontend.sh
```

### View Logs

```bash
# All services
docker-compose -f docker-compose.<env>.yml logs -f

# Specific service
docker-compose -f docker-compose.<env>.yml logs -f auth-service

# Last 100 lines
docker-compose -f docker-compose.<env>.yml logs --tail=100 -f
```

### Check Service Health

```bash
# All services status
docker-compose -f docker-compose.<env>.yml ps

# Check specific service health
curl http://localhost:8081/actuator/health
```

### Rebuild Single Service

```bash
# Using deploy script (recommended)
./deploy.sh
# Select option 5, enter service name

# Manual rebuild
mvn clean package -DskipTests -pl auth-service -am
docker build -t anshshare/auth-service:latest -f auth-service/Dockerfile auth-service
docker-compose -f docker-compose.<env>.yml up -d --no-deps auth-service
```

### Stop Services

```bash
docker-compose -f docker-compose.<env>.yml down

# Stop and remove volumes (WARNING: Deletes data!)
docker-compose -f docker-compose.<env>.yml down -v
```

## Environment Variables for Production

Production deployment requires these environment variables:

```bash
export JWT_SECRET="<strong-base64-encoded-secret>"
export DB_URL="jdbc:postgresql://<host>:5432/<database>"
export DB_USERNAME="<username>"
export DB_PASSWORD="<strong-password>"
export EUREKA_URL="http://<eureka-host>:8761/eureka/"
```

## UAT Deployment to AWS EC2

### First Time Setup

1. SSH into EC2:
   ```bash
   ssh -i your-key.pem ubuntu@3.16.24.182
   ```

2. Clone repository:
   ```bash
   git clone <repository-url>
   cd ansh-microservices
   ```

3. Build and push images (from local machine):
   ```bash
   ./scripts/build-and-push.sh
   ```

4. Deploy on EC2:
   ```bash
   docker-compose -f docker-compose.uat.yml up -d
   ```

### Update Deployment

1. Pull latest code:
   ```bash
   git pull origin main
   ```

2. Pull latest images:
   ```bash
   docker-compose -f docker-compose.uat.yml pull
   ```

3. Restart services:
   ```bash
   docker-compose -f docker-compose.uat.yml up -d
   ```

## Troubleshooting

### Services won't start

Check logs:
```bash
docker-compose -f docker-compose.<env>.yml logs <service-name>
```

Common issues:
- Database not ready: Wait for postgres healthcheck
- Port already in use: Check with `netstat -tulpn | grep <port>`
- Memory issues: Increase Docker memory allocation

### Database connection errors

Verify connection:
```bash
docker-compose -f docker-compose.<env>.yml exec postgres psql -U <username> -d <database>
```

Check Flyway migrations:
```bash
docker-compose -f docker-compose.<env>.yml logs <service-name> | grep Flyway
```

### Service not registered in Eureka

1. Check Eureka dashboard: http://localhost:8761 (or EC2 IP)
2. Verify `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` is correct
3. Check service logs for Eureka registration messages

### CORS errors

1. Verify frontend origin matches CORS configuration in `api-gateway/application-<env>.yml`
2. Check browser console for exact origin being sent
3. Ensure API Gateway is using correct profile

## Monitoring

### Health Endpoints

All services expose health endpoints:
```bash
curl http://localhost:<port>/actuator/health
```

### Eureka Dashboard

View registered services:
- Local: http://localhost:8761
- UAT: http://3.16.24.182:8761

### Docker Stats

Monitor resource usage:
```bash
docker stats
```

## Security Best Practices

1. **Never commit secrets**: Use environment variables for production
2. **Strong passwords**: Use strong, unique passwords for each environment
3. **JWT secrets**: Use different JWT secrets for each environment
4. **Database access**: Restrict database access by IP/security group
5. **CORS**: Only allow necessary origins
6. **HTTPS**: Use HTTPS in production (handled by CloudFront/ALB)

## Backup and Recovery

### Database Backup

```bash
# Backup
docker-compose -f docker-compose.<env>.yml exec postgres pg_dump -U <username> <database> > backup.sql

# Restore
docker-compose -f docker-compose.<env>.yml exec -T postgres psql -U <username> <database> < backup.sql
```

### Configuration Backup

Always commit configuration changes to git:
```bash
git add .
git commit -m "Update environment configuration"
git push origin main
```

## Performance Tuning

### Database Connection Pool

Adjust in `application-<env>.yml`:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
```

### JVM Memory

Set in docker-compose file:
```yaml
environment:
  - JAVA_OPTS=-Xmx512m -Xms256m
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Deploy to UAT

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build and Deploy
        run: |
          ./deploy.sh <<< "2
1"
```

## Support

For detailed environment configuration information, see:
- [ENVIRONMENT_SETUP.md](ENVIRONMENT_SETUP.md) - Comprehensive environment guide
- [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md) - Docker-specific deployment guide

## Migration from Old Setup

If you're migrating from the old single-environment setup:

1. Back up current data
2. Stop old services: `docker-compose down`
3. Use new environment-specific files
4. Deploy with: `./deploy.sh`

The new setup is backward compatible - existing services will continue to work.

## Quick Reference

| Task | Command |
|------|---------|
| Deploy | `./deploy.sh` |
| View logs | `docker-compose -f docker-compose.<env>.yml logs -f` |
| Stop services | `docker-compose -f docker-compose.<env>.yml down` |
| Rebuild service | `./deploy.sh` → Option 5 |
| Check health | `curl http://localhost:<port>/actuator/health` |
| View Eureka | http://localhost:8761 or http://3.16.24.182:8761 |
| Database backup | `docker-compose exec postgres pg_dump ...` |

---

**Last Updated**: 2026-01-10

For questions or issues, refer to the detailed guides or check service logs.
