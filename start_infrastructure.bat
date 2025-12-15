@echo off
echo Starting Microservices Infrastructure...
cd microservices
docker-compose up -d
echo Infrastructure started!
echo Consul: http://localhost:8500
echo Zipkin: http://localhost:9411
echo Kafka: localhost:9092
pause
