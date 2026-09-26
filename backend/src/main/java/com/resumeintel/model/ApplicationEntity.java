package com.resumeintel.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "application")
public class ApplicationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "company_id")
    private Long companyId;
    @Column(name = "jd_id")
    private Long jdId;

    /** The EXACT resume version used. Never repointed by a later resume edit/version bump. */
    @Column(name = "resume_id", nullable = false)
    private Long resumeId;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;
    @Column(name = "application_url")
    private String applicationUrl = "";
    @Column(name = "date_applied")
    private Instant dateApplied;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.SAVED;

    @Column(name = "recruiter_contact")
    private String recruiterContact = "";
    private String location = "";
    @Column(name = "salary_text")
    private String salaryText = "";
    @Column(name = "interview_date")
    private Instant interviewDate;
    @Column(name = "follow_up_date")
    private Instant followUpDate;
    @Column(columnDefinition = "text")
    private String notes = "";
    @Column(name = "match_score_at_apply_time")
    private Float matchScoreAtApplyTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public Long getJdId() { return jdId; }
    public void setJdId(Long jdId) { this.jdId = jdId; }
    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getApplicationUrl() { return applicationUrl; }
    public void setApplicationUrl(String applicationUrl) { this.applicationUrl = applicationUrl; }
    public Instant getDateApplied() { return dateApplied; }
    public void setDateApplied(Instant dateApplied) { this.dateApplied = dateApplied; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public String getRecruiterContact() { return recruiterContact; }
    public void setRecruiterContact(String recruiterContact) { this.recruiterContact = recruiterContact; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getSalaryText() { return salaryText; }
    public void setSalaryText(String salaryText) { this.salaryText = salaryText; }
    public Instant getInterviewDate() { return interviewDate; }
    public void setInterviewDate(Instant interviewDate) { this.interviewDate = interviewDate; }
    public Instant getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(Instant followUpDate) { this.followUpDate = followUpDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Float getMatchScoreAtApplyTime() { return matchScoreAtApplyTime; }
    public void setMatchScoreAtApplyTime(Float matchScoreAtApplyTime) { this.matchScoreAtApplyTime = matchScoreAtApplyTime; }
}
