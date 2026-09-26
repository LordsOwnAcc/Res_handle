package com.resumeintel.service.matching;

import java.util.List;

public record ResumeProfile(
    Long resumeId,
    List<String> skills,
    List<ProjectFact> projects,
    int experienceMonths,
    String educationLevel, // NONE|DIPLOMA|BACHELORS|MASTERS|PHD
    List<String> atsKeywords
) {}
