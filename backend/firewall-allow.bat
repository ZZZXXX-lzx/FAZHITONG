@echo off
chcp 65001 >nul 2>&1
echo ========================================
echo   法智通 - 放行防火墙端口（局域网/手机访问）
echo ========================================
echo.
echo 此脚本会放行 3000（前端）和 8080（网关）端口，
echo 让同一 WiFi 下的手机/其他设备能通过局域网 IP 访问。
echo.

net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [提示] 当前不是管理员权限，正在尝试以管理员身份重新运行...
    powershell -NoProfile -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
    exit /b
)

echo 正在添加防火墙规则...
netsh advfirewall firewall delete rule name="fazhitong-web" >nul 2>&1
netsh advfirewall firewall add rule name="fazhitong-web" dir=in action=allow protocol=TCP localport=3000,8080 >nul 2>&1
if %errorlevel%==0 (
    echo [成功] 已放行 3000 和 8080 端口
) else (
    echo [失败] 放行失败，请手动检查防火墙
)

echo.
echo ========================================
echo   请用以下局域网地址访问（手机连同一个 WiFi）：
echo ========================================
echo.
for /f "tokens=1,2 delims=:" %%a in ('ipconfig ^| findstr /i "IPv4"') do (
    set ip=%%b
    setlocal enabledelayedexpansion
    set ip=!ip: =!
    if not "!ip!"=="" if not "!ip:~0,3!"=="127" echo    http://!ip!:3000
    endlocal
)
echo.
echo   提示：请选择与手机同一网段（通常 192.168.x.x）的那个地址。
echo.
pause
