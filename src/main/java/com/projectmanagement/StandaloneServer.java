package com.projectmanagement;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import com.projectmanagement.dao.*;
import com.projectmanagement.model.*;
import com.projectmanagement.service.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.*;

public class StandaloneServer {
    private static final int PORT = 8080;
    private static final Gson gson = new Gson();
    
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // Servir les fichiers statiques
        server.createContext("/", new StaticFileHandler());
        
        // API endpoints
        server.createContext("/api/members/", new MemberHandler());
        server.createContext("/api/projects/", new ProjectHandler());
        server.createContext("/api/tasks/", new TaskHandler());
        server.createContext("/api/skills/", new SkillHandler());
        server.createContext("/api/allocate/", new AllocationHandler());
        server.createContext("/api/alerts/", new AlertHandler());
        server.createContext("/api/statistics/", new StatisticsHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("======================================");
        System.out.println("  Serveur démarré avec succès !");
        System.out.println("======================================");
        System.out.println("");
        System.out.println("URL: http://localhost:" + PORT);
        System.out.println("");
        System.out.println("Appuyez sur Ctrl+C pour arrêter");
        System.out.println("======================================");
    }
    
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            
            String filePath = "src/main/webapp" + path;
            File file = new File(filePath);
            
            if (file.exists() && !file.isDirectory()) {
                String contentType = getContentType(filePath);
                byte[] bytes = Files.readAllBytes(file.toPath());
                
                exchange.getResponseHeaders().add("Content-Type", contentType);
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
        
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=UTF-8";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".json")) return "application/json";
            return "text/plain";
        }
    }
    
    static class MemberHandler implements HttpHandler {
        private MemberDAO memberDAO = new MemberDAO();
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            try {
                String response = "";
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();
                
                if (method.equals("GET") && path.endsWith("/")) {
                    response = gson.toJson(memberDAO.getAllMembers());
                } else if (method.equals("POST")) {
                    String body = readBody(exchange);
                    Member member = gson.fromJson(body, Member.class);
                    memberDAO.createMember(member);
                    response = gson.toJson(member);
                } else if (method.equals("PUT")) {
                    String body = readBody(exchange);
                    Member member = gson.fromJson(body, Member.class);
                    memberDAO.updateMember(member);
                    response = gson.toJson(member);
                } else if (method.equals("DELETE")) {
                    int id = extractId(path);
                    memberDAO.deleteMember(id);
                    response = "{\"success\": true}";
                }
                
                sendResponse(exchange, response);
            } catch (Exception e) {
                sendError(exchange, e);
            }
        }
    }
    
    static class ProjectHandler implements HttpHandler {
        private ProjectDAO projectDAO = new ProjectDAO();
        private TaskDAO taskDAO = new TaskDAO();
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            try {
                String response = "";
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();
                
                if (method.equals("GET") && path.matches(".*/\\d+/tasks/?")) {
                    int projectId = Integer.parseInt(path.split("/")[3]);
                    response = gson.toJson(taskDAO.getTasksByProject(projectId));
                } else if (method.equals("GET") && path.endsWith("/")) {
                    response = gson.toJson(projectDAO.getAllProjects());
                } else if (method.equals("POST")) {
                    String body = readBody(exchange);
                    Project project = gson.fromJson(body, Project.class);
                    projectDAO.createProject(project);
                    response = gson.toJson(project);
                } else if (method.equals("PUT")) {
                    String body = readBody(exchange);
                    Project project = gson.fromJson(body, Project.class);
                    projectDAO.updateProject(project);
                    response = gson.toJson(project);
                } else if (method.equals("DELETE")) {
                    int id = extractId(path);
                    projectDAO.deleteProject(id);
                    response = "{\"success\": true}";
                }
                
                sendResponse(exchange, response);
            } catch (Exception e) {
                sendError(exchange, e);
            }
        }
    }
    
    static class TaskHandler implements HttpHandler {
        private TaskDAO taskDAO = new TaskDAO();
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            try {
                String response = "";
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();
                
                if (method.equals("GET")) {
                    int id = extractId(path);
                    response = gson.toJson(taskDAO.getTaskById(id));
                } else if (method.equals("POST")) {
                    String body = readBody(exchange);
                    Task task = gson.fromJson(body, Task.class);
                    taskDAO.createTask(task);
                    response = gson.toJson(task);
                } else if (method.equals("PUT")) {
                    String body = readBody(exchange);
                    Task task = gson.fromJson(body, Task.class);
                    taskDAO.updateTask(task);
                    response = gson.toJson(task);
                } else if (method.equals("DELETE")) {
                    int id = extractId(path);
                    taskDAO.deleteTask(id);
                    response = "{\"success\": true}";
                }
                
                sendResponse(exchange, response);
            } catch (Exception e) {
                sendError(exchange, e);
            }
        }
    }
    
    static class SkillHandler implements HttpHandler {
        private SkillDAO skillDAO = new SkillDAO();
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            try {
                String response = gson.toJson(skillDAO.getAllSkills());
                sendResponse(exchange, response);
            } catch (Exception e) {
                sendError(exchange, e);
            }
        }
    }
    
    static class AllocationHandler implements HttpHandler {
        private TaskAllocationService allocationService = new TaskAllocationService();
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            try {
                int projectId = extractId(exchange.getRequestURI().getPath());
                Map<String, Object> result = allocationService.allocateTasksToMembers(projectId);
                String response = gson.toJson(result);
                sendResponse(exchange, response);
            } catch (Exception e) {
                sendError(exchange, e);
            }
        }
    }
    
    static class AlertHandler implements HttpHandler {
        private AlertDAO alertDAO = new AlertDAO();
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            try {
                String response = "";
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();
                String query = exchange.getRequestURI().getQuery();
                
                if (method.equals("GET") && path.endsWith("/count")) {
                    int count = alertDAO.getUnreadAlertCount();
                    response = "{\"count\": " + count + "}";
                } else if (method.equals("GET") && query != null && query.contains("unread=true")) {
                    response = gson.toJson(alertDAO.getUnreadAlerts());
                } else if (method.equals("GET")) {
                    response = gson.toJson(alertDAO.getAllAlerts());
                } else if (method.equals("DELETE")) {
                    int id = extractId(path);
                    alertDAO.deleteAlert(id);
                    response = "{\"success\": true}";
                } else if (method.equals("PUT") && path.contains("/read")) {
                    int id = extractId(path);
                    alertDAO.markAlertAsRead(id);
                    response = "{\"success\": true}";
                }
                
                sendResponse(exchange, response);
            } catch (Exception e) {
                sendError(exchange, e);
            }
        }
    }
    
    static class StatisticsHandler implements HttpHandler {
        private StatisticsService statisticsService = new StatisticsService();
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORS(exchange);
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            try {
                String response = "";
                String path = exchange.getRequestURI().getPath();
                
                if (path.contains("/workload")) {
                    response = gson.toJson(statisticsService.getMemberWorkloadDistribution());
                } else if (path.matches(".*/project/\\d+/?")) {
                    int projectId = Integer.parseInt(path.split("/")[4]);
                    response = gson.toJson(statisticsService.getProjectStatistics(projectId));
                } else {
                    response = gson.toJson(statisticsService.getOverallStatistics());
                }
                
                sendResponse(exchange, response);
            } catch (Exception e) {
                sendError(exchange, e);
            }
        }
    }
    
    private static void setCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }
    
    private static String readBody(HttpExchange exchange) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "UTF-8"));
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        return body.toString();
    }
    
    private static int extractId(String path) {
        String[] parts = path.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            try {
                return Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                continue;
            }
        }
        return 0;
    }
    
    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
    
    private static void sendError(HttpExchange exchange, Exception e) throws IOException {
        e.printStackTrace();
        String error = "{\"success\": false, \"error\": \"" + e.getMessage().replace("\"", "'") + "\"}";
        byte[] bytes = error.getBytes("UTF-8");
        exchange.sendResponseHeaders(500, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
