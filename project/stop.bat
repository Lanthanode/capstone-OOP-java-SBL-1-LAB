@echo off
setlocal enabledelayedexpansion
title Stop SB-TMS Server

echo [INFO] Searching for running SB-TMS Java processes on port 8080...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080" ^| findstr "LISTENING"') do (
    echo [INFO] Terminating PID %%a listening on port 8080...
    taskkill /F /PID %%a >nul 2>&1
)
echo [SUCCESS] Port 8080 cleared. Server stopped.
ping -n 3 127.0.0.1 >nul
