@echo off
setlocal EnableExtensions EnableDelayedExpansion
title SB-TMS - Stopping Server
color 0C

echo.
echo ============================================================================
echo   Stopping SB-TMS Smart Banking Server...
echo ============================================================================
echo.

set "KILLED=0"

:: 1. Kill any process listening on port 8080
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":8080 " ^| findstr "LISTENING"') do (
    if "%%a" neq "0" (
        echo Terminating process on port 8080 PID %%a
        taskkill /F /PID %%a >nul 2>&1
        set "KILLED=1"
    )
)

:: 2. Kill any java process running com.sbtms.Main via powershell
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-CimInstance Win32_Process -Filter \"Name = 'java.exe'\" | Where-Object { $_.CommandLine -like '*com.sbtms.Main*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }" >nul 2>&1

echo.
echo ============================================================================
echo   [OK] SB-TMS Server has been stopped successfully.
echo   Port 8080 is now released.
echo ============================================================================
echo.
timeout /t 2 >nul 2>&1
exit /b 0
