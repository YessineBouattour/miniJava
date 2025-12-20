package com.projectmanagement.model;

public class MemberSkill {
    private int memberId;
    private int skillId;
    private String skillName;
    private int proficiencyLevel; // 1-5 scale

    public MemberSkill() {
    }

    public MemberSkill(int memberId, int skillId, int proficiencyLevel) {
        this.memberId = memberId;
        this.skillId = skillId;
        this.proficiencyLevel = proficiencyLevel;
    }

    // Getters and Setters
    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
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

    public int getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(int proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }

    @Override
    public String toString() {
        return "MemberSkill{" +
                "memberId=" + memberId +
                ", skillId=" + skillId +
                ", skillName='" + skillName + '\'' +
                ", proficiencyLevel=" + proficiencyLevel +
                '}';
    }
}
