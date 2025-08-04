# Voice-Controlled DevOps Dashboard

A comprehensive voice-controlled DevOps automation system with role-based dashboards for managers/tech leads and developers. This system provides voice commands for Jenkins, GitHub, and other developer tools.

## 🚀 Features

### For Admins (Managers/Tech Leads)
- ✅ **Production Build Approvals** via voice commands
- ✅ **Team Performance Monitoring** with real-time statistics
- ✅ **Deployment Status Overview** across environments
- ✅ **Voice-based Deployment** commands
- ✅ **Approval Workflow Management**

### For Users (Developers)
- ✅ **Personal Build Management** via voice
- ✅ **Branch Deployment** to staging environments
- ✅ **Pull Request Creation** through voice commands
- ✅ **Build Status Monitoring** in real-time
- ✅ **Code Review Tracking**

### Voice Commands Examples

#### Admin Commands:
```
"Approve production build 1234"
"Deploy release-2.1.0 to production"
"Show all pending approvals"
"Abort the current production build"
"Generate deployment report"
```

#### User Commands:
```
"Build my feature branch"
"Deploy feature-branch to staging"
"Create pull request for feature-branch"
"Show my recent builds"
"Check build status"
```

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.2.0
- **Database**: MySQL 8.0
- **GraphQL**: GraphQL Java
- **Voice Recognition**: Google Cloud Speech-to-Text
- **Frontend**: HTML5, CSS3, JavaScript, Bootstrap 5
- **Microservices**: Spring Cloud
- **Security**: Spring Security with JWT

## 📋 Prerequisites

- Java 17 or higher
- MySQL 8.0
- Maven 3.6+
- Node.js (for frontend development)

## 🚀 Quick Start

### 1. Database Setup

```sql
-- Create MySQL database
CREATE DATABASE voice_devops CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user (optional)
CREATE USER 'devops_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON voice_devops.* TO 'devops_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Application Configuration

Update `voice-devops-api/src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/voice_devops?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password

# Voice Recognition (for production)
voice.recognition.enabled=true
voice.recognition.language=en-US
voice.recognition.confidence-threshold=0.8
```

### 3. Build and Run

```bash
# Clone the repository
git clone <repository-url>
cd voice-devops-dashboard

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run -pl voice-devops-api
```

### 4. Access the Dashboards

- **Admin Dashboard**: http://localhost:8080/admin-dashboard.html
- **User Dashboard**: http://localhost:8080/user-dashboard.html
- **API Documentation**: http://localhost:8080/api

## 🎯 Demo Scenarios

### Scenario 1: Admin Approving Production Build

1. **Login as Admin** and navigate to Admin Dashboard
2. **Click "Start Voice"** button
3. **Say**: "Approve build 1234"
4. **System Response**: "Build 1234 approved successfully. Build is now running."

### Scenario 2: Developer Building Feature Branch

1. **Login as Developer** and navigate to User Dashboard
2. **Click "Start Voice"** button
3. **Say**: "Build my feature-branch"
4. **System Response**: "Build triggered for branch feature-branch. Build ID: BUILD-1001"

### Scenario 3: Production Deployment

1. **Admin says**: "Deploy release-2.1.0 to production"
2. **System creates** production deployment build
3. **Build requires approval** - appears in pending approvals
4. **Admin approves** via voice command
5. **Deployment proceeds** to production

## 📊 API Endpoints

### Voice Commands
- `POST /api/voice/command` - Process voice command
- `POST /api/voice/audio` - Process audio file
- `GET /api/voice/commands` - Get available commands

### Dashboard Data
- `GET /api/dashboard/admin` - Admin dashboard data
- `GET /api/dashboard/user/{username}` - User dashboard data
- `GET /api/dashboard/builds` - All builds
- `GET /api/dashboard/commands` - Recent voice commands

### Build Management
- `GET /api/dashboard/builds/{buildId}` - Build details
- `POST /api/builds/{buildId}/approve` - Approve build
- `POST /api/builds/{buildId}/abort` - Abort build

## 🏗️ Project Structure

```
voice-devops-dashboard/
├── voice-devops-api/                 # Main API service
│   ├── src/main/java/com/devops/
│   │   ├── controller/               # REST controllers
│   │   ├── entity/                   # JPA entities
│   │   ├── repository/               # Data repositories
│   │   ├── service/                  # Business logic
│   │   └── VoiceDevOpsApplication.java
│   └── src/main/resources/
│       └── application.properties
├── voice-devops-dashboard/           # Frontend dashboard
│   └── src/main/resources/static/
│       ├── admin-dashboard.html
│       └── user-dashboard.html
├── mock-jenkins-service/            # Mock Jenkins service
├── mock-github-service/             # Mock GitHub service
└── pom.xml                          # Parent POM
```

## 🔧 Configuration

### Voice Recognition Setup

For production use, configure Google Cloud Speech-to-Text:

1. **Create Google Cloud Project**
2. **Enable Speech-to-Text API**
3. **Create Service Account** and download JSON key
4. **Set environment variable**:
   ```bash
   export GOOGLE_APPLICATION_CREDENTIALS="path/to/service-account-key.json"
   ```

### Security Configuration

Update security settings in `application.properties`:

```properties
# JWT Configuration
jwt.secret=your-jwt-secret-key
jwt.expiration=86400000

# OAuth2 Configuration (for GitHub integration)
spring.security.oauth2.client.registration.github.client-id=your-github-client-id
spring.security.oauth2.client.registration.github.client-secret=your-github-client-secret
```

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn test -Dtest=*IntegrationTest
```

### Manual Testing
1. **Start the application**
2. **Open browser** to http://localhost:8080
3. **Test voice commands** using browser's speech recognition
4. **Verify responses** in the dashboard

## 📈 Monitoring

### Health Checks
- **Application Health**: http://localhost:8080/actuator/health
- **Database Health**: http://localhost:8080/actuator/health/db
- **Voice Service Health**: http://localhost:8080/actuator/health/voice

### Metrics
- **Build Statistics**: Track success/failure rates
- **Voice Command Analytics**: Monitor command usage
- **User Activity**: Track dashboard usage

## 🔒 Security Features

- **Role-based Access Control** (ADMIN/USER)
- **JWT Authentication** for API access
- **Voice Command Validation** and sanitization
- **Audit Logging** for all voice commands
- **Build Approval Workflow** for production deployments

## 🚀 Deployment

### Docker Deployment

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/voice-devops-api-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: voice-devops-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: voice-devops-api
  template:
    metadata:
      labels:
        app: voice-devops-api
    spec:
      containers:
      - name: voice-devops-api
        image: voice-devops-api:latest
        ports:
        - containerPort: 8080
```

## 🤝 Contributing

1. **Fork the repository**
2. **Create feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit changes**: `git commit -m 'Add amazing feature'`
4. **Push to branch**: `git push origin feature/amazing-feature`
5. **Open Pull Request**

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- **Email**: support@voice-devops.com
- **Documentation**: [Wiki](https://github.com/voice-devops/wiki)
- **Issues**: [GitHub Issues](https://github.com/voice-devops/issues)

## 🎉 Acknowledgments

- **Spring Boot Team** for the excellent framework
- **Google Cloud** for Speech-to-Text API
- **Bootstrap** for the beautiful UI components
- **Font Awesome** for the icons

---

**Happy Voice-Controlled DevOps! 🎤🚀** 