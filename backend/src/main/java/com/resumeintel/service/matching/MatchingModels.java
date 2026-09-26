package com.resumeintel.service.matching;

import java.util.List;

public record ProjectFact(String name, List<String> technologies, String description) {}

public record ResumeProfile(
    Long resumeId,
    List<String> skills,
    List<ProjectFact> projects,
    int experienceMonths,
    String educationLevel, // NONE|DIPLOMA|BACHELORS|MASTERS|PHD
    List<String> atsKeywords
) {}

public record JdProfile(
    Long jdId,
    List<String> requiredSkills,
    List<String> preferredSkills,
    Float experienceRequiredYears,
    String minEducation,
    List<String> responsibilities,
    List<String> keywords
) {}

public record SkillEvidence(String skill, List<String> foundIn) {}

public record MatchBreakdown(
    List<SkillEvidence> strongMatches,
    List<SkillEvidence> partialMatches,
    List<String> missing,
    List<String> potentialIssues,
    String disclaimer
) {}

public record MatchResult(
    Long resumeId,
    Long jdId,
    float overall,
    float requiredMatch,
    float preferredMatch,
    float experienceMatch,
    float projectMatch,
    float educationMatch,
    float keywordMatch,
    MatchBreakdown breakdown
) {}
