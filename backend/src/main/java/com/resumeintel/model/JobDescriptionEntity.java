package com.resumeintel.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "job_description")
public class JobDescriptionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "company_id")
    private Long companyId;
    private String title;
    @Column(name = "raw_text", nullable = false, columnDefinition = "text")
    private String rawText;
    private String location = "";
    @Column(name = "employment_type")
    private String employmentType = "";
    @Column(name = "experience_required_years")
    private Float experienceRequiredYears;
    @Enumerated(EnumType.STRING)
    @Column(name = "education_requirement")
    private EducationLevel educationRequirement = EducationLevel.NONE;
    @Column(name = "salary_text")
    private String salaryText = "";
    @Column(name = "application_url")
    private String applicationUrl = "";
    @Column(name = "required_skills", columnDefinition = "text")
    private String requiredSkills = "";
    @Column(name = "preferred_skills", columnDefinition = "text")
    private String preferredSkills = "";
    @Column(columnDefinition = "text")
    private String responsibilities = "";
    @Column(columnDefinition = "text")
    private String keywords = "";
    @Column(name = "ai_summary", columnDefinition = "text")
    private String aiSummary = "";
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
    public Float getExperienceRequiredYears() { return experienceRequiredYears; }
    public void setExperienceRequiredYears(Float experienceRequiredYears) { this.experienceRequiredYears = experienceRequiredYears; }
    public EducationLevel getEducationRequirement() { return educationRequirement; }
    public void setEducationRequirement(EducationLevel educationRequirement) { this.educationRequirement = educationRequirement; }
    public String getSalaryText() { return salaryText; }
    public void setSalaryText(String salaryText) { this.salaryText = salaryText; }
    public String getApplicationUrl() { return applicationUrl; }
    public void setApplicationUrl(String applicationUrl) { this.applicationUrl = applicationUrl; }
    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
    public String getPreferredSkills() { return preferredSkills; }
    public void setPreferredSkills(String preferredSkills) { this.preferredSkills = preferredSkills; }
    public String getResponsibilities() { return responsibilities; }
    public void setResponsibilities(String responsibilities) { this.responsibilities = responsibilities; }
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
