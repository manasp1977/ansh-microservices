#!/bin/bash
# Deploy AnshShare microservices to EC2 instance

set -e

EC2_IP="3.21.126.175"
EC2_USER="ec2-user"
KEY_FILE="ec2-key-pair.pem"

echo "🚀 Deploying AnshShare Microservices to EC2"
echo "============================================"
echo ""

# Step 1: Install Docker on EC2
echo "📦 Step 1: Installing Docker on EC2..."
ssh -i $KEY_FILE $EC2_USER@$EC2_IP << 'ENDSSH'
  # Update system
  sudo yum update -y

  # Install Docker
  sudo yum install -y docker

  # Start Docker service
  sudo systemctl start docker
  sudo systemctl enable docker

  # Add ec2-user to docker group
  sudo usermod -a -G docker ec2-user

  # Install Docker Compose
  sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
  sudo chmod +x /usr/local/bin/docker-compose

  # Verify installation
  docker --version
  docker-compose --version

  echo "✅ Docker and Docker Compose installed successfully"
ENDSSH

echo ""
echo "⚠️  Please log out and log back in for Docker permissions to take effect"
echo ""
read -p "Press Enter after you've logged out and back in..."

# Step 2: Transfer files to EC2
echo "📤 Step 2: Transferring deployment files..."
scp -i $KEY_FILE docker-compose.yml $EC2_USER@$EC2_IP:~/
scp -i $KEY_FILE init-databases.sql $EC2_USER@$EC2_IP:~/

echo "✅ Files transferred"
echo ""

# Step 3: Save and load Docker images
echo "🐳 Step 3: Transferring Docker images..."

SERVICES=(
  "eureka-server"
  "api-gateway"
  "auth-service"
  "receipt-service"
  "listing-service"
  "cart-service"
  "settlement-service"
  "analytics-service"
  "admin-service"
  "user-service"
  "chat-service"
  "customer-service"
)

for SERVICE in "${SERVICES[@]}"; do
  echo "  Saving $SERVICE..."
  docker save anshshare/$SERVICE:latest | gzip > /tmp/${SERVICE}.tar.gz

  echo "  Transferring $SERVICE..."
  scp -i $KEY_FILE /tmp/${SERVICE}.tar.gz $EC2_USER@$EC2_IP:~/

  echo "  Loading $SERVICE on EC2..."
  ssh -i $KEY_FILE $EC2_USER@$EC2_IP "docker load < ${SERVICE}.tar.gz && rm ${SERVICE}.tar.gz"

  rm /tmp/${SERVICE}.tar.gz
  echo "  ✅ $SERVICE deployed"
done

echo ""
echo "✅ All Docker images transferred"
echo ""

# Step 4: Start services
echo "🚀 Step 4: Starting all services..."
ssh -i $KEY_FILE $EC2_USER@$EC2_IP << 'ENDSSH'
  cd ~
  docker-compose up -d

  echo ""
  echo "⏳ Waiting for services to start..."
  sleep 30

  echo ""
  echo "📊 Service Status:"
  docker-compose ps
ENDSSH

echo ""
echo "🎉 Deployment Complete!"
echo "======================="
echo ""
echo "Access your services at:"
echo "  Eureka Dashboard: http://$EC2_IP:8761"
echo "  API Gateway:      http://$EC2_IP:8080"
echo ""
echo "To view logs:"
echo "  ssh -i $KEY_FILE $EC2_USER@$EC2_IP"
echo "  docker-compose logs -f [service-name]"
echo ""
echo "To stop services:"
echo "  ssh -i $KEY_FILE $EC2_USER@$EC2_IP"
echo "  docker-compose down"
echo ""
