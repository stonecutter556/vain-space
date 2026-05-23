@echo off
title Vain Space

cd /d "%~dp0"

echo [1/3] MySQL...
net start MySQL84 >nul 2>&1 && echo OK || echo OK

echo [2/3] Backend...
start "Backend" cmd /c "cd /d "%~dp0backend" && mvn spring-boot:run"

echo [3/3] Frontend...
start "Frontend" cmd /c "cd /d "%~dp0frontend" && npm run dev"

echo.
echo Frontend : http://localhost:3000
echo Backend  : http://localhost:8080
echo.
pause
