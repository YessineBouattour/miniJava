package com.projectmanagement.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.projectmanagement.dao.MemberDAO;
import com.projectmanagement.model.Member;
import com.projectmanagement.model.MemberSkill;
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

@WebServlet("/api/members/*")
public class MemberServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(MemberServlet.class);
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private final MemberDAO memberDAO = new MemberDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all members
                List<Member> members = memberDAO.findAll();
                sendJsonResponse(response, HttpServletResponse.SC_OK, members);
            } else {
                // Get specific member
                int id = Integer.parseInt(pathInfo.substring(1));
                Member member = memberDAO.findById(id);
                
                if (member != null) {
                    sendJsonResponse(response, HttpServletResponse.SC_OK, member);
                } else {
                    sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Member not found");
                }
            }
        } catch (Exception e) {
            logger.error("Error in GET /api/members", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo != null && pathInfo.contains("/skills")) {
                // Add skill to member: POST /api/members/{id}/skills
                handleAddSkill(request, response, pathInfo);
            } else {
                // Create new member
                String json = readRequestBody(request);
                Member member = gson.fromJson(json, Member.class);
                
                int id = memberDAO.create(member);
                member.setId(id);
                
                sendJsonResponse(response, HttpServletResponse.SC_CREATED, member);
            }
        } catch (Exception e) {
            logger.error("Error in POST /api/members", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            String json = readRequestBody(request);
            Member member = gson.fromJson(json, Member.class);
            
            memberDAO.update(member);
            
            sendJsonResponse(response, HttpServletResponse.SC_OK, member);
        } catch (Exception e) {
            logger.error("Error in PUT /api/members", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo != null && pathInfo.contains("/skills")) {
                // Remove skill from member: DELETE /api/members/{id}/skills/{skillId}
                handleRemoveSkill(request, response, pathInfo);
            } else {
                int id = Integer.parseInt(pathInfo.substring(1));
                memberDAO.delete(id);
                
                sendSuccessResponse(response, "Member deleted successfully");
            }
        } catch (Exception e) {
            logger.error("Error in DELETE /api/members", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void handleAddSkill(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws Exception {
        
        // Parse URL: /api/members/{id}/skills
        String[] parts = pathInfo.split("/");
        int memberId = Integer.parseInt(parts[1]);
        
        String json = readRequestBody(request);
        MemberSkill skill = gson.fromJson(json, MemberSkill.class);
        
        memberDAO.addSkill(memberId, skill.getSkillId(), skill.getProficiencyLevel());
        
        sendSuccessResponse(response, "Skill added successfully");
    }

    private void handleRemoveSkill(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws Exception {
        
        // Parse URL: /api/members/{id}/skills/{skillId}
        String[] parts = pathInfo.split("/");
        int memberId = Integer.parseInt(parts[1]);
        int skillId = Integer.parseInt(parts[3]);
        
        memberDAO.removeSkill(memberId, skillId);
        
        sendSuccessResponse(response, "Skill removed successfully");
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
