package com.projectmanagement.service;

import com.projectmanagement.dao.AlertDAO;
import com.projectmanagement.dao.MemberDAO;
import com.projectmanagement.dao.TaskDAO;
import com.projectmanagement.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Intelligent Task Allocation Algorithm
 * Uses a heuristic approach combining multiple factors:
 * - Skills matching
 * - Workload balancing
 * - Priority and deadlines
 * - Task dependencies
 */
public class TaskAllocationService {
    private static final Logger logger = LoggerFactory.getLogger(TaskAllocationService.class);
    
    private final TaskDAO taskDAO;
    private final MemberDAO memberDAO;
    private final AlertDAO alertDAO;

    public TaskAllocationService() {
        this.taskDAO = new TaskDAO();
        this.memberDAO = new MemberDAO();
        this.alertDAO = new AlertDAO();
    }

    /**
     * Main allocation method - assigns all unassigned tasks in a project
     */
    public AllocationResult allocateTasks(int projectId) throws SQLException {
        logger.info("Starting task allocation for project: {}", projectId);
        
        // Get all unassigned tasks
        List<Task> unassignedTasks = taskDAO.findUnassignedByProject(projectId);
        List<Member> availableMembers = memberDAO.findAll();
        
        if (unassignedTasks.isEmpty()) {
            logger.info("No unassigned tasks found for project: {}", projectId);
            return new AllocationResult(0, 0, "No unassigned tasks");
        }
        
        if (availableMembers.isEmpty()) {
            logger.warn("No available members found");
            return new AllocationResult(0, unassignedTasks.size(), "No available members");
        }
        
        // Sort tasks by priority and deadline
        List<Task> sortedTasks = prioritizeTasks(unassignedTasks);
        
        int assignedCount = 0;
        int failedCount = 0;
        
        // Allocate each task
        for (Task task : sortedTasks) {
            try {
                Member bestMember = findBestMember(task, availableMembers);
                
                if (bestMember != null) {
                    assignTaskToMember(task, bestMember);
                    
                    // Update member workload
                    bestMember.setCurrentWorkload(
                        bestMember.getCurrentWorkload() + task.getEstimatedHours()
                    );
                    memberDAO.updateWorkload(bestMember.getId(), bestMember.getCurrentWorkload());
                    
                    assignedCount++;
                    logger.info("Assigned task '{}' to member '{}'", task.getTitle(), bestMember.getName());
                    
                    // Check for overload
                    if (bestMember.isOverloaded()) {
                        createOverloadAlert(bestMember, task);
                    }
                } else {
                    failedCount++;
                    logger.warn("Could not find suitable member for task: {}", task.getTitle());
                    createNoSuitableMemberAlert(task, projectId);
                }
            } catch (Exception e) {
                failedCount++;
                logger.error("Error assigning task: {}", task.getTitle(), e);
            }
        }
        
        String message = String.format("Assigned %d tasks, failed %d", assignedCount, failedCount);
        logger.info("Allocation complete: {}", message);
        
        return new AllocationResult(assignedCount, failedCount, message);
    }

    /**
     * Find the best member for a task using scoring algorithm
     */
    private Member findBestMember(Task task, List<Member> members) {
        Member bestMember = null;
        double bestScore = -1;
        
        for (Member member : members) {
            double score = calculateMemberScore(task, member);
            
            if (score > bestScore) {
                bestScore = score;
                bestMember = member;
            }
        }
        
        // Only return member if score is above threshold
        return bestScore >= 0.3 ? bestMember : null;
    }

    /**
     * Calculate a score for how suitable a member is for a task
     * Score components:
     * - Skill match (40%)
     * - Availability (30%)
     * - Current workload balance (20%)
     * - Priority bonus (10%)
     */
    private double calculateMemberScore(Task task, Member member) {
        double skillScore = calculateSkillScore(task, member);
        double availabilityScore = calculateAvailabilityScore(task, member);
        double workloadScore = calculateWorkloadScore(member);
        double priorityBonus = task.getPriorityScore() * 0.025; // 0-0.1
        
        // If member doesn't have required skills, return 0
        if (skillScore == 0) {
            return 0;
        }
        
        // If member doesn't have enough availability, return 0
        if (availabilityScore == 0) {
            return 0;
        }
        
        // Weighted combination
        double totalScore = (skillScore * 0.4) + 
                           (availabilityScore * 0.3) + 
                           (workloadScore * 0.2) + 
                           priorityBonus;
        
        logger.debug("Member {} score for task {}: {:.3f} (skill={:.2f}, avail={:.2f}, workload={:.2f})",
                    member.getName(), task.getTitle(), totalScore, skillScore, availabilityScore, workloadScore);
        
        return totalScore;
    }

    /**
     * Calculate skill matching score (0-1)
     */
    private double calculateSkillScore(Task task, Member member) {
        List<TaskSkill> requiredSkills = task.getRequiredSkills();
        
        if (requiredSkills.isEmpty()) {
            return 0.5; // Neutral score if no skills required
        }
        
        int matchedSkills = 0;
        int totalSkillLevel = 0;
        int maxSkillLevel = 0;
        
        for (TaskSkill taskSkill : requiredSkills) {
            maxSkillLevel += taskSkill.getRequiredLevel();
            
            // Check if member has this skill
            Optional<MemberSkill> memberSkill = member.getSkills().stream()
                .filter(ms -> ms.getSkillId() == taskSkill.getSkillId())
                .findFirst();
            
            if (memberSkill.isPresent()) {
                int proficiency = memberSkill.get().getProficiencyLevel();
                if (proficiency >= taskSkill.getRequiredLevel()) {
                    matchedSkills++;
                    totalSkillLevel += proficiency;
                } else {
                    // Has skill but not sufficient level
                    return 0;
                }
            } else {
                // Missing required skill
                return 0;
            }
        }
        
        if (requiredSkills.size() == matchedSkills) {
            // All skills matched, score based on proficiency level
            return Math.min(1.0, (double) totalSkillLevel / maxSkillLevel);
        }
        
        return 0;
    }

    /**
     * Calculate availability score (0-1)
     */
    private double calculateAvailabilityScore(Task task, Member member) {
        double availableHours = member.getAvailableHours();
        double requiredHours = task.getEstimatedHours();
        
        if (availableHours < requiredHours) {
            return 0; // Not enough availability
        }
        
        // Score higher for members with just enough time (better balance)
        // but don't penalize too much for having more availability
        double ratio = requiredHours / availableHours;
        
        if (ratio >= 0.5) {
            return 1.0; // Optimal use of time
        } else {
            return 0.5 + ratio; // Still good but not optimal
        }
    }

    /**
     * Calculate workload balance score (0-1)
     * Higher score for less loaded members
     */
    private double calculateWorkloadScore(Member member) {
        double workloadPercentage = member.getWorkloadPercentage();
        
        if (workloadPercentage >= 100) {
            return 0; // Already at or over capacity
        }
        
        // Linear decrease from 1.0 (0% loaded) to 0.1 (100% loaded)
        return 1.0 - (workloadPercentage / 100.0 * 0.9);
    }

    /**
     * Sort tasks by priority and deadline
     */
    private List<Task> prioritizeTasks(List<Task> tasks) {
        return tasks.stream()
            .sorted((t1, t2) -> {
                // First by priority
                int priorityCompare = Integer.compare(t2.getPriorityScore(), t1.getPriorityScore());
                if (priorityCompare != 0) {
                    return priorityCompare;
                }
                
                // Then by deadline
                if (t1.getDeadline() != null && t2.getDeadline() != null) {
                    return t1.getDeadline().compareTo(t2.getDeadline());
                }
                
                return 0;
            })
            .collect(Collectors.toList());
    }

    /**
     * Assign a task to a member
     */
    private void assignTaskToMember(Task task, Member member) throws SQLException {
        taskDAO.assignTask(task.getId(), member.getId());
        task.setAssignedMemberId(member.getId());
        task.setAssignedMemberName(member.getName());
    }

    /**
     * Create alert for member overload
     */
    private void createOverloadAlert(Member member, Task task) throws SQLException {
        Alert alert = new Alert();
        alert.setType(Alert.AlertType.OVERLOAD);
        alert.setSeverity(Alert.Severity.HIGH);
        alert.setTitle("Member Overload Detected");
        alert.setMessage(String.format(
            "Member '%s' is now overloaded with %.1f hours (%.1f%% capacity) after assigning task '%s'",
            member.getName(), member.getCurrentWorkload(), 
            member.getWorkloadPercentage(), task.getTitle()
        ));
        alert.setMemberId(member.getId());
        alert.setTaskId(task.getId());
        
        alertDAO.create(alert);
    }

    /**
     * Create alert when no suitable member found
     */
    private void createNoSuitableMemberAlert(Task task, int projectId) throws SQLException {
        Alert alert = new Alert();
        alert.setType(Alert.AlertType.CONFLICT);
        alert.setSeverity(Alert.Severity.CRITICAL);
        alert.setTitle("No Suitable Member Found");
        alert.setMessage(String.format(
            "Could not find a suitable member for task '%s'. " +
            "Required skills may not be available or all members are at capacity.",
            task.getTitle()
        ));
        alert.setProjectId(projectId);
        alert.setTaskId(task.getId());
        
        alertDAO.create(alert);
    }

    /**
     * Result object for allocation operation
     */
    public static class AllocationResult {
        private final int assignedCount;
        private final int failedCount;
        private final String message;

        public AllocationResult(int assignedCount, int failedCount, String message) {
            this.assignedCount = assignedCount;
            this.failedCount = failedCount;
            this.message = message;
        }

        public int getAssignedCount() {
            return assignedCount;
        }

        public int getFailedCount() {
            return failedCount;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSuccess() {
            return failedCount == 0;
        }
    }
}
