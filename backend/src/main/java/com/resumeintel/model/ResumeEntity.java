package com.resumeintel.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "resume")
public class ResumeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "folder_id")
    private Long folderId;

    @Column(nullable = false)
    private String name;

    @Column(name = "version_label", nullable = false)
    private String versionLabel = "v1";

    /** Groups R03.1/R03.2/R03.3 as one lineage — see spec's resume versioning requirement. */
    @Column(name = "version_group_key", nullable = false)
    private String versionGroupKey;

    @Column(name = "document_id")
    private Long documentId;

    private String description = "";

    @Column(name = "target_roles")
    private String targetRoles = "";

    @Column(name = "target_industries")
    private String targetIndustries = "";

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level")
    private EducationLevel educationLevel = EducationLevel.NONE;

    @Column(name = "custom_notes")
    private String customNotes = "";

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "last_modified_at")
    private Instant lastModifiedAt = Instant.now();

    @Column(name = "is_user_edited")
    private boolean userEdited = false;

    @PreUpdate
    void onUpdate() { this.lastModifiedAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVersionLabel() { return versionLabel; }
    public void setVersionLabel(String versionLabel) { this.versionLabel = versionLabel; }
    public String getVersionGroupKey() { return versionGroupKey; }
    public void setVersionGroupKey(String versionGroupKey) { this.versionGroupKey = versionGroupKey; }
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTargetRoles() { return targetRoles; }
    public void setTargetRoles(String targetRoles) { this.targetRoles = targetRoles; }
    public String getTargetIndustries() { return targetIndustries; }
    public void setTargetIndustries(String targetIndustries) { this.targetIndustries = targetIndustries; }
    public EducationLevel getEducationLevel() { return educationLevel; }
    public void setEducationLevel(EducationLevel educationLevel) { this.educationLevel = educationLevel; }
    public String getCustomNotes() { return customNotes; }
    public void setCustomNotes(String customNotes) { this.customNotes = customNotes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(Instant lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
    public boolean isUserEdited() { return userEdited; }
    public void setUserEdited(boolean userEdited) { this.userEdited = userEdited; }
}
