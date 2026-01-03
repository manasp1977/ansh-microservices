@echo off
REM Environment Setup Script for AnshShare Microservices
REM This script sets environment variables for the current session
REM For permanent changes, use setup-environment-permanent.bat (requires admin)

echo ========================================
echo  AnshShare Environment Setup
echo ========================================
echo.

REM ====================
REM Java Configuration
REM ====================
echo Setting up Java...
set JAVA_HOME=C:\Users\k_man\.jdks\openjdk-23.0.2
set PATH=%JAVA_HOME%\bin;%PATH%
echo [OK] JAVA_HOME = %JAVA_HOME%
echo.

REM ====================
REM Maven Configuration
REM ====================
echo Setting up Maven...
REM Check common Maven installation locations
if exist "C:\Program Files\Apache\maven" (
    set MAVEN_HOME=C:\Program Files\Apache\maven
    set M2_HOME=%MAVEN_HOME%
    set PATH=%MAVEN_HOME%\bin;%PATH%
    echo [OK] MAVEN_HOME = %MAVEN_HOME%
) else if exist "C:\Program Files\Maven" (
    set MAVEN_HOME=C:\Program Files\Maven
    set M2_HOME=%MAVEN_HOME%
    set PATH=%MAVEN_HOME%\bin;%PATH%
    echo [OK] MAVEN_HOME = %MAVEN_HOME%
) else if exist "C:\apache-maven-3.9.9" (
    set MAVEN_HOME=C:\apache-maven-3.9.9
    set M2_HOME=%MAVEN_HOME%
    set PATH=%MAVEN_HOME%\bin;%PATH%
    echo [OK] MAVEN_HOME = %MAVEN_HOME%
) else if exist "C:\Maven" (
    set MAVEN_HOME=C:\Maven
    set M2_HOME=%MAVEN_HOME%
    set PATH=%MAVEN_HOME%\bin;%PATH%
    echo [OK] MAVEN_HOME = %MAVEN_HOME%
) else (
    echo [WARNING] Maven not found in common locations
    echo Please set MAVEN_HOME manually in this script
    echo Common locations to check:
    echo   - C:\Program Files\Apache\maven
    echo   - C:\apache-maven-x.x.x
    echo   - C:\Maven
)
echo.

REM ====================
REM PostgreSQL Configuration
REM ====================
echo Setting up PostgreSQL...
REM Check common PostgreSQL installation locations
if exist "C:\Program Files\PostgreSQL\16" (
    set PGSQL_HOME=C:\Program Files\PostgreSQL\16
    set PATH=%PGSQL_HOME%\bin;%PATH%
    echo [OK] PGSQL_HOME = %PGSQL_HOME%
) else if exist "C:\Program Files\PostgreSQL\15" (
    set PGSQL_HOME=C:\Program Files\PostgreSQL\15
    set PATH=%PGSQL_HOME%\bin;%PATH%
    echo [OK] PGSQL_HOME = %PGSQL_HOME%
) else if exist "C:\Program Files\PostgreSQL\14" (
    set PGSQL_HOME=C:\Program Files\PostgreSQL\14
    set PATH=%PGSQL_HOME%\bin;%PATH%
    echo [OK] PGSQL_HOME = %PGSQL_HOME%
) else if exist "C:\PostgreSQL\16" (
    set PGSQL_HOME=C:\PostgreSQL\16
    set PATH=%PGSQL_HOME%\bin;%PATH%
    echo [OK] PGSQL_HOME = %PGSQL_HOME%
) else (
    echo [WARNING] PostgreSQL not found in common locations
    echo Please set PGSQL_HOME manually in this script
    echo Common locations to check:
    echo   - C:\Program Files\PostgreSQL\{version}
    echo   - C:\PostgreSQL\{version}
)
echo.

REM ====================
REM Maven Options
REM ====================
echo Setting Maven options...
set MAVEN_OPTS=-Xmx2048m -Xms512m
echo [OK] MAVEN_OPTS = %MAVEN_OPTS%
echo.

echo ========================================
echo  Environment Setup Complete
echo ========================================
echo.
echo Current session environment:
echo   JAVA_HOME  = %JAVA_HOME%
echo   MAVEN_HOME = %MAVEN_HOME%
echo   PGSQL_HOME = %PGSQL_HOME%
echo.
echo NOTE: These settings are for THIS SESSION ONLY
echo To make permanent, run: setup-environment-permanent.bat (as Administrator)
echo.

REM Verify installations
echo ========================================
echo  Verifying Installations
echo ========================================
echo.

echo Java version:
java -version 2>&1
echo.

echo Maven version:
call mvn -version 2>&1
echo.

echo PostgreSQL version:
psql --version 2>&1
echo.

echo ========================================
echo  Verification Complete
echo ========================================
echo.
echo If you see version information above, setup was successful!
echo.
echo Next steps:
echo 1. Run: setup-databases.bat
echo 2. Run: cd .. ^&^& mvn clean install
echo 3. Run: start-all-services.bat
echo.

pause
