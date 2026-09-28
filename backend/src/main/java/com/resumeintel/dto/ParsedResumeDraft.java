package com.resumeintel.dto;

import com.resumeintel.dto.ResumeDtos.ProjectInput;
import com.resumeintel.dto.ResumeDtos.SkillInput;
import java.util.List;

/** A DRAFT extracted from an uploaded file. Nothing is saved until the user reviews and submits it. */
public record ParsedResumeDraft(
    String name,
    String email,
    String phone,
    String github,
    String linkedin,
    String description,
    List<String> targetRoles,
    String educationLevel,
    int experienceMonths,
    List<SkillInput> skills,
    List<ProjectInput> projects,
    List<String> warnings
) {}
