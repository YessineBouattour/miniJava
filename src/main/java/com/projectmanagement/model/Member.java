package com.projectmanagement.model;

// import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Member {
    private int id;
    private String name;
    private String prenom; //il faut l'ajouter
    private String email;
    private int weeklyAvailability; // hours per week
    private double currentWorkload; // current hours assigned
    private List<MemberSkill> skills;
    // private Timestamp createdAt;
    // private Timestamp updatedAt;

    public Member() {
        this.skills = new ArrayList<>();
    }

    public Member(String name, String prenom, String email, int weeklyAvailability) {
        this();
        this.name = name;
        this.prenom = prenom;
        this.email = email;
        this.weeklyAvailability = weeklyAvailability;
        this.currentWorkload = 0.0;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getWeeklyAvailability() {
        return weeklyAvailability;
    }

    public void setWeeklyAvailability(int weeklyAvailability) {
        this.weeklyAvailability = weeklyAvailability;
    }

    public double getCurrentWorkload() {
        return currentWorkload;
    }

    public void setCurrentWorkload(double currentWorkload) {
        this.currentWorkload = currentWorkload;
    }

    public List<MemberSkill> getSkills() {
        return skills;
    }

    public void setSkills(List<MemberSkill> skills) {
        this.skills = skills;
    }

    // public Timestamp getCreatedAt() {
    //     return createdAt;
    // }

    // public void setCreatedAt(Timestamp createdAt) {
    //     this.createdAt = createdAt;
    // }

    // public Timestamp getUpdatedAt() {
    //     return updatedAt;
    // }

    // public void setUpdatedAt(Timestamp updatedAt) {
    //     this.updatedAt = updatedAt;
    // }

    // Business methods
    public boolean hasSkill(Skill skill, int requiredLevel) {
        return skills.stream()
                .anyMatch(ms -> ms.getSkill() == skill && ms.getProficiencyLevel() >= requiredLevel);
    }

    public double getAvailableHours() {
        return Math.max(0, weeklyAvailability - currentWorkload);
    }

    public double getWorkloadPercentage() {
        if (weeklyAvailability == 0) return 0;
        return (currentWorkload / weeklyAvailability) * 100;
    }

    public boolean isOverloaded() {
        return currentWorkload > weeklyAvailability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return id == member.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", weeklyAvailability=" + weeklyAvailability +
                ", currentWorkload=" + currentWorkload +
                ", workloadPercentage=" + String.format("%.2f", getWorkloadPercentage()) + "%" +
                '}';
    }
}
