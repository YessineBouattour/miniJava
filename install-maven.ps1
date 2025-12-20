# Guide d'Installation de Maven
# ==============================

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "  Installation Automatique de Maven" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""

# Variables
$MAVEN_VERSION = "3.9.6"
$MAVEN_URL = "https://dlcdn.apache.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.zip"
$INSTALL_DIR = "C:\Program Files\Apache"
$MAVEN_HOME = "$INSTALL_DIR\apache-maven-$MAVEN_VERSION"

Write-Host "Cette installation va:" -ForegroundColor Yellow
Write-Host "  1. Télécharger Maven $MAVEN_VERSION" -ForegroundColor White
Write-Host "  2. L'installer dans: $INSTALL_DIR" -ForegroundColor White
Write-Host "  3. Ajouter Maven au PATH système" -ForegroundColor White
Write-Host ""

$continue = Read-Host "Continuer? (O/N)"
if ($continue -ne "O" -and $continue -ne "o") {
    Write-Host "Installation annulée." -ForegroundColor Yellow
    exit 0
}

# Vérifier les permissions administrateur
$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host ""
    Write-Host "ERREUR: Ce script nécessite des droits administrateur!" -ForegroundColor Red
    Write-Host "Faites un clic droit sur PowerShell et 'Exécuter en tant qu'administrateur'" -ForegroundColor Yellow
    pause
    exit 1
}

Write-Host ""
Write-Host "[1/4] Création du dossier d'installation..." -ForegroundColor Yellow
if (-not (Test-Path $INSTALL_DIR)) {
    New-Item -ItemType Directory -Path $INSTALL_DIR -Force | Out-Null
    Write-Host "  - Dossier créé: $INSTALL_DIR" -ForegroundColor Green
} else {
    Write-Host "  - Dossier existe déjà" -ForegroundColor Gray
}

Write-Host ""
Write-Host "[2/4] Téléchargement de Maven $MAVEN_VERSION..." -ForegroundColor Yellow
$zipPath = "$env:TEMP\maven.zip"
try {
    Invoke-WebRequest -Uri $MAVEN_URL -OutFile $zipPath -UseBasicParsing
    Write-Host "  - Téléchargement terminé" -ForegroundColor Green
} catch {
    Write-Host "  - ERREUR lors du téléchargement" -ForegroundColor Red
    Write-Host "    $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[3/4] Extraction de Maven..." -ForegroundColor Yellow
try {
    Expand-Archive -Path $zipPath -DestinationPath $INSTALL_DIR -Force
    Write-Host "  - Extraction terminée" -ForegroundColor Green
    Remove-Item $zipPath -Force
} catch {
    Write-Host "  - ERREUR lors de l'extraction" -ForegroundColor Red
    Write-Host "    $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[4/4] Configuration du PATH..." -ForegroundColor Yellow

# Ajouter au PATH système
$currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
$mavenBin = "$MAVEN_HOME\bin"

if ($currentPath -notlike "*$mavenBin*") {
    [Environment]::SetEnvironmentVariable("Path", "$currentPath;$mavenBin", "Machine")
    Write-Host "  - Maven ajouté au PATH système" -ForegroundColor Green
} else {
    Write-Host "  - Maven déjà dans le PATH" -ForegroundColor Gray
}

# Définir MAVEN_HOME
[Environment]::SetEnvironmentVariable("MAVEN_HOME", $MAVEN_HOME, "Machine")
Write-Host "  - Variable MAVEN_HOME définie" -ForegroundColor Green

# Mettre à jour le PATH de la session courante
$env:Path = "$env:Path;$mavenBin"
$env:MAVEN_HOME = $MAVEN_HOME

Write-Host ""
Write-Host "=====================================================" -ForegroundColor Green
Write-Host "  Installation de Maven Terminée!" -ForegroundColor Green
Write-Host "=====================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Maven installé dans: $MAVEN_HOME" -ForegroundColor White
Write-Host ""
Write-Host "IMPORTANT:" -ForegroundColor Yellow
Write-Host "  1. Fermez et rouvrez votre terminal PowerShell" -ForegroundColor White
Write-Host "  2. Vérifiez l'installation avec: mvn -version" -ForegroundColor White
Write-Host "  3. Relancez le script setup.ps1" -ForegroundColor White
Write-Host ""

pause
