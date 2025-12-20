package com.projectmanagement.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.projectmanagement.service.TaskAllocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/allocate/*")
public class AllocationServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AllocationServlet.class);
    private final Gson gson = new GsonBuilder().create();
    private final TaskAllocationService allocationService = new TaskAllocationService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        try {
            // POST /api/allocate/{projectId}
            int projectId = Integer.parseInt(pathInfo.substring(1));
            
            logger.info("Starting task allocation for project: {}", projectId);
            TaskAllocationService.AllocationResult result = allocationService.allocateTasks(projectId);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", result.isSuccess());
            responseData.put("assignedCount", result.getAssignedCount());
            responseData.put("failedCount", result.getFailedCount());
            responseData.put("message", result.getMessage());
            
            sendJsonResponse(response, HttpServletResponse.SC_OK, responseData);
        } catch (Exception e) {
            logger.error("Error in POST /api/allocate", e);
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
