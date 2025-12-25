# 🚀 Guide de Lancement du Projet

## Prérequis

1. **Java JDK 11 ou supérieur** installé
2. **MySQL** en cours d'exécution (XAMPP recommandé)
3. Base de données créée et configurée

## 🎯 Lancement Rapide

### **Méthode 1 : Double-clic sur START.bat (Recommandé)**
1. Double-cliquez sur le fichier `START.bat`
2. Le serveur se lancera automatiquement
3. Ouvrez votre navigateur à l'adresse : http://localhost:8080

### **Méthode 2 : Via PowerShell**
1. Clic droit sur le dossier → "Ouvrir dans le Terminal"
2. Exécutez :
   ```powershell
   .\START.bat
   ```

### **Méthode 3 : PowerShell direct**
Si vous avez des restrictions de politique d'exécution :
```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

## 📝 Configuration Base de Données

Avant le premier lancement, vérifiez le fichier `src\main\resources\db.properties` :
```properties
db.url=jdbc:mysql://localhost:3306/project_management
db.username=root
db.password=
```

## 🏗️ Ce que fait le script automatiquement

1. ✅ Télécharge toutes les dépendances nécessaires (MySQL Connector, Gson, HikariCP, SLF4J)
2. ✅ Compile tous les fichiers Java
3. ✅ Lance le serveur sur le port 8080

## 🌐 Accès à l'Application

Une fois le serveur démarré, ouvrez votre navigateur :
- **URL** : http://localhost:8080
- **Interface** : Dashboard de gestion de projets

## ⚠️ Dépannage

### Le script ne se lance pas
- **Problème** : "Scripts are disabled on this system"
- **Solution** : Utilisez `START.bat` au lieu de `run.ps1` directement

### Port 8080 déjà utilisé
```powershell
# Arrêter le processus Java existant
Get-Process java | Stop-Process -Force
```

### Erreur de connexion à la base de données
1. Vérifiez que MySQL est démarré (XAMPP)
2. Vérifiez les identifiants dans `db.properties`
3. Assurez-vous que la base `project_management` existe

### Java n'est pas reconnu
```powershell
# Vérifier l'installation de Java
java -version
```
Si erreur, ajoutez Java au PATH système.

## 📂 Structure du Projet

```
miniJava/
├── START.bat              ← Double-cliquez ici pour lancer!
├── run.ps1                ← Script PowerShell de démarrage
├── database/              ← Scripts SQL
│   ├── schema.sql
│   └── test_data.sql
├── src/
│   ├── main/
│   │   ├── java/         ← Code Java backend
│   │   └── webapp/       ← Interface web (HTML/CSS/JS)
│   └── test/             ← Tests
└── lib/                   ← Dépendances (auto-téléchargées)
```

## 👥 Équipe & Répartition

Frontend divisé en modules par personne :
- **Personne 1** : Gestion membres (`members.js`)
- **Personne 2** : Gestion compétences (`skills.js`)
- **Personne 3** : Gestion projets et tâches (`projects.js`)
- **Personne 4** : Statistiques et timeline (`statistics.js`, `timeline.js`)
- **Personne 5** : API et alertes (`api.js`, `alerts.js`)

## 📊 Données de Test

Le système est pré-chargé avec :
- 6 membres avec compétences variées
- 2 projets (E-commerce, CRM)
- 15 tâches réparties
- Calcul automatique de charge de travail

## 💡 Fonctionnalités

✨ **Dashboard** : Vue d'ensemble des projets et charges
👥 **Gestion Membres** : CRUD complet + compétences + charge de travail
📁 **Gestion Projets** : CRUD projets et tâches avec affectation
📊 **Statistiques** : Visualisation des charges et avancement
📅 **Timeline** : Diagramme de Gantt interactif
🔔 **Alertes** : Notifications de surcharge et conflits

---

**Pour toute question, consultez la documentation complète dans les fichiers `.md` du projet.**
