@echo off
chcp 65001 >nul 2>&1
set "JAVA_HOME=C:\Users\刘子轩\.jdks\ms-21.0.12"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"

REM ===== Environment Variables =====
set DB_PASSWORD=123456
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=fazhitong
set DB_USERNAME=root

echo ========================================
echo   法智通法律服务平台 - 启动脚本
echo ========================================
echo.

echo [1/8] 启动 Gateway 网关 (port 8080)...
start "fazhitong-gateway" cmd /c "cd /d "%~dp0gateway" & java -jar target\gateway-1.0.0.jar"
timeout /t 5 /nobreak >nul 2>&1

echo [2/8] 启动 Auth 认证服务 (port 8081)...
start "fazhitong-auth" cmd /c "cd /d "%~dp0auth-service" & java -jar target\auth-service-1.0.0.jar"
timeout /t 3 /nobreak >nul 2>&1

echo [3/8] 启动 User 用户服务 (port 8082)...
start "fazhitong-user" cmd /c "cd /d "%~dp0user-service" & java -jar target\user-service-1.0.0.jar"
timeout /t 3 /nobreak >nul 2>&1

echo [4/8] 启动 Document 文书服务 (port 8083)...
start "fazhitong-document" cmd /c "cd /d "%~dp0document-service" & java -jar target\document-service-1.0.0.jar"
timeout /t 3 /nobreak >nul 2>&1

echo [5/8] 启动 Consultation 咨询服务 (port 8084)...
start "fazhitong-consultation" cmd /c "cd /d "%~dp0consultation-service" & java -jar target\consultation-service-1.0.0.jar"
timeout /t 3 /nobreak >nul 2>&1

echo [6/8] 启动 Contract 合同服务 (port 8085)...
start "fazhitong-contract" cmd /c "cd /d "%~dp0contract-service" & java -jar target\contract-service-1.0.0.jar"
timeout /t 3 /nobreak >nul 2>&1

echo [7/8] 启动 Case 案例服务 (port 8086)...
start "fazhitong-case" cmd /c "cd /d "%~dp0case-service" & java -jar target\case-service-1.0.0.jar"
timeout /t 3 /nobreak >nul 2>&1

echo [8/8] 启动 Payment 支付服务 (port 8087)...
start "fazhitong-payment" cmd /c "cd /d "%~dp0payment-service" & java -jar target\payment-service-1.0.0.jar"

echo.
echo ========================================
echo   所有服务已启动！
echo ========================================
echo   Gateway:        http://localhost:8080
echo   Auth Service:   http://localhost:8081
echo   User Service:   http://localhost:8082
echo   Doc Service:    http://localhost:8083
echo   Consultation:   http://localhost:8084
echo   Contract:       http://localhost:8085
echo   Case Service:   http://localhost:8086
echo   Payment:        http://localhost:8087
echo ========================================
echo.
pause
