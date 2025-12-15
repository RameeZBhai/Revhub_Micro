# 🚀 RevHub - Social Media Platform

A modern, scalable social media platform built with **Angular 18** frontend and **Spring Boot microservices** backend.

## 📋 Table of Contents
- [Features](#-features)
- [Architecture](#-architecture)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [Manual Setup](#-manual-setup)
- [API Documentation](#-api-documentation)
- [Project Structure](#-project-structure)
- [Technologies Used](#-technologies-used)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)

## ✨ Features

### 🔐 Authentication & User Management
- User registration and login
- JWT-based authentication
- Password recovery with OTP
- Profile management with image upload

### 📱 Social Features
- Create posts with images and text
- Like and comment on posts
- Follow/unfollow users
- Real-time messaging
- Activity notifications
- User search and discovery

### 💬 Real-time Communication
- WebSocket-based chat
- Instant message delivery
- Online status indicators
- Message history

### 🔔 Notifications
- Real-time activity alerts
- Follow notifications
- Like and comment notifications
- Message notifications

## 🏗️ Architecture

### Microservices Backend
```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Frontend  │───▶│ API Gateway │───▶│Microservices│
│  (Angular)  │    │   (8080)    │    │   (808X)    │
└─────────────┘    └─────────────┘    └─────────────┘
                                              │
                                              ▼
                                    ┌─────────────┐
                                    │  Databases  │
                                    │MySQL/MongoDB│
                                    └─────────────┘
```

### Services Overview
| Service | Port | Purpose | Database |
|---------|------|---------|----------|
| API Gateway | 8080 | Entry point & routing | - |
| Auth Service | 8082 | Authentication | MySQL |
| User Service | 8089 | User profiles | MySQL |
| Post Service | 8083 | Content management | MongoDB |
| Chat Service | 8084 | Messaging | MongoDB |
| Follow Service | 8086 | Social connections | MySQL |
| Notification Service | 8085 | Alerts | MongoDB |
| Search Service | 8087 | Search & discovery | - |

## 📋 Prerequisites

### Required Software
- **Java 17+** - [Download](https://adoptium.net/)
- **Node.js 18+** - [Download](https://nodejs.org/)
- **MySQL 8.0+** - [Download](https://dev.mysql.com/downloads/)
- **MongoDB 6.0+** - [Download](https://www.mongodb.com/try/download/community)
- **Apache Kafka** - [Download](https://kafka.apache.org/downloads)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)
- **Angular CLI** - `npm install -g @angular/cli`

### System Requirements
- **RAM**: 8GB minimum, 16GB recommended
- **Storage**: 5GB free space
- **OS**: Windows 10+, macOS 10.15+, or Linux

## 🚀 Quick Start

### Option 1: Automated Setup (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd RevHubP2Ram
   ```

2. **One-time MySQL setup** (Run only once)
   ```bash
   # Double-click or run:
   setup_mysql_once.bat
   ```
   This sets MySQL root password to `10532` for auto-connection.

3. **Start infrastructure**
   ```bash
   # Double-click or run:
   start_infrastructure.bat
   ```

4. **Start all services**
   ```bash
   # Double-click or run:
   run_all_services.bat
   ```

5. **Start frontend**
   ```bash
   # Double-click or run:
   run_frontend.bat
   ```

6. **Access the application**
   - Frontend: http://localhost:4200
   - API Gateway: http://localhost:8080

### Option 2: Docker Setup (Coming Soon)
```bash
docker-compose up -d
```

## 🔧 Manual Setup

### 1. Database Setup

#### MySQL Setup
```sql
-- Create databases
CREATE DATABASE revhub_auth;
CREATE DATABASE revhub_users;
CREATE DATABASE revhub_follows;

-- Create user and grant permissions
CREATE USER 'revhub'@'localhost' IDENTIFIED BY '10532';
GRANT ALL PRIVILEGES ON revhub_*.* TO 'revhub'@'localhost';
FLUSH PRIVILEGES;
```

#### MongoDB Setup
```bash
# Start MongoDB service
mongod --dbpath /path/to/data/directory

# Create collections (auto-created on first use)
# - revhub_posts
# - revhub_chat  
# - revhub_notifications
```

### 2. Backend Services

Start each service in separate terminals:

```bash
# API Gateway
cd microservices/api-gateway
mvn spring-boot:run

# Auth Service
cd microservices/auth-service
mvn spring-boot:run

# User Service
cd microservices/user-service
mvn spring-boot:run

# Post Service
cd microservices/post-service
mvn spring-boot:run

# Chat Service
cd microservices/chat-service
mvn spring-boot:run

# Follow Service
cd microservices/follow-service
mvn spring-boot:run

# Notification Service
cd microservices/notification-service
mvn spring-boot:run

# Search Service
cd microservices/search-service
mvn spring-boot:run
```

### 3. Frontend Setup

```bash
cd frontend
npm install
ng serve
```

## 📚 API Documentation

### Authentication Endpoints
```
POST /api/auth/register    # User registration
POST /api/auth/login       # User login
POST /api/auth/forgot      # Password recovery
POST /api/auth/verify-otp  # OTP verification
POST /api/auth/reset       # Password reset
```

### User Management
```
GET  /api/profile/{username}     # Get user profile
PUT  /api/profile               # Update profile
GET  /api/suggestions           # Get user suggestions
```

### Posts & Content
```
GET  /api/posts                 # Get feed posts
POST /api/posts/upload          # Create new post
POST /api/posts/{id}/like       # Toggle like
POST /api/posts/{id}/comment    # Add comment
```

### Social Features
```
POST /api/follow/{user}/follow/{target}    # Follow user
DELETE /api/follow/{user}/unfollow/{target} # Unfollow user
GET  /api/follow/{user}/followers          # Get followers
GET  /api/follow/{user}/following          # Get following
```

### Messaging
```
GET  /api/chat/conversation/{username}     # Get conversation
POST /api/chat/send                       # Send message
GET  /api/chat/contacts                   # Get chat contacts
```

### Notifications
```
GET  /api/notifications                   # Get notifications
PUT  /api/notifications/read-all          # Mark all as read
GET  /api/notifications/unread-count      # Get unread count
```

## 📁 Project Structure

```
RevHubP2Ram/
├── 🌐 frontend/                    # Angular 18 Frontend
│   ├── src/app/
│   │   ├── core/                   # Services, guards, interceptors
│   │   ├── modules/                # Feature modules
│   │   └── shared/                 # Shared components
│   └── public/                     # Static assets
├── ⚙️ microservices/              # Spring Boot Backend
│   ├── api-gateway/               # Entry point (8080)
│   ├── auth-service/              # Authentication (8082)
│   ├── user-service/              # User management (8089)
│   ├── post-service/              # Content (8083)
│   ├── chat-service/              # Messaging (8084)
│   ├── follow-service/            # Social (8086)
│   ├── notification-service/      # Alerts (8085)
│   └── search-service/            # Search (8087)
└── 📄 Scripts & Config            # Startup scripts & docs
```

## 🛠️ Technologies Used

### Frontend
- **Angular 18** - Modern web framework
- **TypeScript** - Type-safe JavaScript
- **RxJS** - Reactive programming
- **Bootstrap** - UI components
- **WebSocket** - Real-time communication

### Backend
- **Spring Boot 3.2** - Java framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database abstraction
- **Spring WebSocket** - Real-time messaging
- **Apache Kafka** - Message streaming
- **Consul** - Service discovery

### Databases
- **MySQL 8.0** - Relational data
- **MongoDB 6.0** - Document storage

### DevOps & Tools
- **Maven** - Build automation
- **Docker** - Containerization
- **Nginx** - Web server
- **Git** - Version control

## 🐛 Troubleshooting

### Common Issues

#### Port Already in Use
```bash
# Kill process on port
netstat -ano | findstr :8080
taskkill /PID <process_id> /F
```

#### Database Connection Failed
```bash
# Check MySQL service
net start mysql80

# Check MongoDB service  
net start MongoDB
```

#### CORS Issues
- Ensure API Gateway CORS configuration is correct
- Check `cors-config.js` for frontend proxy settings

#### Service Discovery Issues
```bash
# Restart Consul
consul agent -dev
```

### Log Locations
- **Backend logs**: Console output from each service
- **Frontend logs**: Browser developer console
- **Database logs**: Check respective database log directories

## 🔧 Configuration

### Environment Variables
```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=10532

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Service Discovery
CONSUL_HOST=localhost
CONSUL_PORT=8500
```

### Application Properties
Each microservice has its own `application.yml` with service-specific configurations.

## 🚀 Deployment

### Development
- Use provided batch scripts for local development
- Services run on localhost with default ports

### Production
- Configure environment-specific properties
- Use Docker containers for deployment
- Set up load balancers for API Gateway
- Configure external databases

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Angular style guide for frontend
- Use Spring Boot best practices for backend
- Write unit tests for new features
- Update documentation for API changes

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Developer**: Rameezshaik
- **Architecture**: Microservices with Angular frontend
- **Version**: 1.0.0

## 📞 Support

For support and questions:
- Create an issue in the repository
- Check the troubleshooting section
- Review the API documentation

---

**Happy Coding! 🎉**

Built with ❤️ using modern web technologies.