# Installation Complète - Plateforme de Gestion de Projets
# =========================================================
# Ce script installe automatiquement tous les prérequis manquants

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "  Installation et Configuration Complète" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier les permissions administrateur
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

Write-Host "Vérification des prérequis..." -ForegroundColor Yellow
Write-Host ""

# Liste des prérequis
$prerequisites = @{
    "Java" = { java -version 2>&1 | Out-Null; $? }
    "Maven" = { mvn -version 2>&1 | Out-Null; $? }
    "MySQL" = { mysql --version 2>&1 | Out-Null; $? }
}

$missing = @()

foreach ($tool in $prerequisites.Keys) {
    Write-Host "  Vérification de $tool..." -NoNewline
    $installed = & $prerequisites[$tool]
    
    if ($installed) {
        Write-Host " OK" -ForegroundColor Green
    } else {
        Write-Host " MANQUANT" -ForegroundColor Red
        $missing += $tool
    }
}

Write-Host ""

if ($missing.Count -eq 0) {
    Write-Host "Tous les prérequis sont installés!" -ForegroundColor Green
    Write-Host "Exécution de la configuration..." -ForegroundColor Yellow
    Write-Host ""
    
    # Exécuter le script de configuration
    & "$PSScriptRoot\setup.ps1"
    exit $LASTEXITCODE
}

# Afficher ce qui manque
Write-Host "=====================================================" -ForegroundColor Yellow
Write-Host "  Prérequis manquants détectés" -ForegroundColor Yellow
Write-Host "=====================================================" -ForegroundColor Yellow
Write-Host ""

Write-Host "Les outils suivants doivent être installés:" -ForegroundColor White
foreach ($tool in $missing) {
    Write-Host "  - $tool" -ForegroundColor Red
}
Write-Host ""

# Instructions d'installation
Write-Host "Instructions d'installation:" -ForegroundColor Cyan
Write-Host ""

if ($missing -contains "Java") {
    Write-Host "Java:" -ForegroundColor Yellow
    Write-Host "  1. Téléchargez Java 11+ depuis: https://adoptium.net/" -ForegroundColor White
    Write-Host "  2. Installez et ajoutez au PATH" -ForegroundColor White
    Write-Host "  3. Vérifiez avec: java -version" -ForegroundColor White
    Write-Host ""
}

if ($missing -contains "Maven") {
    Write-Host "Maven:" -ForegroundColor Yellow
    if ($isAdmin) {
        Write-Host "  Option 1: Installation automatique (recommandé)" -ForegroundColor Green
        Write-Host "    Exécutez: .\install-maven.ps1" -ForegroundColor White
        Write-Host ""
    }
    Write-Host "  Option 2: Installation manuelle" -ForegroundColor White
    Write-Host "    1. Téléchargez depuis: https://maven.apache.org/download.cgi" -ForegroundColor White
    Write-Host "    2. Extrayez dans C:\Program Files\Apache\" -ForegroundColor White
    Write-Host "    3. Ajoutez au PATH: C:\Program Files\Apache\apache-maven-X.X.X\bin" -ForegroundColor White
    Write-Host "    4. Définissez MAVEN_HOME" -ForegroundColor White
    Write-Host "    5. Vérifiez avec: mvn -version" -ForegroundColor White
    Write-Host ""
}

if ($missing -contains "MySQL") {
    Write-Host "MySQL:" -ForegroundColor Yellow
    Write-Host "  1. Téléchargez MySQL 8.0+ depuis: https://dev.mysql.com/downloads/installer/" -ForegroundColor White
    Write-Host "  2. Installez MySQL Server" -ForegroundColor White
    Write-Host "  3. Notez le mot de passe root défini" -ForegroundColor White
    Write-Host "  4. Ajoutez au PATH: C:\Program Files\MySQL\MySQL Server 8.0\bin" -ForegroundColor White
    Write-Host "  5. Vérifiez avec: mysql --version" -ForegroundColor White
    Write-Host ""
}

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "  Prochaines étapes" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Installez les prérequis manquants" -ForegroundColor White
Write-Host "2. Fermez et rouvrez PowerShell" -ForegroundColor White
Write-Host "3. Relancez ce script: .\install-all.ps1" -ForegroundColor White
Write-Host ""

# Option d'installation automatique de Maven
if ($missing -contains "Maven" -and $missing.Count -eq 1 -and $isAdmin) {
    Write-Host ""
    $installMaven = Read-Host "Voulez-vous installer Maven automatiquement maintenant? (O/N)"
    if ($installMaven -eq "O" -or $installMaven -eq "o") {
        Write-Host ""
        & "$PSScriptRoot\install-maven.ps1"
        
        Write-Host ""
        Write-Host "Maven installé! Relançons la configuration..." -ForegroundColor Green
        Write-Host ""
        Start-Sleep -Seconds 2
        
        # Recharger les variables d'environnement
        $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
        
        # Relancer ce script
        & "$PSScriptRoot\install-all.ps1"
    }
}

Write-Host ""
pause
