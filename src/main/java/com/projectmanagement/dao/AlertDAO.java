package com.projectmanagement.dao;

import com.projectmanagement.model.Alert;
import com.projectmanagement.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlertDAO {
    private static final Logger logger = LoggerFactory.getLogger(AlertDAO.class);

    public int create(Alert alert) throws SQLException {
        String sql = "INSERT INTO alerts (type, severity, title, message, member_id, project_id, task_id, is_read) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, alert.getType().name());
            stmt.setString(2, alert.getSeverity().name());
            stmt.setString(3, alert.getTitle());
            stmt.setString(4, alert.getMessage());
            setIntOrNull(stmt, 5, alert.getMemberId());
            setIntOrNull(stmt, 6, alert.getProjectId());
            setIntOrNull(stmt, 7, alert.getTaskId());
            stmt.setBoolean(8, alert.isRead());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating alert failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    alert.setId(id);
                    logger.info("Created alert: {} [{}]", alert.getTitle(), alert.getType());
                    return id;
                } else {
                    throw new SQLException("Creating alert failed, no ID obtained.");
                }
            }
        }
    }

    public Alert findById(int id) throws SQLException {
        String sql = "SELECT a.*, m.name as member_name, p.name as project_name, t.title as task_title " +
                    "FROM alerts a " +
                    "LEFT JOIN members m ON a.member_id = m.id " +
                    "LEFT JOIN projects p ON a.project_id = p.id " +
                    "LEFT JOIN tasks t ON a.task_id = t.id " +
                    "WHERE a.id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractAlertFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Alert> findAll() throws SQLException {
        return findAll(false);
    }

    public List<Alert> findAll(boolean unreadOnly) throws SQLException {
        String sql = "SELECT a.*, m.name as member_name, p.name as project_name, t.title as task_title " +
                    "FROM alerts a " +
                    "LEFT JOIN members m ON a.member_id = m.id " +
                    "LEFT JOIN projects p ON a.project_id = p.id " +
                    "LEFT JOIN tasks t ON a.task_id = t.id ";
        
        if (unreadOnly) {
            sql += "WHERE a.is_read = FALSE ";
        }
        
        sql += "ORDER BY a.severity DESC, a.created_at DESC";
        
        List<Alert> alerts = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                alerts.add(extractAlertFromResultSet(rs));
            }
        }
        return alerts;
    }

    public List<Alert> findByMember(int memberId) throws SQLException {
        String sql = "SELECT a.*, m.name as member_name, p.name as project_name, t.title as task_title " +
                    "FROM alerts a " +
                    "LEFT JOIN members m ON a.member_id = m.id " +
                    "LEFT JOIN projects p ON a.project_id = p.id " +
                    "LEFT JOIN tasks t ON a.task_id = t.id " +
                    "WHERE a.member_id = ? " +
                    "ORDER BY a.severity DESC, a.created_at DESC";
        List<Alert> alerts = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alerts.add(extractAlertFromResultSet(rs));
                }
            }
        }
        return alerts;
    }

    public List<Alert> findByProject(int projectId) throws SQLException {
        String sql = "SELECT a.*, m.name as member_name, p.name as project_name, t.title as task_title " +
                    "FROM alerts a " +
                    "LEFT JOIN members m ON a.member_id = m.id " +
                    "LEFT JOIN projects p ON a.project_id = p.id " +
                    "LEFT JOIN tasks t ON a.task_id = t.id " +
                    "WHERE a.project_id = ? " +
                    "ORDER BY a.severity DESC, a.created_at DESC";
        List<Alert> alerts = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alerts.add(extractAlertFromResultSet(rs));
                }
            }
        }
        return alerts;
    }

    public void markAsRead(int id) throws SQLException {
        String sql = "UPDATE alerts SET is_read = TRUE WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void markAllAsRead() throws SQLException {
        String sql = "UPDATE alerts SET is_read = TRUE";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM alerts WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
            logger.info("Deleted alert with ID: {}", id);
        }
    }

    public int getUnreadCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM alerts WHERE is_read = FALSE";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Alert extractAlertFromResultSet(ResultSet rs) throws SQLException {
        Alert alert = new Alert();
        alert.setId(rs.getInt("id"));
        alert.setType(Alert.AlertType.valueOf(rs.getString("type")));
        alert.setSeverity(Alert.Severity.valueOf(rs.getString("severity")));
        alert.setTitle(rs.getString("title"));
        alert.setMessage(rs.getString("message"));
        
        int memberId = rs.getInt("member_id");
        if (!rs.wasNull()) {
            alert.setMemberId(memberId);
            alert.setMemberName(rs.getString("member_name"));
        }
        
        int projectId = rs.getInt("project_id");
        if (!rs.wasNull()) {
            alert.setProjectId(projectId);
            alert.setProjectName(rs.getString("project_name"));
        }
        
        int taskId = rs.getInt("task_id");
        if (!rs.wasNull()) {
            alert.setTaskId(taskId);
            alert.setTaskTitle(rs.getString("task_title"));
        }
        
        alert.setRead(rs.getBoolean("is_read"));
        alert.setCreatedAt(rs.getTimestamp("created_at"));
        return alert;
    }

    private void setIntOrNull(PreparedStatement stmt, int paramIndex, Integer value) throws SQLException {
        if (value != null) {
            stmt.setInt(paramIndex, value);
        } else {
            stmt.setNull(paramIndex, Types.INTEGER);
        }
    }
}
