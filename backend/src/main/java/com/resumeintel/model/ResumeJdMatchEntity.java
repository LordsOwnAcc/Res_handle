package com.resumeintel.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

@Entity
@Table(name = "resume_jd_match", uniqueConstraints = @UniqueConstraint(columnNames = {"resume_id", "jd_id"}))
public class ResumeJdMatchEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "resume_id", nullable = false)
    private Long resumeId;
    @Column(name = "jd_id", nullable = false)
    private Long jdId;
    @Column(name = "overall_score")
    private float overallScore;
    @Column(name = "required_match_score")
    private float requiredMatchScore;
    @Column(name = "preferred_match_score")
    private float preferredMatchScore;
    @Column(name = "experience_match_score")
    private float experienceMatchScore;
    @Column(name = "project_match_score")
    private float projectMatchScore;
    @Column(name = "education_match_score")
    private float educationMatchScore;
    @Column(name = "keyword_match_score")
    private float keywordMatchScore;

    /** JSON-encoded MatchBreakdown (strong/partial/missing + evidence). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "breakdown_json", columnDefinition = "jsonb")
    private String breakdownJson;

    @Column(name = "computed_at")
    private Instant computedAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }
    public Long getJdId() { return jdId; }
    public void setJdId(Long jdId) { this.jdId = jdId; }
    public float getOverallScore() { return overallScore; }
    public void setOverallScore(float overallScore) { this.overallScore = overallScore; }
    public float getRequiredMatchScore() { return requiredMatchScore; }
    public void setRequiredMatchScore(float requiredMatchScore) { this.requiredMatchScore = requiredMatchScore; }
    public float getPreferredMatchScore() { return preferredMatchScore; }
    public void setPreferredMatchScore(float preferredMatchScore) { this.preferredMatchScore = preferredMatchScore; }
    public float getExperienceMatchScore() { return experienceMatchScore; }
    public void setExperienceMatchScore(float experienceMatchScore) { this.experienceMatchScore = experienceMatchScore; }
    public float getProjectMatchScore() { return projectMatchScore; }
    public void setProjectMatchScore(float projectMatchScore) { this.projectMatchScore = projectMatchScore; }
    public float getEducationMatchScore() { return educationMatchScore; }
    public void setEducationMatchScore(float educationMatchScore) { this.educationMatchScore = educationMatchScore; }
    public float getKeywordMatchScore() { return keywordMatchScore; }
    public void setKeywordMatchScore(float keywordMatchScore) { this.keywordMatchScore = keywordMatchScore; }
    public String getBreakdownJson() { return breakdownJson; }
    public void setBreakdownJson(String breakdownJson) { this.breakdownJson = breakdownJson; }
    public Instant getComputedAt() { return computedAt; }
    public void setComputedAt(Instant computedAt) { this.computedAt = computedAt; }
}
