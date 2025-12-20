package com.projectmanagement.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.projectmanagement.dao.ProjectDAO;
import com.projectmanagement.dao.TaskDAO;
import com.projectmanagement.model.Project;
import com.projectmanagement.model.Task;
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

@WebServlet("/api/projects/*")
public class ProjectServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ProjectServlet.class);
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final TaskDAO taskDAO = new TaskDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all projects
                List<Project> projects = projectDAO.findAll();
                
                // Load tasks for each project
                for (Project project : projects) {
                    List<Task> tasks = taskDAO.findByProject(project.getId());
                    project.setTasks(tasks);
                }
                
                sendJsonResponse(response, HttpServletResponse.SC_OK, projects);
            } else if (pathInfo.contains("/tasks")) {
                // Get tasks for a project: GET /api/projects/{id}/tasks
                String[] parts = pathInfo.split("/");
                int projectId = Integer.parseInt(parts[1]);
                List<Task> tasks = taskDAO.findByProject(projectId);
                sendJsonResponse(response, HttpServletResponse.SC_OK, tasks);
            } else {
                // Get specific project
                int id = Integer.parseInt(pathInfo.substring(1));
                Project project = projectDAO.findById(id);
                
                if (project != null) {
                    List<Task> tasks = taskDAO.findByProject(id);
                    project.setTasks(tasks);
                    sendJsonResponse(response, HttpServletResponse.SC_OK, project);
                } else {
                    sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Project not found");
                }
            }
        } catch (Exception e) {
            logger.error("Error in GET /api/projects", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            String json = readRequestBody(request);
            Project project = gson.fromJson(json, Project.class);
            
            int id = projectDAO.create(project);
            project.setId(id);
            
            sendJsonResponse(response, HttpServletResponse.SC_CREATED, project);
        } catch (Exception e) {
            logger.error("Error in POST /api/projects", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            String json = readRequestBody(request);
            Project project = gson.fromJson(json, Project.class);
            
            projectDAO.update(project);
            
            sendJsonResponse(response, HttpServletResponse.SC_OK, project);
        } catch (Exception e) {
            logger.error("Error in PUT /api/projects", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            projectDAO.delete(id);
            
            sendSuccessResponse(response, "Project deleted successfully");
        } catch (Exception e) {
            logger.error("Error in DELETE /api/projects", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
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
