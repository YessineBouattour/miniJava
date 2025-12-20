// Timeline Visualization Module
class TimelineVisualizer {
    constructor(containerId) {
        this.container = document.getElementById(containerId);
        this.tasks = [];
        this.members = new Map();
        this.startDate = null;
        this.endDate = null;
    }

    setData(tasks, project) {
        this.tasks = tasks;
        this.startDate = new Date(project.startDate);
        this.endDate = new Date(project.deadline);
        
        // Group tasks by member
        this.members.clear();
        tasks.forEach(task => {
            if (task.assignedMemberId) {
                if (!this.members.has(task.assignedMemberId)) {
                    this.members.set(task.assignedMemberId, {
                        id: task.assignedMemberId,
                        name: task.assignedMemberName,
                        tasks: []
                    });
                }
                this.members.get(task.assignedMemberId).tasks.push(task);
            }
        });
        
        // Add unassigned tasks
        const unassignedTasks = tasks.filter(t => !t.assignedMemberId);
        if (unassignedTasks.length > 0) {
            this.members.set(0, {
                id: 0,
                name: 'Unassigned',
                tasks: unassignedTasks
            });
        }
    }

    render() {
        if (this.tasks.length === 0) {
            this.container.innerHTML = '<div class="empty-state"><i class="fas fa-calendar-alt"></i><p>No tasks to display</p></div>';
            return;
        }

        const totalDays = Math.ceil((this.endDate - this.startDate) / (1000 * 60 * 60 * 24));
        
        let html = '<div class="timeline-content">';
        
        // Timeline header with dates
        html += '<div class="timeline-header">';
        html += `<div class="timeline-label">Team Member</div>`;
        html += '<div class="timeline-dates">';
        html += `<span>${this.formatDate(this.startDate)}</span>`;
        html += `<span>${this.formatDate(this.endDate)}</span>`;
        html += '</div>';
        html += '</div>';
        
        // Render each member's timeline
        this.members.forEach((member, memberId) => {
            html += this.renderMemberTimeline(member, totalDays);
        });
        
        html += '</div>';
        this.container.innerHTML = html;
    }

    renderMemberTimeline(member, totalDays) {
        let html = '<div class="timeline-row">';
        html += `<div class="timeline-label">${member.name}</div>`;
        html += '<div class="timeline-bars">';
        
        member.tasks.forEach(task => {
            if (task.startDate && task.deadline) {
                const taskStart = new Date(task.startDate);
                const taskEnd = new Date(task.deadline);
                
                const startOffset = Math.max(0, (taskStart - this.startDate) / (1000 * 60 * 60 * 24));
                const duration = (taskEnd - taskStart) / (1000 * 60 * 60 * 24);
                
                const leftPercent = (startOffset / totalDays) * 100;
                const widthPercent = (duration / totalDays) * 100;
                
                html += `<div class="timeline-bar ${task.status}" 
                    style="left: ${leftPercent}%; width: ${widthPercent}%;"
                    title="${task.title} (${task.estimatedHours}h)"
                    onclick="showTaskDetails(${task.id})">
                    <span>${task.title}</span>
                </div>`;
            }
        });
        
        html += '</div>';
        html += '</div>';
        
        return html;
    }

    formatDate(date) {
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    }
}

// Global timeline instance
let timelineVisualizer = null;

// Initialize timeline
function initTimeline() {
    timelineVisualizer = new TimelineVisualizer('timelineContainer');
}

// Load timeline for selected project
async function loadTimeline() {
    const select = document.getElementById('timelineProjectSelect');
    const projectId = parseInt(select.value);
    
    if (!projectId) {
        document.getElementById('timelineContainer').innerHTML = 
            '<div class="empty-state"><i class="fas fa-calendar-alt"></i><p>Please select a project</p></div>';
        return;
    }
    
    try {
        const project = await ProjectsAPI.getById(projectId);
        const tasks = await ProjectsAPI.getTasks(projectId);
        
        if (!timelineVisualizer) {
            initTimeline();
        }
        
        timelineVisualizer.setData(tasks, project);
        timelineVisualizer.render();
    } catch (error) {
        console.error('Error loading timeline:', error);
        showNotification('Error loading timeline', 'error');
    }
}

// Populate timeline project selector
async function populateTimelineProjectSelect() {
    try {
        const projects = await ProjectsAPI.getAll();
        const select = document.getElementById('timelineProjectSelect');
        
        select.innerHTML = '<option value="">Select a project</option>';
        projects.forEach(project => {
            const option = document.createElement('option');
            option.value = project.id;
            option.textContent = project.name;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading projects:', error);
    }
}
