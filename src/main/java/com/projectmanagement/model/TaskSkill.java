package com.projectmanagement.model;

public class TaskSkill {
    // private int taskId;
    // private int skillId;
    // private String skillName; //on peut la trouver via une jointure skillId
    private Task task;
    private Skill skill;
    private int requiredLevel; // minimum proficiency required

    public TaskSkill() {
    }

    public TaskSkill(Task task, Skill skill, int requiredLevel) {
        this.task = task;
        this.skill = skill;
        this.requiredLevel = requiredLevel;
    }

    // Getters and Setters
    // public int getTaskId() {
    //     return taskId;
    // }

    // public void setTaskId(int taskId) {
    //     this.taskId = taskId;
    // }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    // public int getSkillId() {
    //     return skillId;
    // }

    // public void setSkillId(int skillId) {
    //     this.skillId = skillId;
    // }

    // public String getSkillName() {
    //     return skillName;
    // }

    // public void setSkillName(String skillName) {
    //     this.skillName = skillName;
    // }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
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
                "taskId=" + task.getId() +
                ", skillId=" + skill.getId() +
                ", skillName='" + skill.getName() + '\'' +
                ", requiredLevel=" + requiredLevel +
                '}';
    }
}
