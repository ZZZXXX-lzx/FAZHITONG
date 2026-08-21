@echo off
chcp 65001 >nul 2>&1
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0.."

echo ========================================
echo   法保通 - 一键启动后端+前端
echo ========================================
echo.

REM Start backend services
echo [1/3] 启动后端微服务...
start "fabaotong-backend" cmd /c "cd /d "%~dp0" && call start-all.bat"

timeout /t 10 /nobreak >nul 2>&1

REM Start PC Portal frontend
echo [2/3] 启动用户端前端 (port 3000)...
start "fabaotong-pc-portal" cmd /c "cd /d "%~dp0..\frontend\pc-portal" && npm run dev"

timeout /t 3 /nobreak >nul 2>&1

REM Start Admin frontend
echo [3/3] 启动管理端前端 (port 3001)...
start "fabaotong-admin" cmd /c "cd /d "%~dp0..\frontend\admin" && npm run dev"

echo.
echo ========================================
echo   全部启动完成！
echo ========================================
echo   用户端前端:  http://localhost:3000
echo   管理端前端:  http://localhost:3001
echo   API网关:     http://localhost:8080
echo ========================================
pause
