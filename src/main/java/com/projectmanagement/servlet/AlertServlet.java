package com.projectmanagement.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.projectmanagement.dao.AlertDAO;
import com.projectmanagement.dao.SkillDAO;
import com.projectmanagement.model.Alert;
import com.projectmanagement.model.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/alerts/*")
public class AlertServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AlertServlet.class);
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private final AlertDAO alertDAO = new AlertDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        String unreadParam = request.getParameter("unread");
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all alerts
                boolean unreadOnly = "true".equalsIgnoreCase(unreadParam);
                List<Alert> alerts = alertDAO.findAll(unreadOnly);
                sendJsonResponse(response, HttpServletResponse.SC_OK, alerts);
            } else if (pathInfo.equals("/count")) {
                // Get unread count
                int count = alertDAO.getUnreadCount();
                Map<String, Integer> result = new HashMap<>();
                result.put("count", count);
                sendJsonResponse(response, HttpServletResponse.SC_OK, result);
            } else {
                // Get specific alert
                int id = Integer.parseInt(pathInfo.substring(1));
                Alert alert = alertDAO.findById(id);
                
                if (alert != null) {
                    sendJsonResponse(response, HttpServletResponse.SC_OK, alert);
                } else {
                    sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Alert not found");
                }
            }
        } catch (Exception e) {
            logger.error("Error in GET /api/alerts", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo != null && pathInfo.contains("/read")) {
                // Mark as read: PUT /api/alerts/{id}/read
                String[] parts = pathInfo.split("/");
                int id = Integer.parseInt(parts[1]);
                alertDAO.markAsRead(id);
                sendSuccessResponse(response, "Alert marked as read");
            } else if (pathInfo != null && pathInfo.equals("/read-all")) {
                // Mark all as read
                alertDAO.markAllAsRead();
                sendSuccessResponse(response, "All alerts marked as read");
            }
        } catch (Exception e) {
            logger.error("Error in PUT /api/alerts", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            alertDAO.delete(id);
            
            sendSuccessResponse(response, "Alert deleted successfully");
        } catch (Exception e) {
            logger.error("Error in DELETE /api/alerts", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void sendJsonResponse(HttpServletResponse response, int status, Object data) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(data));
    }

    private void sendSuccessResponse(HttpServletResponse response, String message) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        sendJsonResponse(response, HttpServletResponse.SC_OK, result);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        sendJsonResponse(response, status, error);
    }
}

@WebServlet("/api/skills/*")
class SkillServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(SkillServlet.class);
    private final Gson gson = new GsonBuilder().create();
    private final SkillDAO skillDAO = new SkillDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            List<Skill> skills = skillDAO.findAll();
            sendJsonResponse(response, HttpServletResponse.SC_OK, skills);
        } catch (Exception e) {
            logger.error("Error in GET /api/skills", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void sendJsonResponse(HttpServletResponse response, int status, Object data) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(data));
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        sendJsonResponse(response, status, error);
    }
}
