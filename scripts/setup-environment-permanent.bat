@echo off
REM Permanent Environment Setup Script for AnshShare Microservices
REM IMPORTANT: Run this script AS ADMINISTRATOR for system-wide changes
REM Or run as normal user for user-level changes only

echo ========================================
echo  AnshShare Permanent Environment Setup
echo ========================================
echo.
echo WARNING: This will modify your system environment variables
echo.
choice /C YN /M "Do you want to continue"
if errorlevel 2 goto :EOF
if errorlevel 1 goto :CONTINUE

:CONTINUE
echo.

REM Check for admin privileges
net session >nul 2>&1
if %errorLevel% == 0 (
    echo [OK] Running with Administrator privileges
    echo Will set SYSTEM environment variables
    set SCOPE=MACHINE
) else (
    echo [INFO] Running without Administrator privileges
    echo Will set USER environment variables only
    set SCOPE=USER
)
echo.

REM ====================
REM Java Configuration
REM ====================
echo Setting up Java...
setx JAVA_HOME "C:\Users\k_man\.jdks\openjdk-23.0.2" /M >nul 2>&1 || setx JAVA_HOME "C:\Users\k_man\.jdks\openjdk-23.0.2" >nul 2>&1
echo [OK] JAVA_HOME set to: C:\Users\k_man\.jdks\openjdk-23.0.2
echo.

REM ====================
REM Maven Configuration
REM ====================
echo Setting up Maven...
echo.
echo Please enter the full path to your Maven installation
echo Example: C:\Program Files\Apache\maven-3.9.9
echo Or press Enter to skip if not installed
echo.
set /p MAVEN_PATH="Maven path: "

if not "%MAVEN_PATH%"=="" (
    if exist "%MAVEN_PATH%" (
        setx MAVEN_HOME "%MAVEN_PATH%" /M >nul 2>&1 || setx MAVEN_HOME "%MAVEN_PATH%" >nul 2>&1
        setx M2_HOME "%MAVEN_PATH%" /M >nul 2>&1 || setx M2_HOME "%MAVEN_PATH%" >nul 2>&1
        echo [OK] MAVEN_HOME set to: %MAVEN_PATH%
    ) else (
        echo [ERROR] Path does not exist: %MAVEN_PATH%
    )
) else (
    echo [SKIPPED] Maven configuration skipped
)
echo.

REM ====================
REM PostgreSQL Configuration
REM ====================
echo Setting up PostgreSQL...
echo.
echo Please enter the full path to your PostgreSQL installation
echo Example: C:\Program Files\PostgreSQL\16
echo Or press Enter to skip if not installed
echo.
set /p PGSQL_PATH="PostgreSQL path: "

if not "%PGSQL_PATH%"=="" (
    if exist "%PGSQL_PATH%" (
        setx PGSQL_HOME "%PGSQL_PATH%" /M >nul 2>&1 || setx PGSQL_HOME "%PGSQL_PATH%" >nul 2>&1
        echo [OK] PGSQL_HOME set to: %PGSQL_PATH%
    ) else (
        echo [ERROR] Path does not exist: %PGSQL_PATH%
    )
) else (
    echo [SKIPPED] PostgreSQL configuration skipped
)
echo.

REM ====================
REM PATH Configuration
REM ====================
echo.
echo ========================================
echo  Adding to PATH
echo ========================================
echo.
echo IMPORTANT: You need to manually add these to your PATH:
echo.
echo 1. %%JAVA_HOME%%\bin
echo 2. %%MAVEN_HOME%%\bin
echo 3. %%PGSQL_HOME%%\bin
echo.
echo To add to PATH:
echo   1. Press Win + Pause/Break (or Win + X, then System)
echo   2. Click "Advanced system settings"
echo   3. Click "Environment Variables"
echo   4. Under System variables (or User variables), select "Path"
echo   5. Click "Edit"
echo   6. Click "New" and add each path above
echo   7. Click OK on all dialogs
echo.
echo OR run this PowerShell command as Administrator:
echo.
echo [System.Environment]::SetEnvironmentVariable^(
echo     "Path",
echo     [System.Environment]::GetEnvironmentVariable^("Path", "Machine"^) +
echo     ";%%JAVA_HOME%%\bin;%%MAVEN_HOME%%\bin;%%PGSQL_HOME%%\bin",
echo     "Machine"
echo ^)
echo.

pause

echo.
echo ========================================
echo  Environment Variables Set
echo ========================================
echo.
echo The following environment variables have been set:
echo   JAVA_HOME  = C:\Users\k_man\.jdks\openjdk-23.0.2
if not "%MAVEN_PATH%"=="" echo   MAVEN_HOME = %MAVEN_PATH%
if not "%PGSQL_PATH%"=="" echo   PGSQL_HOME = %PGSQL_PATH%
echo.
echo IMPORTANT: You must RESTART your command prompt/terminal
echo for these changes to take effect!
echo.
echo After restart, verify with:
echo   java -version
echo   mvn -version
echo   psql --version
echo.

pause
