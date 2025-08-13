# 🚀 Azure Deployment Guide for Voice DevOps Dashboard

## Overview
This guide will help you deploy the Voice DevOps Dashboard to Azure App Service, making it accessible via public endpoints and ready for production use.

## 📋 Prerequisites

### 1. **Azure Account**
- Active Azure subscription
- Contributor or Owner role on the subscription

### 2. **Azure CLI**
- Install Azure CLI: [https://docs.microsoft.com/en-us/cli/azure/install-azure-cli](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli)
- Login: `az login`

### 3. **Java Development Environment**
- Java 17 JDK
- Maven 3.6+
- Git

### 4. **Application Code**
- Voice DevOps Dashboard source code
- Access to GitHub repository (if using GitHub deployment)

## 🎯 Deployment Options

### **Option 1: Quick Deploy (Recommended)**
Use the provided deployment scripts for automated deployment.

### **Option 2: Manual Deploy**
Follow the step-by-step manual deployment process.

### **Option 3: Azure Portal**
Deploy directly through the Azure Portal web interface.

## 🚀 Quick Deployment

### **For Linux/macOS Users:**
```bash
# Make script executable
chmod +x deploy-to-azure.sh

# Run deployment script
./deploy-to-azure.sh
```

### **For Windows Users:**
```powershell
# Run PowerShell script
.\deploy-to-azure.ps1
```

## 📝 Manual Deployment Steps

### **Step 1: Create Resource Group**
```bash
az group create --name voice-devops-rg --location eastus
```

### **Step 2: Create App Service Plan**
```bash
az appservice plan create \
    --name voice-devops-plan \
    --resource-group voice-devops-rg \
    --sku B1 \
    --is-linux
```

### **Step 3: Create Web App**
```bash
az webapp create \
    --name voice-devops-dashboard \
    --resource-group voice-devops-rg \
    --plan voice-devops-plan \
    --runtime "JAVA|17-java17" \
    --deployment-local-git
```

### **Step 4: Configure Java Settings**
```bash
az webapp config set \
    --resource-group voice-devops-rg \
    --name voice-devops-dashboard \
    --java-version 17 \
    --java-container "JAVA" \
    --java-container-version "17"
```

### **Step 5: Set Application Settings**
```bash
az webapp config appsettings set \
    --resource-group voice-devops-rg \
    --name voice-devops-dashboard \
    --settings \
    "PORT=8080" \
    "WEBSITES_PORT=8080" \
    "SPRING_PROFILES_ACTIVE=azure" \
    "ADMIN_USERNAME=admin" \
    "ADMIN_PASSWORD=YourSecurePassword123!" \
    "VOICE_RECOGNITION_ENABLED=false" \
    "DEPLOYMENT_AGENT_ENABLED=true"
```

### **Step 6: Configure CORS**
```bash
az webapp cors add \
    --resource-group voice-devops-rg \
    --name voice-devops-dashboard \
    --allowed-origins "https://*.azurewebsites.net" "https://*.azure.com"
```

### **Step 7: Enable Application Insights**
```bash
az monitor app-insights component create \
    --app voice-devops-insights \
    --location eastus \
    --resource-group voice-devops-rg \
    --application-type web \
    --kind web
```

## 🔧 Configuration Files

### **1. application-azure.properties**
Production-ready configuration for Azure deployment with:
- Azure SQL Database support
- Environment variable configuration
- Production logging settings
- Azure-specific optimizations

### **2. Dockerfile.azure**
Azure-optimized Docker image with:
- Multi-stage build
- Security hardening
- Health checks
- Azure-specific optimizations

### **3. azure-deployment.yml**
Comprehensive Azure resource configuration including:
- App Service settings
- Database configuration
- Network settings
- Monitoring and scaling

## 🌐 Available Endpoints

After successful deployment, your application will be accessible at:

### **Main Endpoints:**
- **Root URL**: `https://your-app-name.azurewebsites.net`
- **Admin Dashboard**: `https://your-app-name.azurewebsites.net/admin-dashboard.html`
- **User Dashboard**: `https://your-app-name.azurewebsites.net/user-dashboard.html`
- **Deployment Agent**: `https://your-app-name.azurewebsites.net/deployment-agent-dashboard.html`

### **API Endpoints:**
- **Health Check**: `https://your-app-name.azurewebsites.net/actuator/health`
- **API Info**: `https://your-app-name.azurewebsites.net/actuator/info`
- **Metrics**: `https://your-app-name.azurewebsites.net/actuator/metrics`
- **Dashboard API**: `https://your-app-name.azurewebsites.net/api/dashboard/**`
- **Voice API**: `https://your-app-name.azurewebsites.net/api/voice/**`
- **Deployment API**: `https://your-app-name.azurewebsites.net/api/deployment-agent/**`

## 🔐 Security Configuration

### **Environment Variables to Configure:**
```bash
# Database
AZURE_SQL_CONNECTION_STRING="your-connection-string"
AZURE_SQL_USERNAME="your-username"
AZURE_SQL_PASSWORD="your-password"

# Security
ADMIN_USERNAME="admin"
ADMIN_PASSWORD="your-secure-password"

# External Services
JENKINS_URL="https://your-jenkins.com"
JENKINS_API_TOKEN="your-token"
GITHUB_TOKEN="your-github-token"

# CORS
ALLOWED_ORIGINS="https://your-domain.com,https://*.azurewebsites.net"
```

## 📊 Monitoring and Logging

### **Application Insights:**
- Performance monitoring
- Error tracking
- Usage analytics
- Custom metrics

### **Log Analytics:**
- Centralized logging
- Advanced querying
- Alerting capabilities

### **Health Checks:**
- Database connectivity
- External service status
- Application health
- Readiness probes

## 🔄 Deployment Methods

### **1. GitHub Integration**
```bash
az webapp deployment source config \
    --resource-group voice-devops-rg \
    --name voice-devops-dashboard \
    --repo-url "https://github.com/your-username/voice-devops-dashboard" \
    --branch main \
    --manual-integration
```

### **2. Local Git**
```bash
# Get Git URL
az webapp deployment source config-local-git \
    --name voice-devops-dashboard \
    --resource-group voice-devops-rg

# Add remote and push
git remote add azure <git-url>
git push azure main
```

### **3. Azure Container Registry**
```bash
az webapp config container set \
    --resource-group voice-devops-rg \
    --name voice-devops-dashboard \
    --docker-custom-image-name "your-registry.azurecr.io/voice-devops:latest"
```

## 🚀 Scaling Configuration

### **Auto-scaling Rules:**
- **CPU-based scaling**: Scale out when CPU > 70%, scale in when < 30%
- **Memory-based scaling**: Scale based on memory usage
- **Custom metrics**: Scale based on application-specific metrics

### **Manual Scaling:**
```bash
# Scale to 3 instances
az appservice plan update \
    --name voice-devops-plan \
    --resource-group voice-devops-rg \
    --number-of-workers 3
```

## 🔧 Troubleshooting

### **Common Issues:**

#### **1. Application Won't Start**
- Check Java version configuration
- Verify environment variables
- Review application logs

#### **2. Database Connection Issues**
- Verify connection string format
- Check firewall rules
- Ensure database is accessible

#### **3. CORS Issues**
- Verify allowed origins
- Check CORS configuration
- Test with browser developer tools

#### **4. Performance Issues**
- Monitor Application Insights
- Check resource usage
- Optimize JVM settings

### **Useful Commands:**
```bash
# View application logs
az webapp log tail --name voice-devops-dashboard --resource-group voice-devops-rg

# Check application status
az webapp show --name voice-devops-dashboard --resource-group voice-devops-rg

# Restart application
az webapp restart --name voice-devops-dashboard --resource-group voice-devops-rg
```

## 📈 Post-Deployment Steps

### **1. Verify Deployment**
- Test all endpoints
- Check health status
- Verify functionality

### **2. Configure Custom Domain**
```bash
az webapp config hostname add \
    --webapp-name voice-devops-dashboard \
    --resource-group voice-devops-rg \
    --hostname "api.yourdomain.com"
```

### **3. Set Up SSL Certificate**
- Configure SSL binding
- Set up automatic redirects
- Test HTTPS endpoints

### **4. Configure Monitoring**
- Set up alerts
- Configure dashboards
- Set up log retention

### **5. Performance Optimization**
- Enable CDN
- Configure caching
- Optimize database queries

## 🎯 Production Considerations

### **Security:**
- Use Azure Key Vault for secrets
- Enable Azure AD authentication
- Configure network security groups
- Regular security updates

### **Performance:**
- Use Premium App Service Plan for better performance
- Enable Azure CDN for static content
- Optimize database queries
- Use connection pooling

### **Reliability:**
- Set up backup and recovery
- Configure disaster recovery
- Use multiple regions
- Implement health checks

## 📚 Additional Resources

- [Azure App Service Documentation](https://docs.microsoft.com/en-us/azure/app-service/)
- [Spring Boot on Azure](https://docs.microsoft.com/en-us/azure/developer/java/spring-framework/)
- [Azure CLI Reference](https://docs.microsoft.com/en-us/cli/azure/)
- [Azure DevOps Best Practices](https://docs.microsoft.com/en-us/azure/devops/)

## 🎉 Success!

Your Voice DevOps Dashboard is now deployed to Azure and accessible via public endpoints! 

**Next Steps:**
1. Test all endpoints
2. Configure your external services
3. Set up monitoring and alerts
4. Customize the application
5. Share with your team

For support or questions, refer to the troubleshooting section or Azure documentation. 