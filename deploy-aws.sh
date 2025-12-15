#!/bin/bash
# AWS Deployment Script for RevHub

echo "Starting RevHub AWS Deployment..."

# Download JARs from S3
aws s3 sync s3://revhub-deployment-rameezshaik/jars/ /opt/revhub/

# Stop existing services
pkill -f "java -jar"

# Start services with AWS configuration
cd /opt/revhub

# Start API Gateway first
nohup java -jar -Dspring.profiles.active=aws api-gateway-1.0.0.jar > api-gateway.log 2>&1 &
sleep 10

# Start microservices
nohup java -jar -Dspring.profiles.active=aws auth-service-1.0.0.jar > auth-service.log 2>&1 &
nohup java -jar -Dspring.profiles.active=aws user-service-1.0.0.jar > user-service.log 2>&1 &
nohup java -jar -Dspring.profiles.active=aws post-service-1.0.0.jar > post-service.log 2>&1 &
nohup java -jar -Dspring.profiles.active=aws chat-service-1.0.0.jar > chat-service.log 2>&1 &
nohup java -jar -Dspring.profiles.active=aws follow-service-1.0.0.jar > follow-service.log 2>&1 &
nohup java -jar -Dspring.profiles.active=aws notification-service-1.0.0.jar > notification-service.log 2>&1 &
nohup java -jar -Dspring.profiles.active=aws search-service-1.0.0.jar > search-service.log 2>&1 &

echo "RevHub services started on AWS!"
echo "API Gateway: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):8080"