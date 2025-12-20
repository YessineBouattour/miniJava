package com.projectmanagement.dao;

import com.projectmanagement.model.Task;
import com.projectmanagement.model.TaskSkill;
import com.projectmanagement.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    private static final Logger logger = LoggerFactory.getLogger(TaskDAO.class);

    public int create(Task task) throws SQLException {
        String sql = "INSERT INTO tasks (project_id, title, description, estimated_hours, priority, " +
                    "status, start_date, deadline, assigned_member_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, task.getProjectId());
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getDescription());
            stmt.setDouble(4, task.getEstimatedHours());
            stmt.setString(5, task.getPriority().name());
            stmt.setString(6, task.getStatus().name());
            stmt.setDate(7, task.getStartDate());
            stmt.setDate(8, task.getDeadline());
            if (task.getAssignedMemberId() != null) {
                stmt.setInt(9, task.getAssignedMemberId());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating task failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    task.setId(id);
                    logger.info("Created task: {} with ID: {}", task.getTitle(), id);
                    return id;
                } else {
                    throw new SQLException("Creating task failed, no ID obtained.");
                }
            }
        }
    }

    public Task findById(int id) throws SQLException {
        String sql = "SELECT t.*, m.name as member_name FROM tasks t " +
                    "LEFT JOIN members m ON t.assigned_member_id = m.id WHERE t.id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Task task = extractTaskFromResultSet(rs);
                    task.setRequiredSkills(findTaskSkills(id));
                    task.setDependencies(findTaskDependencies(id));
                    return task;
                }
            }
        }
        return null;
    }

    public List<Task> findByProject(int projectId) throws SQLException {
        String sql = "SELECT t.*, m.name as member_name FROM tasks t " +
                    "LEFT JOIN members m ON t.assigned_member_id = m.id " +
                    "WHERE t.project_id = ? ORDER BY t.priority DESC, t.deadline ASC";
        List<Task> tasks = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Task task = extractTaskFromResultSet(rs);
                    task.setRequiredSkills(findTaskSkills(task.getId()));
                    task.setDependencies(findTaskDependencies(task.getId()));
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    public List<Task> findUnassignedByProject(int projectId) throws SQLException {
        String sql = "SELECT t.*, m.name as member_name FROM tasks t " +
                    "LEFT JOIN members m ON t.assigned_member_id = m.id " +
                    "WHERE t.project_id = ? AND t.assigned_member_id IS NULL " +
                    "ORDER BY t.priority DESC, t.deadline ASC";
        List<Task> tasks = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Task task = extractTaskFromResultSet(rs);
                    task.setRequiredSkills(findTaskSkills(task.getId()));
                    task.setDependencies(findTaskDependencies(task.getId()));
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    public List<Task> findByMember(int memberId) throws SQLException {
        String sql = "SELECT t.*, m.name as member_name FROM tasks t " +
                    "LEFT JOIN members m ON t.assigned_member_id = m.id " +
                    "WHERE t.assigned_member_id = ? ORDER BY t.deadline ASC";
        List<Task> tasks = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Task task = extractTaskFromResultSet(rs);
                    task.setRequiredSkills(findTaskSkills(task.getId()));
                    task.setDependencies(findTaskDependencies(task.getId()));
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    public void update(Task task) throws SQLException {
        String sql = "UPDATE tasks SET title = ?, description = ?, estimated_hours = ?, " +
                    "priority = ?, status = ?, start_date = ?, deadline = ?, assigned_member_id = ? " +
                    "WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setDouble(3, task.getEstimatedHours());
            stmt.setString(4, task.getPriority().name());
            stmt.setString(5, task.getStatus().name());
            stmt.setDate(6, task.getStartDate());
            stmt.setDate(7, task.getDeadline());
            if (task.getAssignedMemberId() != null) {
                stmt.setInt(8, task.getAssignedMemberId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }
            stmt.setInt(9, task.getId());
            
            stmt.executeUpdate();
            logger.info("Updated task: {}", task.getTitle());
        }
    }

    public void assignTask(int taskId, int memberId) throws SQLException {
        String sql = "UPDATE tasks SET assigned_member_id = ?, status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, memberId);
            stmt.setString(2, Task.TaskStatus.TODO.name());
            stmt.setInt(3, taskId);
            stmt.executeUpdate();
            logger.info("Assigned task {} to member {}", taskId, memberId);
        }
    }

    public void unassignTask(int taskId) throws SQLException {
        String sql = "UPDATE tasks SET assigned_member_id = NULL WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, taskId);
            stmt.executeUpdate();
        }
    }

    public void updateStatus(int taskId, Task.TaskStatus status) throws SQLException {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
            logger.info("Deleted task with ID: {}", id);
        }
    }

    public void addSkillRequirement(int taskId, int skillId, int requiredLevel) throws SQLException {
        String sql = "INSERT INTO task_skills (task_id, skill_id, required_level) " +
                    "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE required_level = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, taskId);
            stmt.setInt(2, skillId);
            stmt.setInt(3, requiredLevel);
            stmt.setInt(4, requiredLevel);
            stmt.executeUpdate();
        }
    }

    public void addDependency(int taskId, int dependsOnTaskId) throws SQLException {
        String sql = "INSERT INTO task_dependencies (task_id, depends_on_task_id) VALUES (?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, taskId);
            stmt.setInt(2, dependsOnTaskId);
            stmt.executeUpdate();
        }
    }

    public List<TaskSkill> findTaskSkills(int taskId) throws SQLException {
        String sql = "SELECT ts.*, s.name as skill_name FROM task_skills ts " +
                    "JOIN skills s ON ts.skill_id = s.id WHERE ts.task_id = ?";
        List<TaskSkill> skills = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, taskId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TaskSkill skill = new TaskSkill();
                    skill.setTaskId(rs.getInt("task_id"));
                    skill.setSkillId(rs.getInt("skill_id"));
                    skill.setRequiredLevel(rs.getInt("required_level"));
                    skill.setSkillName(rs.getString("skill_name"));
                    skills.add(skill);
                }
            }
        }
        return skills;
    }

    public List<Integer> findTaskDependencies(int taskId) throws SQLException {
        String sql = "SELECT depends_on_task_id FROM task_dependencies WHERE task_id = ?";
        List<Integer> dependencies = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, taskId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dependencies.add(rs.getInt("depends_on_task_id"));
                }
            }
        }
        return dependencies;
    }

    private Task extractTaskFromResultSet(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setProjectId(rs.getInt("project_id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setEstimatedHours(rs.getDouble("estimated_hours"));
        task.setPriority(Task.Priority.valueOf(rs.getString("priority")));
        task.setStatus(Task.TaskStatus.valueOf(rs.getString("status")));
        task.setStartDate(rs.getDate("start_date"));
        task.setDeadline(rs.getDate("deadline"));
        
        int assignedMemberId = rs.getInt("assigned_member_id");
        if (!rs.wasNull()) {
            task.setAssignedMemberId(assignedMemberId);
            task.setAssignedMemberName(rs.getString("member_name"));
        }
        
        task.setCreatedAt(rs.getTimestamp("created_at"));
        task.setUpdatedAt(rs.getTimestamp("updated_at"));
        return task;
    }
}
