# Docker Build and Deployment Guide

This directory contains Docker configurations and scripts for building and deploying AnshShare microservices.

## Quick Start

```bash
# Build all services locally
cd docker/scripts
./build-all.sh

# Build and push to AWS ECR (requires AWS CLI configured)
./build-and-push-all.sh dev us-east-1
```

## Prerequisites

- Docker installed and running
- AWS CLI configured (for ECR push)
- Git (for versioning tags)

## Docker Desktop Installation (Windows)

### System Requirements

- Windows 10/11 64-bit: Pro, Enterprise, or Education (Build 19041 or higher)
- OR Windows 10/11 Home with WSL 2
- Hardware virtualization must be enabled in BIOS
- At least 4GB RAM (8GB+ recommended)

### Installation Steps

**1. Download Docker Desktop**
- Visit: https://www.docker.com/products/docker-desktop/
- Click "Download for Windows"
- Or direct download: https://desktop.docker.com/win/main/amd64/Docker%20Desktop%20Installer.exe

**2. Run the Installer**
```bash
# Double-click Docker Desktop Installer.exe
```
- Accept the license agreement
- Choose configuration options:
  - ✓ Use WSL 2 instead of Hyper-V (recommended)
  - ✓ Add shortcut to desktop
- Click "Install"
- Wait for installation (~5-10 minutes)
- **Restart your computer** (required)

**3. Start Docker Desktop**
- Launch Docker Desktop from Start Menu
- Accept the Docker Subscription Service Agreement
- Complete quick setup tutorial (optional)
- Wait for Docker Engine to start (whale icon in system tray turns green)

**4. Verify Installation**
```bash
# Check Docker version
docker --version

# Verify Docker is running
docker ps

# Run test container
docker run hello-world
```

**Expected output:**
```
$ docker --version
Docker version 24.0.x, build xxxxx

$ docker ps
CONTAINER ID   IMAGE     COMMAND   CREATED   STATUS    PORTS     NAMES

$ docker run hello-world
Hello from Docker!
This message shows that your installation appears to be working correctly.
```

### Troubleshooting Installation

**Issue: "WSL 2 installation is incomplete"**
```bash
# Install WSL 2
wsl --install

# Set WSL 2 as default
wsl --set-default-version 2

# Restart computer
```

**Issue: "Docker Desktop starting..." stuck**
- Restart Docker Desktop
- Check if virtualization is enabled in BIOS
- Try running as administrator

**Issue: "Hardware assisted virtualization must be enabled"**
- Restart computer and enter BIOS/UEFI settings
- Enable Intel VT-x (Intel) or AMD-V (AMD)
- Enable Virtualization Technology
- Save and restart

**Issue: Permission errors when running Docker commands**
```bash
# Add your user to docker-users group
# Open PowerShell as Administrator:
net localgroup docker-users "YOUR_USERNAME" /add

# Log out and log back in
```

## Directory Structure

```
docker/
├── scripts/
│   ├── build-service.sh         # Build single service
│   ├── build-all.sh            # Build all services
│   ├── ecr-login.sh            # Login to AWS ECR
│   ├── push-to-ecr.sh          # Push single service to ECR
│   └── build-and-push-all.sh   # Build and push all services
└── README.md                    # This file
```

## Building Docker Images

### Build All Services

```bash
cd docker/scripts
./build-all.sh [tag]
```

**Example:**
```bash
./build-all.sh latest
./build-all.sh v1.0.0
```

This will build all 12 microservices:
- eureka-server
- api-gateway
- auth-service
- receipt-service
- listing-service
- cart-service
- settlement-service
- analytics-service
- admin-service
- user-service
- chat-service
- customer-service

**Output:** Images tagged as `anshshare/<service-name>:<tag>`

### Build Single Service

```bash
cd docker/scripts
./build-service.sh <service-name> [tag]
```

**Example:**
```bash
./build-service.sh auth-service latest
./build-service.sh api-gateway v1.0.0
```

### Test Local Build

```bash
# Build a service
./build-service.sh auth-service latest

# Run it locally
docker run -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/auth_db \
  anshshare/auth-service:latest
```

## Pushing to AWS ECR

### Prerequisites for ECR

1. **Configure AWS CLI:**
   ```bash
   aws configure
   ```

2. **Create ECR Repositories** (one-time setup):
   ```bash
   # See aws/scripts/create-ecr-repos.sh
   ```

### Login to ECR

```bash
cd docker/scripts
./ecr-login.sh [region] [account-id]
```

**Example:**
```bash
./ecr-login.sh us-east-1
./ecr-login.sh us-east-1 123456789012
```

### Push Single Service

```bash
cd docker/scripts
./push-to-ecr.sh <service-name> <local-tag> <ecr-tag> [region] [account-id]
```

**Example:**
```bash
# Push with custom tag
./push-to-ecr.sh auth-service latest dev-20260104-abc123 us-east-1

# The script will also push as 'latest' tag
```

### Build and Push All Services

```bash
cd docker/scripts
./build-and-push-all.sh <environment> [region]
```

**Example:**
```bash
# Development
./build-and-push-all.sh dev us-east-1

# Staging
./build-and-push-all.sh staging us-east-1

# Production
./build-and-push-all.sh prod us-east-1
```

**Image Tagging Convention:**
- Format: `<environment>-<YYYYMMDD>-<git-sha>`
- Example: `dev-20260104-a1b2c3d`
- Also tagged as: `latest`

## Docker Images

### Image Details

All images use multi-stage builds:
- **Stage 1:** Build with Maven and JDK (eclipse-temurin:17-jdk-alpine)
- **Stage 2:** Runtime with JRE only (eclipse-temurin:17-jre-alpine)

**Benefits:**
- Smaller final images (~200-300MB vs 800MB+)
- Consistent builds across environments
- Security: Non-root user, minimal attack surface
- Health checks included

### Image Layers

Each service image includes:
1. Base Alpine Linux
2. Java 17 JRE
3. Non-root spring user
4. Application JAR
5. Health check configuration

### Security Features

- **Non-root user:** All containers run as `spring` user
- **Read-only filesystem:** Where possible
- **No secrets in image:** All secrets via environment variables
- **Health checks:** Configured for container orchestration
- **Minimal base:** Alpine Linux for small attack surface

## Troubleshooting

### Build Failures

**Issue: Maven dependencies not found**
```bash
# Clear Docker build cache
docker builder prune -af
```

**Issue: Permission denied on scripts**
```bash
chmod +x docker/scripts/*.sh
```

### ECR Push Failures

**Issue: Authentication error**
```bash
# Re-login to ECR
./ecr-login.sh us-east-1
```

**Issue: Repository does not exist**
```bash
# Create ECR repositories first
# See aws/scripts/create-ecr-repos.sh
```

### Large Image Sizes

**Check image sizes:**
```bash
docker images | grep anshshare
```

**Expected sizes:**
- eureka-server: ~250MB
- api-gateway: ~280MB
- Other services: ~250-300MB

If images are >500MB, check:
- Multi-stage build is working
- Using `-alpine` base images
- .dockerignore is in place

## Environment Variables

Common environment variables for running containers:

```bash
SPRING_PROFILES_ACTIVE=aws          # AWS deployment profile
SPRING_DATASOURCE_URL=jdbc:...      # Database connection
SPRING_DATASOURCE_USERNAME=...       # DB username (or use secrets)
SPRING_DATASOURCE_PASSWORD=...       # DB password (or use secrets)
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=...  # Eureka server URL
AWS_REGION=us-east-1                # AWS region
```

## Best Practices

1. **Always tag images with version numbers** in production
2. **Use build-and-push-all.sh** for consistent deployments
3. **Test locally first** before pushing to ECR
4. **Keep .dockerignore updated** to minimize build context
5. **Use AWS Secrets Manager** for sensitive data, not environment variables

## Next Steps

After building and pushing images:

1. Deploy to AWS ECS using CloudFormation templates in `aws/cloudformation/`
2. See `aws/scripts/` for deployment automation
3. Monitor deployments in AWS Console or CLI

## Support

For issues or questions:
- Check build logs in `/tmp/build-<service>.log`
- Check push logs in `/tmp/push-<service>.log`
- Review Dockerfile for specific service
- Check AWS ECR console for repository status
