# Plateforme de Gestion de Projets Collaboratifs

Une application web complète pour gérer des projets en équipe avec répartition intelligente des tâches selon les compétences, disponibilités et charges de travail des membres.

## 📋 Table des Matières

- [Fonctionnalités](#fonctionnalités)
- [Architecture](#architecture)
- [Technologies Utilisées](#technologies-utilisées)
- [Installation](#installation)
- [Configuration](#configuration)
- [Utilisation](#utilisation)
- [Algorithme d'Allocation](#algorithme-dallocation)
- [Scénarios de Test](#scénarios-de-test)
- [Structure du Projet](#structure-du-projet)
- [API Documentation](#api-documentation)

## ✨ Fonctionnalités

### Gestion des Membres
- ✅ Ajouter, modifier et supprimer des membres d'équipe
- ✅ Définir les compétences avec niveaux de maîtrise (1-5)
- ✅ Configurer la disponibilité hebdomadaire (heures)
- ✅ Suivi automatique de la charge de travail actuelle
- ✅ Visualisation du taux d'occupation

### Gestion des Projets
- ✅ Créer et gérer des projets avec dates de début et deadlines
- ✅ Ajouter des tâches avec estimations, priorités et deadlines
- ✅ Définir les compétences requises pour chaque tâche
- ✅ Établir des dépendances entre tâches
- ✅ Suivi de l'avancement du projet

### Répartition Automatique des Tâches
- ✅ Algorithme intelligent d'allocation basé sur:
  - Correspondance des compétences (40%)
  - Disponibilité des membres (30%)
  - Équilibre de la charge de travail (20%)
  - Priorité des tâches (10%)
- ✅ Respect des contraintes de compétences et disponibilités
- ✅ Optimisation de l'équilibre des charges

### Timeline Interactive
- ✅ Visualisation graphique du projet
- ✅ Affichage des tâches par membre
- ✅ Identification visuelle de l'état des tâches
- ✅ Vue chronologique complète

### Alertes Intelligentes
- ✅ Détection automatique de surcharge
- ✅ Alertes de conflits d'affectation
- ✅ Notification de retards potentiels
- ✅ Système de badges et notifications

### Tableaux de Bord et Statistiques
- ✅ Vue d'ensemble du projet
- ✅ Statistiques de productivité
- ✅ Rapport d'avancement par projet
- ✅ Analyse de l'équilibre des charges
- ✅ Graphiques de progression

## 🏗️ Architecture

### Architecture 3-tiers

```
┌─────────────────────────────────────────┐
│         Frontend (Presentation)         │
│  HTML5 + CSS3 + JavaScript (Vanilla)    │
│         Timeline Visualization           │
└────────────────┬────────────────────────┘
                 │ REST API (JSON)
┌────────────────▼────────────────────────┐
│        Backend (Business Logic)         │
│           Java Servlets                  │
│    - MemberServlet                       │
│    - ProjectServlet                      │
│    - TaskServlet                         │
│    - AllocationServlet                   │
│    - AlertServlet                        │
│    - StatisticsServlet                   │
│                                          │
│      Service Layer:                      │
│    - TaskAllocationService               │
│    - StatisticsService                   │
└────────────────┬────────────────────────┘
                 │ JDBC
┌────────────────▼────────────────────────┐
│      Data Layer (Persistence)            │
│              MySQL Database              │
│                                          │
│  Tables:                                 │
│    - members                             │
│    - skills                              │
│    - member_skills                       │
│    - projects                            │
│    - tasks                               │
│    - task_skills                         │
│    - task_dependencies                   │
│    - alerts                              │
│    - task_history                        │
│                                          │
│  DAO Pattern:                            │
│    - MemberDAO                           │
│    - SkillDAO                            │
│    - ProjectDAO                          │
│    - TaskDAO                             │
│    - AlertDAO                            │
└──────────────────────────────────────────┘
```

### Composants Principaux

#### Frontend
- **index.html**: Page principale avec navigation SPA
- **style.css**: Design responsive et moderne
- **api.js**: Couche d'abstraction pour les appels API
- **timeline.js**: Module de visualisation de la timeline
- **app.js**: Logique principale de l'application

#### Backend
- **Models**: Entités Java (Member, Task, Project, Alert, etc.)
- **DAO**: Accès aux données avec JDBC et HikariCP
- **Services**: Logique métier (allocation, statistiques)
- **Servlets**: API REST pour communication frontend-backend
- **Filters**: CORS, encodage UTF-8

## 🛠️ Technologies Utilisées

### Frontend
- HTML5
- CSS3 (Design responsive, Flexbox, Grid)
- JavaScript (ES6+, Vanilla JS)
- Font Awesome (Icônes)

### Backend
- Java 11
- Servlets API 4.0
- Maven (Gestion de dépendances)
- HikariCP (Connection pooling)
- Gson (Sérialisation JSON)
- SLF4J (Logging)

### Base de Données
- MySQL 8.0
- JDBC Driver

### Serveur d'Application
- Apache Tomcat 9.x ou supérieur

## 📦 Installation

### Prérequis

1. **Java Development Kit (JDK) 11 ou supérieur**
   ```powershell
   java -version
   ```

2. **Apache Maven 3.6+**
   ```powershell
   mvn -version
   ```

3. **MySQL Server 8.0+**
   ```powershell
   mysql --version
   ```

4. **Apache Tomcat 9.x**
   - Télécharger depuis https://tomcat.apache.org/

### Étapes d'Installation

#### 1. Cloner ou télécharger le projet

```powershell
cd d:\yessine\OneDrive\Bureau\MiniProjetjava
```

#### 2. Configuration de la base de données

Créer la base de données:
```powershell
mysql -u root -p
```

Puis exécuter le script SQL:
```sql
source database/schema.sql
```

Ou sous Windows:
```powershell
mysql -u root -p < database\schema.sql
```

#### 3. Configuration de l'application

Modifier `src/main/resources/db.properties` si nécessaire:
```properties
db.url=jdbc:mysql://localhost:3306/project_management?useSSL=false&serverTimezone=UTC
db.username=root
db.password=VOTRE_MOT_DE_PASSE
```

#### 4. Compiler le projet

```powershell
mvn clean package
```

Cela créera un fichier `project-manager.war` dans le dossier `target/`.

#### 5. Déployer sur Tomcat

**Méthode 1: Copie manuelle**
```powershell
copy target\project-manager.war %CATALINA_HOME%\webapps\
```

**Méthode 2: Manager Tomcat**
- Accéder à http://localhost:8080/manager
- Uploader le fichier WAR

#### 6. Démarrer Tomcat

```powershell
cd %CATALINA_HOME%\bin
startup.bat
```

#### 7. Accéder à l'application

Ouvrir un navigateur et aller à:
```
http://localhost:8080/project-manager/
```

## ⚙️ Configuration

### Configuration de la Base de Données

Le fichier `db.properties` contient les paramètres de connexion:

```properties
# Database Configuration
db.url=jdbc:mysql://localhost:3306/project_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
db.password=

# Connection Pool Settings
db.pool.maxPoolSize=20
db.pool.minIdle=5
db.pool.connectionTimeout=30000
```

### Configuration du Serveur

Dans `web.xml`, vous pouvez configurer:
- Timeout de session
- Filtres CORS
- Encodage des caractères

## 📖 Utilisation

### 1. Ajouter des Membres d'Équipe

1. Aller dans l'onglet **Team Members**
2. Cliquer sur **Add Member**
3. Remplir le formulaire:
   - Nom
   - Email
   - Disponibilité hebdomadaire (heures)
   - Sélectionner les compétences et niveaux
4. Cliquer sur **Add Member**

### 2. Créer un Projet

1. Aller dans l'onglet **Projects**
2. Cliquer sur **Create Project**
3. Remplir les informations:
   - Nom du projet
   - Description
   - Date de début
   - Date limite
4. Cliquer sur **Create Project**

### 3. Ajouter des Tâches

1. Dans la liste des projets, cliquer sur **Add Task**
2. Remplir les informations de la tâche:
   - Titre
   - Description
   - Heures estimées
   - Priorité (Low, Medium, High, Urgent)
   - Date limite
   - Compétences requises
3. Cliquer sur **Add Task**

### 4. Allocation Automatique

1. Dans un projet, cliquer sur **Auto-Allocate**
2. Le système analyse:
   - Les compétences des membres
   - Leur disponibilité
   - Leur charge actuelle
   - Les priorités des tâches
3. Les tâches sont assignées automatiquement
4. Consulter les alertes pour les problèmes détectés

### 5. Visualiser la Timeline

1. Aller dans l'onglet **Timeline**
2. Sélectionner un projet
3. La timeline affiche:
   - Les tâches par membre
   - Les périodes d'exécution
   - L'état des tâches (couleurs)

### 6. Consulter les Statistiques

1. Aller dans l'onglet **Statistics**
2. Voir:
   - Distribution de la charge de travail
   - Progression des projets
   - Membres surchargés
   - Taux d'utilisation de l'équipe

## 🧮 Algorithme d'Allocation

### Principe de l'Algorithme

L'algorithme utilise une **approche heuristique** basée sur un système de scoring multi-critères.

### Score de Correspondance

Pour chaque tâche et chaque membre, un score est calculé:

```
Score Total = (Score Compétences × 0.4) + 
              (Score Disponibilité × 0.3) + 
              (Score Charge × 0.2) + 
              (Bonus Priorité × 0.1)
```

#### 1. Score Compétences (40%)

```java
if (membre ne possède pas toutes les compétences requises) {
    return 0; // Éliminatoire
}

score = Σ(niveau_membre) / Σ(niveau_requis)
```

Vérification stricte du niveau de compétence minimum requis.

#### 2. Score Disponibilité (30%)

```java
if (heures_disponibles < heures_requises) {
    return 0; // Éliminatoire
}

ratio = heures_requises / heures_disponibles

if (ratio >= 0.5) {
    score = 1.0;  // Utilisation optimale
} else {
    score = 0.5 + ratio;  // Pénalité légère
}
```

Favorise les membres ayant juste assez de temps.

#### 3. Score Charge de Travail (20%)

```java
pourcentage_charge = charge_actuelle / disponibilité_hebdo * 100

if (pourcentage_charge >= 100) {
    return 0; // Éliminatoire
}

score = 1.0 - (pourcentage_charge / 100 * 0.9)
```

Favorise les membres moins chargés pour équilibrer.

#### 4. Bonus Priorité (10%)

```java
URGENT = 0.10
HIGH   = 0.075
MEDIUM = 0.05
LOW    = 0.025
```

Les tâches urgentes sont traitées en priorité.

### Processus d'Allocation

1. **Tri des tâches** par priorité puis par deadline
2. **Pour chaque tâche**:
   - Calculer le score pour tous les membres
   - Sélectionner le membre avec le meilleur score (> 0.3)
   - Assigner la tâche
   - Mettre à jour la charge du membre
   - Vérifier si le membre est surchargé → Créer alerte
3. **Si aucun membre qualifié** → Créer alerte critique

### Détection de Surcharge

```java
if (charge_actuelle > disponibilité_hebdo) {
    créer_alerte(type=OVERLOAD, severité=HIGH)
}
```

## 🧪 Scénarios de Test

### Scénario 1: Gestion de l'Équipe ✅

**Objectif**: Ajouter 5 membres avec compétences variées

**Étapes**:
1. Créer 5 membres avec ces profils:
   - **Alice** (40h/semaine): Java Development (5), Database Design (4)
   - **Bob** (35h/semaine): Frontend Development (5), UI/UX Design (4)
   - **Carol** (40h/semaine): Testing (5), Documentation (4)
   - **David** (30h/semaine): Java Development (4), API Development (5)
   - **Emma** (40h/semaine): UI/UX Design (5), Frontend Development (3)

2. Vérifier dans Team Members:
   - Tous les membres sont affichés
   - Compétences correctement assignées
   - Disponibilité = 0% (pas encore de tâches)

**Résultat attendu**: ✅ 5 membres créés avec compétences variées

---

### Scénario 2: Création de Projet ✅

**Objectif**: Créer un projet avec 10 tâches

**Étapes**:
1. Créer projet "Application E-Commerce"
   - Date début: 2025-11-15
   - Deadline: 2025-12-31

2. Ajouter 10 tâches avec dépendances:

   **Backend (Java Development)**
   - T1: Setup database schema (8h, High) - No deps
   - T2: Create API endpoints (16h, High) - Depends on T1
   - T3: Implement authentication (12h, Urgent) - Depends on T2

   **Frontend (Frontend Dev + Design)**
   - T4: Design mockups (10h, Medium) - No deps
   - T5: Implement homepage (15h, High) - Depends on T4
   - T6: Create product catalog (20h, High) - Depends on T5

   **Testing**
   - T7: Unit tests (12h, Medium) - Depends on T2, T3
   - T8: Integration tests (16h, Medium) - Depends on T6, T7
   - T9: User acceptance testing (8h, High) - Depends on T8

   **Documentation**
   - T10: Technical documentation (10h, Low) - Depends on all

3. Vérifier:
   - Projet créé avec status PLANNING
   - 10 tâches visibles
   - Dépendances établies

**Résultat attendu**: ✅ Projet avec 10 tâches et organisation hiérarchique

---

### Scénario 3: Répartition Automatique ✅

**Objectif**: Lancer l'algorithme et vérifier l'équilibre

**Étapes**:
1. Cliquer sur "Auto-Allocate" pour le projet
2. Attendre l'exécution de l'algorithme
3. Vérifier les assignations:
   - T1, T2, T3 → Alice ou David (Java Development)
   - T4, T5, T6 → Bob ou Emma (Frontend + Design)
   - T7, T8, T9 → Carol (Testing)
   - T10 → Carol (Documentation)

4. Vérifier l'équilibre dans Statistics:
   - Aucun membre ne devrait dépasser 100% (40h)
   - Distribution relativement équilibrée
   - Respect des compétences requises

**Résultat attendu**: 
✅ Toutes les tâches assignées
✅ Équilibre de charge respecté
✅ Compétences correspondantes

---

### Scénario 4: Détection de Surcharge ✅

**Objectif**: Tester les alertes de surcharge

**Étapes**:
1. Assigner manuellement à Alice:
   - 5 tâches de 10h chacune (total: 50h)
   - Sa disponibilité est de 40h/semaine

2. Aller dans Alerts
3. Vérifier la présence d'une alerte:
   - Type: OVERLOAD
   - Severity: HIGH
   - Message mentionnant Alice et son dépassement

4. Dans Statistics, vérifier:
   - Alice à 125% (50h/40h)
   - Barre de progression en rouge

**Résultat attendu**: 
✅ Alerte de surcharge créée
✅ Membre identifié comme overloaded

---

### Scénario 5: Modification en Cours de Projet ✅

**Objectif**: Ajouter une tâche urgente

**Étapes**:
1. Avec toutes les tâches déjà assignées
2. Ajouter nouvelle tâche:
   - Titre: "Fix critical security bug"
   - Heures: 8h
   - Priorité: URGENT
   - Compétence: Java Development (niveau 4)

3. Lancer "Auto-Allocate" à nouveau
4. Vérifier:
   - Tâche assignée à un développeur Java
   - Réaffectation intelligente si nécessaire
   - Charge mise à jour

**Résultat attendu**: 
✅ Tâche urgente assignée prioritairement
✅ Système réaffecter intelligemment

---

### Scénario 6: Visualisation ✅

**Objectif**: Afficher la timeline et les charges

**Étapes**:
1. Aller dans Timeline
2. Sélectionner "Application E-Commerce"
3. Vérifier l'affichage:
   - Timeline avec toutes les tâches
   - Tâches groupées par membre
   - Barres de couleur selon le statut:
     - Gris: TODO
     - Bleu: IN_PROGRESS
     - Vert: COMPLETED
     - Rouge: BLOCKED
   - Durées proportionnelles

4. Aller dans Statistics
5. Vérifier:
   - Graphiques de charge par membre
   - Pourcentages corrects
   - Progression du projet

**Résultat attendu**: 
✅ Timeline lisible et complète
✅ Toutes les informations présentes
✅ Statistiques exactes

---

### Scénario 7: Statistiques ✅

**Objectif**: Générer et vérifier les rapports

**Étapes**:
1. Marquer quelques tâches comme COMPLETED
2. Aller dans Dashboard
3. Vérifier:
   - Total Projects: 1
   - Total Members: 5
   - Total Tasks: 11 (10 + 1 urgente)
   - Overloaded Members: 1 (Alice du scénario 4)

4. Aller dans Statistics
5. Vérifier "Workload Balance":
   - Liste de tous les membres
   - Heures assignées correctes
   - Pourcentage correct
   - Tâches comptées

6. Vérifier "Project Progress":
   - Application E-Commerce
   - Pourcentage de complétion
   - X/11 tasks completed

7. Calculer manuellement et comparer:
   ```
   Alice: (T1:8h + T2:16h + T3:12h + T5:10h + T6:10h) = 56h / 40h = 140%
   Bob: Si assigné T4,T5 = 25h / 35h = 71%
   etc.
   ```

**Résultat attendu**: 
✅ Tous les calculs corrects
✅ Rapports complets et précis

---

## 📁 Structure du Projet

```
MiniProjetjava/
├── database/
│   └── schema.sql                      # Script de création DB
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── projectmanagement/
│   │   │           ├── dao/
│   │   │           │   ├── AlertDAO.java
│   │   │           │   ├── MemberDAO.java
│   │   │           │   ├── ProjectDAO.java
│   │   │           │   ├── SkillDAO.java
│   │   │           │   └── TaskDAO.java
│   │   │           ├── filter/
│   │   │           │   ├── CorsFilter.java
│   │   │           │   └── EncodingFilter.java
│   │   │           ├── model/
│   │   │           │   ├── Alert.java
│   │   │           │   ├── Member.java
│   │   │           │   ├── MemberSkill.java
│   │   │           │   ├── Project.java
│   │   │           │   ├── Skill.java
│   │   │           │   ├── Task.java
│   │   │           │   └── TaskSkill.java
│   │   │           ├── service/
│   │   │           │   ├── StatisticsService.java
│   │   │           │   └── TaskAllocationService.java
│   │   │           ├── servlet/
│   │   │           │   ├── AlertServlet.java
│   │   │           │   ├── AllocationServlet.java
│   │   │           │   ├── MemberServlet.java
│   │   │           │   ├── ProjectServlet.java
│   │   │           │   ├── StatisticsServlet.java
│   │   │           │   └── TaskServlet.java
│   │   │           └── util/
│   │   │               └── DatabaseUtil.java
│   │   ├── resources/
│   │   │   └── db.properties
│   │   └── webapp/
│   │       ├── css/
│   │       │   └── style.css
│   │       ├── js/
│   │       │   ├── api.js
│   │       │   ├── app.js
│   │       │   └── timeline.js
│   │       ├── WEB-INF/
│   │       │   └── web.xml
│   │       └── index.html
└── pom.xml                             # Configuration Maven
```

## 🌐 API Documentation

### Members API

**GET** `/api/members/` - Get all members
**GET** `/api/members/{id}` - Get member by ID
**POST** `/api/members/` - Create member
**PUT** `/api/members/` - Update member
**DELETE** `/api/members/{id}` - Delete member
**POST** `/api/members/{id}/skills` - Add skill to member
**DELETE** `/api/members/{id}/skills/{skillId}` - Remove skill

### Projects API

**GET** `/api/projects/` - Get all projects
**GET** `/api/projects/{id}` - Get project by ID
**GET** `/api/projects/{id}/tasks` - Get project tasks
**POST** `/api/projects/` - Create project
**PUT** `/api/projects/` - Update project
**DELETE** `/api/projects/{id}` - Delete project

### Tasks API

**GET** `/api/tasks/{id}` - Get task by ID
**POST** `/api/tasks/` - Create task
**PUT** `/api/tasks/` - Update task
**DELETE** `/api/tasks/{id}` - Delete task
**POST** `/api/tasks/{id}/skills` - Add skill requirement
**POST** `/api/tasks/{id}/dependencies` - Add dependency

### Allocation API

**POST** `/api/allocate/{projectId}` - Allocate tasks for project

### Alerts API

**GET** `/api/alerts/` - Get all alerts
**GET** `/api/alerts/?unread=true` - Get unread alerts
**GET** `/api/alerts/count` - Get unread count
**PUT** `/api/alerts/{id}/read` - Mark as read
**DELETE** `/api/alerts/{id}` - Delete alert

### Statistics API

**GET** `/api/statistics/` - Get overall statistics
**GET** `/api/statistics/workload` - Get workload statistics
**GET** `/api/statistics/project/{id}` - Get project statistics

## 👥 Contributeurs

Ce projet a été développé dans le cadre d'un mini-projet universitaire.

## 📄 Licence

Ce projet est à usage éducatif.

---

**Date**: Novembre 2025
**Version**: 1.0
