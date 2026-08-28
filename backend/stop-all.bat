@echo off
chcp 65001 >nul 2>&1
echo ========================================
echo   法智通法律服务平台 - 停止所有服务
echo ========================================
echo.
echo 正在释放端口 8080-8087 ...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ports=8080..8087; $ids=@(); foreach($p in $ports){ $c=Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue; if($c){ $ids += $c.OwningProcess } }; $ids=@($ids | Sort-Object -Unique); if($ids.Count -gt 0){ foreach($i in $ids){ Stop-Process -Id $i -Force -ErrorAction SilentlyContinue }; Write-Host ('已停止 ' + $ids.Count + ' 个进程') } else { Write-Host '没有运行中的服务' }"
echo.
echo 所有端口已释放。
echo.
pause
