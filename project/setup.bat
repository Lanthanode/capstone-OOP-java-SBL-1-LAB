@echo off
setlocal enabledelayedexpansion
title SB-TMS Build and Setup Script

echo ======================================================================
echo   SMART BANKING TRANSACTION ^& PORTFOLIO MANAGEMENT SYSTEM (SB-TMS)
echo   Capstone Project Build ^& Setup Script
echo   Candidate: Anish Vyapari (Roll No: 25CA1012 ^| PRN: DY25ENGU0AIM012)
echo ======================================================================

set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"

:: 1. Verify Java compiler
where javac >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] javac compiler was not found in your PATH.
    echo Please install OpenJDK 17+ or ensure JAVA_HOME is configured.
    exit /b 1
)

:: 2. Create required runtime directories
if not exist "backend\bin" mkdir "backend\bin"
if not exist "backend\lib" mkdir "backend\lib"
if not exist "database" mkdir "database"
if not exist "screenshots" mkdir "screenshots"
if not exist "docs" mkdir "docs"

:: 3. Verify JDBC & SLF4J Drivers
if not exist "backend\lib\sqlite-jdbc.jar" (
    echo [INFO] Downloading SQLite JDBC driver...
    curl.exe -L -o "backend\lib\sqlite-jdbc.jar" "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.2.0/sqlite-jdbc-3.45.2.0.jar"
)
if not exist "backend\lib\slf4j-api.jar" (
    echo [INFO] Downloading SLF4J API driver...
    curl.exe -L -o "backend\lib\slf4j-api.jar" "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.12/slf4j-api-2.0.12.jar"
)
if not exist "backend\lib\slf4j-simple.jar" (
    echo [INFO] Downloading SLF4J Simple driver...
    curl.exe -L -o "backend\lib\slf4j-simple.jar" "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.12/slf4j-simple-2.0.12.jar"
)

:: 4. Direct Compilation Across All Subsystems
echo [INFO] Compiling SB-TMS Enterprise Java classes...
javac -encoding UTF-8 -cp "backend\lib\*;." -d "backend\bin" backend\src\com\sbtms\*.java backend\src\com\sbtms\model\*.java backend\src\com\sbtms\interfaces\*.java backend\src\com\sbtms\exception\*.java backend\src\com\sbtms\db\*.java backend\src\com\sbtms\service\*.java backend\src\com\sbtms\web\*.java backend\src\com\sbtms\cli\*.java

if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation encountered faults!
    exit /b %ERRORLEVEL%
)

echo [SUCCESS] All Java packages compiled successfully into backend\bin!
