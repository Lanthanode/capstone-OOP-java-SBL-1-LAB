@echo off
setlocal EnableExtensions EnableDelayedExpansion
title Smart Banking Transaction and Portfolio Management System (SB-TMS) - Capstone Launcher

echo ================================================================================
echo   RAMRAO ADIK INSTITUTE OF TECHNOLOGY, NERUL - DEPT OF COMPUTER ENGG.
echo   SMART BANKING TRANSACTION AND ACCOUNT PORTFOLIO MANAGEMENT SYSTEM (SB-TMS)
echo   Capstone Project: Object-Oriented Java and Relational DBMS
echo   Candidate: Anish Vyapari - Roll No: 25CA1012 - PRN: DY25ENGU0AIM012 - Batch: A/A1
echo ================================================================================
echo.

set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"

:: 1. Check for Java Runtime / JDK
echo [1/5] Checking Java runtime environment...
set "JAVA_CMD=java"
set "JAVAC_CMD=javac"

where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [WARNING] java command not found in current PATH. Searching common locations...
    if exist "C:\Program Files\Microsoft\jdk-21.0.12.8-hotspot\bin\java.exe" (
        set "PATH=C:\Program Files\Microsoft\jdk-21.0.12.8-hotspot\bin;!PATH!"
        set "JAVA_CMD=C:\Program Files\Microsoft\jdk-21.0.12.8-hotspot\bin\java.exe"
        set "JAVAC_CMD=C:\Program Files\Microsoft\jdk-21.0.12.8-hotspot\bin\javac.exe"
    ) else if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot\bin\java.exe" (
        set "PATH=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot\bin;!PATH!"
        set "JAVA_CMD=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot\bin\java.exe"
        set "JAVAC_CMD=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot\bin\javac.exe"
    ) else if exist "C:\Program Files\Java\jdk-21\bin\java.exe" (
        set "PATH=C:\Program Files\Java\jdk-21\bin;!PATH!"
        set "JAVA_CMD=C:\Program Files\Java\jdk-21\bin\java.exe"
        set "JAVAC_CMD=C:\Program Files\Java\jdk-21\bin\javac.exe"
    ) else if exist "C:\Program Files\Java\jdk-17\bin\java.exe" (
        set "PATH=C:\Program Files\Java\jdk-17\bin;!PATH!"
        set "JAVA_CMD=C:\Program Files\Java\jdk-17\bin\java.exe"
        set "JAVAC_CMD=C:\Program Files\Java\jdk-17\bin\javac.exe"
    ) else (
        echo [CRITICAL ERROR] Java JDK was not detected on this computer!
        echo Please install OpenJDK 17 or 21 from https://adoptium.net/
        pause
        exit /b 1
    )
)

for /f "tokens=3" %%g in ('%JAVA_CMD% -version 2^>^&1 ^| findstr /i "version"') do (
    set "JAVA_VER=%%~g"
)
echo [INFO] Active Java Version: %JAVA_VER%

:: 2. Check and Create Required Directories
echo [2/5] Initializing workspace directories...
if not exist "backend\bin" mkdir "backend\bin"
if not exist "backend\lib" mkdir "backend\lib"
if not exist "database" mkdir "database"
if not exist "screenshots" mkdir "screenshots"
if not exist "docs" mkdir "docs"

:: 3. Verify SQLite JDBC and SLF4J Driver Jars
echo [3/5] Verifying Relational DBMS drivers...
if not exist "backend\lib\sqlite-jdbc.jar" (
    echo [INFO] Downloading SQLite JDBC Driver...
    curl.exe -L -o "backend\lib\sqlite-jdbc.jar" "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.2.0/sqlite-jdbc-3.45.2.0.jar"
)
if not exist "backend\lib\slf4j-api.jar" (
    echo [INFO] Downloading SLF4J API Driver...
    curl.exe -L -o "backend\lib\slf4j-api.jar" "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.12/slf4j-api-2.0.12.jar"
)
if not exist "backend\lib\slf4j-simple.jar" (
    echo [INFO] Downloading SLF4J Simple Driver...
    curl.exe -L -o "backend\lib\slf4j-simple.jar" "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.12/slf4j-simple-2.0.12.jar"
)

:: 4. Compile Backend if needed
echo [4/5] Checking compilation status...
if not exist "backend\bin\com\sbtms\Main.class" (
    echo [INFO] Compiling SB-TMS Enterprise Java Architecture...
    javac -encoding UTF-8 -cp "backend\lib\*;." -d "backend\bin" backend\src\com\sbtms\*.java backend\src\com\sbtms\model\*.java backend\src\com\sbtms\interfaces\*.java backend\src\com\sbtms\exception\*.java backend\src\com\sbtms\db\*.java backend\src\com\sbtms\service\*.java backend\src\com\sbtms\web\*.java backend\src\com\sbtms\cli\*.java
    if %ERRORLEVEL% neq 0 (
        echo [ERROR] Compilation failed.
        pause
        exit /b 1
    )
    echo [SUCCESS] Compilation completed successfully!
) else (
    echo [INFO] Compiled bytecode up to date in backend\bin.
)

:: 5. Clear any lingering process on port 8080
echo [5/5] Ensuring Port 8080 is available...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080" ^| findstr "LISTENING"') do (
    echo [INFO] Terminating previous process PID %%a on port 8080...
    taskkill /F /PID %%a >nul 2>&1
)

echo.
echo ================================================================================
echo   SERVER STARTING AT: http://localhost:8080/index.html
echo   To Stop the Server: Close this terminal window or double-click stop.bat
echo ================================================================================
echo.

start "" "http://localhost:8080/index.html"

java -cp "backend\bin;backend\lib\*" com.sbtms.Main --port 8080

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Server terminated with error code %ERRORLEVEL%.
)
echo.
pause
