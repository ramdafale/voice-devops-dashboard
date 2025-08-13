# Voice DevOps Dashboard - API Testing Script
# This script tests all available API endpoints

Write-Host "🚀 Testing Voice DevOps Dashboard APIs..." -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
Write-Host ""

$baseUrl = "http://localhost:8080"
$testResults = @()

# Function to test an API endpoint
function Test-ApiEndpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$Method = "GET",
        [string]$Body = $null
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
            $params.ContentType = "application/json"
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
$testResults += Test-ApiEndpoint -Name "Build Details" -Url "$baseUrl/api/dashboard/builds/1"
$testResults += Test-ApiEndpoint -Name "Recent Commands" -Url "$baseUrl/api/dashboard/commands"
$testResults += Test-ApiEndpoint -Name "API Deployments" -Url "$baseUrl/api/dashboard/api-deployments"
$testResults += Test-ApiEndpoint -Name "API Deployment Progress" -Url "$baseUrl/api/dashboard/api-deployments/1/progress"

# Test Voice APIs
Write-Host "🎤 Testing Voice APIs..." -ForegroundColor Yellow
Write-Host "------------------------" -ForegroundColor Yellow

$testResults += Test-ApiEndpoint -Name "Available Commands (admin)" -Url "$baseUrl/api/voice/commands?role=admin"
$testResults += Test-ApiEndpoint -Name "Available Commands (user)" -Url "$baseUrl/api/voice/commands?role=user"

# Test Deployment Agent APIs
Write-Host "🤖 Testing Deployment Agent APIs..." -ForegroundColor Yellow
Write-Host "-----------------------------------" -ForegroundColor Yellow

$testResults += Test-ApiEndpoint -Name "Deployment History" -Url "$baseUrl/api/deployment-agent/history"
$testResults += Test-ApiEndpoint -Name "Deployment History (with params)" -Url "$baseUrl/api/deployment-agent/history?limit=5`&offset=0"

# Test POST endpoints with sample data
Write-Host "📝 Testing POST APIs..." -ForegroundColor Yellow
Write-Host "----------------------" -ForegroundColor Yellow

$approveBody = '{"approvedBy": "admin"}'
$testResults += Test-ApiEndpoint -Name "Approve Build" -Url "$baseUrl/api/dashboard/builds/1/approve" -Method "POST" -Body $approveBody

$voiceCommandBody = "command=build my feature branch&username=admin"
$testResults += Test-ApiEndpoint -Name "Voice Command" -Url "$baseUrl/api/voice/command" -Method "POST" -Body $voiceCommandBody

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
Write-Host "🎉 API testing completed!" -ForegroundColor Green 