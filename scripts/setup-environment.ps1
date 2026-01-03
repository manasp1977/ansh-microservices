# PowerShell Script to Setup Environment Variables
# Run as Administrator for system-wide changes
# Run as normal user for user-level changes only

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " AnshShare Environment Setup (PowerShell)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if running as Administrator
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if ($isAdmin) {
    Write-Host "[OK] Running with Administrator privileges" -ForegroundColor Green
    Write-Host "Will set SYSTEM environment variables" -ForegroundColor Green
    $scope = "Machine"
} else {
    Write-Host "[INFO] Running without Administrator privileges" -ForegroundColor Yellow
    Write-Host "Will set USER environment variables only" -ForegroundColor Yellow
    $scope = "User"
}
Write-Host ""

# ====================
# Java Configuration
# ====================
Write-Host "Setting up Java..." -ForegroundColor Cyan
$javaHome = "C:\Users\k_man\.jdks\openjdk-23.0.2"

if (Test-Path $javaHome) {
    [System.Environment]::SetEnvironmentVariable("JAVA_HOME", $javaHome, $scope)
    Write-Host "[OK] JAVA_HOME set to: $javaHome" -ForegroundColor Green

    # Add to PATH
    $currentPath = [System.Environment]::GetEnvironmentVariable("Path", $scope)
    $javaBin = "$javaHome\bin"

    if ($currentPath -notlike "*$javaBin*") {
        [System.Environment]::SetEnvironmentVariable("Path", "$currentPath;$javaBin", $scope)
        Write-Host "[OK] Added to PATH: $javaBin" -ForegroundColor Green
    } else {
        Write-Host "[INFO] Java already in PATH" -ForegroundColor Yellow
    }
} else {
    Write-Host "[ERROR] Java not found at: $javaHome" -ForegroundColor Red
}
Write-Host ""

# ====================
# Maven Configuration
# ====================
Write-Host "Setting up Maven..." -ForegroundColor Cyan
Write-Host ""

# Search for Maven in common locations
$mavenLocations = @(
    "C:\Program Files\Apache\maven",
    "C:\Program Files\Maven",
    "C:\apache-maven-3.9.9",
    "C:\apache-maven-3.9.8",
    "C:\apache-maven-3.9.6",
    "C:\Maven"
)

$mavenHome = $null
foreach ($location in $mavenLocations) {
    if (Test-Path $location) {
        # Check for subdirectories
        $subdirs = Get-ChildItem -Path $location -Directory -ErrorAction SilentlyContinue
        if ($subdirs) {
            foreach ($dir in $subdirs) {
                if ($dir.Name -like "apache-maven-*") {
                    $mavenHome = $dir.FullName
                    break
                }
            }
        }
        if (-not $mavenHome) {
            $mavenHome = $location
        }
        break
    }
}

if ($mavenHome) {
    [System.Environment]::SetEnvironmentVariable("MAVEN_HOME", $mavenHome, $scope)
    [System.Environment]::SetEnvironmentVariable("M2_HOME", $mavenHome, $scope)
    Write-Host "[OK] MAVEN_HOME set to: $mavenHome" -ForegroundColor Green

    # Add to PATH
    $currentPath = [System.Environment]::GetEnvironmentVariable("Path", $scope)
    $mavenBin = "$mavenHome\bin"

    if ($currentPath -notlike "*$mavenBin*") {
        [System.Environment]::SetEnvironmentVariable("Path", "$currentPath;$mavenBin", $scope)
        Write-Host "[OK] Added to PATH: $mavenBin" -ForegroundColor Green
    } else {
        Write-Host "[INFO] Maven already in PATH" -ForegroundColor Yellow
    }
} else {
    Write-Host "[WARNING] Maven not found in common locations" -ForegroundColor Yellow
    Write-Host "Please install Maven from: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
    Write-Host "Or manually enter the path:" -ForegroundColor Yellow
    $manualMaven = Read-Host "Maven installation path (or press Enter to skip)"

    if ($manualMaven -and (Test-Path $manualMaven)) {
        [System.Environment]::SetEnvironmentVariable("MAVEN_HOME", $manualMaven, $scope)
        [System.Environment]::SetEnvironmentVariable("M2_HOME", $manualMaven, $scope)
        Write-Host "[OK] MAVEN_HOME set to: $manualMaven" -ForegroundColor Green

        $currentPath = [System.Environment]::GetEnvironmentVariable("Path", $scope)
        $mavenBin = "$manualMaven\bin"
        [System.Environment]::SetEnvironmentVariable("Path", "$currentPath;$mavenBin", $scope)
        Write-Host "[OK] Added to PATH: $mavenBin" -ForegroundColor Green
    }
}
Write-Host ""

# ====================
# PostgreSQL Configuration
# ====================
Write-Host "Setting up PostgreSQL..." -ForegroundColor Cyan
Write-Host ""

# Search for PostgreSQL in common locations
$pgsqlLocations = @(
    "C:\Program Files\PostgreSQL\16",
    "C:\Program Files\PostgreSQL\15",
    "C:\Program Files\PostgreSQL\14",
    "C:\PostgreSQL\16",
    "C:\PostgreSQL\15",
    "C:\PostgreSQL\14"
)

$pgsqlHome = $null
foreach ($location in $pgsqlLocations) {
    if (Test-Path $location) {
        $pgsqlHome = $location
        break
    }
}

if ($pgsqlHome) {
    [System.Environment]::SetEnvironmentVariable("PGSQL_HOME", $pgsqlHome, $scope)
    Write-Host "[OK] PGSQL_HOME set to: $pgsqlHome" -ForegroundColor Green

    # Add to PATH
    $currentPath = [System.Environment]::GetEnvironmentVariable("Path", $scope)
    $pgsqlBin = "$pgsqlHome\bin"

    if ($currentPath -notlike "*$pgsqlBin*") {
        [System.Environment]::SetEnvironmentVariable("Path", "$currentPath;$pgsqlBin", $scope)
        Write-Host "[OK] Added to PATH: $pgsqlBin" -ForegroundColor Green
    } else {
        Write-Host "[INFO] PostgreSQL already in PATH" -ForegroundColor Yellow
    }
} else {
    Write-Host "[WARNING] PostgreSQL not found in common locations" -ForegroundColor Yellow
    Write-Host "Please install PostgreSQL from: https://www.postgresql.org/download/" -ForegroundColor Yellow
    Write-Host "Or manually enter the path:" -ForegroundColor Yellow
    $manualPgsql = Read-Host "PostgreSQL installation path (or press Enter to skip)"

    if ($manualPgsql -and (Test-Path $manualPgsql)) {
        [System.Environment]::SetEnvironmentVariable("PGSQL_HOME", $manualPgsql, $scope)
        Write-Host "[OK] PGSQL_HOME set to: $manualPgsql" -ForegroundColor Green

        $currentPath = [System.Environment]::GetEnvironmentVariable("Path", $scope)
        $pgsqlBin = "$manualPgsql\bin"
        [System.Environment]::SetEnvironmentVariable("Path", "$currentPath;$pgsqlBin", $scope)
        Write-Host "[OK] Added to PATH: $pgsqlBin" -ForegroundColor Green
    }
}
Write-Host ""

# ====================
# Maven Options
# ====================
[System.Environment]::SetEnvironmentVariable("MAVEN_OPTS", "-Xmx2048m -Xms512m", $scope)
Write-Host "[OK] MAVEN_OPTS set" -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Setup Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Environment variables have been set for: $scope" -ForegroundColor Green
Write-Host ""
Write-Host "IMPORTANT: You must restart your terminal/IDE for changes to take effect!" -ForegroundColor Yellow
Write-Host ""
Write-Host "After restart, verify with:" -ForegroundColor Cyan
Write-Host "  java -version" -ForegroundColor White
Write-Host "  mvn -version" -ForegroundColor White
Write-Host "  psql --version" -ForegroundColor White
Write-Host ""

# Offer to verify now (requires new process)
$verify = Read-Host "Would you like to verify installations now? (Y/N)"
if ($verify -eq "Y" -or $verify -eq "y") {
    Write-Host ""
    Write-Host "Verifying (in new process)..." -ForegroundColor Cyan
    Write-Host ""

    Write-Host "Java version:" -ForegroundColor Cyan
    & "$javaHome\bin\java.exe" -version 2>&1
    Write-Host ""

    if ($mavenHome) {
        Write-Host "Maven version:" -ForegroundColor Cyan
        & "$mavenHome\bin\mvn.cmd" -version 2>&1
        Write-Host ""
    }

    if ($pgsqlHome) {
        Write-Host "PostgreSQL version:" -ForegroundColor Cyan
        & "$pgsqlHome\bin\psql.exe" --version 2>&1
        Write-Host ""
    }
}

Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Restart your terminal/command prompt" -ForegroundColor White
Write-Host "2. Run: setup-databases.bat" -ForegroundColor White
Write-Host "3. Run: cd .. && mvn clean install" -ForegroundColor White
Write-Host "4. Run: start-all-services.bat" -ForegroundColor White
Write-Host ""

Read-Host "Press Enter to exit"
