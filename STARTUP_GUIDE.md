# 🚀 RevHub Auto-Startup Guide

## One-Time Setup (Run Only Once)

### 1. Set MySQL Password
Run this **ONLY ONCE** to configure MySQL:
```bash
# Double-click or run:
setup_mysql_once.bat
```
This sets the MySQL root password to `10532` to match all service configurations.

### 2. Ensure Services Are Running
Make sure these services are running on your system:
- **MySQL80** service (should be running)
- **MongoDB** service (start if needed)

## Daily Startup (No Setup Required)

After the one-time setup, simply start your services:

### 1. Start Infrastructure
```bash
# Double-click or run:
start_infrastructure.bat
```

### 2. Start All Microservices
```bash
# Double-click or run:
run_all_services.bat
```

### 3. Start Frontend
```bash
# Double-click or run:
run_frontend.bat
```

## Auto-Connection Configuration

### MySQL Services (Auto-connect with password: 10532)
- **Auth Service** (8082) → `revhub_auth` database
- **User Service** (8089) → `revhub_users` database  
- **Follow Service** (8086) → `revhub_follows` database

### MongoDB Services (Auto-connect to localhost:27017)
- **Post Service** (8083) → `revhub_posts` database
- **Chat Service** (8084) → `revhub_chat` database
- **Notification Service** (8085) → `revhub_notifications` database

### Other Services
- **API Gateway** (8080) → No database
- **Search Service** (8087) → No database

## Key Features
✅ **Auto-database creation**: Services create databases automatically  
✅ **No manual setup**: After one-time MySQL password setup  
✅ **Consistent configuration**: All services use same credentials  
✅ **Error handling**: Services wait for database connections  

## Troubleshooting

### MySQL Connection Issues
1. Ensure MySQL80 service is running: `sc query mysql80`
2. Verify password is set to `10532`
3. Check if databases exist (they'll be auto-created)

### MongoDB Connection Issues
1. Start MongoDB service: `net start MongoDB`
2. Verify MongoDB is running on port 27017
3. Databases will be auto-created on first use

### Service Startup Order
1. Infrastructure (Consul, Kafka) - Optional
2. Microservices (any order)
3. Frontend

## Access Points
- **Frontend**: http://localhost:4200
- **API Gateway**: http://localhost:8080
- **Individual Services**: http://localhost:808X (where X is service port)