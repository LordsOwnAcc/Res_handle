package com.resumeintel.service.matching;

import java.util.List;

public record JdProfile(
    Long jdId,
    List<String> requiredSkills,
    List<String> preferredSkills,
    Float experienceRequiredYears,
    String minEducation,
    List<String> responsibilities,
    List<String> keywords
) {}
