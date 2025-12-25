# RÉPARTITION DES FICHIERS FRONTEND PAR PERSONNE

## 📁 Structure Modulaire

```
src/main/webapp/js/
├── api.js              ← Personne 5 (Infrastructure)
├── core.js             ← Fonctions communes (Navigation, Dashboard, Utils)
├── members.js          ← Personne 1 (Module MEMBRES)
├── skills.js           ← Personne 2 (Module COMPÉTENCES)
├── projects.js         ← Personne 3 (Module PROJETS & TÂCHES)
├── alerts.js           ← Personne 5 (Module ALERTES)
├── statistics.js       ← Personne 4 (Module STATISTIQUES)
└── timeline.js         ← Personne 4 (Module ALLOCATION & TIMELINE)
```

---

## 👤 PERSONNE 1 : Module MEMBRES

### Fichier Frontend
**`members.js`** (279 lignes)

### Responsabilités
- ✅ CRUD complet des membres
- ✅ Gestion des disponibilités hebdomadaires
- ✅ Affichage de la charge de travail
- ✅ Association membres-compétences
- ✅ Interface d'édition/suppression

### Fonctions
```javascript
loadMembers()                    // Charger tous les membres
displayMembers(members)          // Afficher la liste des membres
showAddMemberModal()             // Modal d'ajout
addMember(event)                 // Créer un nouveau membre
showEditMemberModal(memberId)    // Modal d'édition
updateMember(event)              // Modifier un membre
deleteMember(memberId, name)     // Supprimer un membre
populateSkillsCheckboxes(id)     // Remplir les checkboxes de skills
```

### Backend associé
- `Member.java`
- `MemberSkill.java`
- `MemberDAO.java`

---

## 👤 PERSONNE 2 : Module COMPÉTENCES

### Fichier Frontend
**`skills.js`** (13 lignes)

### Responsabilités
- ✅ Chargement des compétences
- ✅ Gestion des niveaux (1-5)
- ✅ Données partagées entre modules

### Fonctions
```javascript
loadSkills()    // Charger toutes les compétences disponibles
```

### Backend associé
- `Skill.java`
- `SkillDAO.java`

### Note
Les compétences sont utilisées comme données auxiliaires par les autres modules (membres, tâches). Le module skills charge les données qui sont ensuite utilisées par `allSkills` (variable globale).

---

## 👤 PERSONNE 3 : Module PROJETS & TÂCHES

### Fichier Frontend
**`projects.js`** (374 lignes)

### Responsabilités
- ✅ CRUD projets
- ✅ CRUD tâches
- ✅ Gestion des deadlines
- ✅ Dépendances entre tâches
- ✅ Assignation manuelle des tâches
- ✅ Changement de statut (TODO → IN_PROGRESS → COMPLETED)

### Fonctions Principales
```javascript
// Projets
loadProjects()                      // Charger tous les projets
displayProjects(projects)           // Afficher les projets
showAddProjectModal()               // Modal de création
addProject(event)                   // Créer un projet
viewProjectDetails(projectId)       // Vue détaillée d'un projet
displayProjectDetails(...)          // Afficher détails + tâches

// Tâches
showAddTaskModalForProject(id)      // Modal d'ajout de tâche
addTask(event)                      // Créer une tâche
showAssignTaskModal(taskId, projId) // Modal d'assignation manuelle
assignTask(event)                   // Assigner une tâche à un membre
unassignTask(taskId, projectId)     // Retirer l'assignation
startTask(taskId, projectId)        // Démarrer une tâche (TODO → IN_PROGRESS)
completeTask(taskId, projectId)     // Terminer une tâche (IN_PROGRESS → COMPLETED)

// Allocation automatique
allocateProjectTasks(projectId)     // Lancer l'algorithme d'allocation
```

### Backend associé
- `Project.java`
- `Task.java`
- `TaskSkill.java`
- `ProjectDAO.java`
- `TaskDAO.java`

---

## 👤 PERSONNE 4 : Module ALLOCATION & STATISTIQUES

### Fichiers Frontend
- **`statistics.js`** (96 lignes)
- **`timeline.js`** (159 lignes)

### Responsabilités
- ✅ Algorithme intelligent d'allocation
- ✅ Calcul des scores (compétences, disponibilité, charge)
- ✅ Génération des statistiques
- ✅ Timeline interactive (Gantt-like)
- ✅ Visualisation des données

### Fonctions Statistics
```javascript
loadStatistics()                    // Charger toutes les stats
displayWorkloadStatistics(stats)    // Distribution de charge équipe
displayProjectStatistics(projects)  // Progression des projets
```

### Fonctions Timeline
```javascript
populateTimelineProjectSelect()     // Remplir le sélecteur de projets
loadTimeline()                      // Charger la timeline du projet
generateTimeline(tasks)             // Générer la visualisation Gantt
```

### Backend associé
- `TaskAllocationService.java` - Algorithme d'allocation
- `StatisticsService.java` - Calculs statistiques

---

## 👤 PERSONNE 5 : Module ALERTES & INFRASTRUCTURE

### Fichiers Frontend
- **`api.js`** (161 lignes) - Couche API REST
- **`alerts.js`** (87 lignes) - Gestion des alertes

### Responsabilités API (`api.js`)
- ✅ Toutes les requêtes HTTP (fetch)
- ✅ Gestion des erreurs HTTP
- ✅ Interfaces API pour tous les modules :
  - `MembersAPI` → CRUD membres
  - `SkillsAPI` → CRUD compétences
  - `ProjectsAPI` → CRUD projets
  - `TasksAPI` → CRUD tâches + assignation
  - `AlertsAPI` → Gestion alertes
  - `StatisticsAPI` → Récupération stats
  - `AllocationAPI` → Allocation automatique

### Responsabilités Alertes (`alerts.js`)
- ✅ Affichage des alertes de surcharge
- ✅ Système de notifications
- ✅ Badges d'alertes non lues
- ✅ Marquage lu/non-lu

### Fonctions Alertes
```javascript
loadAlerts()                // Charger toutes les alertes
loadAlertCount()            // Compter les alertes non lues
displayAlerts(alerts)       // Afficher la liste
markAlertRead(alertId)      // Marquer comme lue
markAllAlertsRead()         // Tout marquer comme lu
deleteAlert(alertId)        // Supprimer une alerte
```

### Backend associé
- `Alert.java`
- `AlertDAO.java`
- `DatabaseUtil.java`
- `SimpleServer.java`

---

## 🔧 Module CORE (Commun)

### Fichier
**`core.js`** (175 lignes)

### Contenu
- Variables globales (`currentPage`, `allSkills`, `currentProject`)
- Initialisation de l'application (`DOMContentLoaded`)
- Navigation entre pages (`showPage()`)
- Dashboard principal (`loadDashboard()`)
- Fonctions utilitaires (`closeModal()`, `showNotification()`)

---

## 📝 Ordre de Chargement dans index.html

```html
<!-- 1. API Layer (doit être chargé en premier) -->
<script src="js/api.js"></script>

<!-- 2. Modules métier (peuvent être chargés en parallèle) -->
<script src="js/skills.js"></script>
<script src="js/members.js"></script>
<script src="js/projects.js"></script>
<script src="js/alerts.js"></script>

<!-- 3. Modules de visualisation -->
<script src="js/statistics.js"></script>
<script src="js/timeline.js"></script>

<!-- 4. Core (doit être chargé en dernier pour l'initialisation) -->
<script src="js/core.js"></script>
```

---

## 🔄 Dépendances entre Modules

```
api.js (base)
  ↓
skills.js → members.js
          ↘         ↓
          projects.js
               ↓
          alerts.js
               ↓
          statistics.js
          timeline.js
               ↓
          core.js (initialisation)
```

---

## ✅ Avantages de cette Architecture

1. **Séparation claire** : Chaque personne a son/ses fichier(s)
2. **Travail parallèle** : Pas de conflits Git
3. **Maintenance facile** : Modifications localisées
4. **Testabilité** : Chaque module peut être testé indépendamment
5. **Réutilisabilité** : API layer utilisée par tous
6. **Scalabilité** : Facile d'ajouter de nouveaux modules

---

## 📊 Statistiques

| Personne | Fichiers | Lignes de code | Complexité |
|----------|----------|----------------|------------|
| Personne 1 | `members.js` | 279 | ⭐⭐⭐ |
| Personne 2 | `skills.js` | 13 | ⭐ |
| Personne 3 | `projects.js` | 374 | ⭐⭐⭐⭐ |
| Personne 4 | `statistics.js`, `timeline.js` | 255 | ⭐⭐⭐⭐ |
| Personne 5 | `api.js`, `alerts.js` | 248 | ⭐⭐⭐ |
| **Commun** | `core.js` | 175 | ⭐⭐ |
| **TOTAL** | 8 fichiers | **1344 lignes** | - |
