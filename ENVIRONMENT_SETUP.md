# Environment-Specific Configuration Guide

This project supports multiple deployment environments with environment-specific configurations.

## Supported Environments

1. **Local** - Local development environment
2. **UAT** - User Acceptance Testing environment (AWS EC2)
3. **Prod** - Production environment (Future)

## Project Structure

```
ansh-microservices/
├── config/
│   └── environments/
│       ├── local.properties      # Local environment properties
│       ├── uat.properties        # UAT environment properties
│       └── prod.properties       # Production environment properties
├── docker-compose.local.yml      # Local Docker Compose configuration
├── docker-compose.uat.yml        # UAT Docker Compose configuration
├── docker-compose.prod.yml       # Production Docker Compose configuration
├── init-databases.sql            # UAT/Prod database initialization
├── init-databases-local.sql      # Local database initialization
├── deploy.sh                     # Interactive deployment script
└── scripts/
    ├── generate-profiles.sh      # Generate Spring Boot profiles
    └── generate-docker-compose.sh # Generate Docker Compose files
```

Each service has environment-specific application profiles:
```
<service>/src/main/resources/
├── application.yml               # Base configuration
├── application-local.yml         # Local-specific configuration
├── application-uat.yml           # UAT-specific configuration
└── application-prod.yml          # Production-specific configuration
```

## Environment Configurations

### Local Environment

- **Database**: PostgreSQL on localhost:5432
  - Username: `postgres`
  - Password: `manasp1977`
- **Eureka**: http://localhost:8761/eureka/
- **CORS**: Allows localhost origins
- **Spring Profile**: `local`

### UAT Environment

- **Database**: PostgreSQL in Docker container
  - Host: `postgres` (Docker container name)
  - Username: `anshshare_admin`
  - Password: `AnshShare2024!`
- **Eureka**: http://eureka-server:8761/eureka/
- **CORS**: Allows splitsavers.com and S3 bucket URLs
- **Spring Profile**: `uat`
- **EC2 IP**: 3.16.24.182

### Production Environment

- **Database**: To be configured (RDS or dedicated PostgreSQL)
  - Configured via environment variables
  - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- **Eureka**: Configured via `EUREKA_URL`
- **CORS**: Only production domains
- **Spring Profile**: `prod`
- **JWT Secret**: Must be provided via `JWT_SECRET` environment variable

## Deployment

### Quick Start

Use the interactive deployment script:

```bash
./deploy.sh
```

The script will prompt you to:
1. Select environment (local/uat/prod)
2. Choose deployment action:
   - Build and deploy services
   - Deploy existing images
   - Stop services
   - View logs
   - Rebuild specific service

### Manual Deployment

#### Local Environment

1. Build the services:
   ```bash
   mvn clean package -DskipTests
   ```

2. Build Docker images:
   ```bash
   docker build -t anshshare/<service>:latest -f <service>/Dockerfile <service>
   ```

3. Deploy with Docker Compose:
   ```bash
   docker-compose -f docker-compose.local.yml up -d
   ```

#### UAT Environment

1. Build and push images:
   ```bash
   mvn clean package -DskipTests
   docker build -t anshshare/<service>:latest -f <service>/Dockerfile <service>
   docker push anshshare/<service>:latest
   ```

2. On EC2 instance:
   ```bash
   docker-compose -f docker-compose.uat.yml pull
   docker-compose -f docker-compose.uat.yml up -d
   ```

#### Production Environment

1. Set required environment variables:
   ```bash
   export JWT_SECRET=<strong-production-secret>
   export DB_URL=jdbc:postgresql://<prod-db-host>:5432/<db-name>
   export DB_USERNAME=<prod-username>
   export DB_PASSWORD=<prod-password>
   ```

2. Deploy:
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

## Service URLs by Environment

### Local

| Service | URL |
|---------|-----|
| API Gateway | http://localhost:8080 |
| Eureka | http://localhost:8761 |
| Auth Service | http://localhost:8081 |
| Receipt Service | http://localhost:8082 |
| Listing Service | http://localhost:8083 |
| Cart Service | http://localhost:8084 |
| Settlement Service | http://localhost:8085 |
| Analytics Service | http://localhost:8086 |
| Admin Service | http://localhost:8087 |
| User Service | http://localhost:8088 |
| Customer Service | http://localhost:8089 |
| Chat Service | http://localhost:8090 |
| WishHub Service | http://localhost:8091 |

### UAT

| Service | URL |
|---------|-----|
| API Gateway | http://3.16.24.182:8080 |
| Frontend | https://www.splitsavers.com |
| API | https://api.splitsavers.com |

### Production

To be configured based on production infrastructure.

## CORS Configuration by Environment

### Local
- `http://localhost:*`
- `http://127.0.0.1:*`

### UAT
- `http://3.16.24.182:*`
- `http://s3-ansh-ui.s3-website.us-east-2.amazonaws.com`
- `http://*.s3-website.*.amazonaws.com`
- `http://splitsavers.com`
- `http://www.splitsavers.com`
- `https://splitsavers.com`
- `https://www.splitsavers.com`
- `https://api.splitsavers.com`

### Production
- `https://splitsavers.com`
- `https://www.splitsavers.com`
- `https://api.splitsavers.com`

## Database Management

### Creating New Databases

**Local**: Update `init-databases-local.sql`

**UAT/Prod**: Update `init-databases.sql`

### Flyway Migrations

Flyway is enabled in all environments with different validation settings:

- **Local/UAT**: `validate-on-migrate: false` (allows migrations to run freely)
- **Production**: `validate-on-migrate: true` (strict validation)

Migration files are located in each service's `src/main/resources/db/migration/` directory.

## Environment Variables

### Required for Production

| Variable | Description | Example |
|----------|-------------|---------|
| `JWT_SECRET` | JWT signing secret (base64 encoded) | Required |
| `DB_URL` | Database JDBC URL | `jdbc:postgresql://host:5432/db` |
| `DB_USERNAME` | Database username | `prod_admin` |
| `DB_PASSWORD` | Database password | Strong password required |
| `EUREKA_URL` | Eureka server URL | `http://eureka:8761/eureka/` |

### Optional

| Variable | Description | Default |
|----------|-------------|---------|
| `FILE_UPLOAD_DIR` | File upload directory | `/app/uploads` |
| `FILE_BASE_URL` | Base URL for uploaded files | Service-specific |

## Switching Environments

To switch from one environment to another:

1. Stop current environment:
   ```bash
   docker-compose -f docker-compose.<current-env>.yml down
   ```

2. Deploy new environment:
   ```bash
   ./deploy.sh
   # Select desired environment
   ```

## Troubleshooting

### Services not connecting to database

Check Spring profile is correctly set:
```bash
docker-compose -f docker-compose.<env>.yml exec <service> env | grep SPRING_PROFILES_ACTIVE
```

### CORS errors

Verify CORS configuration in `application-<env>.yml` for api-gateway matches your frontend origin.

### Service discovery issues

Check Eureka dashboard and verify services are registered:
- Local: http://localhost:8761
- UAT: http://3.16.24.182:8761

## Best Practices

1. **Never commit secrets**: Use environment variables for sensitive data in production
2. **Test locally first**: Always test changes in local environment before deploying to UAT/Prod
3. **Use proper profiles**: Ensure correct Spring profile is activated for each environment
4. **Monitor logs**: Use `docker-compose logs -f` to monitor service health
5. **Backup databases**: Regular backups, especially before migrations in production
6. **Rolling deployments**: For production, consider blue-green or rolling deployment strategies

## Maintenance Scripts

### Rebuild all services
```bash
./deploy.sh
# Choose: 1 (Build and deploy services)
```

### Rebuild single service
```bash
./deploy.sh
# Choose: 5 (Rebuild specific service)
# Enter service name
```

### View logs
```bash
docker-compose -f docker-compose.<env>.yml logs -f <service-name>
```

### Check service health
```bash
docker-compose -f docker-compose.<env>.yml ps
```

### Clean up
```bash
# Stop and remove containers
docker-compose -f docker-compose.<env>.yml down

# Remove volumes (WARNING: This deletes data!)
docker-compose -f docker-compose.<env>.yml down -v
```

## Support

For issues or questions about environment configuration:
1. Check service logs: `docker-compose -f docker-compose.<env>.yml logs <service>`
2. Verify environment variables are set correctly
3. Ensure Spring profile matches the intended environment
4. Check database connectivity and credentials
