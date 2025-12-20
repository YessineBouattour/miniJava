package com.projectmanagement.model;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Task {
    private int id;
    private int projectId;
    private String title;
    private String description;
    private double estimatedHours;
    private Priority priority;
    private TaskStatus status;
    private Date startDate;
    private Date deadline;
    private Integer assignedMemberId;
    private String assignedMemberName;
    private List<TaskSkill> requiredSkills;
    private List<Integer> dependencies; // IDs of tasks this depends on
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }

    public enum TaskStatus {
        TODO, IN_PROGRESS, COMPLETED, BLOCKED
    }

    public Task() {
        this.requiredSkills = new ArrayList<>();
        this.dependencies = new ArrayList<>();
        this.priority = Priority.MEDIUM;
        this.status = TaskStatus.TODO;
    }

    public Task(int projectId, String title, double estimatedHours) {
        this();
        this.projectId = projectId;
        this.title = title;
        this.estimatedHours = estimatedHours;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public Integer getAssignedMemberId() {
        return assignedMemberId;
    }

    public void setAssignedMemberId(Integer assignedMemberId) {
        this.assignedMemberId = assignedMemberId;
    }

    public String getAssignedMemberName() {
        return assignedMemberName;
    }

    public void setAssignedMemberName(String assignedMemberName) {
        this.assignedMemberName = assignedMemberName;
    }

    public List<TaskSkill> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<TaskSkill> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public List<Integer> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Integer> dependencies) {
        this.dependencies = dependencies;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business methods
    public boolean isAssigned() {
        return assignedMemberId != null;
    }

    public boolean hasDependencies() {
        return !dependencies.isEmpty();
    }

    public int getPriorityScore() {
        switch (priority) {
            case URGENT: return 4;
            case HIGH: return 3;
            case MEDIUM: return 2;
            case LOW: return 1;
            default: return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", estimatedHours=" + estimatedHours +
                ", assignedTo=" + assignedMemberName +
                '}';
    }
}
