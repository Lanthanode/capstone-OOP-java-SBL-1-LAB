@echo off
setlocal EnableExtensions EnableDelayedExpansion
title SB-TMS Smart Banking System - Launcher
color 0B

echo.
echo ============================================================================
echo   SMART BANKING TRANSACTION ^& ACCOUNT PORTFOLIO MANAGEMENT SYSTEM
echo   SB-TMS Capstone Project ^| One-Click Universal Launcher
echo.
echo   Candidate : Anish Vyapari  ^| Roll No: 25CA1012 ^| PRN: DY25ENGU0AIM012
echo   Batch     : A/A1           ^| Dept: Computer Engineering, RAIT Nerul
echo ============================================================================
echo.

:: ============================================================================
:: STEP 0: Find Project Root Directory
:: ============================================================================
set "ROOT=%~dp0"
if "!ROOT:~-1!"=="\" set "ROOT=!ROOT:~0,-1!"
set "P="

:: 1. Current directory is project
if exist "!ROOT!\backend\src" set "P=!ROOT!"
:: 2. Project subfolder
if not defined P if exist "!ROOT!\project\backend\src" set "P=!ROOT!\project"
:: 3. Parent directory
if not defined P (
    for %%X in ("!ROOT!\..") do (
        if exist "%%~fX\backend\src" set "P=%%~fX"
        if exist "%%~fX\project\backend\src" set "P=%%~fX\project"
    )
)
:: 4. Desktop location
if not defined P (
    if exist "%USERPROFILE%\Desktop\SB-TMS\project\backend\src" set "P=%USERPROFILE%\Desktop\SB-TMS\project"
    if exist "%USERPROFILE%\Desktop\SB-TMS\backend\src" set "P=%USERPROFILE%\Desktop\SB-TMS"
)
:: 5. Subdirectories
if not defined P (
    for /d %%D in ("!ROOT!\*") do (
        if not defined P if exist "%%D\backend\src" set "P=%%D"
        if not defined P if exist "%%D\project\backend\src" set "P=%%D\project"
    )
)

:: 6. If files not found on clean laptop, auto-download from GitHub
if not defined P (
    echo [Setup] Project files not found locally.
    echo [Setup] Auto-downloading SB-TMS from GitHub repository...
    set "DEST_DIR=%USERPROFILE%\Desktop\SB-TMS"
    if exist "!DEST_DIR!" rmdir /s /q "!DEST_DIR!" >nul 2>&1
    mkdir "!DEST_DIR!" >nul 2>&1
    
    where git >nul 2>&1
    if !ERRORLEVEL! equ 0 (
        git clone --depth 1 "https://github.com/Lanthanode/capstone-OOP-java-SBL-1-LAB.git" "!DEST_DIR!" 2>&1
    ) else (
        where curl.exe >nul 2>&1
        if !ERRORLEVEL! equ 0 (
            curl.exe -fSL --progress-bar -o "%TEMP%\sbtms.zip" "https://github.com/Lanthanode/capstone-OOP-java-SBL-1-LAB/archive/refs/heads/main.zip"
        ) else (
            powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest 'https://github.com/Lanthanode/capstone-OOP-java-SBL-1-LAB/archive/refs/heads/main.zip' -OutFile '$env:TEMP\sbtms.zip' -UseBasicParsing"
        )
        powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Expand-Archive '$env:TEMP\sbtms.zip' '$env:TEMP\sbtms_unzip' -Force; $src = (Get-ChildItem '$env:TEMP\sbtms_unzip' -Dir | Select-Object -First 1).FullName; Copy-Item -Path \"$src\*\" -Destination '!DEST_DIR!' -Recurse -Force; Remove-Item '$env:TEMP\sbtms.zip', '$env:TEMP\sbtms_unzip' -Recurse -Force -EA SilentlyContinue"
    )
    
    if exist "!DEST_DIR!\project\backend\src" set "P=!DEST_DIR!\project"
    if not defined P if exist "!DEST_DIR!\backend\src" set "P=!DEST_DIR!"
    if defined P (
        copy /y "%~f0" "!DEST_DIR!\START.bat" >nul 2>&1
        echo [OK] SB-TMS installed to Desktop\SB-TMS!
    ) else (
        echo [ERROR] Could not install SB-TMS. Please check internet connection.
        pause & exit /b 1
    )
)

cd /d "!P!"
echo [OK] Project directory: !P!

:: ============================================================================
:: STEP 1: Detect or Auto-Install Java
:: ============================================================================
set "JAVA_EXE="
set "JAVAC_EXE="

:: 1. Check PATH
where java >nul 2>&1
if !ERRORLEVEL! equ 0 (
    set "JAVA_EXE=java"
    where javac >nul 2>&1 && set "JAVAC_EXE=javac"
)

:: 2. Check JAVA_HOME
if not defined JAVA_EXE if defined JAVA_HOME (
    if exist "!JAVA_HOME!\bin\java.exe" (
        set "JAVA_EXE=!JAVA_HOME!\bin\java.exe"
        if exist "!JAVA_HOME!\bin\javac.exe" set "JAVAC_EXE=!JAVA_HOME!\bin\javac.exe"
        set "PATH=!JAVA_HOME!\bin;!PATH!"
    )
)

:: 3. Check local portable-jdk
if not defined JAVA_EXE (
    for %%L in ("!P!\portable-jdk" "!P!\..\portable-jdk" "%USERPROFILE%\Desktop\SB-TMS\portable-jdk" "%LOCALAPPDATA%\SB-TMS\portable-jdk") do (
        if not defined JAVA_EXE if exist "%%~L\bin\java.exe" (
            set "JAVA_EXE=%%~L\bin\java.exe"
            if exist "%%~L\bin\javac.exe" set "JAVAC_EXE=%%~L\bin\javac.exe"
            set "PATH=%%~L\bin;!PATH!"
        )
    )
)

:: 4. Check Program Files
if not defined JAVA_EXE (
    call :check_java_dir "C:\Program Files\Java"
    call :check_java_dir "C:\Program Files\Eclipse Adoptium"
    call :check_java_dir "C:\Program Files\AdoptOpenJDK"
    call :check_java_dir "C:\Program Files\Microsoft"
    call :check_java_dir "C:\Program Files\Amazon Corretto"
    call :check_java_dir "C:\Program Files\Zulu"
    call :check_java_dir "C:\Program Files\BellSoft"
    if exist "C:\Program Files (x86)\Java" call :check_java_dir "C:\Program Files (x86)\Java"
)

:: 5. Check Registry
if not defined JAVA_EXE (
    for %%K in (
        "HKLM\SOFTWARE\JavaSoft\JDK"
        "HKLM\SOFTWARE\JavaSoft\Java Development Kit"
        "HKLM\SOFTWARE\JavaSoft\Java Runtime Environment"
    ) do (
        if not defined JAVA_EXE (
            set "RVER=" & for /f "tokens=2*" %%a in ('reg query %%K /v CurrentVersion 2^>nul') do set "RVER=%%b"
            if defined RVER (
                for /f "tokens=2*" %%a in ('reg query "%%~K\!RVER!" /v JavaHome 2^>nul') do (
                    if exist "%%b\bin\java.exe" (
                        set "JAVA_EXE=%%b\bin\java.exe"
                        if exist "%%b\bin\javac.exe" set "JAVAC_EXE=%%b\bin\javac.exe"
                        set "PATH=%%b\bin;!PATH!"
                    )
                )
            )
        )
    )
)

:: 6. If NO Java on PC, auto-download portable OpenJDK 21
if not defined JAVA_EXE (
    echo.
    echo ----------------------------------------------------------------------------
    echo  [Notice] Java is not installed on this PC.
    echo  Auto-downloading portable OpenJDK 21 [one-time setup]...
    echo ----------------------------------------------------------------------------
    set "JDK_DEST=!P!\portable-jdk"
    if not exist "!JDK_DEST!" mkdir "!JDK_DEST!" >nul 2>&1
    
    set "JDK_ZIP=%TEMP%\openjdk21.zip"
    set "JDK_TMP=%TEMP%\openjdk21_tmp"
    if exist "!JDK_ZIP!" del /f /q "!JDK_ZIP!" >nul 2>&1
    if exist "!JDK_TMP!" rmdir /s /q "!JDK_TMP!" >nul 2>&1
    
    echo  Downloading OpenJDK 21 binary...
    where curl.exe >nul 2>&1
    if !ERRORLEVEL! equ 0 (
        curl.exe -fSL --progress-bar -o "!JDK_ZIP!" "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse"
    ) else (
        powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest 'https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse' -OutFile '!JDK_ZIP!' -UseBasicParsing"
    )
    
    echo  Extracting portable JDK...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Expand-Archive '!JDK_ZIP!' '!JDK_TMP!' -Force; $inner = (Get-ChildItem '!JDK_TMP!' -Dir | Select-Object -First 1).FullName; Copy-Item -Path \"$inner\*\" -Destination '!JDK_DEST!' -Recurse -Force; Remove-Item '!JDK_ZIP!', '!JDK_TMP!' -Recurse -Force -EA SilentlyContinue"
    
    if exist "!JDK_DEST!\bin\java.exe" (
        set "JAVA_EXE=!JDK_DEST!\bin\java.exe"
        if exist "!JDK_DEST!\bin\javac.exe" set "JAVAC_EXE=!JDK_DEST!\bin\javac.exe"
        set "PATH=!JDK_DEST!\bin;!PATH!"
        echo  [OK] Portable OpenJDK 21 ready!
    ) else (
        echo  [FAIL] Could not setup Java automatically.
        echo  Please install Java from https://adoptium.net/
        pause & exit /b 1
    )
)

echo [OK] Java runtime: !JAVA_EXE!

:: ============================================================================
:: STEP 2: Verify Required Directories & Libraries
:: ============================================================================
if not exist "backend\bin" mkdir "backend\bin"
if not exist "backend\lib" mkdir "backend\lib"
if not exist "database" mkdir "database"

:: SQLite JDBC Driver
if not exist "backend\lib\sqlite-jdbc.jar" (
    echo [Setup] Downloading SQLite JDBC driver...
    where curl.exe >nul 2>&1 && curl.exe -fSL --retry 2 -o "backend\lib\sqlite-jdbc.jar" "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.2.0/sqlite-jdbc-3.45.2.0.jar" 2>nul
    if not exist "backend\lib\sqlite-jdbc.jar" powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest 'https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.2.0/sqlite-jdbc-3.45.2.0.jar' -OutFile 'backend\lib\sqlite-jdbc.jar' -UseBasicParsing"
)

:: SLF4J API
if not exist "backend\lib\slf4j-api.jar" (
    echo [Setup] Downloading SLF4J API...
    where curl.exe >nul 2>&1 && curl.exe -fSL --retry 2 -o "backend\lib\slf4j-api.jar" "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.12/slf4j-api-2.0.12.jar" 2>nul
    if not exist "backend\lib\slf4j-api.jar" powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest 'https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.12/slf4j-api-2.0.12.jar' -OutFile 'backend\lib\slf4j-api.jar' -UseBasicParsing"
)

:: SLF4J Simple
if not exist "backend\lib\slf4j-simple.jar" (
    echo [Setup] Downloading SLF4J Simple...
    where curl.exe >nul 2>&1 && curl.exe -fSL --retry 2 -o "backend\lib\slf4j-simple.jar" "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.12/slf4j-simple-2.0.12.jar" 2>nul
    if not exist "backend\lib\slf4j-simple.jar" powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest 'https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.12/slf4j-simple-2.0.12.jar' -OutFile 'backend\lib\slf4j-simple.jar' -UseBasicParsing"
)

:: ============================================================================
:: STEP 3: Verify / Compile Java Bytecode
:: ============================================================================
set "NEED_COMPILE=0"
if not exist "backend\bin\com\sbtms\Main.class" set "NEED_COMPILE=1"

if "!NEED_COMPILE!"=="1" (
    if not defined JAVAC_EXE (
        echo [ERROR] Java Compiler javac is required for compilation but not found.
        echo Please ensure JDK is installed.
        pause & exit /b 1
    )
    echo [Setup] Compiling backend sources [Java 11+ compatible bytecode]...
    set "SRC_FILES="
    for /r "backend\src" %%f in (*.java) do set "SRC_FILES=!SRC_FILES! "%%f""
    "!JAVAC_EXE!" -encoding UTF-8 --release 11 -cp "backend\lib\*;." -d "backend\bin" !SRC_FILES!
    if !ERRORLEVEL! neq 0 (
        echo [FAIL] Compilation error.
        pause & exit /b 1
    )
    echo [OK] Compiled successfully.
) else (
    echo [OK] Compiled bytecode verified.
)

:: ============================================================================
:: STEP 4: Free Port 8080 If Occupied
:: ============================================================================
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":8080 " ^| findstr "LISTENING"') do (
    if "%%a" neq "0" (
        echo [Notice] Terminating process on port 8080 PID %%a
        taskkill /F /PID %%a >nul 2>&1
    )
)

:: ============================================================================
:: STEP 5: Launch Server & Auto-Open Web Browser
:: ============================================================================
echo.
echo ============================================================================
echo   Starting SB-TMS Banking Server on http://localhost:8080
echo ============================================================================
echo.

:: Launch background health check + browser opener
start /min "" powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; for($i=1; $i -le 30; $i++){ Start-Sleep -Milliseconds 600; try { $res=Invoke-WebRequest -Uri 'http://localhost:8080/api/health' -UseBasicParsing -TimeoutSec 1; if($res.StatusCode -eq 200){ Start-Process 'http://localhost:8080/index.html'; break } } catch{} }"

:: Start server in background
start /b "" "!JAVA_EXE!" -cp "backend\bin;backend\lib\*" com.sbtms.Main --port 8080

:: Wait until healthy or timeout
set "ATTEMPTS=0"
:wait_health
if !ATTEMPTS! geq 25 goto :health_done
set /a ATTEMPTS+=1
ping -n 2 127.0.0.1 >nul 2>&1
curl.exe -s -o nul -w "%%{http_code}" "http://localhost:8080/api/health" 2>nul | findstr "200" >nul 2>&1
if !ERRORLEVEL! equ 0 goto :health_done
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; try{$r=Invoke-WebRequest 'http://localhost:8080/api/health' -UseBasicParsing -TimeoutSec 1; if($r.StatusCode -eq 200){exit 0}else{exit 1}}catch{exit 1}" >nul 2>&1
if !ERRORLEVEL! equ 0 goto :health_done
goto :wait_health

:health_done
echo.
echo ============================================================================
echo   [SUCCESS] SB-TMS Banking System is LIVE!
echo.
echo   Web Dashboard  : http://localhost:8080/index.html
echo   DBMS Inspector : http://localhost:8080/index.html#sql-inspector
echo   REST Health    : http://localhost:8080/api/health
echo ============================================================================
echo   Server is actively running.
echo   To stop the server: Run STOP.bat or press Ctrl+C in this window.
echo ============================================================================
echo.

:: Keep script running while java is active
:keep_alive
ping -n 4 127.0.0.1 >nul 2>&1
tasklist /fi "imagename eq java.exe" 2>nul | findstr /i "java.exe" >nul 2>&1
if !ERRORLEVEL! equ 0 goto :keep_alive

echo.
echo [Notice] Server process terminated.
pause
exit /b 0

:: Helper: check directory recursively for java.exe
:check_java_dir
if defined JAVA_EXE exit /b 0
if not exist "%~1" exit /b 0
for /f "delims=" %%J in ('dir /b /s /a-d "%~1\java.exe" 2^>nul') do (
    if not defined JAVA_EXE (
        set "JAVA_EXE=%%J"
        if exist "%%~dpJjavac.exe" set "JAVAC_EXE=%%~dpJjavac.exe"
        set "PATH=%%~dpJ;!PATH!"
    )
)
exit /b 0
