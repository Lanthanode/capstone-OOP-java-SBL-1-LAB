@echo off
setlocal EnableExtensions
title Stop SB-TMS Server
cd /d "%~dp0project"
call stop.bat
