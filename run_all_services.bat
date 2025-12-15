@echo off
echo Starting All Microservices...

echo Starting API Gateway...
start "API Gateway" cmd /c "cd microservices/api-gateway && mvn spring-boot:run"
timeout /t 5

echo Starting Auth Service...
start "Auth Service" cmd /c "cd microservices/auth-service && mvn spring-boot:run"
timeout /t 5

echo Starting User Service...
start "User Service" cmd /c "cd microservices/user-service && mvn spring-boot:run"

echo Starting Post Service...
start "Post Service" cmd /c "cd microservices/post-service && mvn spring-boot:run"

echo Starting Notification Service...
start "Notification Service" cmd /c "cd microservices/notification-service && mvn spring-boot:run"

echo Starting Chat Service...
start "Chat Service" cmd /c "cd microservices/chat-service && mvn spring-boot:run"

echo Starting Follow Service...
start "Follow Service" cmd /c "cd microservices/follow-service && mvn spring-boot:run"

echo Starting Search Service...
start "Search Service" cmd /c "cd microservices/search-service && mvn spring-boot:run"

echo All services launch commands issued. Please check the individual windows for startup status.
pause
