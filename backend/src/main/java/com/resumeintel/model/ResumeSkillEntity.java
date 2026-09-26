package com.resumeintel.model;

import jakarta.persistence.*;

@Entity
@Table(name = "resume_skill")
public class ResumeSkillEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "resume_id", nullable = false)
    private Long resumeId;
    @Column(name = "skill_name", nullable = false)
    private String skillName;
    private String category = "tool"; // language|framework|database|cloud|devops|ml|tool
    @Column(name = "evidence_project_id")
    private Long evidenceProjectId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Long getEvidenceProjectId() { return evidenceProjectId; }
    public void setEvidenceProjectId(Long evidenceProjectId) { this.evidenceProjectId = evidenceProjectId; }
}
