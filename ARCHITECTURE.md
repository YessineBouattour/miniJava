# Rapport d'Architecture - Plateforme de Gestion de Projets Collaboratifs

## 1. Introduction

Ce document décrit l'architecture technique de la plateforme de gestion de projets collaboratifs avec répartition automatique des tâches.

## 2. Vue d'Ensemble de l'Architecture

### 2.1 Architecture Logicielle

L'application suit une **architecture 3-tiers** classique:

1. **Couche Présentation** (Frontend)
   - Interface utilisateur HTML5/CSS3/JavaScript
   - Communication via API REST

2. **Couche Logique Métier** (Backend)
   - Servlets Java pour l'API REST
   - Services pour la logique métier complexe
   - Algorithme d'allocation intelligent

3. **Couche Données** (Persistence)
   - Base de données MySQL
   - Pattern DAO pour l'accès aux données
   - Connection pooling avec HikariCP

```
┌─────────────────────────────────────────────────────┐
│              FRONTEND (Presentation)                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐          │
│  │  HTML5   │  │   CSS3   │  │JavaScript│          │
│  │  Pages   │  │  Styles  │  │   SPA    │          │
│  └──────────┘  └──────────┘  └──────────┘          │
│         │              │              │              │
│         └──────────────┴──────────────┘              │
│                       │                              │
│              REST API (JSON/HTTP)                    │
└───────────────────────┼─────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────┐
│              BACKEND (Business Logic)                │
│  ┌──────────────────────────────────────┐           │
│  │         Servlets (Controllers)        │           │
│  │  ┌────────┐ ┌────────┐ ┌────────┐   │           │
│  │  │ Member │ │Project │ │  Task  │   │           │
│  │  └────────┘ └────────┘ └────────┘   │           │
│  └──────────────────┬───────────────────┘           │
│                     │                                │
│  ┌──────────────────▼───────────────────┐           │
│  │         Services (Business)           │           │
│  │  ┌──────────────┐ ┌──────────────┐  │           │
│  │  │  Allocation  │ │  Statistics  │  │           │
│  │  │   Service    │ │   Service    │  │           │
│  │  └──────────────┘ └──────────────┘  │           │
│  └──────────────────┬───────────────────┘           │
│                     │                                │
│  ┌──────────────────▼───────────────────┐           │
│  │          DAO Layer (Data)             │           │
│  │  ┌───────┐ ┌───────┐ ┌───────┐      │           │
│  │  │Member │ │Project│ │ Task  │      │           │
│  │  │  DAO  │ │  DAO  │ │  DAO  │      │           │
│  │  └───────┘ └───────┘ └───────┘      │           │
│  └──────────────────┬───────────────────┘           │
└───────────────────────┼─────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────┐
│            DATABASE (Persistence)                    │
│                   MySQL 8.0                          │
│  ┌────────────────────────────────────────┐         │
│  │  Tables:                                │         │
│  │  • members, skills, member_skills       │         │
│  │  • projects, tasks                      │         │
│  │  • task_skills, task_dependencies       │         │
│  │  • alerts, task_history                 │         │
│  └────────────────────────────────────────┘         │
└─────────────────────────────────────────────────────┘
```

### 2.2 Patterns de Conception Utilisés

#### 2.2.1 MVC (Model-View-Controller)
- **Model**: Classes de domaine (Member, Task, Project, etc.)
- **View**: Pages HTML et JavaScript
- **Controller**: Servlets Java

#### 2.2.2 DAO (Data Access Object)
- Abstraction de l'accès aux données
- Séparation logique métier / accès données
- Facilite les tests et la maintenance

#### 2.2.3 Service Layer
- Encapsulation de la logique métier complexe
- Réutilisation du code
- Transactions métier

#### 2.2.4 Singleton
- DatabaseUtil pour le pool de connexions
- Garantit une seule instance

## 3. Modèle de Données

### 3.1 Diagramme Entité-Association

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   Members   │       │   Skills    │       │  Projects   │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │       │ id (PK)     │       │ id (PK)     │
│ name        │       │ name        │       │ name        │
│ email       │       │ description │       │ description │
│ weekly_...  │       └─────────────┘       │ start_date  │
│ current_... │              │              │ deadline    │
└──────┬──────┘              │              │ status      │
       │                     │              └──────┬──────┘
       │     ┌───────────────┘                     │
       │     │                                     │
       │     ▼                                     ▼
       │  ┌──────────────┐                  ┌──────────┐
       │  │ member_skills│                  │  Tasks   │
       │  ├──────────────┤                  ├──────────┤
       │  │ member_id(FK)│                  │ id (PK)  │
       └─►│ skill_id (FK)│           ┌─────►│ project_id
          │ proficiency  │           │      │ title    │
          └──────────────┘           │      │ description
                 │                   │      │ estimated_h
                 │                   │      │ priority │
                 └───────┐           │      │ status   │
                         ▼           │      │ deadline │
                   ┌────────────┐    │      │ assigned_m
                   │task_skills │    │      └─────┬────┘
                   ├────────────┤    │            │
                   │ task_id(FK)│────┘            │
                   │ skill_id(FK)                 │
                   │ required_l │                 │
                   └────────────┘                 │
                         │                        │
                         └────────────┬───────────┘
                                      ▼
                              ┌──────────────────┐
                              │task_dependencies │
                              ├──────────────────┤
                              │ task_id (FK)     │
                              │ depends_on_id(FK)│
                              └──────────────────┘

┌─────────────┐
│   Alerts    │
├─────────────┤
│ id (PK)     │
│ type        │
│ severity    │
│ title       │
│ message     │
│ member_id(FK)
│ project_id(FK)
│ task_id(FK) │
│ is_read     │
└─────────────┘
```

### 3.2 Tables Principales

#### members
Stocke les informations des membres d'équipe.

#### skills
Catalogue des compétences disponibles.

#### member_skills
Association many-to-many entre membres et compétences avec niveau.

#### projects
Définition des projets avec dates et statuts.

#### tasks
Tâches du projet avec estimations et affectations.

#### task_skills
Compétences requises pour chaque tâche.

#### task_dependencies
Dépendances entre tâches pour l'ordonnancement.

#### alerts
Notifications et alertes système.

## 4. Algorithme d'Allocation des Tâches

### 4.1 Principe Général

L'algorithme utilise une **approche heuristique multi-critères** pour assigner optimalement les tâches aux membres.

### 4.2 Score de Correspondance

Pour chaque paire (tâche, membre), un score est calculé:

```
Score Total = ω₁ × Score_Compétences + 
              ω₂ × Score_Disponibilité + 
              ω₃ × Score_Charge + 
              ω₄ × Bonus_Priorité

où: ω₁=0.4, ω₂=0.3, ω₃=0.2, ω₄=0.1
```

### 4.3 Composantes du Score

#### 4.3.1 Score de Compétences (40%)

```java
ScoreCompétences(tâche, membre):
    compétences_requises = tâche.requiredSkills
    
    POUR CHAQUE compétence IN compétences_requises:
        SI membre n'a pas cette compétence:
            RETOURNER 0  // Éliminatoire
        
        SI niveau_membre < niveau_requis:
            RETOURNER 0  // Éliminatoire
    
    // Toutes les compétences sont présentes
    total_niveau = SOMME(niveau_membre pour chaque compétence)
    max_niveau = SOMME(niveau_requis pour chaque compétence)
    
    RETOURNER min(1.0, total_niveau / max_niveau)
```

**Justification**: Le respect strict des compétences garantit la qualité.

#### 4.3.2 Score de Disponibilité (30%)

```java
ScoreDisponibilité(tâche, membre):
    heures_disponibles = membre.weeklyAvailability - membre.currentWorkload
    heures_requises = tâche.estimatedHours
    
    SI heures_disponibles < heures_requises:
        RETOURNER 0  // Éliminatoire
    
    ratio = heures_requises / heures_disponibles
    
    SI ratio >= 0.5:
        RETOURNER 1.0  // Utilisation optimale
    SINON:
        RETOURNER 0.5 + ratio  // Pénalité légère
```

**Justification**: Favorise l'utilisation efficace du temps disponible.

#### 4.3.3 Score de Charge (20%)

```java
ScoreCharge(membre):
    pourcentage = (membre.currentWorkload / membre.weeklyAvailability) × 100
    
    SI pourcentage >= 100:
        RETOURNER 0  // Éliminatoire
    
    // Score décroissant linéaire
    RETOURNER 1.0 - (pourcentage / 100 × 0.9)
```

**Justification**: Équilibre la charge de travail entre membres.

#### 4.3.4 Bonus de Priorité (10%)

```java
BonusPriorité(tâche):
    SELON tâche.priority:
        URGENT: RETOURNER 0.10
        HIGH:   RETOURNER 0.075
        MEDIUM: RETOURNER 0.05
        LOW:    RETOURNER 0.025
```

**Justification**: Les tâches urgentes sont traitées en priorité.

### 4.4 Processus d'Allocation

```
ALGORITHME AllouerTâches(projet):
    tâches = ObtenirTâchesNonAssignées(projet)
    membres = ObtenirMembresDispo()
    
    // 1. Trier les tâches par priorité puis deadline
    tâches = Trier(tâches, PAR priorité DESC, deadline ASC)
    
    // 2. Pour chaque tâche
    POUR CHAQUE tâche IN tâches:
        meilleur_score = -1
        meilleur_membre = NULL
        
        // 3. Calculer le score pour chaque membre
        POUR CHAQUE membre IN membres:
            score = CalculerScore(tâche, membre)
            
            SI score > meilleur_score ET score >= SEUIL_MIN:
                meilleur_score = score
                meilleur_membre = membre
        
        // 4. Assigner si un membre qualifié trouvé
        SI meilleur_membre != NULL:
            AssignerTâche(tâche, meilleur_membre)
            
            // Mettre à jour la charge
            meilleur_membre.currentWorkload += tâche.estimatedHours
            MettreÀJourCharge(meilleur_membre)
            
            // Vérifier surcharge
            SI meilleur_membre.currentWorkload > meilleur_membre.weeklyAvailability:
                CréerAlerte(type=OVERLOAD, membre=meilleur_membre)
            
            compteur_assignées++
        SINON:
            CréerAlerte(type=CONFLICT, tâche=tâche)
            compteur_échecs++
    
    RETOURNER RésultatAllocation(compteur_assignées, compteur_échecs)
```

### 4.5 Complexité Algorithmique

- **Temps**: O(n × m) où n = nombre de tâches, m = nombre de membres
- **Espace**: O(n + m)

Pour 100 tâches et 10 membres: ~1000 opérations ≈ < 1 seconde

### 4.6 Optimisations Possibles

1. **Pré-filtrage**: Filtrer les membres par compétences avant calcul de score
2. **Cache**: Mémoriser les scores pour tâches similaires
3. **Parallélisation**: Calculer les scores en parallèle
4. **Algorithme génétique**: Pour des projets très complexes (> 1000 tâches)

## 5. API REST

### 5.1 Design de l'API

L'API suit les principes **RESTful**:
- Utilisation des verbes HTTP (GET, POST, PUT, DELETE)
- Ressources identifiées par URLs
- Format JSON pour les données
- Codes de statut HTTP appropriés

### 5.2 Structure des Endpoints

```
/api
├── /members
│   ├── GET    /              (Liste tous)
│   ├── GET    /{id}          (Un membre)
│   ├── POST   /              (Créer)
│   ├── PUT    /              (Modifier)
│   ├── DELETE /{id}          (Supprimer)
│   └── /skills
│       ├── POST   /{id}/skills       (Ajouter compétence)
│       └── DELETE /{id}/skills/{sid} (Retirer compétence)
│
├── /projects
│   ├── GET    /              (Liste tous)
│   ├── GET    /{id}          (Un projet)
│   ├── GET    /{id}/tasks    (Tâches du projet)
│   ├── POST   /              (Créer)
│   ├── PUT    /              (Modifier)
│   └── DELETE /{id}          (Supprimer)
│
├── /tasks
│   ├── GET    /{id}          (Une tâche)
│   ├── POST   /              (Créer)
│   ├── PUT    /              (Modifier)
│   ├── DELETE /{id}          (Supprimer)
│   ├── POST   /{id}/skills   (Ajouter compétence)
│   └── POST   /{id}/dependencies (Ajouter dépendance)
│
├── /allocate
│   └── POST   /{projectId}   (Lancer allocation)
│
├── /alerts
│   ├── GET    /              (Liste toutes)
│   ├── GET    /?unread=true  (Non lues)
│   ├── GET    /count         (Nombre non lues)
│   ├── PUT    /{id}/read     (Marquer lue)
│   ├── PUT    /read-all      (Marquer toutes lues)
│   └── DELETE /{id}          (Supprimer)
│
├── /skills
│   └── GET    /              (Liste toutes)
│
└── /statistics
    ├── GET    /              (Stats globales)
    ├── GET    /workload      (Charge de travail)
    └── GET    /project/{id}  (Stats projet)
```

### 5.3 Format des Réponses

**Succès**:
```json
{
  "id": 1,
  "name": "Alice Johnson",
  "email": "alice@example.com",
  "weeklyAvailability": 40,
  "currentWorkload": 25.5,
  "skills": [
    {
      "skillId": 1,
      "skillName": "Java Development",
      "proficiencyLevel": 5
    }
  ]
}
```

**Erreur**:
```json
{
  "success": false,
  "error": "Member not found"
}
```

## 6. Frontend - Architecture SPA

### 6.1 Structure

Application **Single Page Application** (SPA) sans framework:
- Vanilla JavaScript (ES6+)
- Navigation dynamique
- Chargement asynchrone des données

### 6.2 Modules JavaScript

```
app.js
├── Page Navigation
│   └── showPage(pageName)
│
├── Member Management
│   ├── loadMembers()
│   ├── displayMembers()
│   └── addMember()
│
├── Project Management
│   ├── loadProjects()
│   ├── displayProjects()
│   ├── viewProjectDetails()
│   └── addProject()
│
├── Task Management
│   ├── addTask()
│   └── allocateProjectTasks()
│
├── Alerts Management
│   ├── loadAlerts()
│   ├── loadAlertCount()
│   └── markAlertRead()
│
└── Statistics
    └── loadStatistics()

api.js
├── MembersAPI
├── ProjectsAPI
├── TasksAPI
├── AlertsAPI
├── AllocationAPI
└── StatisticsAPI

timeline.js
├── TimelineVisualizer
│   ├── setData()
│   ├── render()
│   └── renderMemberTimeline()
└── loadTimeline()
```

### 6.3 Gestion de l'État

```javascript
// État global minimal
let currentPage = 'dashboard';
let allSkills = [];
let currentProject = null;

// Rechargement automatique
setInterval(loadAlertCount, 30000); // Toutes les 30s
```

## 7. Sécurité

### 7.1 Mesures Implémentées

1. **SQL Injection**: PreparedStatements
2. **XSS**: Pas de `innerHTML` avec données utilisateur
3. **CORS**: Filtre configuré
4. **Connection Pooling**: Limite les connexions
5. **Validation**: Côté backend (servlets)

### 7.2 Améliorations Futures

- Authentification JWT
- Autorisation basée sur rôles
- HTTPS obligatoire
- Rate limiting sur API
- CSRF protection

## 8. Performance

### 8.1 Optimisations

1. **Connection Pooling** (HikariCP):
   - Max 20 connexions
   - Min 5 idle
   - Réutilisation des connexions

2. **Requêtes SQL**:
   - Index sur clés étrangères
   - JOINs optimisés
   - Pas de N+1 queries

3. **Frontend**:
   - Chargement asynchrone
   - Mise en cache des compétences
   - Minimisation des requêtes API

### 8.2 Métriques Estimées

- Allocation de 100 tâches: < 1 seconde
- Chargement page dashboard: < 500ms
- Requête API moyenne: < 100ms

## 9. Tests et Qualité

### 9.1 Stratégie de Test

- **Tests manuels**: 7 scénarios complets
- **Tests d'intégration**: Via interface web
- **Tests de charge**: Jusqu'à 1000 tâches

### 9.2 Scénarios Couverts

1. ✅ Gestion équipe (5 membres)
2. ✅ Création projet (10 tâches)
3. ✅ Allocation automatique
4. ✅ Détection surcharge
5. ✅ Modification en cours
6. ✅ Visualisation timeline
7. ✅ Statistiques et rapports

## 10. Déploiement

### 10.1 Configuration Requise

**Serveur**:
- CPU: 2 cores minimum
- RAM: 2 GB minimum
- Disque: 500 MB
- Java 11+
- Tomcat 9.x

**Base de Données**:
- MySQL 8.0+
- 100 MB espace
- max_connections >= 50

### 10.2 Procédure de Déploiement

1. Créer la base de données
2. Configurer `db.properties`
3. Compiler: `mvn clean package`
4. Déployer le WAR sur Tomcat
5. Vérifier les logs
6. Tester l'application

## 11. Maintenance et Évolutions

### 11.1 Maintenance

- **Logs**: SLF4J pour traçabilité
- **Monitoring**: Logs Tomcat
- **Backup DB**: Script de sauvegarde régulier

### 11.2 Évolutions Possibles

1. **Authentification**: Login/logout
2. **Notifications**: Email/push
3. **Export**: PDF/Excel des rapports
4. **Diagramme de Gantt**: Timeline avancée
5. **API mobile**: App iOS/Android
6. **Machine Learning**: Prédiction de durées
7. **Collaboration**: Chat, commentaires
8. **Versionning**: Historique des changements

## 12. Conclusion

Cette architecture offre:
- ✅ **Scalabilité**: Supporte des milliers de tâches
- ✅ **Maintenabilité**: Code modulaire et documenté
- ✅ **Extensibilité**: Facile d'ajouter des fonctionnalités
- ✅ **Performance**: Réponse rapide et optimisée
- ✅ **Fiabilité**: Gestion d'erreurs robuste

L'utilisation de patterns éprouvés (MVC, DAO, Service Layer) et de technologies standards (Java, MySQL, REST) garantit la pérennité du système.

---

**Auteurs**: Équipe de développement  
**Date**: Novembre 2025  
**Version**: 1.0
