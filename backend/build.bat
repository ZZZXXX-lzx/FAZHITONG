@echo off
chcp 65001 >nul 2>&1
set "JAVA_HOME=C:\Users\刘子轩\.jdks\ms-21.0.12"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"

echo ========================================
echo   法智通 - 后端一键打包
echo ========================================
echo.
echo 使用系统 Maven 打包全部模块（跳过测试）...

where mvn >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 mvn，请先安装 Maven 并配置到 PATH。
    pause
    exit /b 1
)

mvn clean package -DskipTests -q
if errorlevel 1 (
    echo [错误] 打包失败，请检查上方日志。
    pause
    exit /b 1
)

echo.
echo 打包完成，产物位于各模块 target 目录。
echo.
pause
