@echo off
echo Starting RevHub deployment...

set DEPLOY_PATH=C:\RevHub\deploy

echo Stopping existing services...
taskkill /F /IM java.exe 2>nul
timeout /t 5

echo Creating deployment directory...
if not exist %DEPLOY_PATH% mkdir %DEPLOY_PATH%

echo Starting API Gateway...
start /B java -jar %DEPLOY_PATH%\api-gateway-*.jar
timeout /t 10

echo Starting microservices...
start /B java -jar %DEPLOY_PATH%\auth-service-*.jar
start /B java -jar %DEPLOY_PATH%\user-service-*.jar
start /B java -jar %DEPLOY_PATH%\post-service-*.jar
start /B java -jar %DEPLOY_PATH%\chat-service-*.jar
start /B java -jar %DEPLOY_PATH%\follow-service-*.jar
start /B java -jar %DEPLOY_PATH%\notification-service-*.jar
start /B java -jar %DEPLOY_PATH%\search-service-*.jar

echo Deployment complete!
echo Frontend: http://localhost:4200
echo API Gateway: http://localhost:8080