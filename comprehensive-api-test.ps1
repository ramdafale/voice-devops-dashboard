# Comprehensive API Testing Script for Voice DevOps Dashboard
# This script tests all API endpoints with proper parameters

Write-Host "🚀 Comprehensive Voice DevOps Dashboard API Testing..." -ForegroundColor Green
Write-Host "=====================================================" -ForegroundColor Green
Write-Host ""

$baseUrl = "http://localhost:8080"
$testResults = @()

# Function to test an API endpoint
function Test-ApiEndpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$Method = "GET",
        [string]$Body = $null,
        [string]$ContentType = $null
    )
    
    Write-Host "Testing: $Name" -ForegroundColor Cyan
    Write-Host "URL: $Url" -ForegroundColor Gray
    Write-Host "Method: $Method" -ForegroundColor Gray
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
        }
        
        if ($Body) {
            $params.Body = $Body
            if ($ContentType) {
                $params.ContentType = $ContentType
            }
        }
        
        $response = Invoke-WebRequest @params -ErrorAction Stop
        
        $result = @{
            Name = $Name
            StatusCode = $response.StatusCode
            Success = $response.StatusCode -eq 200
            Response = $response.Content
            Error = $null
        }
        
        Write-Host "✅ Status: $($response.StatusCode)" -ForegroundColor Green
        Write-Host "Response: $($response.Content.Substring(0, [Math]::Min(200, $response.Content.Length)))..." -ForegroundColor White
        
    } catch {
        $result = @{
            Name = $Name
            StatusCode = $_.Exception.Response.StatusCode.value__
            Success = $false
            Response = $null
            Error = $_.Exception.Message
        }
        
        Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    Write-Host ""
    return $result
}

# Test Dashboard APIs
Write-Host "📊 Testing Dashboard APIs..." -ForegroundColor Yellow
Write-Host "----------------------------" -ForegroundColor Yellow

$testResults += Test-ApiEndpoint -Name "Admin Dashboard" -Url "$baseUrl/api/dashboard/admin"
$testResults += Test-ApiEndpoint -Name "User Dashboard (admin)" -Url "$baseUrl/api/dashboard/user/admin"
$testResults += Test-ApiEndpoint -Name "All Builds" -Url "$baseUrl/api/dashboard/builds"

# Get actual build data for testing specific endpoints
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/dashboard/builds" -Method GET
    $builds = $response.Content | ConvertFrom-Json
    if ($builds.Count -gt 0) {
        $firstBuild = $builds[0]
        $jenkinsBuildId = $firstBuild.jenkinsBuildId
        
        Write-Host "Using build ID: $jenkinsBuildId for testing..." -ForegroundColor Yellow
        
        $testResults += Test-ApiEndpoint -Name "Build Details (with correct ID)" -Url "$baseUrl/api/dashboard/builds/$jenkinsBuildId"
        $testResults += Test-ApiEndpoint -Name "API Deployment Progress (with correct ID)" -Url "$baseUrl/api/dashboard/api-deployments/$jenkinsBuildId/progress"
        $testResults += Test-ApiEndpoint -Name "Approve Build (with correct ID)" -Url "$baseUrl/api/dashboard/builds/$jenkinsBuildId/approve" -Method "POST" -Body '{"approvedBy": "admin"}' -ContentType "application/json"
    }
} catch {
    Write-Host "Could not get build data for testing specific endpoints" -ForegroundColor Red
}

$testResults += Test-ApiEndpoint -Name "Recent Commands" -Url "$baseUrl/api/dashboard/commands"
$testResults += Test-ApiEndpoint -Name "API Deployments" -Url "$baseUrl/api/dashboard/api-deployments"

# Test Voice APIs
Write-Host "🎤 Testing Voice APIs..." -ForegroundColor Yellow
Write-Host "------------------------" -ForegroundColor Yellow

$testResults += Test-ApiEndpoint -Name "Available Commands (admin)" -Url "$baseUrl/api/voice/commands?role=admin"
$testResults += Test-ApiEndpoint -Name "Available Commands (user)" -Url "$baseUrl/api/voice/commands?role=user"

# Test Voice Command with proper parameters
$testResults += Test-ApiEndpoint -Name "Voice Command (POST)" -Url "$baseUrl/api/voice/command" -Method "POST" -Body "command=build my feature branch&username=admin" -ContentType "application/x-www-form-urlencoded"

# Test Deployment Agent APIs
Write-Host "🤖 Testing Deployment Agent APIs..." -ForegroundColor Yellow
Write-Host "-----------------------------------" -ForegroundColor Yellow

$testResults += Test-ApiEndpoint -Name "Deployment History" -Url "$baseUrl/api/deployment-agent/history"
$testResults += Test-ApiEndpoint -Name "Deployment History (with params)" -Url "$baseUrl/api/deployment-agent/history?limit=5&offset=0"
$testResults += Test-ApiEndpoint -Name "Jenkins Info" -Url "$baseUrl/api/deployment-agent/jenkins/info"
$testResults += Test-ApiEndpoint -Name "GitHub Info" -Url "$baseUrl/api/deployment-agent/github/info"

# Test POST endpoints
Write-Host "📝 Testing POST APIs..." -ForegroundColor Yellow
Write-Host "----------------------" -ForegroundColor Yellow

$testResults += Test-ApiEndpoint -Name "Deployment Orchestration" -Url "$baseUrl/api/deployment-agent/orchestrate" -Method "POST" -Body '{"target": "production", "branch": "main"}' -ContentType "application/json"
$testResults += Test-ApiEndpoint -Name "Deployment Recommendations" -Url "$baseUrl/api/deployment-agent/recommendations" -Method "POST" -Body '{"target": "staging", "branch": "feature/new-feature"}' -ContentType "application/json"
$testResults += Test-ApiEndpoint -Name "Execute Deployment" -Url "$baseUrl/api/deployment-agent/execute" -Method "POST" -Body '{"deploymentId": "DEP-001", "approval": "APPROVED"}' -ContentType "application/json"
$testResults += Test-ApiEndpoint -Name "Rollback Deployment" -Url "$baseUrl/api/deployment-agent/rollback" -Method "POST" -Body '{"deploymentId": "DEP-001", "reason": "Critical bug found"}' -ContentType "application/json"

# Summary
Write-Host "📋 API Test Summary" -ForegroundColor Green
Write-Host "==================" -ForegroundColor Green
Write-Host ""

$successCount = ($testResults | Where-Object { $_.Success }).Count
$totalCount = $testResults.Count

Write-Host "Total APIs Tested: $totalCount" -ForegroundColor White
Write-Host "Successful: $successCount" -ForegroundColor Green
Write-Host "Failed: $($totalCount - $successCount)" -ForegroundColor Red
Write-Host ""

# Show failed APIs
$failedApis = $testResults | Where-Object { -not $_.Success }
if ($failedApis.Count -gt 0) {
    Write-Host "❌ Failed APIs:" -ForegroundColor Red
    foreach ($api in $failedApis) {
        Write-Host "  • $($api.Name): $($api.Error)" -ForegroundColor Red
    }
    Write-Host ""
}

# Show all results in table format
Write-Host "📊 Detailed Results:" -ForegroundColor Yellow
Write-Host "-------------------" -ForegroundColor Yellow

$testResults | Format-Table -Property Name, StatusCode, Success, Error -AutoSize

Write-Host ""
Write-Host "🎉 Comprehensive API testing completed!" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green 