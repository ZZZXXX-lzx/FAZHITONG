# 法智通开发环境启动脚本
# 需要：Java 17+, Node.js, MySQL

$rootDir = Split-Path -Parent $PSScriptRoot

Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║      法智通 - 法律服务平台启动脚本     ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Check prerequisites
$javaOk = $false
try { java -version 2>&1 | Select-String "17" | Out-Null; $javaOk = $true } catch {}
if (-not $javaOk) { Write-Host "[WARN] 未检测到 Java 17" -ForegroundColor Yellow }

$nodeOk = $false
try { $v = node --version; if ($v -match "v(18|20|22|24)") { $nodeOk = $true } } catch {}
if (-not $nodeOk) { Write-Host "[WARN] 未检测到 Node.js 18+" -ForegroundColor Yellow }

Write-Host "[INFO] 启动后端服务（需要先准备好 MySQL 数据库）..." -ForegroundColor Green
Write-Host "[INFO] 请在单独的终端中启动后端服务："
Write-Host "       cd $rootDir\backend" -ForegroundColor Yellow
Write-Host "       .\mvnw.cmd clean install -DskipTests" -ForegroundColor Yellow
Write-Host "       （然后分别启动各个微服务）" -ForegroundColor Yellow
Write-Host ""

Write-Host "[INFO] 启动前端开发服务器..." -ForegroundColor Green

# Start frontend dev servers
$portalDir = "$rootDir\frontend\pc-portal"
$adminDir = "$rootDir\frontend\admin"

Write-Host "       PC门户: http://localhost:3000" -ForegroundColor Yellow
Write-Host "       管理后台: http://localhost:3001" -ForegroundColor Yellow
Write-Host ""

# Start both frontends in background
Start-Process powershell -ArgumentList "-NoExit -Command cd '$portalDir'; npm run dev"
Start-Process powershell -ArgumentList "-NoExit -Command cd '$adminDir'; npm run dev"

Write-Host "[INFO] 启动 Electron 桌面应用..." -ForegroundColor Green
cd $PSScriptRoot
npx electron .
