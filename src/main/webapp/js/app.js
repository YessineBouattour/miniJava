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
        
        // Display recent projects
        const recentProjects = projects.slice(0, 5);
        displayRecentProjects(recentProjects);
        
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
        const completion = project.tasks ? 
            (project.tasks.filter(t => t.status === 'COMPLETED').length / project.tasks.length * 100) : 0;
        
        html += `
            <div class="task-item" onclick="viewProjectDetails(${project.id})">
                <div class="task-info">
                    <h4>${project.name}</h4>
                    <p>${project.tasks ? project.tasks.length : 0} tasks • ${completion.toFixed(0)}% complete</p>
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
    memberWorkloads.slice(0, 5).forEach(member => {
        const percentage = member.workloadPercentage || 0;
        const progressClass = percentage > 100 ? 'danger' : percentage > 80 ? 'warning' : '';
        
        html += `
            <div class="workload-item">
                <div class="workload-header">
                    <span>${member.name}</span>
                    <span class="workload-percentage">${member.currentWorkload.toFixed(1)}h / ${member.weeklyAvailability}h</span>
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
        const workloadPercentage = member.workloadPercentage || 0;
        const progressClass = workloadPercentage > 100 ? 'danger' : workloadPercentage > 80 ? 'warning' : '';
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
                        <span>${member.currentWorkload.toFixed(1)}h / ${member.weeklyAvailability}h</span>
                    </div>
                    <div class="progress-bar">
                        <div class="progress-fill ${progressClass}" style="width: ${Math.min(workloadPercentage, 100)}%"></div>
                    </div>
                </div>
                <div class="skills-tags">
                    ${member.skills.map(skill => `<span class="skill-tag">${skill.skillName} (${skill.proficiencyLevel})</span>`).join('')}
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
    } catch (error) {
        console.error('Error adding member:', error);
        showNotification('Error adding member: ' + error.message, 'error');
    }
}

// Projects Functions
async function loadProjects() {
    try {
        const projects = await ProjectsAPI.getAll();
        displayProjects(projects);
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
        const taskCount = project.tasks ? project.tasks.length : 0;
        const completedTasks = project.tasks ? project.tasks.filter(t => t.status === 'COMPLETED').length : 0;
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
                    <span><i class="fas fa-tasks"></i> ${taskCount} tasks</span>
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
            html += `
                <div class="task-item">
                    <div class="task-info">
                        <h4>${task.title}</h4>
                        <p>
                            ${task.estimatedHours}h • 
                            ${task.assignedMemberName || 'Unassigned'} • 
                            <span class="priority-badge ${task.priority}">${task.priority}</span> • 
                            <span class="status-badge ${task.status.toLowerCase().replace('_', '-')}">${task.status}</span>
                        </p>
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
        
        loadProjects();
        loadAlerts();
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
        const unreadClass = !alert.read ? 'unread' : '';
        
        html += `
            <div class="alert-item ${alert.type} ${unreadClass}">
                <div class="alert-content">
                    <h4>${alert.title}</h4>
                    <p>${alert.message}</p>
                    ${alert.memberName ? `<p><strong>Member:</strong> ${alert.memberName}</p>` : ''}
                    ${alert.projectName ? `<p><strong>Project:</strong> ${alert.projectName}</p>` : ''}
                    <div class="alert-time">${new Date(alert.createdAt).toLocaleString()}</div>
                </div>
                <div>
                    ${!alert.read ? `<button class="btn btn-sm btn-secondary" onclick="markAlertRead(${alert.id})">Mark Read</button>` : ''}
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

// Statistics Functions
async function loadStatistics() {
    try {
        const workloadStats = await StatisticsAPI.getWorkload();
        displayWorkloadStatistics(workloadStats);
        
        const projects = await ProjectsAPI.getAll();
        displayProjectStatistics(projects);
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
