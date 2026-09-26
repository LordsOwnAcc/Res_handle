package com.resumeintel.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ai_analysis")
public class AIAnalysisEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "resume_id", nullable = false)
    private Long resumeId;
    @Column(columnDefinition = "text", nullable = false)
    private String summary;
    @Column(name = "best_target_roles")
    private String bestTargetRoles = "";
    @Column(name = "suitable_jd_categories")
    private String suitableJdCategories = "";
    @Column(name = "strong_skills")
    private String strongSkills = "";
    @Column(name = "weak_areas")
    private String weakAreas = "";
    @Column(name = "ats_keywords")
    private String atsKeywords = "";
    /** Certainty of extraction/classification ONLY — never a judgment about employability. */
    @Column(name = "extraction_confidence")
    private float extractionConfidence;
    @Column(name = "generated_at")
    private Instant generatedAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getBestTargetRoles() { return bestTargetRoles; }
    public void setBestTargetRoles(String bestTargetRoles) { this.bestTargetRoles = bestTargetRoles; }
    public String getSuitableJdCategories() { return suitableJdCategories; }
    public void setSuitableJdCategories(String suitableJdCategories) { this.suitableJdCategories = suitableJdCategories; }
    public String getStrongSkills() { return strongSkills; }
    public void setStrongSkills(String strongSkills) { this.strongSkills = strongSkills; }
    public String getWeakAreas() { return weakAreas; }
    public void setWeakAreas(String weakAreas) { this.weakAreas = weakAreas; }
    public String getAtsKeywords() { return atsKeywords; }
    public void setAtsKeywords(String atsKeywords) { this.atsKeywords = atsKeywords; }
    public float getExtractionConfidence() { return extractionConfidence; }
    public void setExtractionConfidence(float extractionConfidence) { this.extractionConfidence = extractionConfidence; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }
}
