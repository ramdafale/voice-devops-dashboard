# Voice DevOps Dashboard - Setup Verification Script
# This script verifies that JDK 17 and Maven are properly configured

Write-Host "🔍 Verifying Voice DevOps Dashboard Setup..." -ForegroundColor Blue
Write-Host "=============================================" -ForegroundColor Blue

$allGood = $true

# Check Java
Write-Host "☕ Checking Java installation..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    if ($javaVersion -match '"17') {
        Write-Host "✅ Java 17 is installed and working" -ForegroundColor Green
        Write-Host "   Version: $javaVersion" -ForegroundColor Gray
    } else {
        Write-Host "❌ Java 17 is not installed or wrong version" -ForegroundColor Red
        Write-Host "   Found: $javaVersion" -ForegroundColor Gray
        $allGood = $false
    }
} catch {
    Write-Host "❌ Java is not installed or not in PATH" -ForegroundColor Red
    $allGood = $false
}

# Check JAVA_HOME
Write-Host "🏠 Checking JAVA_HOME..." -ForegroundColor Yellow
if ($env:JAVA_HOME) {
    Write-Host "✅ JAVA_HOME is set to: $env:JAVA_HOME" -ForegroundColor Green
} else {
    Write-Host "❌ JAVA_HOME is not set" -ForegroundColor Red
    $allGood = $false
}

# Check Maven
Write-Host "📚 Checking Maven installation..." -ForegroundColor Yellow
try {
    $mvnVersion = mvn -version 2>&1 | Select-String "Apache Maven"
    if ($mvnVersion) {
        Write-Host "✅ Maven is installed and working" -ForegroundColor Green
        Write-Host "   Version: $mvnVersion" -ForegroundColor Gray
    } else {
        Write-Host "❌ Maven is not working properly" -ForegroundColor Red
        $allGood = $false
    }
} catch {
    Write-Host "❌ Maven is not installed or not in PATH" -ForegroundColor Red
    $allGood = $false
}

# Check MAVEN_HOME
Write-Host "🏠 Checking MAVEN_HOME..." -ForegroundColor Yellow
if ($env:MAVEN_HOME) {
    Write-Host "✅ MAVEN_HOME is set to: $env:MAVEN_HOME" -ForegroundColor Green
} else {
    Write-Host "❌ MAVEN_HOME is not set" -ForegroundColor Red
    $allGood = $false
}

# Check project compilation
Write-Host "🔨 Testing project compilation..." -ForegroundColor Yellow
try {
    Set-Location "voice-devops-api"
    $compileResult = mvn clean compile -q 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Project compiles successfully" -ForegroundColor Green
    } else {
        Write-Host "❌ Project compilation failed" -ForegroundColor Red
        Write-Host "   Error: $compileResult" -ForegroundColor Gray
        $allGood = $false
    }
} catch {
    Write-Host "❌ Could not test project compilation" -ForegroundColor Red
    $allGood = $false
} finally {
    Set-Location ".."
}

# Summary
Write-Host ""
Write-Host "=============================================" -ForegroundColor Blue
if ($allGood) {
    Write-Host "🎉 All checks passed! Your setup is ready." -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Yellow
    Write-Host "1. Run the project: cd voice-devops-api && mvn spring-boot:run" -ForegroundColor White
    Write-Host "2. Access at: http://localhost:8080" -ForegroundColor White
} else {
    Write-Host "⚠️  Some issues were found. Please fix them:" -ForegroundColor Red
    Write-Host "1. Run the setup script: .\setup-local-dev.ps1" -ForegroundColor Yellow
    Write-Host "2. Or follow the manual setup guide: LOCAL_DEVELOPMENT_SETUP.md" -ForegroundColor Yellow
    Write-Host "3. Restart PowerShell after installation" -ForegroundColor Yellow
}
Write-Host "=============================================" -ForegroundColor Blue 