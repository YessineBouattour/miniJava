package com.projectmanagement.model;

public class TaskSkill {
    private int taskId;
    private int skillId;
    private String skillName;
    private int requiredLevel; // minimum proficiency required

    public TaskSkill() {
    }

    public TaskSkill(int taskId, int skillId, int requiredLevel) {
        this.taskId = taskId;
        this.skillId = skillId;
        this.requiredLevel = requiredLevel;
    }

    // Getters and Setters
    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    @Override
    public String toString() {
        return "TaskSkill{" +
                "taskId=" + taskId +
                ", skillId=" + skillId +
                ", skillName='" + skillName + '\'' +
                ", requiredLevel=" + requiredLevel +
                '}';
    }
}
