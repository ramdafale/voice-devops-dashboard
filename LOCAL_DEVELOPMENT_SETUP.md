# Local Development Setup Guide

This guide will help you set up JDK 17 and Maven to run the Voice DevOps Dashboard project locally.

## Prerequisites

- Windows 10/11
- PowerShell (run as Administrator)
- Internet connection

## Option 1: Automated Setup (Recommended)

1. **Open PowerShell as Administrator**
2. **Navigate to the project directory:**
   ```powershell
   cd "C:\cursor project\voice-devops-dashboard"
   ```
3. **Run the setup script:**
   ```powershell
   .\setup-local-dev.ps1
   ```

## Option 2: Manual Installation

### Step 1: Install JDK 17

#### Option A: Using Chocolatey (Recommended)
1. **Install Chocolatey** (if not already installed):
   ```powershell
   Set-ExecutionPolicy Bypass -Scope Process -Force
   [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
   iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
   ```

2. **Install OpenJDK 17:**
   ```powershell
   choco install openjdk17 -y
   ```

#### Option B: Manual Download
1. **Download OpenJDK 17** from [Adoptium](https://adoptium.net/temurin/releases/?version=17)
2. **Run the installer** and follow the setup wizard
3. **Add to PATH** (usually done automatically by installer)

### Step 2: Install Maven

#### Option A: Using Chocolatey
```powershell
choco install maven -y
```

#### Option B: Manual Download
1. **Download Maven** from [Apache Maven](https://maven.apache.org/download.cgi)
2. **Extract to** `C:\Program Files\Apache\maven`
3. **Add to PATH:**
   - Add `C:\Program Files\Apache\maven\bin` to your system PATH
   - Add `MAVEN_HOME` environment variable pointing to `C:\Program Files\Apache\maven`

### Step 3: Verify Installation

1. **Open a new PowerShell window**
2. **Check Java version:**
   ```powershell
   java -version
   ```
   Expected output: `openjdk version "17.x.x"`

3. **Check Maven version:**
   ```powershell
   mvn -version
   ```
   Expected output: `Apache Maven x.x.x`

## Project Configuration

### Environment Variables

The following environment variables should be set:

- `JAVA_HOME`: Path to JDK installation (e.g., `C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x-hotspot`)
- `MAVEN_HOME`: Path to Maven installation (e.g., `C:\Program Files\Apache\maven`)
- `PATH`: Should include both `%JAVA_HOME%\bin` and `%MAVEN_HOME%\bin`

### IDE Configuration

#### IntelliJ IDEA
1. **File → Project Structure → Project**
2. **Project SDK:** Select JDK 17
3. **Project language level:** 17

#### Eclipse
1. **Window → Preferences → Java → Installed JREs**
2. **Add** JDK 17 installation
3. **Set as default**

#### VS Code
1. **Install Extension Pack for Java**
2. **Java: Configure Java Runtime**
3. **Select JDK 17**

## Running the Project

### Option 1: Maven Command Line
```powershell
cd voice-devops-api
mvn spring-boot:run
```

### Option 2: IDE
1. **Open** `VoiceDevOpsApplication.java`
2. **Run** the main method

### Option 3: Docker (Alternative)
```powershell
.\start-demo.sh
```

## Troubleshooting

### Common Issues

#### "java is not recognized"
- **Solution:** Restart PowerShell after installation
- **Alternative:** Add Java bin directory to PATH manually

#### "mvn is not recognized"
- **Solution:** Restart PowerShell after installation
- **Alternative:** Add Maven bin directory to PATH manually

#### "JAVA_HOME is not set"
- **Solution:** Set JAVA_HOME environment variable
- **Command:** `$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x-hotspot"`

#### Maven build fails
- **Solution:** Ensure JDK 17 is in PATH
- **Check:** `java -version` should show version 17

### Verification Commands

```powershell
# Check Java
java -version
echo $env:JAVA_HOME

# Check Maven
mvn -version
echo $env:MAVEN_HOME

# Check PATH
echo $env:PATH
```

## Next Steps

After successful setup:

1. **Test the project:**
   ```powershell
   cd voice-devops-api
   mvn clean compile
   mvn spring-boot:run
   ```

2. **Access the application:**
   - Main API: http://localhost:8080
   - Admin Dashboard: http://localhost:8080/admin-dashboard.html
   - User Dashboard: http://localhost:8080/user-dashboard.html

3. **Run tests:**
   ```powershell
   mvn test
   ```

## Support

If you encounter issues:
1. Check the troubleshooting section above
2. Verify all environment variables are set correctly
3. Restart PowerShell/IDE after installation
4. Check the project's README.md for additional information 