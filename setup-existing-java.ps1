# Voice DevOps Dashboard - Setup for Existing Java Installation
# This script configures your existing Java installation and sets up Maven

Write-Host "🚀 Setting up Voice DevOps Dashboard with your existing Java installation..." -ForegroundColor Green
Write-Host "=================================================================================" -ForegroundColor Green

# Set Java environment variables
$javaPath = "C:\Users\2033023\.jdks\openjdk-23.0.2"
$javaBinPath = "$javaPath\bin"

Write-Host "☕ Configuring Java environment..." -ForegroundColor Yellow
Write-Host "   Java Path: $javaPath" -ForegroundColor Gray
Write-Host "   Java Bin: $javaBinPath" -ForegroundColor Gray

# Set JAVA_HOME for current session
$env:JAVA_HOME = $javaPath
$env:PATH = "$javaBinPath;$env:PATH"

Write-Host "✅ Java environment configured for current session" -ForegroundColor Green

# Verify Java is accessible
Write-Host "🔍 Verifying Java configuration..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "✅ Java is accessible: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Java is not accessible from PATH" -ForegroundColor Red
    Write-Host "   Using full path: $javaBinPath\java.exe" -ForegroundColor Gray
}

# Check if Maven is already installed
Write-Host "📚 Checking Maven installation..." -ForegroundColor Yellow
try {
    $mvnVersion = mvn -version 2>&1 | Select-String "Apache Maven"
    if ($mvnVersion) {
        Write-Host "✅ Maven is already installed: $mvnVersion" -ForegroundColor Green
    } else {
        Write-Host "❌ Maven is not working properly" -ForegroundColor Red
        $mavenNeeded = $true
    }
} catch {
    Write-Host "📚 Maven is not installed, installing now..." -ForegroundColor Yellow
    $mavenNeeded = $true
}

# Install Maven if needed
if ($mavenNeeded) {
    Write-Host "📚 Installing Apache Maven..." -ForegroundColor Yellow
    
    # Try using winget first
    try {
        Write-Host "   Trying winget installation..." -ForegroundColor Gray
        winget install Apache.Maven --accept-source-agreements
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Maven installed successfully via winget" -ForegroundColor Green
        } else {
            throw "Winget installation failed"
        }
    } catch {
        Write-Host "   Winget failed, trying manual download..." -ForegroundColor Gray
        
        # Manual Maven installation
        $mavenVersion = "3.9.6"
        $mavenUrl = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
        $mavenZip = "$env:TEMP\maven.zip"
        $mavenDir = "C:\Program Files\Apache\maven"
        
        Write-Host "   Downloading Maven $mavenVersion..." -ForegroundColor Gray
        Invoke-WebRequest -Uri $mavenUrl -OutFile $mavenZip
        
        Write-Host "   Extracting Maven..." -ForegroundColor Gray
        if (!(Test-Path "C:\Program Files\Apache")) {
            New-Item -ItemType Directory -Path "C:\Program Files\Apache" -Force
        }
        Expand-Archive -Path $mavenZip -DestinationPath "C:\Program Files\Apache" -Force
        
        # Rename extracted folder
        $extractedFolder = Get-ChildItem "C:\Program Files\Apache" | Where-Object { $_.Name -like "apache-maven-*" } | Select-Object -First 1
        if ($extractedFolder) {
            Rename-Item -Path $extractedFolder.FullName -NewName "maven" -Force
        }
        
        # Set Maven environment variables
        $env:MAVEN_HOME = $mavenDir
        $env:PATH = "$mavenDir\bin;$env:PATH"
        
        Write-Host "✅ Maven installed manually to $mavenDir" -ForegroundColor Green
        
        # Clean up
        Remove-Item $mavenZip -Force
    }
}

# Refresh environment variables
Write-Host "🔄 Refreshing environment variables..." -ForegroundColor Yellow
try {
    refreshenv
} catch {
    Write-Host "   refreshenv not available, environment variables set for current session" -ForegroundColor Gray
}

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
Write-Host "Environment Variables Set:" -ForegroundColor Yellow
Write-Host "   JAVA_HOME: $env:JAVA_HOME" -ForegroundColor White
Write-Host "   MAVEN_HOME: $env:MAVEN_HOME" -ForegroundColor White
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
Write-Host ""
Write-Host "Note: To make these environment variables permanent, add them to your system environment variables." -ForegroundColor Yellow 