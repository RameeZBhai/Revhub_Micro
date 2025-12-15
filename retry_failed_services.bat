@echo off
echo Retrying Failed Services...

echo Starting User Service...
start "User Service" cmd /c "cd microservices/user-service && mvn spring-boot:run"

echo Starting Chat Service...
start "Chat Service" cmd /c "cd microservices/chat-service && mvn spring-boot:run"

echo Starting Follow Service...
start "Follow Service" cmd /c "cd microservices/follow-service && mvn spring-boot:run"

echo Retry launch commands issued.
pause
