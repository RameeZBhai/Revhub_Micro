# Jenkins Deployment Setup

## Prerequisites
1. Install Jenkins on Windows
2. Install required plugins:
   - Git Plugin
   - Maven Integration Plugin
   - NodeJS Plugin
   - Credentials Plugin

## Jenkins Configuration

### 1. Global Tool Configuration
- **JDK**: Configure JDK-17
- **Maven**: Configure Maven-3.8
- **NodeJS**: Configure NodeJS-18

### 2. Credentials Setup
- Add credential `db-password` with value `10532`

### 3. Create Pipeline Job
1. New Item → Pipeline
2. Pipeline → Definition: Pipeline script from SCM
3. SCM: Git
4. Repository URL: your-repo-url
5. Script Path: Jenkinsfile

## Manual Deployment
Run `deploy.bat` to start services manually after build.

## Service Ports
- API Gateway: 8080
- Auth Service: 8082
- User Service: 8089
- Post Service: 8083
- Chat Service: 8084
- Follow Service: 8086
- Notification Service: 8085
- Search Service: 8087