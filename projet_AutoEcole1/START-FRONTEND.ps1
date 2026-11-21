$ErrorActionPreference = "Stop"
$Projet = Split-Path -Parent $MyInvocation.MyCommand.Path
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  DEMARRAGE FRONTEND ANGULAR" -ForegroundColor Cyan
Write-Host "  Projet: $Projet" -ForegroundColor Gray
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""
Set-Location "$Projet\frontend"
Write-Host "[1/2] Repertoire courant: $(Get-Location)" -ForegroundColor Green
Write-Host "[2/2] Lancement ng serve --port 4200 ..." -ForegroundColor Green
Write-Host ""
npx ng serve --host 0.0.0.0 --port 4200 --disable-host-check --open
