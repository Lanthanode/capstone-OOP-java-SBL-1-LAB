@echo off
setlocal enabledelayedexpansion
title Smart Banking System (SB-TMS) - Standalone CLI Console

set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"

echo ================================================================================
echo   Launching Standalone Interactive CLI Demonstration (Experiment 11 Mode)
echo ================================================================================
echo.

if not exist "backend\bin\com\sbtms\Main.class" (
    call setup.bat
)

java -cp "backend\bin;backend\lib\*" com.sbtms.Main --cli

pause
