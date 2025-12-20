# Guide de Démarrage Rapide

## Installation Express (5 minutes)

### 1. Prérequis
- ✅ Java 11+ installé
- ✅ MySQL 8.0+ installé et démarré
- ✅ Maven installé
- ✅ Tomcat 9.x installé

### 2. Configuration Base de Données

```powershell
# Démarrer MySQL
mysql -u root -p

# Créer la base de données
source database/schema.sql

# (Optionnel) Charger les données de test
source database/test_data.sql
```

### 3. Configuration Application

Éditer `src/main/resources/db.properties`:
```properties
db.username=root
db.password=VOTRE_MOT_DE_PASSE
```

### 4. Compilation et Déploiement

```powershell
# Compiler
mvn clean package

# Copier vers Tomcat
copy target\project-manager.war %CATALINA_HOME%\webapps\

# Démarrer Tomcat
cd %CATALINA_HOME%\bin
startup.bat
```

### 5. Accès à l'Application

Ouvrir: **http://localhost:8080/project-manager/**

---

## Test Rapide des Fonctionnalités

### Avec Données de Test Chargées

Si vous avez exécuté `test_data.sql`, vous avez déjà:
- ✅ 5 membres d'équipe
- ✅ 2 projets
- ✅ 15 tâches configurées

**Actions à tester**:

1. **Voir l'équipe** → Onglet "Team Members"
   - 5 membres affichés avec compétences

2. **Voir les projets** → Onglet "Projects"
   - Projet "E-Commerce Application" visible
   - Cliquer pour voir les détails

3. **Allocation automatique**
   - Sur le projet E-Commerce
   - Cliquer "Auto-Allocate"
   - Observer les tâches assignées

4. **Voir les alertes** → Onglet "Alerts"
   - Vérifier les alertes de surcharge éventuelles

5. **Voir la timeline** → Onglet "Timeline"
   - Sélectionner "E-Commerce Application"
   - Observer la distribution temporelle

6. **Statistiques** → Onglet "Statistics"
   - Voir la charge de travail par membre
   - Progression du projet

### Sans Données de Test

Si vous préférez tester manuellement:

1. **Ajouter des membres**
   ```
   Team Members → Add Member
   
   Exemple:
   - Nom: Jean Dupont
   - Email: jean@example.com
   - Disponibilité: 40h
   - Compétences: Java Development (5), Database Design (4)
   ```

2. **Créer un projet**
   ```
   Projects → Create Project
   
   Exemple:
   - Nom: Mon Premier Projet
   - Date début: Aujourd'hui
   - Deadline: Dans 1 mois
   ```

3. **Ajouter des tâches**
   ```
   Sur le projet → Add Task
   
   Exemple:
   - Titre: Créer l'API REST
   - Heures: 16h
   - Priorité: High
   - Compétences: Java Development (4)
   ```

4. **Tester l'allocation**
   ```
   Sur le projet → Auto-Allocate
   ```

---

## Résolution de Problèmes Courants

### Erreur de Connexion MySQL

**Problème**: `Access denied for user 'root'@'localhost'`

**Solution**:
```sql
mysql -u root -p
ALTER USER 'root'@'localhost' IDENTIFIED BY 'nouveau_mot_de_passe';
FLUSH PRIVILEGES;
```
Puis mettre à jour `db.properties`

### Erreur 404 - Application non trouvée

**Vérifications**:
1. Tomcat est démarré: `http://localhost:8080`
2. Le WAR est déployé: Vérifier `%CATALINA_HOME%\webapps\project-manager`
3. Le déploiement est complet: Attendre 30 secondes

### Erreur 500 - Internal Server Error

**Vérifier les logs**:
```powershell
# Logs Tomcat
type %CATALINA_HOME%\logs\catalina.out

# Ou sous Windows
type %CATALINA_HOME%\logs\localhost.*.log
```

**Causes communes**:
- Base de données non accessible
- Mauvais identifiants dans `db.properties`
- Tables non créées (exécuter `schema.sql`)

### La page ne se charge pas

**Solutions**:
1. Vider le cache du navigateur (Ctrl+Shift+Delete)
2. Essayer un autre navigateur
3. Vérifier la console JavaScript (F12)

### Les données ne s'affichent pas

**Vérifier**:
1. La base de données contient des données:
   ```sql
   USE project_management;
   SELECT COUNT(*) FROM members;
   SELECT COUNT(*) FROM projects;
   ```

2. L'API fonctionne:
   - Tester: `http://localhost:8080/project-manager/api/members/`
   - Devrait retourner du JSON

---

## Commandes Utiles

### Maven
```powershell
# Compiler sans tests
mvn clean package -DskipTests

# Nettoyer le projet
mvn clean

# Voir les dépendances
mvn dependency:tree
```

### MySQL
```sql
-- Voir toutes les tables
SHOW TABLES;

-- Compter les enregistrements
SELECT 
    (SELECT COUNT(*) FROM members) as Members,
    (SELECT COUNT(*) FROM projects) as Projects,
    (SELECT COUNT(*) FROM tasks) as Tasks;

-- Réinitialiser la base
DROP DATABASE project_management;
source database/schema.sql;
```

### Tomcat
```powershell
# Démarrer
%CATALINA_HOME%\bin\startup.bat

# Arrêter
%CATALINA_HOME%\bin\shutdown.bat

# Voir les logs en temps réel
tail -f %CATALINA_HOME%\logs\catalina.out
```

---

## Structure des URLs

- **Application**: http://localhost:8080/project-manager/
- **API Members**: http://localhost:8080/project-manager/api/members/
- **API Projects**: http://localhost:8080/project-manager/api/projects/
- **API Tasks**: http://localhost:8080/project-manager/api/tasks/
- **API Statistics**: http://localhost:8080/project-manager/api/statistics/
- **Tomcat Manager**: http://localhost:8080/manager/html

---

## Prochaines Étapes

1. ✅ Installer et configurer
2. ✅ Tester les fonctionnalités de base
3. ✅ Exécuter les 7 scénarios de test (voir README.md)
4. 📝 Personnaliser pour vos besoins
5. 🎥 Créer la vidéo de démonstration

---

## Support

Pour plus de détails, consulter:
- **README.md** - Documentation complète
- **database/schema.sql** - Structure de la base de données
- **database/test_data.sql** - Données de test

Bon développement ! 🚀
