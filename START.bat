@echo off
setlocal EnableExtensions EnableDelayedExpansion
title SB-TMS Smart Banking System - Auto Setup
color 0B

echo.
echo  ============================================================================
echo  ^|                                                                          ^|
echo  ^|   SMART BANKING TRANSACTION ^& ACCOUNT PORTFOLIO MANAGEMENT SYSTEM       ^|
echo  ^|   SB-TMS Capstone Project - One-Click Auto Setup ^& Launch               ^|
echo  ^|                                                                          ^|
echo  ^|   Candidate : Anish Vyapari                                              ^|
echo  ^|   Roll No   : 25CA1012  ^|  PRN: DY25ENGU0AIM012  ^|  Batch: A/A1         ^|
echo  ^|   Institute : Ramrao Adik Institute of Technology, Nerul                 ^|
echo  ^|   Department: Computer Engineering                                       ^|
echo  ^|                                                                          ^|
echo  ============================================================================
echo.

:: ============================================================================
:: STEP 0: Find project directory (handles any extraction method)
:: ============================================================================
set "ROOT=%~dp0"
if "!ROOT:~-1!"=="\" set "ROOT=!ROOT:~0,-1!"
set "PROJECT="

:: Strategy 1: project folder right next to START.bat
if exist "!ROOT!\project\backend\src" set "PROJECT=!ROOT!\project"

:: Strategy 2: backend\src is right here (START.bat is inside project folder)
if not defined PROJECT (
    if exist "!ROOT!\backend\src" set "PROJECT=!ROOT!"
)

:: Strategy 3: Check one level deeper (zip extracted with wrapper folder)
if not defined PROJECT (
    for /d %%D in ("!ROOT!\*") do (
        if not defined PROJECT (
            if exist "%%D\project\backend\src" set "PROJECT=%%D\project"
            if exist "%%D\backend\src" set "PROJECT=%%D"
        )
    )
)

:: Strategy 4: Check parent directory
if not defined PROJECT (
    for %%P in ("!ROOT!\..") do (
        if exist "%%~fP\project\backend\src" set "PROJECT=%%~fP\project"
    )
)

:: Strategy 5: Search nearby for backend\src (max 3 levels deep)
if not defined PROJECT (
    for /r "!ROOT!" %%F in (Main.java) do (
        if not defined PROJECT (
            set "FOUND_PATH=%%~dpF"
            for %%X in ("!FOUND_PATH!..\..\..\..\..") do (
                if exist "%%~fX\backend\src" set "PROJECT=%%~fX"
                if exist "%%~fX\project\backend\src" set "PROJECT=%%~fX\project"
            )
        )
    )
)

if not defined PROJECT (
    echo.
    echo  [FATAL] Could not locate the project files.
    echo  Make sure START.bat is in the same folder as the "project" directory.
    echo.
    echo  Expected structure:
    echo    SB-TMS-Ready-To-Run\
    echo      START.bat          ^<-- you are here
    echo      project\
    echo        backend\
    echo        frontend\
    echo        database\
    echo.
    echo  Current location: !ROOT!
    echo.
    pause
    exit /b 1
)

cd /d "!PROJECT!"
echo       [OK] Project found at: !PROJECT!

:: ============================================================================
:: STEP 1: FIND JAVA
:: ============================================================================
echo [1/6] Searching for Java JDK on this system...

set "JAVA_FOUND=0"
set "JAVA_EXE="
set "JAVAC_EXE="

:: --- Strategy A: Check PATH ---
where java >nul 2>&1
if !ERRORLEVEL! equ 0 (
    where javac >nul 2>&1
    if !ERRORLEVEL! equ 0 (
        for /f "delims=" %%i in ('where java') do (
            if "!JAVA_EXE!"=="" set "JAVA_EXE=%%i"
        )
        for /f "delims=" %%i in ('where javac') do (
            if "!JAVAC_EXE!"=="" set "JAVAC_EXE=%%i"
        )
        set "JAVA_FOUND=1"
        echo       [OK] Found java on PATH: !JAVA_EXE!
    )
)

:: --- Strategy B: Check JAVA_HOME ---
if "!JAVA_FOUND!"=="0" (
    if defined JAVA_HOME (
        if exist "!JAVA_HOME!\bin\java.exe" (
            if exist "!JAVA_HOME!\bin\javac.exe" (
                set "JAVA_EXE=!JAVA_HOME!\bin\java.exe"
                set "JAVAC_EXE=!JAVA_HOME!\bin\javac.exe"
                set "PATH=!JAVA_HOME!\bin;!PATH!"
                set "JAVA_FOUND=1"
                echo       [OK] Found via JAVA_HOME: !JAVA_HOME!
            )
        )
    )
)

:: --- Strategy C: Check common JDK install locations one by one ---
if "!JAVA_FOUND!"=="0" (
    echo       [INFO] Not on PATH or JAVA_HOME. Scanning install locations...
    call :search_jdk "C:\Program Files\Java"
    call :search_jdk "C:\Program Files\Microsoft"
    call :search_jdk "C:\Program Files\Eclipse Adoptium"
    call :search_jdk "C:\Program Files\AdoptOpenJDK"
    call :search_jdk "C:\Program Files\Zulu"
    call :search_jdk "C:\Program Files\Amazon Corretto"
    call :search_jdk "C:\Program Files\BellSoft"
    call :search_jdk "C:\Program Files\Semeru"
    call :search_jdk "C:\Program Files\Red Hat"
    call :search_jdk "C:\Program Files\SapMachine"
    call :search_jdk "C:\Program Files (x86)\Java"
)

:: --- Strategy D: Check Windows Registry ---
if "!JAVA_FOUND!"=="0" (
    echo       [INFO] Checking Windows Registry...
    set "REG_VER="
    for /f "tokens=2*" %%a in ('reg query "HKLM\SOFTWARE\JavaSoft\JDK" /v CurrentVersion 2^>nul') do set "REG_VER=%%b"
    if defined REG_VER (
        set "REG_HOME="
        for /f "tokens=2*" %%a in ('reg query "HKLM\SOFTWARE\JavaSoft\JDK\!REG_VER!" /v JavaHome 2^>nul') do set "REG_HOME=%%b"
        if defined REG_HOME (
            if exist "!REG_HOME!\bin\java.exe" (
                set "JAVA_EXE=!REG_HOME!\bin\java.exe"
                set "JAVAC_EXE=!REG_HOME!\bin\javac.exe"
                set "PATH=!REG_HOME!\bin;!PATH!"
                set "JAVA_FOUND=1"
                echo       [OK] Found via Registry: !REG_HOME!
            )
        )
    )
)

:: --- Strategy E: Use portable JDK (already downloaded) ---
if "!JAVA_FOUND!"=="0" (
    if exist "!ROOT!\portable-jdk\bin\java.exe" (
        set "JAVA_EXE=!ROOT!\portable-jdk\bin\java.exe"
        set "JAVAC_EXE=!ROOT!\portable-jdk\bin\javac.exe"
        set "PATH=!ROOT!\portable-jdk\bin;!PATH!"
        set "JAVA_FOUND=1"
        echo       [OK] Using portable JDK from previous download.
    )
)

:: --- Strategy F: Download portable JDK automatically ---
if "!JAVA_FOUND!"=="0" (
    echo.
    echo  =====================================================================
    echo   Java JDK was NOT found on this computer.
    echo   Downloading a portable JDK automatically... Please wait.
    echo  =====================================================================
    echo.

    set "JDK_DIR=!ROOT!\portable-jdk"
    set "JDK_ZIP=!ROOT!\jdk-download.zip"

    echo       [DOWNLOAD] Fetching OpenJDK 21 from Adoptium...
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; " ^
        "try { " ^
        "  $url = 'https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse'; " ^
        "  Write-Host '       Downloading... (this may take a few minutes)'; " ^
        "  Invoke-WebRequest -Uri $url -OutFile '!JDK_ZIP!' -UseBasicParsing; " ^
        "  Write-Host '       Extracting...'; " ^
        "  Expand-Archive -Path '!JDK_ZIP!' -DestinationPath '!ROOT!\jdk-temp' -Force; " ^
        "  $extracted = (Get-ChildItem '!ROOT!\jdk-temp' -Directory | Select-Object -First 1).FullName; " ^
        "  if ($extracted) { " ^
        "    if (Test-Path '!JDK_DIR!') { Remove-Item '!JDK_DIR!' -Recurse -Force }; " ^
        "    Move-Item $extracted '!JDK_DIR!' -Force; " ^
        "    Remove-Item '!ROOT!\jdk-temp' -Recurse -Force -ErrorAction SilentlyContinue; " ^
        "    Remove-Item '!JDK_ZIP!' -Force -ErrorAction SilentlyContinue; " ^
        "    Write-Host '       JDK installed to: !JDK_DIR!'; " ^
        "  } " ^
        "} catch { " ^
        "  Write-Host '[ERROR] Download failed:' $_.Exception.Message; " ^
        "  exit 1; " ^
        "}"

    if exist "!JDK_DIR!\bin\java.exe" (
        set "JAVA_EXE=!JDK_DIR!\bin\java.exe"
        set "JAVAC_EXE=!JDK_DIR!\bin\javac.exe"
        set "PATH=!JDK_DIR!\bin;!PATH!"
        set "JAVA_FOUND=1"
        echo       [OK] Portable JDK installed successfully!
    ) else (
        echo.
        echo  =====================================================================
        echo   [FATAL] Could not install Java automatically.
        echo   Please install Java JDK 17 or 21 manually from:
        echo     https://adoptium.net/
        echo   Then run this script again.
        echo  =====================================================================
        pause
        exit /b 1
    )
)

:: Display Java version
echo.
"!JAVA_EXE!" -version 2>&1 | findstr /i "version"
echo.

:: ============================================================================
:: STEP 2: Create required directories
:: ============================================================================
echo [2/6] Initializing workspace directories...
if not exist "backend\bin" mkdir "backend\bin"
if not exist "backend\lib" mkdir "backend\lib"
if not exist "database" mkdir "database"
if not exist "screenshots" mkdir "screenshots"
if not exist "docs" mkdir "docs"
echo       [OK] All directories ready.

:: ============================================================================
:: STEP 3: Download database drivers if missing
:: ============================================================================
echo [3/6] Checking database drivers...

call :download_jar "backend\lib\sqlite-jdbc.jar" "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.2.0/sqlite-jdbc-3.45.2.0.jar" "SQLite JDBC"
call :download_jar "backend\lib\slf4j-api.jar" "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.12/slf4j-api-2.0.12.jar" "SLF4J API"
call :download_jar "backend\lib\slf4j-simple.jar" "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.12/slf4j-simple-2.0.12.jar" "SLF4J Simple"

:: Verify all jars exist
if not exist "backend\lib\sqlite-jdbc.jar" goto :driver_fail
if not exist "backend\lib\slf4j-api.jar" goto :driver_fail
if not exist "backend\lib\slf4j-simple.jar" goto :driver_fail
echo       [OK] All drivers present.
goto :drivers_ok

:driver_fail
echo       [FATAL] Could not download required drivers. Check internet connection.
pause
exit /b 1

:drivers_ok

:: ============================================================================
:: STEP 4: ALWAYS recompile (prevents cross-machine bytecode conflicts)
:: ============================================================================
echo [4/6] Compiling Java source code...

:: Clean old bytecode
if exist "backend\bin\com" (
    echo       [CLEAN] Removing old compiled classes...
    rmdir /s /q "backend\bin\com" >nul 2>&1
)

:: Collect all .java source files
set "JAVA_SOURCES="
for /r "backend\src" %%f in (*.java) do (
    set "JAVA_SOURCES=!JAVA_SOURCES! "%%f""
)

if "!JAVA_SOURCES!"=="" (
    echo       [FATAL] No Java source files found in backend\src!
    pause
    exit /b 1
)

"!JAVAC_EXE!" -encoding UTF-8 -cp "backend\lib\*;." -d "backend\bin" !JAVA_SOURCES!
if !ERRORLEVEL! neq 0 (
    echo.
    echo       [ERROR] Compilation failed! See errors above.
    pause
    exit /b 1
)
echo       [OK] All Java classes compiled successfully.

:: ============================================================================
:: STEP 5: Clear port 8080 if occupied
:: ============================================================================
echo [5/6] Ensuring port 8080 is available...

set "PORT_CLEARED=0"
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":8080 " ^| findstr "LISTENING"') do (
    if "%%a" neq "0" (
        echo       [INFO] Killing process PID %%a on port 8080...
        taskkill /F /PID %%a >nul 2>&1
        set "PORT_CLEARED=1"
    )
)
if "!PORT_CLEARED!"=="1" (
    echo       [OK] Port 8080 cleared. Waiting for release...
    ping -n 3 127.0.0.1 >nul 2>&1
) else (
    echo       [OK] Port 8080 is free.
)

:: ============================================================================
:: STEP 6: Launch server, wait for health, then open browser
:: ============================================================================
echo [6/6] Starting SB-TMS Server...
echo.
echo  ============================================================================
echo  ^|                                                                          ^|
echo  ^|   SERVER STARTING AT: http://localhost:8080                              ^|
echo  ^|                                                                          ^|
echo  ^|   The browser will open automatically when the server is ready.          ^|
echo  ^|   To stop: close this window or press Ctrl+C.                           ^|
echo  ^|                                                                          ^|
echo  ============================================================================
echo.

:: Start server in background
start /b "" "!JAVA_EXE!" -cp "backend\bin;backend\lib\*" com.sbtms.Main --port 8080

:: Wait for server readiness by polling health endpoint
echo       Waiting for server...
set "RETRIES=0"
:wait_loop
if !RETRIES! geq 30 (
    echo       [WARNING] Server took too long. Opening browser anyway...
    goto :open_browser
)
ping -n 2 127.0.0.1 >nul 2>&1
set /a RETRIES+=1

:: Check if server is responding
curl.exe -s -o nul -w "%%{http_code}" "http://localhost:8080/api/health" 2>nul | findstr "200" >nul 2>&1
if !ERRORLEVEL! equ 0 (
    echo       [OK] Server is running and healthy!
    goto :open_browser
)
if !RETRIES! lss 10 (
    goto :wait_loop
) else (
    echo       Still waiting... ^(!RETRIES!s^)
    goto :wait_loop
)

:open_browser
echo.
echo       Opening browser...
start "" "http://localhost:8080/index.html"

echo.
echo  ============================================================================
echo  ^|   Server is LIVE. Do NOT close this window while using the app.         ^|
echo  ============================================================================
echo.
echo  Press Ctrl+C or close this window to stop the server.
echo.

:: Keep alive - wait for java to exit
:keep_alive
ping -n 6 127.0.0.1 >nul 2>&1
tasklist /fi "imagename eq java.exe" 2>nul | findstr /i "java.exe" >nul 2>&1
if !ERRORLEVEL! equ 0 goto :keep_alive

echo.
echo  Server has stopped.
pause
exit /b 0

:: ============================================================================
:: SUBROUTINES
:: ============================================================================

:search_jdk
:: Search a directory for java.exe + javac.exe
if "!JAVA_FOUND!"=="1" exit /b 0
set "SEARCH_DIR=%~1"
if not exist "!SEARCH_DIR!" exit /b 0
for /f "delims=" %%J in ('dir /b /s /a-d "!SEARCH_DIR!\java.exe" 2^>nul') do (
    if "!JAVA_FOUND!"=="0" (
        set "CANDIDATE_DIR=%%~dpJ"
        if exist "!CANDIDATE_DIR!javac.exe" (
            set "JAVA_EXE=%%J"
            set "JAVAC_EXE=!CANDIDATE_DIR!javac.exe"
            set "PATH=!CANDIDATE_DIR!;!PATH!"
            set "JAVA_FOUND=1"
            echo       [OK] Found JDK at: !CANDIDATE_DIR!
        )
    )
)
exit /b 0

:download_jar
:: Download a jar if it doesn't exist. Args: %1=path %2=url %3=name
set "JAR_PATH=%~1"
set "JAR_URL=%~2"
set "JAR_NAME=%~3"
if exist "!JAR_PATH!" (
    echo       [OK] !JAR_NAME! present.
    exit /b 0
)
echo       [DOWNLOAD] !JAR_NAME!...
curl.exe -fSL --retry 3 -o "!JAR_PATH!" "!JAR_URL!" 2>nul
if exist "!JAR_PATH!" exit /b 0
:: Fallback to PowerShell
echo       [FALLBACK] Using PowerShell...
powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '!JAR_URL!' -OutFile '!JAR_PATH!' -UseBasicParsing"
exit /b 0
