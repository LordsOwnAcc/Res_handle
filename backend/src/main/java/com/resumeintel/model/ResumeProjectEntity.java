package com.resumeintel.model;

import jakarta.persistence.*;

@Entity
@Table(name = "resume_project")
public class ResumeProjectEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "resume_id", nullable = false)
    private Long resumeId;
    private String name;
    /** Comma-joined technology list. */
    private String technologies = "";
    private String description = "";
    private String impact = "";
    private String link = "";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTechnologies() { return technologies; }
    public void setTechnologies(String technologies) { this.technologies = technologies; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImpact() { return impact; }
    public void setImpact(String impact) { this.impact = impact; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}
