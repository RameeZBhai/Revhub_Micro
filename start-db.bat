@echo off
echo Starting MongoDB...
cd microservices
docker-compose up -d mongodb
echo MongoDB started.
pause
