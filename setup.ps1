# Script de Configuration Automatique
# Plateforme de Gestion de Projets Collaboratifs
# =============================================

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "  Configuration Automatique de l'Application" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""

# Variables de configuration
$PROJECT_DIR = "d:\yessine\OneDrive\Bureau\MiniProjetjava"
$DB_NAME = "project_management"
$DB_USER = "root"
$WAR_NAME = "project-manager"

# =============================================
# Étape 1 : Vérification des prérequis
# =============================================
Write-Host "[1/8] Vérification des prérequis..." -ForegroundColor Yellow

# Vérifier Java
Write-Host "  - Vérification de Java..." -NoNewline
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host " OK" -ForegroundColor Green
    Write-Host "    $javaVersion" -ForegroundColor Gray
} catch {
    Write-Host " ERREUR" -ForegroundColor Red
    Write-Host "    Java n'est pas installé ou pas dans le PATH" -ForegroundColor Red
    exit 1
}

# Vérifier Maven
Write-Host "  - Vérification de Maven..." -NoNewline
try {
    $mavenVersion = mvn -version 2>&1 | Select-String "Apache Maven"
    Write-Host " OK" -ForegroundColor Green
    Write-Host "    $mavenVersion" -ForegroundColor Gray
} catch {
    Write-Host " ERREUR" -ForegroundColor Red
    Write-Host "    Maven n'est pas installé ou pas dans le PATH" -ForegroundColor Red
    exit 1
}

# Vérifier MySQL
Write-Host "  - Vérification de MySQL..." -NoNewline
try {
    $mysqlVersion = mysql --version 2>&1
    Write-Host " OK" -ForegroundColor Green
    Write-Host "    $mysqlVersion" -ForegroundColor Gray
} catch {
    Write-Host " ERREUR" -ForegroundColor Red
    Write-Host "    MySQL n'est pas installé ou pas dans le PATH" -ForegroundColor Red
    exit 1
}

# Vérifier Tomcat
Write-Host "  - Vérification de Tomcat..." -NoNewline
if ($env:CATALINA_HOME) {
    Write-Host " OK" -ForegroundColor Green
    Write-Host "    CATALINA_HOME: $env:CATALINA_HOME" -ForegroundColor Gray
} else {
    Write-Host " ATTENTION" -ForegroundColor Yellow
    Write-Host "    Variable CATALINA_HOME non définie" -ForegroundColor Yellow
    $env:CATALINA_HOME = Read-Host "    Entrez le chemin de Tomcat (ex: C:\Program Files\Apache\Tomcat 9.0)"
    if (-not (Test-Path $env:CATALINA_HOME)) {
        Write-Host "    Chemin invalide!" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""

# =============================================
# Étape 2 : Configuration MySQL
# =============================================
Write-Host "[2/8] Configuration de la base de données..." -ForegroundColor Yellow

# Demander le mot de passe MySQL
$DB_PASSWORD = Read-Host "  Entrez le mot de passe MySQL root" -AsSecureString
$DB_PASSWORD_TEXT = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($DB_PASSWORD)
)

Write-Host "  - Test de connexion MySQL..." -NoNewline
$testConnection = "SELECT 1;" | mysql -u $DB_USER -p$DB_PASSWORD_TEXT 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host " OK" -ForegroundColor Green
} else {
    Write-Host " ERREUR" -ForegroundColor Red
    Write-Host "    Impossible de se connecter à MySQL" -ForegroundColor Red
    Write-Host "    Vérifiez votre mot de passe" -ForegroundColor Red
    exit 1
}

# Créer la base de données
Write-Host "  - Création de la base de données..." -NoNewline
$schemaPath = Join-Path $PROJECT_DIR "database\schema.sql"
if (Test-Path $schemaPath) {
    Get-Content $schemaPath | mysql -u $DB_USER -p$DB_PASSWORD_TEXT 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host " OK" -ForegroundColor Green
    } else {
        Write-Host " ERREUR" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host " ERREUR" -ForegroundColor Red
    Write-Host "    Fichier schema.sql introuvable" -ForegroundColor Red
    exit 1
}

# Charger les données de test
Write-Host "  - Chargement des données de test..." -NoNewline
$testDataPath = Join-Path $PROJECT_DIR "database\test_data.sql"
if (Test-Path $testDataPath) {
    Get-Content $testDataPath | mysql -u $DB_USER -p$DB_PASSWORD_TEXT 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host " OK" -ForegroundColor Green
    } else {
        Write-Host " ATTENTION" -ForegroundColor Yellow
    }
} else {
    Write-Host " IGNORÉ (fichier non trouvé)" -ForegroundColor Gray
}

Write-Host ""

# =============================================
# Étape 3 : Configuration du fichier db.properties
# =============================================
Write-Host "[3/8] Configuration du fichier de connexion..." -ForegroundColor Yellow

$resourcesDir = Join-Path $PROJECT_DIR "src\main\resources"
if (-not (Test-Path $resourcesDir)) {
    New-Item -ItemType Directory -Path $resourcesDir -Force | Out-Null
}

$dbPropertiesPath = Join-Path $resourcesDir "db.properties"
$dbPropertiesContent = @"
# Configuration Base de Données MySQL
# Généré automatiquement le $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
db.url=jdbc:mysql://localhost:3306/$DB_NAME?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=$DB_USER
db.password=$DB_PASSWORD_TEXT
db.driver=com.mysql.cj.jdbc.Driver

# Configuration Pool de Connexions HikariCP
db.pool.maximumPoolSize=20
db.pool.minimumIdle=5
db.pool.connectionTimeout=30000
db.pool.idleTimeout=600000
db.pool.maxLifetime=1800000
"@

Set-Content -Path $dbPropertiesPath -Value $dbPropertiesContent -Encoding UTF8
Write-Host "  - Fichier db.properties créé" -ForegroundColor Green
Write-Host "    $dbPropertiesPath" -ForegroundColor Gray

Write-Host ""

# =============================================
# Étape 4 : Nettoyage des anciennes compilations
# =============================================
Write-Host "[4/8] Nettoyage du projet..." -ForegroundColor Yellow

Set-Location $PROJECT_DIR
Write-Host "  - Exécution de mvn clean..." -NoNewline
mvn clean -q 2>&1 | Out-Null
if ($LASTEXITCODE -eq 0) {
    Write-Host " OK" -ForegroundColor Green
} else {
    Write-Host " ERREUR" -ForegroundColor Red
    exit 1
}

Write-Host ""

# =============================================
# Étape 5 : Compilation du projet
# =============================================
Write-Host "[5/8] Compilation du projet Maven..." -ForegroundColor Yellow
Write-Host "  (Cela peut prendre 1-2 minutes lors de la première exécution)" -ForegroundColor Gray

mvn package -DskipTests
if ($LASTEXITCODE -eq 0) {
    Write-Host "  - Compilation réussie" -ForegroundColor Green
} else {
    Write-Host "  - Échec de la compilation" -ForegroundColor Red
    exit 1
}

# Vérifier que le WAR existe
$warPath = Join-Path $PROJECT_DIR "target\$WAR_NAME.war"
if (Test-Path $warPath) {
    $warSize = (Get-Item $warPath).Length / 1MB
    Write-Host "  - Fichier WAR créé: $([math]::Round($warSize, 2)) MB" -ForegroundColor Green
} else {
    Write-Host "  - Fichier WAR non trouvé!" -ForegroundColor Red
    exit 1
}

Write-Host ""

# =============================================
# Étape 6 : Arrêt de Tomcat
# =============================================
Write-Host "[6/8] Préparation du déploiement..." -ForegroundColor Yellow

$tomcatShutdown = Join-Path $env:CATALINA_HOME "bin\shutdown.bat"
if (Test-Path $tomcatShutdown) {
    Write-Host "  - Arrêt de Tomcat..." -NoNewline
    Start-Process -FilePath $tomcatShutdown -WindowStyle Hidden -Wait
    Start-Sleep -Seconds 3
    Write-Host " OK" -ForegroundColor Green
} else {
    Write-Host "  - Tomcat n'est pas en cours d'exécution" -ForegroundColor Gray
}

Write-Host ""

# =============================================
# Étape 7 : Déploiement sur Tomcat
# =============================================
Write-Host "[7/8] Déploiement sur Tomcat..." -ForegroundColor Yellow

$webappsDir = Join-Path $env:CATALINA_HOME "webapps"
$deployPath = Join-Path $webappsDir "$WAR_NAME.war"
$deployDir = Join-Path $webappsDir $WAR_NAME

# Supprimer l'ancien déploiement
if (Test-Path $deployPath) {
    Write-Host "  - Suppression de l'ancien WAR..." -NoNewline
    Remove-Item $deployPath -Force
    Write-Host " OK" -ForegroundColor Green
}

if (Test-Path $deployDir) {
    Write-Host "  - Suppression de l'ancien dossier..." -NoNewline
    Remove-Item $deployDir -Recurse -Force
    Write-Host " OK" -ForegroundColor Green
}

# Copier le nouveau WAR
Write-Host "  - Copie du nouveau WAR..." -NoNewline
Copy-Item $warPath $deployPath -Force
if ($?) {
    Write-Host " OK" -ForegroundColor Green
} else {
    Write-Host " ERREUR" -ForegroundColor Red
    exit 1
}

Write-Host ""

# =============================================
# Étape 8 : Démarrage de Tomcat
# =============================================
Write-Host "[8/8] Démarrage de Tomcat..." -ForegroundColor Yellow

$tomcatStartup = Join-Path $env:CATALINA_HOME "bin\startup.bat"
if (Test-Path $tomcatStartup) {
    Start-Process -FilePath $tomcatStartup -WindowStyle Hidden
    Write-Host "  - Tomcat démarré" -ForegroundColor Green
    Write-Host "  - Déploiement en cours (attendre 10-30 secondes)..." -ForegroundColor Yellow
    
    # Attendre le déploiement
    Write-Host "  - Attente du déploiement..." -NoNewline
    for ($i = 1; $i -le 30; $i++) {
        Start-Sleep -Seconds 1
        if (Test-Path $deployDir) {
            Write-Host " OK (après $i secondes)" -ForegroundColor Green
            break
        }
        if ($i -eq 30) {
            Write-Host " TIMEOUT" -ForegroundColor Yellow
            Write-Host "    Le déploiement peut prendre plus de temps" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "  - Script de démarrage Tomcat introuvable!" -ForegroundColor Red
    exit 1
}

Write-Host ""

# =============================================
# Résumé
# =============================================
Write-Host "=====================================================" -ForegroundColor Green
Write-Host "  CONFIGURATION TERMINÉE AVEC SUCCÈS!" -ForegroundColor Green
Write-Host "=====================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Informations de connexion:" -ForegroundColor Cyan
Write-Host "  URL Application: http://localhost:8080/$WAR_NAME/" -ForegroundColor White
Write-Host "  URL API:         http://localhost:8080/$WAR_NAME/api/" -ForegroundColor White
Write-Host ""
Write-Host "Base de données:" -ForegroundColor Cyan
Write-Host "  Nom:        $DB_NAME" -ForegroundColor White
Write-Host "  Membres:    5 (avec compétences)" -ForegroundColor White
Write-Host "  Projets:    2 (E-Commerce + Mobile)" -ForegroundColor White
Write-Host "  Tâches:     15 (avec dépendances)" -ForegroundColor White
Write-Host ""
Write-Host "Prochaines étapes:" -ForegroundColor Cyan
Write-Host "  1. Ouvrez http://localhost:8080/$WAR_NAME/ dans votre navigateur" -ForegroundColor White
Write-Host "  2. Explorez les différentes pages (Dashboard, Team Members, Projects...)" -ForegroundColor White
Write-Host "  3. Testez l'allocation automatique sur le projet E-Commerce" -ForegroundColor White
Write-Host "  4. Consultez la timeline et les statistiques" -ForegroundColor White
Write-Host ""
Write-Host "Logs Tomcat:" -ForegroundColor Cyan
Write-Host "  $env:CATALINA_HOME\logs\catalina.out" -ForegroundColor White
Write-Host ""
Write-Host "Pour arrêter Tomcat:" -ForegroundColor Cyan
Write-Host "  $env:CATALINA_HOME\bin\shutdown.bat" -ForegroundColor White
Write-Host ""
Write-Host "Bon développement! 🚀" -ForegroundColor Green
Write-Host ""

# Ouvrir le navigateur automatiquement
$openBrowser = Read-Host "Voulez-vous ouvrir l'application dans le navigateur? (O/N)"
if ($openBrowser -eq "O" -or $openBrowser -eq "o") {
    Start-Sleep -Seconds 2
    Start-Process "http://localhost:8080/$WAR_NAME/"
}
