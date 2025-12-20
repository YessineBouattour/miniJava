package com.projectmanagement.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.projectmanagement.service.StatisticsService;
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

@WebServlet("/api/statistics/*")
public class StatisticsServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsServlet.class);
    private final Gson gson = new GsonBuilder().create();
    private final StatisticsService statisticsService = new StatisticsService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get overall statistics
                Map<String, Object> stats = statisticsService.getOverallStatistics();
                sendJsonResponse(response, HttpServletResponse.SC_OK, stats);
            } else if (pathInfo.equals("/workload")) {
                // Get member workload statistics
                Map<String, Object> stats = statisticsService.getMemberWorkloadStatistics();
                sendJsonResponse(response, HttpServletResponse.SC_OK, stats);
            } else if (pathInfo.startsWith("/project/")) {
                // Get project statistics: GET /api/statistics/project/{id}
                String[] parts = pathInfo.split("/");
                int projectId = Integer.parseInt(parts[2]);
                Map<String, Object> stats = statisticsService.getProjectStatistics(projectId);
                sendJsonResponse(response, HttpServletResponse.SC_OK, stats);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid statistics endpoint");
            }
        } catch (Exception e) {
            logger.error("Error in GET /api/statistics", e);
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
