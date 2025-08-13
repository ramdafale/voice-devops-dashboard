@echo off
REM Voice DevOps Dashboard - Local Development Setup Script (Batch Version)
REM This script sets up JDK 17 and Maven for local development

echo 🚀 Setting up Voice DevOps Dashboard Local Development Environment...
echo ==================================================================

REM Check if Chocolatey is installed
where choco >nul 2>&1
if %errorlevel% neq 0 (
    echo 📦 Installing Chocolatey package manager...
    powershell -Command "Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))"
) else (
    echo ✅ Chocolatey is already installed
)

REM Install JDK 17
echo ☕ Installing OpenJDK 17...
choco install openjdk17 -y

REM Install Maven
echo 📚 Installing Apache Maven...
choco install maven -y

REM Refresh environment variables
echo 🔄 Refreshing environment variables...
call refreshenv

REM Verify installations
echo ✅ Verifying installations...
echo.

echo Java Version:
java -version

echo.
echo Maven Version:
mvn -version

echo.
echo 🎉 Setup complete! You can now run the project locally.
echo.
echo Next steps:
echo 1. Navigate to the voice-devops-api directory: cd voice-devops-api
echo 2. Run: mvn spring-boot:run
echo 3. Or use the Docker setup: start-demo.sh
echo.
echo The application will be available at:
echo • Main API: http://localhost:8080
echo • Admin Dashboard: http://localhost:8080/admin-dashboard.html
echo • User Dashboard: http://localhost:8080/user-dashboard.html

pause 