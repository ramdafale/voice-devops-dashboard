# Rewards Deployment Test Guide

This guide explains how to test the "deploy rewards details" voice command functionality.

## 🚀 Quick Start

### 1. Start the Spring Boot API
```bash
cd voice-devops-dashboard/voice-devops-api
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### 2. Open the Mock Jenkins Portal
Open `voice-devops-dashboard/mock-jenkins-service/html/index.html` in your browser.

### 3. Test the Voice Command
1. Click the "Start Voice" button in the Jenkins portal
2. The system will simulate the voice command "deploy rewards details"
3. Watch the deployment progress in real-time

## 🔧 How It Works

### Voice Command Flow
1. **Voice Input**: "deploy rewards details" is captured
2. **API Call**: Jenkins portal calls `POST /api/voice/command`
3. **Processing**: Spring Boot processes the command via `VoiceCommandProcessor`
4. **Build Creation**: A new build record is created in the database
5. **Progress Simulation**: 5-second deployment progress is simulated
6. **Real-time Updates**: Jenkins portal polls for updates every 500ms

### API Endpoints Used
- `POST /api/voice/command` - Process voice command
- `GET /api/dashboard/builds` - Get all builds
- `GET /api/dashboard/api-deployments` - Get API deployments
- `GET /api/dashboard/admin` - Get admin dashboard data

### Database Changes
- New build record with `jobName: "api-deploy"`
- `apiName: "Rewards Details API"`
- `status: "RUNNING"` → `"SUCCESS"`
- `deploymentProgress: 0` → `100`

## 🧪 Testing Options

### Option 1: Use the Test HTML File
Open `voice-devops-dashboard/test-rewards-deployment.html` in your browser to test individual API endpoints.

### Option 2: Use the Mock Jenkins Portal
1. Open the Jenkins portal
2. Click "Start Voice" 
3. Watch the deployment progress

### Option 3: Direct API Testing
```bash
# Test voice command
curl -X POST http://localhost:8080/api/voice/command \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "command=deploy rewards details api&username=admin"

# Get all builds
curl http://localhost:8080/api/dashboard/builds

# Get API deployments
curl http://localhost:8080/api/dashboard/api-deployments
```

## 📊 Expected Results

### Voice Command Response
```json
{
  "message": "Rewards Details API deployment started successfully. Build ID: API-1234567890. Progress tracking enabled. Deployment will complete in 5 seconds.",
  "success": true
}
```

### Build Record
```json
{
  "jenkinsBuildId": "API-1234567890",
  "jobName": "api-deploy",
  "branchName": "rewards-api-v1",
  "status": "RUNNING",
  "environment": "production",
  "apiName": "Rewards Details API",
  "deploymentProgress": 0,
  "triggeredBy": "admin"
}
```

### Progress Updates
- Progress increases from 0% to 100% over 5 seconds
- Status changes from "RUNNING" to "SUCCESS"
- Duration is set to 5 seconds

## 🔍 Troubleshooting

### Common Issues

1. **CORS Errors**: The API is configured to allow all origins
2. **Port Issues**: Make sure the API is running on port 8080
3. **Database Issues**: The API uses H2 in-memory database, so data resets on restart

### Debug Steps
1. Check browser console for errors
2. Verify API is running: `http://localhost:8080/api/dashboard/admin`
3. Check application logs for detailed error messages

## 🎯 Demo Scenario

1. **Start**: Open Jenkins portal and click "Start Voice"
2. **Command**: System simulates "deploy rewards details"
3. **Processing**: API creates build and starts progress simulation
4. **Progress**: Watch the progress bar update in real-time
5. **Completion**: Deployment shows as successful after 5 seconds
6. **Verification**: Check the API Deployments section for the new build

## 📝 Code Changes Made

### Mock Jenkins Service (`mock-jenkins-service/html/index.html`)
- Modified `processVoiceCommand()` to call Spring Boot API
- Added `deployRewardsDetailsAPI()` function
- Added `pollForRewardsAPIDeployment()` for real-time updates
- Added `updateBuildsFromAPI()` to fetch real data
- Updated initialization to load from API

### Spring Boot API
- Added build approval endpoint: `POST /api/builds/{buildId}/approve`
- Removed context path to fix URL routing
- Enhanced `VoiceCommandProcessor` with rewards deployment logic

### Database
- Build entity supports API deployments with progress tracking
- Voice command processing creates real build records 