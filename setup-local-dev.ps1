# Voice DevOps Dashboard - Local Development Setup Script
# This script sets up JDK 17 and Maven for local development

Write-Host "🚀 Setting up Voice DevOps Dashboard Local Development Environment..." -ForegroundColor Green
Write-Host "==================================================================" -ForegroundColor Green

# Check if Chocolatey is installed
if (!(Get-Command choco -ErrorAction SilentlyContinue)) {
    Write-Host "📦 Installing Chocolatey package manager..." -ForegroundColor Yellow
    Set-ExecutionPolicy Bypass -Scope Process -Force
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
    iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
} else {
    Write-Host "✅ Chocolatey is already installed" -ForegroundColor Green
}

# Install JDK 17
Write-Host "☕ Installing OpenJDK 17..." -ForegroundColor Yellow
choco install openjdk17 -y

# Install Maven
Write-Host "📚 Installing Apache Maven..." -ForegroundColor Yellow
choco install maven -y

# Refresh environment variables
Write-Host "🔄 Refreshing environment variables..." -ForegroundColor Yellow
refreshenv

# Verify installations
Write-Host "✅ Verifying installations..." -ForegroundColor Green
Write-Host ""

Write-Host "Java Version:" -ForegroundColor Cyan
java -version

Write-Host "Maven Version:" -ForegroundColor Cyan
mvn -version

Write-Host ""
Write-Host "🎉 Setup complete! You can now run the project locally." -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Navigate to the voice-devops-api directory: cd voice-devops-api" -ForegroundColor White
Write-Host "2. Run: mvn spring-boot:run" -ForegroundColor White
Write-Host "3. Or use the Docker setup: ./start-demo.sh" -ForegroundColor White
Write-Host ""
Write-Host "The application will be available at:" -ForegroundColor Cyan
Write-Host "• Main API: http://localhost:8080" -ForegroundColor White
Write-Host "• Admin Dashboard: http://localhost:8080/admin-dashboard.html" -ForegroundColor White
Write-Host "• User Dashboard: http://localhost:8080/user-dashboard.html" -ForegroundColor White 