package com.resumeintel.dto;

import com.resumeintel.model.EducationLevel;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;

public class ResumeDtos {

    public record SkillInput(@NotBlank String skillName, String category) {}
    public record ProjectInput(@NotBlank String name, List<String> technologies, String description, String impact, String link) {}
    public record ExperienceInput(@NotBlank String company, String role, Integer durationMonths, String responsibilities, List<String> technologies) {}

    public record CreateResumeRequest(
        @NotBlank String name,
        String versionLabel,
        String versionGroupKey,
        String description,
        List<String> targetRoles,
        List<String> targetIndustries,
        EducationLevel educationLevel,
        String customNotes,
        List<SkillInput> skills,
        List<ProjectInput> projects,
        List<ExperienceInput> experience
    ) {}

    public record ResumeSummary(
        Long id, String name, String versionLabel, String versionGroupKey,
        String description, List<String> targetRoles, Instant lastModifiedAt
    ) {}

    public record ResumeDetail(
        Long id, String name, String versionLabel, String versionGroupKey,
        String description, List<String> targetRoles, List<String> targetIndustries,
        String educationLevel, String customNotes,
        List<SkillInput> skills, List<ProjectInput> projects, List<ExperienceInput> experience,
        Instant createdAt, Instant lastModifiedAt
    ) {}
}
