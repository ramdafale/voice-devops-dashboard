# Azure Deployment Script for Voice DevOps Dashboard (PowerShell)
# This script deploys the application to Azure App Service

param(
    [string]$ResourceGroup = "voice-devops-rg",
    [string]$Location = "eastus",
    [string]$AppServicePlan = "voice-devops-plan",
    [string]$AppName = "voice-devops-dashboard",
    [string]$Sku = "B1",
    [string]$JavaVersion = "17"
)

# Error handling
$ErrorActionPreference = "Stop"

Write-Host "🚀 Starting Azure Deployment for Voice DevOps Dashboard" -ForegroundColor Blue

# Check if Azure CLI is installed
try {
    $null = Get-Command az -ErrorAction Stop
} catch {
    Write-Host "❌ Azure CLI is not installed. Please install it first." -ForegroundColor Red
    Write-Host "Visit: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli" -ForegroundColor Yellow
    exit 1
}

# Check if user is logged in to Azure
try {
    $null = az account show 2>$null
} catch {
    Write-Host "⚠️  Please log in to Azure first:" -ForegroundColor Yellow
    az login
}

# Create Resource Group
Write-Host "📦 Creating Resource Group: $ResourceGroup" -ForegroundColor Blue
az group create --name $ResourceGroup --location $Location --output table

# Create App Service Plan
Write-Host "📋 Creating App Service Plan: $AppServicePlan" -ForegroundColor Blue
az appservice plan create --name $AppServicePlan --resource-group $ResourceGroup --sku $Sku --is-linux --output table

# Create Web App
Write-Host "🌐 Creating Web App: $AppName" -ForegroundColor Blue
az webapp create --name $AppName --resource-group $ResourceGroup --plan $AppServicePlan --runtime "JAVA|17-java17" --deployment-local-git --output table

# Configure Java version
Write-Host "☕ Configuring Java Version: $JavaVersion" -ForegroundColor Blue
az webapp config set --resource-group $ResourceGroup --name $AppName --java-version $JavaVersion --java-container "JAVA" --java-container-version "17"

# Configure application settings
Write-Host "⚙️  Configuring Application Settings" -ForegroundColor Blue
az webapp config appsettings set --resource-group $ResourceGroup --name $AppName --settings @(
    "PORT=8080",
    "WEBSITES_PORT=8080",
    "JAVA_OPTS=-Xmx1024m -Xms512m",
    "SPRING_PROFILES_ACTIVE=azure",
    "ADMIN_USERNAME=admin",
    "ADMIN_PASSWORD=ChangeMe123!",
    "VOICE_RECOGNITION_ENABLED=false",
    "DEPLOYMENT_AGENT_ENABLED=true",
    "ALLOWED_ORIGINS=https://*.azurewebsites.net,https://*.azure.com"
)

# Configure CORS
Write-Host "🌍 Configuring CORS" -ForegroundColor Blue
az webapp cors add --resource-group $ResourceGroup --name $AppName --allowed-origins "https://*.azurewebsites.net" "https://*.azure.com"

# Enable Application Insights
Write-Host "📊 Enabling Application Insights" -ForegroundColor Blue
az monitor app-insights component create --app "voice-devops-insights" --location $Location --resource-group $ResourceGroup --application-type web --kind web

# Get the Application Insights connection string
$InsightsConnectionString = az monitor app-insights component show --app "voice-devops-insights" --resource-group $ResourceGroup --query connectionString --output tsv

# Configure Application Insights
az webapp config appsettings set --resource-group $ResourceGroup --name $AppName --settings "APPLICATIONINSIGHTS_CONNECTION_STRING=$InsightsConnectionString"

# Configure deployment source
Write-Host "📥 Configuring Deployment Source" -ForegroundColor Blue
Write-Host "Choose deployment method:" -ForegroundColor Yellow
Write-Host "1) Deploy from GitHub" -ForegroundColor White
Write-Host "2) Deploy from Local Git" -ForegroundColor White
Write-Host "3) Deploy from Azure Container Registry" -ForegroundColor White

$DeployChoice = Read-Host "Enter your choice (1-3)"

switch ($DeployChoice) {
    "1" {
        $GitHubRepo = Read-Host "Enter GitHub repository URL"
        $GitHubBranch = Read-Host "Enter GitHub branch (default: main)"
        if ([string]::IsNullOrEmpty($GitHubBranch)) { $GitHubBranch = "main" }
        
        az webapp deployment source config --resource-group $ResourceGroup --name $AppName --repo-url $GitHubRepo --branch $GitHubBranch --manual-integration
    }
    "2" {
        Write-Host "📝 Local Git deployment configured" -ForegroundColor Yellow
        Write-Host "Use the following commands to deploy:" -ForegroundColor White
        $GitUrl = az webapp deployment source config-local-git --name $AppName --resource-group $ResourceGroup --query url --output tsv
        Write-Host "git remote add azure $GitUrl" -ForegroundColor Green
        Write-Host "git push azure main" -ForegroundColor Green
    }
    "3" {
        $AcrName = Read-Host "Enter Azure Container Registry name"
        $ImageName = Read-Host "Enter image name"
        $ImageTag = Read-Host "Enter image tag"
        
        az webapp config container set --resource-group $ResourceGroup --name $AppName --docker-custom-image-name "$AcrName.azurecr.io/$ImageName:$ImageTag"
    }
    default {
        Write-Host "❌ Invalid choice. Exiting." -ForegroundColor Red
        exit 1
    }
}

# Get the application URL
$AppUrl = az webapp show --resource-group $ResourceGroup --name $AppName --query defaultHostName --output tsv

Write-Host "✅ Deployment completed successfully!" -ForegroundColor Green
Write-Host "🌐 Your application is available at: https://$AppUrl" -ForegroundColor Blue

# Display available endpoints
Write-Host "🔗 Available Endpoints:" -ForegroundColor Yellow
Write-Host "• Main Dashboard: https://$AppUrl" -ForegroundColor Green
Write-Host "• Admin Dashboard: https://$AppUrl/admin-dashboard.html" -ForegroundColor Green
Write-Host "• User Dashboard: https://$AppUrl/user-dashboard.html" -ForegroundColor Green
Write-Host "• Deployment Agent: https://$AppUrl/deployment-agent-dashboard.html" -ForegroundColor Green
Write-Host "• API Health Check: https://$AppUrl/actuator/health" -ForegroundColor Green
Write-Host "• API Info: https://$AppUrl/actuator/info" -ForegroundColor Green
Write-Host "• API Metrics: https://$AppUrl/actuator/metrics" -ForegroundColor Green

# Display next steps
Write-Host "📋 Next Steps:" -ForegroundColor Yellow
Write-Host "1. Update the application settings with your actual values" -ForegroundColor White
Write-Host "2. Configure your database connection string" -ForegroundColor White
Write-Host "3. Set up Jenkins and GitHub integration" -ForegroundColor White
Write-Host "4. Test the application endpoints" -ForegroundColor White
Write-Host "5. Configure custom domain (optional)" -ForegroundColor White
Write-Host "6. Set up monitoring and alerts" -ForegroundColor White

Write-Host "🎉 Voice DevOps Dashboard is now deployed to Azure!" -ForegroundColor Green 