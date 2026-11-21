$ErrorActionPreference = "Stop"
$Projet = Split-Path -Parent $MyInvocation.MyCommand.Path
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  DEMARRAGE BACKEND SPRING BOOT" -ForegroundColor Cyan
Write-Host "  Projet: $Projet" -ForegroundColor Gray
Write-Host "  PostgreSQL requis sur localhost:5432/db autoecole" -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""
Set-Location "$Projet\backend"
Write-Host "[1/2] Repertoire courant: $(Get-Location)" -ForegroundColor Green
Write-Host "[2/2] Lancement mvnw spring-boot:run ..." -ForegroundColor Green
Write-Host ""
.\mvnw.cmd spring-boot:run -ErrorAction SilentlyContinue
# If mvnw failed or is unavailable, fallback to bundled Maven in tools/apache-maven-3.9.6
if ($LASTEXITCODE -ne 0) {
	$BundledMaven = Join-Path $Projet "tools\apache-maven-3.9.6"
	$MvnCmd = Join-Path $BundledMaven "bin\mvn.cmd"
	if (Test-Path $MvnCmd) {
		Write-Host "mvnw failed or not present. Using bundled Maven: $MvnCmd" -ForegroundColor Yellow
		# Ensure JAVA_HOME is set, try to read from environment or common locations
		if (-not $env:JAVA_HOME) {
			$possible = @("C:\\Program Files\\Eclipse Adoptium\\jdk-17*", "C:\\Program Files\\Microsoft\\jdk-17*", "C:\\Program Files\\Zulu\\zulu-17*")
			foreach ($p in $possible) {
				$found = Get-ChildItem -Path $p -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
				if ($found) { $env:JAVA_HOME = $found.FullName; break }
			}
		}
		if (-not $env:JAVA_HOME) {
			Write-Host "WARNING: JAVA_HOME is not set. Maven may fail if Java is not installed." -ForegroundColor Red
		} else {
			Write-Host "Using JAVA_HOME=$env:JAVA_HOME" -ForegroundColor Green
		}

		Push-Location $Project\backend
		& "$MvnCmd" spring-boot:run
		Pop-Location
	} else {
		Write-Host "Bundled Maven not found at $MvnCmd. Please ensure Maven is available or run .\mvnw.cmd manually." -ForegroundColor Red
		exit 1
	}
}
