@echo off
setlocal EnableExtensions EnableDelayedExpansion
title SB-TMS - Smart Banking System

:: This script can be run directly from the project/ directory
:: For best results, use START.bat from the repository root

set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"

echo.
echo  ============================================================================
echo   SMART BANKING TRANSACTION ^& PORTFOLIO MANAGEMENT SYSTEM (SB-TMS)
echo   Candidate: Anish Vyapari ^| PRN: DY25ENGU0AIM012 ^| Batch: A/A1
echo  ============================================================================
echo.

:: --- Find Java ---
set "JAVA_EXE=java"
set "JAVAC_EXE=javac"
set "JAVA_OK=0"

where java >nul 2>&1
if %ERRORLEVEL% equ 0 (
    where javac >nul 2>&1
    if !ERRORLEVEL! equ 0 (
        set "JAVA_OK=1"
    )
)

:: Check for portable JDK one level up
if "!JAVA_OK!"=="0" (
    if exist "%PROJECT_DIR%..\portable-jdk\bin\java.exe" (
        set "JAVA_EXE=%PROJECT_DIR%..\portable-jdk\bin\java.exe"
        set "JAVAC_EXE=%PROJECT_DIR%..\portable-jdk\bin\javac.exe"
        set "PATH=%PROJECT_DIR%..\portable-jdk\bin;!PATH!"
        set "JAVA_OK=1"
        echo  [INFO] Using portable JDK from repository.
    )
)

:: Search common locations
if "!JAVA_OK!"=="0" (
    for %%D in (
        "C:\Program Files\Java"
        "C:\Program Files\Microsoft"
        "C:\Program Files\Eclipse Adoptium"
        "C:\Program Files\AdoptOpenJDK"
        "C:\Program Files\Zulu"
        "C:\Program Files\Amazon Corretto"
    ) do (
        if "!JAVA_OK!"=="0" (
            if exist %%D (
                for /f "delims=" %%J in ('dir /b /s /a-d %%D\javac.exe 2^>nul') do (
                    if "!JAVA_OK!"=="0" (
                        set "JAVAC_EXE=%%J"
                        set "JAVA_EXE=%%~dpJjava.exe"
                        set "PATH=%%~dpJ;!PATH!"
                        set "JAVA_OK=1"
                    )
                )
            )
        )
    )
)

if "!JAVA_OK!"=="0" (
    echo  [FATAL] Java JDK not found. Please run START.bat from the repo root instead.
    echo          It will download Java automatically.
    pause
    exit /b 1
)

echo  [OK] Java found.

:: --- Create directories ---
if not exist "backend\bin" mkdir "backend\bin"
if not exist "backend\lib" mkdir "backend\lib"
if not exist "database" mkdir "database"

:: --- Download drivers if missing ---
if not exist "backend\lib\sqlite-jdbc.jar" (
    echo  [DOWNLOAD] SQLite JDBC...
    curl.exe -fSL --retry 3 -o "backend\lib\sqlite-jdbc.jar" "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.2.0/sqlite-jdbc-3.45.2.0.jar" 2>nul
)
if not exist "backend\lib\slf4j-api.jar" (
    echo  [DOWNLOAD] SLF4J API...
    curl.exe -fSL --retry 3 -o "backend\lib\slf4j-api.jar" "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.12/slf4j-api-2.0.12.jar" 2>nul
)
if not exist "backend\lib\slf4j-simple.jar" (
    echo  [DOWNLOAD] SLF4J Simple...
    curl.exe -fSL --retry 3 -o "backend\lib\slf4j-simple.jar" "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.12/slf4j-simple-2.0.12.jar" 2>nul
)

:: --- Compile if needed ---
if not exist "backend\bin\com\sbtms\Main.class" (
    echo  [BUILD] Compiling Java sources...
    for /r "backend\src" %%f in (*.java) do set "SRCS=!SRCS! "%%f""
    "!JAVAC_EXE!" -encoding UTF-8 -cp "backend\lib\*;." -d "backend\bin" !SRCS!
    if !ERRORLEVEL! neq 0 (
        echo  [ERROR] Compilation failed.
        pause
        exit /b 1
    )
    echo  [OK] Compiled.
) else (
    echo  [OK] Classes already compiled.
)

:: --- Kill old server on port 8080 ---
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":8080 " ^| findstr "LISTENING"') do (
    if "%%a" neq "0" taskkill /F /PID %%a >nul 2>&1
)

echo.
echo  Server starting at: http://localhost:8080/index.html
echo  Close this window to stop the server.
echo.

start "" "http://localhost:8080/index.html"
"!JAVA_EXE!" -cp "backend\bin;backend\lib\*" com.sbtms.Main --port 8080

if %ERRORLEVEL% neq 0 (
    echo.
    echo  [ERROR] Server crashed with code %ERRORLEVEL%.
)
pause
