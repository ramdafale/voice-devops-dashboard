#!/bin/bash

# Azure Deployment Script for Voice DevOps Dashboard
# This script deploys the application to Azure App Service

set -e

# Configuration Variables
RESOURCE_GROUP="voice-devops-rg"
LOCATION="eastus"
APP_SERVICE_PLAN="voice-devops-plan"
APP_NAME="voice-devops-dashboard"
SKU="B1"
JAVA_VERSION="17"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Starting Azure Deployment for Voice DevOps Dashboard${NC}"

# Check if Azure CLI is installed
if ! command -v az &> /dev/null; then
    echo -e "${RED}❌ Azure CLI is not installed. Please install it first.${NC}"
    echo "Visit: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli"
    exit 1
fi

# Check if user is logged in to Azure
if ! az account show &> /dev/null; then
    echo -e "${YELLOW}⚠️  Please log in to Azure first:${NC}"
    az login
fi

# Create Resource Group
echo -e "${BLUE}📦 Creating Resource Group: ${RESOURCE_GROUP}${NC}"
az group create \
    --name $RESOURCE_GROUP \
    --location $LOCATION \
    --output table

# Create App Service Plan
echo -e "${BLUE}📋 Creating App Service Plan: ${APP_SERVICE_PLAN}${NC}"
az appservice plan create \
    --name $APP_SERVICE_PLAN \
    --resource-group $RESOURCE_GROUP \
    --sku $SKU \
    --is-linux \
    --output table

# Create Web App
echo -e "${BLUE}🌐 Creating Web App: ${APP_NAME}${NC}"
az webapp create \
    --name $APP_NAME \
    --resource-group $RESOURCE_GROUP \
    --plan $APP_SERVICE_PLAN \
    --runtime "JAVA|17-java17" \
    --deployment-local-git \
    --output table

# Configure Java version
echo -e "${BLUE}☕ Configuring Java Version: ${JAVA_VERSION}${NC}"
az webapp config set \
    --resource-group $RESOURCE_GROUP \
    --name $APP_NAME \
    --java-version $JAVA_VERSION \
    --java-container "JAVA" \
    --java-container-version "17"

# Configure application settings
echo -e "${BLUE}⚙️  Configuring Application Settings${NC}"
az webapp config appsettings set \
    --resource-group $RESOURCE_GROUP \
    --name $APP_NAME \
    --settings \
    "PORT=8080" \
    "WEBSITES_PORT=8080" \
    "JAVA_OPTS=-Xmx1024m -Xms512m" \
    "SPRING_PROFILES_ACTIVE=azure" \
    "ADMIN_USERNAME=admin" \
    "ADMIN_PASSWORD=ChangeMe123!" \
    "VOICE_RECOGNITION_ENABLED=false" \
    "DEPLOYMENT_AGENT_ENABLED=true" \
    "ALLOWED_ORIGINS=https://*.azurewebsites.net,https://*.azure.com"

# Configure CORS
echo -e "${BLUE}🌍 Configuring CORS${NC}"
az webapp cors add \
    --resource-group $RESOURCE_GROUP \
    --name $APP_NAME \
    --allowed-origins "https://*.azurewebsites.net" "https://*.azure.com"

# Enable Application Insights (Optional)
echo -e "${BLUE}📊 Enabling Application Insights${NC}"
az monitor app-insights component create \
    --app voice-devops-insights \
    --location $LOCATION \
    --resource-group $RESOURCE_GROUP \
    --application-type web \
    --kind web

# Get the Application Insights connection string
INSIGHTS_CONNECTION_STRING=$(az monitor app-insights component show \
    --app voice-devops-insights \
    --resource-group $RESOURCE_GROUP \
    --query connectionString \
    --output tsv)

# Configure Application Insights
az webapp config appsettings set \
    --resource-group $RESOURCE_GROUP \
    --name $APP_NAME \
    --settings "APPLICATIONINSIGHTS_CONNECTION_STRING=$INSIGHTS_CONNECTION_STRING"

# Configure deployment source (GitHub or Local Git)
echo -e "${BLUE}📥 Configuring Deployment Source${NC}"
echo "Choose deployment method:"
echo "1) Deploy from GitHub"
echo "2) Deploy from Local Git"
echo "3) Deploy from Azure Container Registry"
read -p "Enter your choice (1-3): " DEPLOY_CHOICE

case $DEPLOY_CHOICE in
    1)
        read -p "Enter GitHub repository URL: " GITHUB_REPO
        read -p "Enter GitHub branch (default: main): " GITHUB_BRANCH
        GITHUB_BRANCH=${GITHUB_BRANCH:-main}
        
        az webapp deployment source config \
            --resource-group $RESOURCE_GROUP \
            --name $APP_NAME \
            --repo-url $GITHUB_REPO \
            --branch $GITHUB_BRANCH \
            --manual-integration
        ;;
    2)
        echo -e "${YELLOW}📝 Local Git deployment configured${NC}"
        echo "Use the following commands to deploy:"
        echo "git remote add azure $(az webapp deployment source config-local-git --name $APP_NAME --resource-group $RESOURCE_GROUP --query url --output tsv)"
        echo "git push azure main"
        ;;
    3)
        read -p "Enter Azure Container Registry name: " ACR_NAME
        read -p "Enter image name: " IMAGE_NAME
        read -p "Enter image tag: " IMAGE_TAG
        
        az webapp config container set \
            --resource-group $RESOURCE_GROUP \
            --name $APP_NAME \
            --docker-custom-image-name "$ACR_NAME.azurecr.io/$IMAGE_NAME:$IMAGE_TAG"
        ;;
    *)
        echo -e "${RED}❌ Invalid choice. Exiting.${NC}"
        exit 1
        ;;
esac

# Get the application URL
APP_URL=$(az webapp show \
    --resource-group $RESOURCE_GROUP \
    --name $APP_NAME \
    --query defaultHostName \
    --output tsv)

echo -e "${GREEN}✅ Deployment completed successfully!${NC}"
echo -e "${BLUE}🌐 Your application is available at: https://${APP_URL}${NC}"
echo -e "${BLUE}📊 Application Insights: https://portal.azure.com/#@$(az account show --query user.name --output tsv)/resource/subscriptions/$(az account show --query id --output tsv)/resourceGroups/${RESOURCE_GROUP}/providers/Microsoft.Insights/components/voice-devops-insights${NC}"

# Display available endpoints
echo -e "${YELLOW}🔗 Available Endpoints:${NC}"
echo -e "${GREEN}• Main Dashboard: https://${APP_URL}${NC}"
echo -e "${GREEN}• Admin Dashboard: https://${APP_URL}/admin-dashboard.html${NC}"
echo -e "${GREEN}• User Dashboard: https://${APP_URL}/user-dashboard.html${NC}"
echo -e "${GREEN}• Deployment Agent: https://${APP_URL}/deployment-agent-dashboard.html${NC}"
echo -e "${GREEN}• API Health Check: https://${APP_URL}/actuator/health${NC}"
echo -e "${GREEN}• API Info: https://${APP_URL}/actuator/info${NC}"
echo -e "${GREEN}• API Metrics: https://${APP_URL}/actuator/metrics${NC}"

# Display next steps
echo -e "${YELLOW}📋 Next Steps:${NC}"
echo "1. Update the application settings with your actual values"
echo "2. Configure your database connection string"
echo "3. Set up Jenkins and GitHub integration"
echo "4. Test the application endpoints"
echo "5. Configure custom domain (optional)"
echo "6. Set up monitoring and alerts"

echo -e "${GREEN}🎉 Voice DevOps Dashboard is now deployed to Azure!${NC}" 