// Global state
let currentPage = 'dashboard';
let allSkills = [];
let currentProject = null;

// Initialize app
document.addEventListener('DOMContentLoaded', async () => {
    await loadSkills();
    await loadDashboard();
    await loadAlertCount();
    setInterval(loadAlertCount, 30000); // Refresh alerts every 30 seconds
});

// Page Navigation
function showPage(pageName) {
    // Hide all pages
    document.querySelectorAll('.page').forEach(page => {
        page.classList.remove('active');
    });
    
    // Show selected page
    document.getElementById(pageName).classList.add('active');
    
    // Update nav menu
    document.querySelectorAll('.nav-menu a').forEach(link => {
        link.classList.remove('active');
    });
    event.target.classList.add('active');
    
    currentPage = pageName;
    
    // Load page data
    switch(pageName) {
        case 'dashboard':
            loadDashboard();
            break;
        case 'members':
            loadMembers();
            break;
        case 'projects':
            loadProjects();
            break;
        case 'timeline':
            populateTimelineProjectSelect();
            break;
        case 'statistics':
            loadStatistics();
            break;
        case 'alerts':
            loadAlerts();
            break;
    }
}

// Load Skills
async function loadSkills() {
    try {
        allSkills = await SkillsAPI.getAll();
    } catch (error) {
        console.error('Error loading skills:', error);
    }
}

// Dashboard Functions
async function loadDashboard() {
    try {
        const stats = await StatisticsAPI.getOverall();
        const workloadStats = await StatisticsAPI.getWorkload();
        const projects = await ProjectsAPI.getAll();
        
        // Update stats cards
        document.getElementById('totalProjects').textContent = stats.totalProjects || 0;
        document.getElementById('totalMembers').textContent = stats.totalMembers || 0;
        document.getElementById('totalTasks').textContent = stats.totalTasks || 0;
        document.getElementById('overloadedMembers').textContent = workloadStats.overloadedMembers || 0;
        
        // Load tasks for each project
        const projectsWithTasks = await Promise.all(
            projects.slice(0, 5).map(async (project) => {
                try {
                    const tasks = await ProjectsAPI.getTasks(project.id);
                    return { ...project, tasks };
                } catch (error) {
                    console.error(`Error loading tasks for project ${project.id}:`, error);
                    return { ...project, tasks: [] };
                }
            })
        );
        
        // Display recent projects
        displayRecentProjects(projectsWithTasks);
        
        // Display workload overview
        displayWorkloadOverview(workloadStats.memberWorkloads || []);
    } catch (error) {
        console.error('Error loading dashboard:', error);
    }
}

function displayRecentProjects(projects) {
    const container = document.getElementById('recentProjects');
    
    if (projects.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>No projects yet</p></div>';
        return;
    }
    
    let html = '<div class="task-list">';
    projects.forEach(project => {
        const tasks = project.tasks || [];
        const taskCount = tasks.length;
        const completedCount = tasks.filter(t => t.status === 'COMPLETED').length;
        const completion = taskCount > 0 ? (completedCount / taskCount * 100) : 0;
        
        html += `
            <div class="task-item project-clickable" onclick="viewProjectDetails(${project.id})" title="Click to view project details">
                <div class="task-info">
                    <h4>${project.name} <i class="fas fa-arrow-right" style="font-size: 0.8rem; color: #999; margin-left: 0.5rem;"></i></h4>
                    <p>${taskCount} task${taskCount !== 1 ? 's' : ''} • ${completion.toFixed(0)}% complete</p>
                </div>
                <span class="status-badge ${project.status.toLowerCase().replace('_', '-')}">${project.status}</span>
            </div>
        `;
    });
    html += '</div>';
    
    container.innerHTML = html;
}

function displayWorkloadOverview(memberWorkloads) {
    const container = document.getElementById('workloadOverview');
    
    if (memberWorkloads.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>No team members yet</p></div>';
        return;
    }
    
    let html = '';
    // Afficher TOUS les membres (pas seulement les 5 premiers)
    memberWorkloads.forEach(member => {
        let percentage = member.workloadPercentage || 0;
        if (percentage === 0 && member.weeklyAvailability > 0) {
            percentage = (member.currentWorkload / member.weeklyAvailability) * 100;
        }
        
        let progressClass = '';
        if (percentage > 100) {
            progressClass = 'danger';
        } else if (percentage >= 90) {
            progressClass = 'warning';
        } else if (percentage >= 75) {
            progressClass = 'high';
        } else if (percentage >= 50) {
            progressClass = 'normal';
        } else {
            progressClass = 'low';
        }
        
        html += `
            <div class="workload-item">
                <div class="workload-header">
                    <span>${member.name}</span>
                    <span class="workload-percentage">${member.currentWorkload.toFixed(1)}h / ${member.weeklyAvailability}h (${percentage.toFixed(0)}%)</span>
                </div>
                <div class="progress-bar">
                    <div class="progress-fill ${progressClass}" style="width: ${Math.min(percentage, 100)}%"></div>
                </div>
            </div>
        `;
    });
    
    container.innerHTML = html;
}

// Members Functions
async function loadMembers() {
    try {
        const members = await MembersAPI.getAll();
        displayMembers(members);
    } catch (error) {
        console.error('Error loading members:', error);
        showNotification('Error loading members', 'error');
    }
}

function displayMembers(members) {
    const container = document.getElementById('membersList');
    
    if (members.length === 0) {
        container.innerHTML = '<div class="empty-state"><i class="fas fa-users"></i><p>No team members yet. Add your first member!</p></div>';
        return;
    }
    
    let html = '';
    members.forEach(member => {
        // Calculer le pourcentage si non fourni par l'API
        let workloadPercentage = member.workloadPercentage || 0;
        if (workloadPercentage === 0 && member.weeklyAvailability > 0) {
            workloadPercentage = (member.currentWorkload / member.weeklyAvailability) * 100;
        }
        
        let progressClass = '';
        if (workloadPercentage > 100) {
            progressClass = 'danger'; // Rouge - Surchargé
        } else if (workloadPercentage >= 90) {
            progressClass = 'warning'; // Orange - Presque surchargé
        } else if (workloadPercentage >= 75) {
            progressClass = 'high'; // Jaune - Charge élevée
        } else if (workloadPercentage >= 50) {
            progressClass = 'normal'; // Vert - Charge normale
        } else {
            progressClass = 'low'; // Bleu - Charge légère
        }
        const initials = member.name.split(' ').map(n => n[0]).join('').toUpperCase();
        
        html += `
            <div class="member-card">
                <div class="member-header">
                    <div class="member-avatar">${initials}</div>
                    <div class="member-info">
                        <h3>${member.name}</h3>
                        <p>${member.email}</p>
                    </div>
                </div>
                <div class="member-stats">
                    <div class="stat-row">
                        <span>Workload:</span>
                        <span>${member.currentWorkload.toFixed(1)}h / ${member.weeklyAvailability}h (${workloadPercentage.toFixed(0)}%)</span>
                    </div>
                    <div class="progress-bar">
                        <div class="progress-fill ${progressClass}" style="width: ${Math.min(workloadPercentage, 100)}%"></div>
                    </div>
                </div>
                <div class="skills-tags">
                    ${member.skills.map(skill => `<span class="skill-tag">${skill.skill ? skill.skill.name : 'Unknown'} (${skill.proficiencyLevel})</span>`).join('')}
                </div>
                <div class="member-actions">
                    <button class="btn btn-secondary" onclick="showEditMemberModal(${member.id})">
                        <i class="fas fa-edit"></i> Edit
                    </button>
                    <button class="btn btn-danger" onclick="deleteMember(${member.id}, '${member.name.replace(/'/g, "\\'")}')">                        <i class="fas fa-trash"></i> Delete
                    </button>
                </div>
            </div>
        `;
    });
    
    container.innerHTML = html;
}

function showAddMemberModal() {
    populateSkillsCheckboxes('skillsCheckboxes');
    document.getElementById('addMemberModal').classList.add('active');
}

function populateSkillsCheckboxes(containerId) {
    const container = document.getElementById(containerId);
    let html = '';
    
    allSkills.forEach(skill => {
        html += `
            <label>
                <input type="checkbox" name="skill_${skill.id}" value="${skill.id}">
                ${skill.name}
                <select name="level_${skill.id}" style="width: 60px; margin-left: 5px;">
                    <option value="1">1</option>
                    <option value="2">2</option>
                    <option value="3" selected>3</option>
                    <option value="4">4</option>
                    <option value="5">5</option>
                </select>
            </label>
        `;
    });
    
    container.innerHTML = html;
}

async function addMember(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    
    const member = {
        name: formData.get('name'),
        email: formData.get('email'),
        weeklyAvailability: parseInt(formData.get('weeklyAvailability')),
        currentWorkload: 0
    };
    
    try {
        const result = await MembersAPI.create(member);
        const memberId = result.id;
        
        // Add skills
        for (const skill of allSkills) {
            const checkbox = form.querySelector(`input[name="skill_${skill.id}"]`);
            if (checkbox && checkbox.checked) {
                const level = parseInt(form.querySelector(`select[name="level_${skill.id}"]`).value);
                await MembersAPI.addSkill(memberId, skill.id, level);
            }
        }
        
        closeModal('addMemberModal');
        form.reset();
        showNotification('Member added successfully!', 'success');
        loadMembers();
        
        // Rafraîchir aussi le dashboard si on y est
        if (currentPage === 'dashboard') {
            loadDashboard();
        }
    } catch (error) {
        console.error('Error adding member:', error);
        showNotification('Error adding member: ' + error.message, 'error');
    }
}

async function showEditMemberModal(memberId) {
    try {
        // Get all members and find the one we need
        const members = await MembersAPI.getAll();
        const member = members.find(m => m.id === memberId);
        
        if (!member) {
            throw new Error('Member not found');
        }
        
        document.getElementById('editMemberId').value = member.id;
        document.getElementById('editMemberName').value = member.name;
        document.getElementById('editMemberEmail').value = member.email;
        document.getElementById('editMemberAvailability').value = member.weeklyAvailability;
        
        // Populate skills with current member skills selected
        const container = document.getElementById('editSkillsCheckboxes');
        let html = '';
        
        allSkills.forEach(skill => {
            const memberSkill = member.skills.find(ms => ms.skill && ms.skill.id === skill.id);
            const checked = memberSkill ? 'checked' : '';
            const level = memberSkill ? memberSkill.proficiencyLevel : 3;
            
            html += `
                <label>
                    <input type="checkbox" name="skill_${skill.id}" value="${skill.id}" ${checked}>
                    ${skill.name}
                    <select name="level_${skill.id}" style="width: 60px; margin-left: 5px;">
                        <option value="1" ${level === 1 ? 'selected' : ''}>1</option>
                        <option value="2" ${level === 2 ? 'selected' : ''}>2</option>
                        <option value="3" ${level === 3 ? 'selected' : ''}>3</option>
                        <option value="4" ${level === 4 ? 'selected' : ''}>4</option>
                        <option value="5" ${level === 5 ? 'selected' : ''}>5</option>
                    </select>
                </label>
            `;
        });
        
        container.innerHTML = html;
        document.getElementById('editMemberModal').classList.add('active');
    } catch (error) {
        console.error('Error loading member:', error);
        showNotification('Error loading member details', 'error');
    }
}

async function updateMember(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    const memberId = parseInt(formData.get('memberId'));
    
    const member = {
        id: memberId,
        name: formData.get('name'),
        email: formData.get('email'),
        weeklyAvailability: parseInt(formData.get('weeklyAvailability'))
    };
    
    try {
        await MembersAPI.update(member);
        
        // Update skills - first remove all, then add selected ones
        const members = await MembersAPI.getAll();
        const currentMember = members.find(m => m.id === memberId);
        
        if (currentMember) {
            for (const ms of currentMember.skills) {
                await MembersAPI.removeSkill(memberId, ms.skill.id);
            }
        }
        
        // Add selected skills
        for (const skill of allSkills) {
            const checkbox = form.querySelector(`input[name="skill_${skill.id}"]`);
            if (checkbox && checkbox.checked) {
                const level = parseInt(form.querySelector(`select[name="level_${skill.id}"]`).value);
                await MembersAPI.addSkill(memberId, skill.id, level);
            }
        }
        
        closeModal('editMemberModal');
        showNotification('Member updated successfully!', 'success');
        loadMembers();
        
        if (currentPage === 'dashboard') {
            loadDashboard();
        }
    } catch (error) {
        console.error('Error updating member:', error);
        showNotification('Error updating member: ' + error.message, 'error');
    }
}

async function deleteMember(memberId, memberName) {
    if (!confirm(`Are you sure you want to delete ${memberName}? This action cannot be undone.`)) {
        return;
    }
    
    try {
        await MembersAPI.delete(memberId);
        showNotification('Member deleted successfully!', 'success');
        loadMembers();
        
        if (currentPage === 'dashboard') {
            loadDashboard();
        }
    } catch (error) {
        console.error('Error deleting member:', error);
        showNotification('Error deleting member: ' + error.message, 'error');
    }
}

// Projects Functions
async function loadProjects() {
    try {
        const projects = await ProjectsAPI.getAll();
        
        // Load tasks for each project
        const projectsWithTasks = await Promise.all(
            projects.map(async (project) => {
                try {
                    const tasks = await ProjectsAPI.getTasks(project.id);
                    return { ...project, tasks };
                } catch (error) {
                    console.error(`Error loading tasks for project ${project.id}:`, error);
                    return { ...project, tasks: [] };
                }
            })
        );
        
        displayProjects(projectsWithTasks);
    } catch (error) {
        console.error('Error loading projects:', error);
        showNotification('Error loading projects', 'error');
    }
}

function displayProjects(projects) {
    const container = document.getElementById('projectsList');
    
    if (projects.length === 0) {
        container.innerHTML = '<div class="empty-state"><i class="fas fa-folder"></i><p>No projects yet. Create your first project!</p></div>';
        return;
    }
    
    let html = '';
    projects.forEach(project => {
        const tasks = project.tasks || [];
        const taskCount = tasks.length;
        const completedTasks = tasks.filter(t => t.status === 'COMPLETED').length;
        const completion = taskCount > 0 ? (completedTasks / taskCount * 100) : 0;
        
        html += `
            <div class="project-card" onclick="viewProjectDetails(${project.id})">
                <div class="project-header">
                    <h3>${project.name}</h3>
                    <span class="status-badge ${project.status.toLowerCase().replace('_', '-')}">${project.status}</span>
                </div>
                <p>${project.description || 'No description'}</p>
                <div class="project-meta">
                    <span><i class="fas fa-calendar"></i> ${project.deadline}</span>
                    <span><i class="fas fa-tasks"></i> ${taskCount} task${taskCount !== 1 ? 's' : ''}</span>
                </div>
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${completion}%"></div>
                </div>
                <div class="project-actions" onclick="event.stopPropagation()">
                    <button class="btn btn-sm btn-primary" onclick="showAddTaskModalForProject(${project.id})">
                        <i class="fas fa-plus"></i> Add Task
                    </button>
                    <button class="btn btn-sm btn-secondary" onclick="allocateProjectTasks(${project.id})">
                        <i class="fas fa-magic"></i> Auto-Allocate
                    </button>
                </div>
            </div>
        `;
    });
    
    container.innerHTML = html;
}

function showAddProjectModal() {
    document.getElementById('addProjectModal').classList.add('active');
}

async function addProject(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    
    const project = {
        name: formData.get('name'),
        description: formData.get('description'),
        startDate: formData.get('startDate'),
        deadline: formData.get('deadline'),
        status: 'PLANNING'
    };
    
    try {
        await ProjectsAPI.create(project);
        closeModal('addProjectModal');
        form.reset();
        showNotification('Project created successfully!', 'success');
        loadProjects();
    } catch (error) {
        console.error('Error creating project:', error);
        showNotification('Error creating project: ' + error.message, 'error');
    }
}

async function viewProjectDetails(projectId) {
    try {
        const project = await ProjectsAPI.getById(projectId);
        const tasks = await ProjectsAPI.getTasks(projectId);
        const stats = await StatisticsAPI.getProject(projectId);
        
        displayProjectDetails(project, tasks, stats);
        document.getElementById('projectDetailsModal').classList.add('active');
    } catch (error) {
        console.error('Error loading project details:', error);
        showNotification('Error loading project details', 'error');
    }
}

function displayProjectDetails(project, tasks, stats) {
    const container = document.getElementById('projectDetailsContent');
    
    let html = `
        <h2>${project.name}</h2>
        <p>${project.description || 'No description'}</p>
        
        <div class="project-meta">
            <span><i class="fas fa-calendar-start"></i> Start: ${project.startDate}</span>
            <span><i class="fas fa-calendar-day"></i> Deadline: ${project.deadline}</span>
            <span class="status-badge ${project.status.toLowerCase().replace('_', '-')}">${project.status}</span>
        </div>
        
        <div class="stats-grid" style="margin-top: 1rem;">
            <div class="stat-card">
                <div class="stat-content">
                    <h3>${stats.totalTasks || 0}</h3>
                    <p>Total Tasks</p>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-content">
                    <h3>${stats.completedTasks || 0}</h3>
                    <p>Completed</p>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-content">
                    <h3>${(stats.completionPercentage || 0).toFixed(1)}%</h3>
                    <p>Progress</p>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-content">
                    <h3>${(stats.totalEstimatedHours || 0).toFixed(1)}h</h3>
                    <p>Total Hours</p>
                </div>
            </div>
        </div>
        
        <h3 style="margin-top: 2rem;">Tasks</h3>
        <div class="task-list">
    `;
    
    if (tasks.length === 0) {
        html += '<p>No tasks in this project yet.</p>';
    } else {
        tasks.forEach(task => {
            // Boutons de changement de statut
            let statusButtons = '';
            if (task.status === 'TODO' && task.assignedMember && task.assignedMember.id) {
                statusButtons = `<button class="btn btn-small btn-primary" onclick="startTask(${task.id}, ${project.id})" title="Commencer la tâche">
                    <i class="fas fa-play"></i> Commencer
                </button>`;
            } else if (task.status === 'IN_PROGRESS') {
                statusButtons = `<button class="btn btn-small btn-success" onclick="completeTask(${task.id}, ${project.id})" title="Marquer comme terminée">
                    <i class="fas fa-check"></i> Terminer
                </button>`;
            } else if (task.status === 'COMPLETED') {
                statusButtons = `<span class="task-completed-badge">
                    <i class="fas fa-check-circle"></i> Terminée
                </span>`;
            }
            
            // Bouton d'assignation manuelle (uniquement si TODO et pas assignée)
            let assignButton = '';
            if (task.status === 'TODO' && (!task.assignedMember || !task.assignedMember.id)) {
                assignButton = `<button class="btn btn-small btn-secondary" onclick="showAssignTaskModal(${task.id}, ${project.id})" title="Assigner manuellement">
                    <i class="fas fa-user-plus"></i> Assigner
                </button>`;
            }
            
            // Bouton de retrait d'assignation (uniquement si TODO et assignée)
            let unassignButton = '';
            if (task.status === 'TODO' && task.assignedMember && task.assignedMember.id) {
                unassignButton = `<button class="btn btn-small btn-warning" onclick="unassignTask(${task.id}, ${project.id})" title="Retirer l'assignation">
                    <i class="fas fa-user-minus"></i> Retirer
                </button>`;
            }
            
            html += `
                <div class="task-item">
                    <div class="task-info">
                        <h4>${task.title}</h4>
                        <p>
                            ${task.estimatedHours}h • 
                            ${(task.assignedMember && task.assignedMember.name) || 'Unassigned'} • 
                            <span class="priority-badge ${task.priority}">${task.priority}</span> • 
                            <span class="status-badge ${task.status.toLowerCase().replace('_', '-')}">${task.status}</span>
                        </p>
                    </div>
                    <div class="task-actions">
                        ${assignButton}
                        ${unassignButton}
                        ${statusButtons}
                    </div>
                </div>
            `;
        });
    }
    
    html += '</div>';
    container.innerHTML = html;
}

function showAddTaskModalForProject(projectId) {
    currentProject = projectId;
    document.getElementById('taskProjectId').value = projectId;
    populateSkillsCheckboxes('taskSkillsCheckboxes');
    document.getElementById('addTaskModal').classList.add('active');
}

async function addTask(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    
    const task = {
        projectId: parseInt(formData.get('projectId')),
        title: formData.get('title'),
        description: formData.get('description'),
        estimatedHours: parseFloat(formData.get('estimatedHours')),
        priority: formData.get('priority'),
        deadline: formData.get('deadline') || null,
        requiredSkills: []
    };
    
    // Collect required skills
    for (const skill of allSkills) {
        const checkbox = form.querySelector(`input[name="skill_${skill.id}"]`);
        if (checkbox && checkbox.checked) {
            const level = parseInt(form.querySelector(`select[name="level_${skill.id}"]`).value);
            task.requiredSkills.push({
                skillId: skill.id,
                requiredLevel: level
            });
        }
    }
    
    try {
        await TasksAPI.create(task);
        closeModal('addTaskModal');
        form.reset();
        showNotification('Task added successfully!', 'success');
        loadProjects();
    } catch (error) {
        console.error('Error adding task:', error);
        showNotification('Error adding task: ' + error.message, 'error');
    }
}

async function allocateProjectTasks(projectId) {
    if (!confirm('This will automatically assign all unassigned tasks in this project. Continue?')) {
        return;
    }
    
    try {
        showNotification('Allocating tasks...', 'info');
        const result = await AllocationAPI.allocateTasks(projectId);
        
        if (result.success) {
            showNotification(`Successfully assigned ${result.assignedCount} tasks!`, 'success');
        } else {
            showNotification(`Assigned ${result.assignedCount} tasks, ${result.failedCount} failed.`, 'warning');
        }
        
        // Rafraîchir toutes les vues concernées
        loadProjects();
        loadAlerts();
        
        // Rafraîchir le dashboard si on y est
        if (currentPage === 'dashboard') {
            loadDashboard();
        }
        
        // Rafraîchir les membres si on est sur cette page
        if (currentPage === 'members') {
            loadMembers();
        }
    } catch (error) {
        console.error('Error allocating tasks:', error);
        showNotification('Error allocating tasks: ' + error.message, 'error');
    }
}

// Alerts Functions
async function loadAlerts() {
    try {
        const alerts = await AlertsAPI.getAll();
        displayAlerts(alerts);
    } catch (error) {
        console.error('Error loading alerts:', error);
    }
}

async function loadAlertCount() {
    try {
        const result = await AlertsAPI.getUnreadCount();
        const badge = document.getElementById('alertBadge');
        badge.textContent = result.count;
        badge.style.display = result.count > 0 ? 'inline-block' : 'none';
    } catch (error) {
        console.error('Error loading alert count:', error);
    }
}

function displayAlerts(alerts) {
    const container = document.getElementById('alertsList');
    
    if (alerts.length === 0) {
        container.innerHTML = '<div class="empty-state"><i class="fas fa-bell"></i><p>No alerts</p></div>';
        return;
    }
    
    let html = '';
    alerts.forEach(alert => {
        const unreadClass = !alert.isRead ? 'unread' : '';
        
        html += `
            <div class="alert-item ${alert.type.toLowerCase()} ${unreadClass}">
                <div class="alert-content">
                    <h4>${alert.title}</h4>
                    <p>${alert.message}</p>
                    ${alert.member && alert.member.name ? `<p><strong>Member:</strong> ${alert.member.name}</p>` : ''}
                    ${alert.project && alert.project.name ? `<p><strong>Project:</strong> ${alert.project.name}</p>` : ''}
                    <div class="alert-time">${new Date(alert.createdAt).toLocaleString()}</div>
                </div>
                <div>
                    ${!alert.isRead ? `<button class="btn btn-sm btn-secondary" onclick="markAlertRead(${alert.id})">Mark Read</button>` : ''}
                    <button class="btn btn-sm btn-danger" onclick="deleteAlert(${alert.id})">Delete</button>
                </div>
            </div>
        `;
    });
    
    container.innerHTML = html;
}

async function markAlertRead(alertId) {
    try {
        await AlertsAPI.markAsRead(alertId);
        loadAlerts();
        loadAlertCount();
    } catch (error) {
        console.error('Error marking alert as read:', error);
    }
}

async function markAllAlertsRead() {
    try {
        await AlertsAPI.markAllAsRead();
        showNotification('All alerts marked as read', 'success');
        loadAlerts();
        loadAlertCount();
    } catch (error) {
        console.error('Error marking alerts as read:', error);
    }
}

async function deleteAlert(alertId) {
    try {
        await AlertsAPI.delete(alertId);
        loadAlerts();
        loadAlertCount();
    } catch (error) {
        console.error('Error deleting alert:', error);
    }
}

async function loadStatistics() {
    try {
        const workloadStats = await StatisticsAPI.getWorkload();
        displayWorkloadStatistics(workloadStats);
        
        const projects = await ProjectsAPI.getAll();
        
<<<<<<< HEAD
=======
        // Charger les tâches pour chaque projet
>>>>>>> 47aa7be65ffd6a4e7ebd2f3aecc36049a38a2f3b
        const projectsWithTasks = await Promise.all(
            projects.map(async (project) => {
                try {
                    const tasks = await ProjectsAPI.getTasks(project.id);
                    return { ...project, tasks };
                } catch (error) {
                    console.error(`Error loading tasks for project ${project.id}:`, error);
                    return { ...project, tasks: [] };
                }
            })
        );
        
        displayProjectStatistics(projectsWithTasks);
    } catch (error) {
        console.error('Error loading statistics:', error);
    }
}

function displayWorkloadStatistics(stats) {
    const container = document.getElementById('workloadChart');
    
    let html = '<h3>Team Workload Distribution</h3>';
    html += `<p>Average Workload: ${(stats.averageWorkloadPercentage || 0).toFixed(1)}%</p>`;
    html += `<p>Team Utilization: ${(stats.utilizationPercentage || 0).toFixed(1)}%</p>`;
    html += '<div style="margin-top: 1rem;">';
    
    if (stats.memberWorkloads) {
        stats.memberWorkloads.forEach(member => {
            const percentage = member.workloadPercentage || 0;
            const progressClass = percentage > 100 ? 'danger' : percentage > 80 ? 'warning' : '';
            
            html += `
                <div class="workload-item">
                    <div class="workload-header">
                        <span>${member.name}</span>
                        <span class="workload-percentage">
                            ${member.currentWorkload.toFixed(1)}h / ${member.weeklyAvailability}h 
                            (${percentage.toFixed(1)}%)
                        </span>
                    </div>
                    <div class="progress-bar">
                        <div class="progress-fill ${progressClass}" style="width: ${Math.min(percentage, 100)}%"></div>
                    </div>
                    <p style="font-size: 0.9rem; color: #666; margin-top: 0.25rem;">
                        ${member.taskCount} tasks • ${member.availableHours.toFixed(1)}h available
                    </p>
                </div>
            `;
        });
    }
    
    html += '</div>';
    container.innerHTML = html;
}

function displayProjectStatistics(projects) {
    const container = document.getElementById('progressChart');
    
    let html = '<h3>Project Progress Overview</h3>';
    
    if (projects.length === 0) {
        html += '<p>No projects to display</p>';
    } else {
        projects.forEach(project => {
            const taskCount = project.tasks ? project.tasks.length : 0;
            const completedTasks = project.tasks ? project.tasks.filter(t => t.status === 'COMPLETED').length : 0;
            const completion = taskCount > 0 ? (completedTasks / taskCount * 100) : 0;
            
            html += `
                <div class="workload-item">
                    <div class="workload-header">
                        <span>${project.name}</span>
                        <span class="workload-percentage">${completedTasks}/${taskCount} tasks (${completion.toFixed(1)}%)</span>
                    </div>
                    <div class="progress-bar">
                        <div class="progress-fill" style="width: ${completion}%"></div>
                    </div>
                </div>
            `;
        });
    }
    
    container.innerHTML = html;
}

async function startTask(taskId, projectId) {
    if (!confirm('Voulez-vous commencer cette tâche ?')) {
        return;
    }
    
    try {
        const task = await TasksAPI.getById(taskId);
        
        // Nettoyer l'objet pour l'update - ne garder que les champs simples
        const cleanTask = {
            id: task.id,
            projectId: task.projectId,
            title: task.title,
            description: task.description,
            estimatedHours: task.estimatedHours,
            priority: task.priority,
            status: 'IN_PROGRESS',
            startDate: task.startDate,
            deadline: task.deadline,
            assignedMemberId: task.assignedMember ? task.assignedMember.id : null
        };
        
        await TasksAPI.update(cleanTask);
        
        // Mettre à jour le statut du projet si nécessaire
        await updateProjectStatus(projectId);
        
        showNotification('Tâche démarrée avec succès !', 'success');
        
        if (projectId) {
            viewProjectDetails(projectId);
        }
    } catch (error) {
        console.error('Error starting task:', error);
        showNotification('Erreur lors du démarrage de la tâche: ' + error.message, 'error');
    }
}

async function completeTask(taskId, projectId) {
    if (!confirm('Marquer cette tâche comme terminée ?')) {
        return;
    }
    
    try {
        const task = await TasksAPI.getById(taskId);
        
        // Nettoyer l'objet pour l'update - ne garder que les champs simples
        const cleanTask = {
            id: task.id,
            projectId: task.projectId,
            title: task.title,
            description: task.description,
            estimatedHours: task.estimatedHours,
            priority: task.priority,
            status: 'COMPLETED',
            startDate: task.startDate,
            deadline: task.deadline,
            assignedMemberId: task.assignedMember ? task.assignedMember.id : null
        };
        
        await TasksAPI.update(cleanTask);
        
        // Mettre à jour le statut du projet si nécessaire
        await updateProjectStatus(projectId);
        
        showNotification('Tâche terminée ! Félicitations ! 🎉', 'success');
        
        // Rafraîchir l'affichage
        if (projectId) {
            viewProjectDetails(projectId);
        }
        
        // Rafraîchir aussi le dashboard si on y est
        if (currentPage === 'dashboard') {
            loadDashboard();
        }
    } catch (error) {
        console.error('Error completing task:', error);
        showNotification('Erreur lors de la complétion de la tâche: ' + error.message, 'error');
    }
}

/**
 * Met à jour automatiquement le statut du projet en fonction des tâches
 */
async function updateProjectStatus(projectId) {
    try {
        console.log(`Updating project ${projectId} status...`);
        const project = await ProjectsAPI.getById(projectId);
        const tasks = await ProjectsAPI.getTasks(projectId);
        
        console.log(`Project current status: ${project.status}`);
        console.log(`Tasks: ${tasks.length} total`);
        
        if (tasks.length === 0) {
            console.log('No tasks, skipping status update');
            return; // Pas de tâches, pas de changement
        }
        
        // Compter les tâches par statut
        const completedCount = tasks.filter(t => t.status === 'COMPLETED').length;
        const inProgressCount = tasks.filter(t => t.status === 'IN_PROGRESS').length;
        const todoCount = tasks.filter(t => t.status === 'TODO').length;
        
        console.log(`Tasks breakdown: ${completedCount} COMPLETED, ${inProgressCount} IN_PROGRESS, ${todoCount} TODO`);
        
        let newStatus = project.status;
        
        // Déterminer le nouveau statut
        if (completedCount === tasks.length) {
            // Toutes les tâches sont terminées
            newStatus = 'COMPLETED';
            console.log('All tasks completed, setting project to COMPLETED');
        } else if (inProgressCount > 0 || completedCount > 0) {
            // Au moins une tâche en cours ou terminée
            newStatus = 'IN_PROGRESS';
            console.log('Some tasks in progress or completed, setting project to IN_PROGRESS');
        } else {
            console.log('All tasks are TODO, keeping project status as is');
        }
        
        // Mettre à jour uniquement si le statut a changé
        if (newStatus !== project.status) {
            console.log(`Updating project status from ${project.status} to ${newStatus}`);
            const updatedProject = {
                id: project.id,
                name: project.name,
                description: project.description,
                startDate: project.startDate,
                deadline: project.deadline,
                status: newStatus
            };
            
            await ProjectsAPI.update(updatedProject);
            console.log(`Project ${projectId} status successfully updated to ${newStatus}`);
        } else {
            console.log(`Project status unchanged (${project.status})`);
        }
    } catch (error) {
        console.error('Error updating project status:', error);
    }
}

// Manual task assignment functions
async function showAssignTaskModal(taskId, projectId) {
    currentTaskId = taskId;
    currentProject = projectId;
    
    try {
        // Charger les membres disponibles
        const members = await MembersAPI.getAll();
        const task = await TasksAPI.getById(taskId);
        
        if (!members || members.length === 0) {
            showNotification('Aucun membre disponible', 'error');
            return;
        }
        
        // Créer les options pour les membres
        const memberOptions = members.map(m => {
            // Calculer le pourcentage de charge si non fourni
            const workloadPct = m.workloadPercentage || 
                               (m.weeklyAvailability > 0 ? (m.currentWorkload / m.weeklyAvailability * 100) : 0);
            const workload = workloadPct.toFixed(0);
            return `<option value="${m.id}">${m.name} (${workload}% chargé)</option>`;
        }).join('');
        
        // Créer le modal HTML
        const modalHtml = `
            <div class="modal-backdrop" id="assignTaskBackdrop" onclick="closeAssignTaskModal()"></div>
            <div class="modal active" id="assignTaskModal">
                <div class="modal-content">
                    <div class="modal-header">
                        <h2><i class="fas fa-user-plus"></i> Assigner la tâche</h2>
                        <button class="close-btn" onclick="closeAssignTaskModal()">×</button>
                    </div>
                    <div class="modal-body">
                        <p><strong>Tâche :</strong> ${task.title}</p>
                        <p><strong>Heures estimées :</strong> ${task.estimatedHours}h</p>
                        <br>
                        <label for="memberSelect">Sélectionner un membre :</label>
                        <select id="memberSelect" class="form-control">
                            <option value="">-- Choisir un membre --</option>
                            ${memberOptions}
                        </select>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" onclick="closeAssignTaskModal()">Annuler</button>
                        <button type="button" class="btn btn-primary" onclick="assignTaskToMember()">Assigner</button>
                    </div>
                </div>
            </div>
        `;
        
        // Ajouter le modal au DOM
        const modalContainer = document.createElement('div');
        modalContainer.id = 'assignTaskContainer';
        modalContainer.innerHTML = modalHtml;
        document.body.appendChild(modalContainer);
        
    } catch (error) {
        console.error('Error showing assign modal:', error);
        console.error('Error details:', error.message);
        showNotification('Erreur lors du chargement des données: ' + error.message, 'error');
    }
}

function closeAssignTaskModal() {
    const container = document.getElementById('assignTaskContainer');
    if (container) {
        container.remove();
    }
}

async function assignTaskToMember() {
    const memberId = document.getElementById('memberSelect').value;
    
    if (!memberId) {
        showNotification('Veuillez sélectionner un membre', 'error');
        return;
    }
    
    try {
        await TasksAPI.assignToMember(currentTaskId, parseInt(memberId));
        showNotification('Tâche assignée avec succès !', 'success');
        closeAssignTaskModal();
        
        // Rafraîchir l'affichage
        if (currentProject) {
            viewProjectDetails(currentProject);
        }
        
        // Rafraîchir le dashboard si nécessaire
        if (currentPage === 'dashboard') {
            loadDashboard();
        }
        
        // Rafraîchir le compteur d'alertes
        loadAlertCount();
        
    } catch (error) {
        console.error('Error assigning task:', error);
        
        // Vérifier si c'est une erreur d'incompétence
        if (error.message && error.message.includes('INCOMPETENT')) {
            const message = error.message.replace('INCOMPETENT: ', '');
            showNotification('❌ ' + message, 'error');
        } else {
            showNotification('Erreur lors de l\'assignation: ' + error.message, 'error');
        }
    }
}

async function unassignTask(taskId, projectId) {
    if (!confirm('Êtes-vous sûr de vouloir retirer l\'assignation de cette tâche ?')) {
        return;
    }
    
    try {
        await TasksAPI.unassign(taskId);
        showNotification('Assignation retirée avec succès !', 'success');
        
        // Rafraîchir l'affichage
        if (projectId) {
            viewProjectDetails(projectId);
        }
        
        // Rafraîchir le dashboard si nécessaire
        if (currentPage === 'dashboard') {
            loadDashboard();
        }
    } catch (error) {
        console.error('Error unassigning task:', error);
        showNotification('Erreur: ' + error.message, 'error');
    }
}


// Utility Functions
function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
}

function showNotification(message, type = 'info') {
    // Simple alert for now - can be enhanced with a toast library
    const icon = type === 'success' ? '✓' : type === 'error' ? '✗' : 'ℹ';
    alert(`${icon} ${message}`);
}

// Close modals when clicking outside
window.onclick = function(event) {
    if (event.target.classList.contains('modal')) {
        event.target.classList.remove('active');
    }
}
