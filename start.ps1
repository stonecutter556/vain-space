Write-Host "====================================" -ForegroundColor Cyan
Write-Host "  Starting Vain Space..." -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[1/3] Starting MySQL..." -NoNewline
try {
    Start-Service MySQL84 -ErrorAction Stop
    Write-Host " OK" -ForegroundColor Green
} catch {
    Write-Host " (may already be running)" -ForegroundColor Yellow
}

Write-Host "[2/3] Starting Backend..." -ForegroundColor Gray
$backendDir = Join-Path $PSScriptRoot "backend"
Start-Process "cmd.exe" -ArgumentList "/c cd /d $backendDir && mvn spring-boot:run" -WindowStyle Normal

Write-Host "[3/3] Starting Frontend..." -ForegroundColor Gray
$frontendDir = Join-Path $PSScriptRoot "frontend"
Start-Process "cmd.exe" -ArgumentList "/c cd /d $frontendDir && npm run dev" -WindowStyle Normal

Write-Host ""
Write-Host "====================================" -ForegroundColor Cyan
Write-Host "  Frontend : http://localhost:3000" -ForegroundColor White
Write-Host "  Backend  : http://localhost:8080" -ForegroundColor White
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press any key to exit..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
