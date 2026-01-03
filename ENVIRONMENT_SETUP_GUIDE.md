# Environment Setup Guide

## Overview

This guide will help you set up Java, Maven, and PostgreSQL environment variables for the AnshShare microservices project.

## Known Installation Paths

Based on your system:

- **Java**: `C:\Users\k_man\.jdks\openjdk-23.0.2` ✅
- **Maven**: *To be determined*
- **PostgreSQL**: *To be determined*

## Quick Setup Options

### Option 1: Automated PowerShell Script (Recommended)

**For permanent system-wide changes (requires Administrator)**:

```powershell
# Right-click PowerShell and select "Run as Administrator"
cd C:\Users\k_man\IdeaProjects\ansh\ansh-microservices\scripts
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
.\setup-environment.ps1
```

**For current user only (no admin required)**:

```powershell
# Open PowerShell normally
cd C:\Users\k_man\IdeaProjects\ansh\ansh-microservices\scripts
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
.\setup-environment.ps1
```

The script will:
- ✅ Automatically find and configure Java
- ✅ Search for Maven in common locations
- ✅ Search for PostgreSQL in common locations
- ✅ Set environment variables
- ✅ Add to PATH
- ✅ Verify installations

### Option 2: Batch Script (Current Session Only)

For temporary setup (current terminal session only):

```cmd
cd C:\Users\k_man\IdeaProjects\ansh\ansh-microservices\scripts
setup-environment.bat
```

This sets environment variables for the current command prompt session only.

### Option 3: Manual GUI Setup

#### Step 1: Open Environment Variables

**Method A** (Windows 11/10):
1. Press `Win + X`
2. Click "System"
3. Click "Advanced system settings" on the right
4. Click "Environment Variables" button

**Method B**:
1. Right-click "This PC" or "My Computer"
2. Select "Properties"
3. Click "Advanced system settings"
4. Click "Environment Variables"

**Method C** (Quick):
1. Press `Win + R`
2. Type: `sysdm.cpl`
3. Press Enter
4. Click "Environment Variables"

#### Step 2: Set JAVA_HOME

In the "Environment Variables" window:

1. Under "System variables" (or "User variables"), click "New"
2. Variable name: `JAVA_HOME`
3. Variable value: `C:\Users\k_man\.jdks\openjdk-23.0.2`
4. Click "OK"

#### Step 3: Set MAVEN_HOME (if Maven is installed)

1. First, find your Maven installation:
   - Common locations:
     - `C:\Program Files\Apache\maven`
     - `C:\apache-maven-3.9.x`
     - `C:\Program Files\Maven`

2. Create new system variable:
   - Variable name: `MAVEN_HOME`
   - Variable value: `<your Maven installation path>`
   - Click "OK"

3. Create another variable:
   - Variable name: `M2_HOME`
   - Variable value: `<same as MAVEN_HOME>`
   - Click "OK"

**If Maven is not installed**:
1. Download from: https://maven.apache.org/download.cgi
2. Extract to: `C:\apache-maven-3.9.9` (or similar)
3. Set environment variables as above

#### Step 4: Set PGSQL_HOME (if PostgreSQL is installed)

1. First, find your PostgreSQL installation:
   - Common locations:
     - `C:\Program Files\PostgreSQL\16`
     - `C:\Program Files\PostgreSQL\15`
     - `C:\PostgreSQL\16`

2. Create new system variable:
   - Variable name: `PGSQL_HOME`
   - Variable value: `<your PostgreSQL installation path>`
   - Click "OK"

**If PostgreSQL is not installed**:
1. Download from: https://www.postgresql.org/download/windows/
2. Install (recommended version: 14, 15, or 16)
3. Set environment variable as above

#### Step 5: Update PATH

1. In "Environment Variables", under "System variables", find and select "Path"
2. Click "Edit"
3. Click "New" and add each of these (one at a time):
   - `%JAVA_HOME%\bin`
   - `%MAVEN_HOME%\bin` (if Maven is installed)
   - `%PGSQL_HOME%\bin` (if PostgreSQL is installed)
4. Click "OK" on all dialogs

#### Step 6: Set MAVEN_OPTS (Optional but Recommended)

1. Create new system variable:
   - Variable name: `MAVEN_OPTS`
   - Variable value: `-Xmx2048m -Xms512m`
   - Click "OK"

#### Step 7: Apply Changes

1. Click "OK" on all open dialogs
2. **Restart your command prompt/terminal** (IMPORTANT!)
3. **Restart your IDE** (IntelliJ IDEA, Eclipse, etc.)

## Verification

After setting environment variables, verify with:

```cmd
# Open a NEW command prompt (IMPORTANT!)

# Test Java
java -version
# Expected: openjdk version "23.0.2" (or similar)

# Test Maven
mvn -version
# Expected: Apache Maven 3.x.x

# Test PostgreSQL
psql --version
# Expected: psql (PostgreSQL) 14.x or higher
```

If any command is not recognized:
1. Verify the environment variable is set correctly
2. Ensure the bin directory is in PATH
3. Make sure you opened a **NEW** terminal after setting variables

## Troubleshooting

### Java Not Found

**Symptoms**:
```
'java' is not recognized as an internal or external command
```

**Solutions**:
1. Verify JAVA_HOME is set:
   ```cmd
   echo %JAVA_HOME%
   ```
   Should show: `C:\Users\k_man\.jdks\openjdk-23.0.2`

2. Verify PATH includes Java:
   ```cmd
   echo %PATH%
   ```
   Should contain: `C:\Users\k_man\.jdks\openjdk-23.0.2\bin`

3. Did you restart the terminal? **You must open a NEW terminal!**

### Maven Not Found

**Symptoms**:
```
'mvn' is not recognized as an internal or external command
```

**Solutions**:
1. Check if Maven is installed:
   ```cmd
   dir "C:\Program Files\Apache" /b
   dir C:\ /b | findstr maven
   ```

2. If not installed, download from: https://maven.apache.org/download.cgi
   - Download Binary zip archive (e.g., apache-maven-3.9.9-bin.zip)
   - Extract to: `C:\apache-maven-3.9.9`
   - Set MAVEN_HOME to: `C:\apache-maven-3.9.9`
   - Add to PATH: `%MAVEN_HOME%\bin`

3. If installed, verify MAVEN_HOME:
   ```cmd
   echo %MAVEN_HOME%
   ```

### PostgreSQL Not Found

**Symptoms**:
```
'psql' is not recognized as an internal or external command
```

**Solutions**:
1. Check if PostgreSQL is installed:
   ```cmd
   dir "C:\Program Files\PostgreSQL" /b
   ```

2. If not installed, download from: https://www.postgresql.org/download/windows/
   - Recommended version: PostgreSQL 14, 15, or 16
   - During installation, remember the postgres user password

3. If installed, verify PGSQL_HOME and PATH:
   ```cmd
   echo %PGSQL_HOME%
   echo %PATH%
   ```

### Environment Variables Not Taking Effect

**Common Mistake**: Not restarting terminal after setting variables

**Solution**:
1. Close ALL command prompts/terminals
2. Close your IDE (IntelliJ, Eclipse, VS Code)
3. Open a NEW command prompt
4. Open your IDE again
5. Test again

**Alternative**: Restart your computer (ensures everything is refreshed)

## Automated Installation Scripts

If software is not installed, use these scripts:

### Install Maven (PowerShell)

```powershell
# Download and install Maven automatically
$mavenVersion = "3.9.9"
$mavenUrl = "https://dlcdn.apache.org/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
$downloadPath = "$env:TEMP\maven.zip"
$installPath = "C:\apache-maven-$mavenVersion"

# Download
Invoke-WebRequest -Uri $mavenUrl -OutFile $downloadPath

# Extract
Expand-Archive -Path $downloadPath -DestinationPath "C:\" -Force

# Set environment variables
[System.Environment]::SetEnvironmentVariable("MAVEN_HOME", $installPath, "Machine")
[System.Environment]::SetEnvironmentVariable("M2_HOME", $installPath, "Machine")

$currentPath = [System.Environment]::GetEnvironmentVariable("Path", "Machine")
[System.Environment]::SetEnvironmentVariable("Path", "$currentPath;$installPath\bin", "Machine")

Write-Host "Maven installed to: $installPath"
Write-Host "Please restart your terminal"
```

### Install PostgreSQL

PostgreSQL requires a proper installer:

1. Go to: https://www.postgresql.org/download/windows/
2. Download the installer for Windows
3. Run the installer
4. During installation:
   - Set password for postgres user (remember this!)
   - Select port: 5432 (default)
   - Install components: PostgreSQL Server, pgAdmin 4, Command Line Tools
5. After installation, PostgreSQL should be in PATH automatically
6. Verify: `psql --version`

## Next Steps

Once environment variables are set and verified:

1. **Create Databases**:
   ```cmd
   cd C:\Users\k_man\IdeaProjects\ansh\ansh-microservices\scripts
   setup-databases.bat
   ```

2. **Build Project**:
   ```cmd
   cd C:\Users\k_man\IdeaProjects\ansh\ansh-microservices
   mvn clean install
   ```

3. **Start Services**:
   ```cmd
   cd scripts
   start-all-services.bat
   ```

4. **Run Tests**:
   ```cmd
   cd scripts
   run-e2e-tests.bat
   ```

## Quick Reference

### Environment Variables Summary

| Variable | Value | Purpose |
|----------|-------|---------|
| JAVA_HOME | C:\Users\k_man\.jdks\openjdk-23.0.2 | Java installation |
| MAVEN_HOME | C:\apache-maven-3.9.x | Maven installation |
| M2_HOME | Same as MAVEN_HOME | Maven (legacy) |
| PGSQL_HOME | C:\Program Files\PostgreSQL\16 | PostgreSQL |
| MAVEN_OPTS | -Xmx2048m -Xms512m | Maven memory |

### PATH Additions

Must include:
- %JAVA_HOME%\bin
- %MAVEN_HOME%\bin
- %PGSQL_HOME%\bin

## Support

If you encounter issues:
1. Check this guide's troubleshooting section
2. Run `scripts\find-installations.bat` to locate software
3. Run `scripts\setup-environment.ps1` for automated setup
4. Refer to TESTING_GUIDE.md for additional help

---

**Environment Setup Guide Complete!**
