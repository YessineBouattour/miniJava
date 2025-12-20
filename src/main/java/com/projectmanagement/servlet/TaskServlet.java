package com.projectmanagement.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.projectmanagement.dao.TaskDAO;
import com.projectmanagement.model.Task;
import com.projectmanagement.model.TaskSkill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/tasks/*")
public class TaskServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(TaskServlet.class);
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    private final TaskDAO taskDAO = new TaskDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                    "Please specify task ID or use /api/projects/{id}/tasks");
            } else {
                // Get specific task
                int id = Integer.parseInt(pathInfo.substring(1));
                Task task = taskDAO.findById(id);
                
                if (task != null) {
                    sendJsonResponse(response, HttpServletResponse.SC_OK, task);
                } else {
                    sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Task not found");
                }
            }
        } catch (Exception e) {
            logger.error("Error in GET /api/tasks", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo != null && pathInfo.contains("/skills")) {
                // Add skill requirement: POST /api/tasks/{id}/skills
                handleAddSkillRequirement(request, response, pathInfo);
            } else if (pathInfo != null && pathInfo.contains("/dependencies")) {
                // Add dependency: POST /api/tasks/{id}/dependencies
                handleAddDependency(request, response, pathInfo);
            } else {
                // Create new task
                String json = readRequestBody(request);
                Task task = gson.fromJson(json, Task.class);
                
                int id = taskDAO.create(task);
                task.setId(id);
                
                // Add skill requirements if present
                if (task.getRequiredSkills() != null) {
                    for (TaskSkill skill : task.getRequiredSkills()) {
                        taskDAO.addSkillRequirement(id, skill.getSkillId(), skill.getRequiredLevel());
                    }
                }
                
                // Add dependencies if present
                if (task.getDependencies() != null) {
                    for (Integer depId : task.getDependencies()) {
                        taskDAO.addDependency(id, depId);
                    }
                }
                
                sendJsonResponse(response, HttpServletResponse.SC_CREATED, task);
            }
        } catch (Exception e) {
            logger.error("Error in POST /api/tasks", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            String json = readRequestBody(request);
            Task task = gson.fromJson(json, Task.class);
            
            taskDAO.update(task);
            
            sendJsonResponse(response, HttpServletResponse.SC_OK, task);
        } catch (Exception e) {
            logger.error("Error in PUT /api/tasks", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            taskDAO.delete(id);
            
            sendSuccessResponse(response, "Task deleted successfully");
        } catch (Exception e) {
            logger.error("Error in DELETE /api/tasks", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void handleAddSkillRequirement(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws Exception {
        
        String[] parts = pathInfo.split("/");
        int taskId = Integer.parseInt(parts[1]);
        
        String json = readRequestBody(request);
        TaskSkill skill = gson.fromJson(json, TaskSkill.class);
        
        taskDAO.addSkillRequirement(taskId, skill.getSkillId(), skill.getRequiredLevel());
        
        sendSuccessResponse(response, "Skill requirement added successfully");
    }

    private void handleAddDependency(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws Exception {
        
        String[] parts = pathInfo.split("/");
        int taskId = Integer.parseInt(parts[1]);
        
        String json = readRequestBody(request);
        Map<String, Integer> data = gson.fromJson(json, Map.class);
        int dependsOnTaskId = data.get("dependsOnTaskId");
        
        taskDAO.addDependency(taskId, dependsOnTaskId);
        
        sendSuccessResponse(response, "Dependency added successfully");
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
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
