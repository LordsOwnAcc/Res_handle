package com.resumeintel.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;

public class JdDtos {
    public record ImportJdRequest(@NotBlank String rawText) {}

    public record JdSummary(
        Long id, String title, String companyName, String location,
        Float experienceRequiredYears, List<String> requiredSkills, String aiSummary, Instant createdAt
    ) {}

    public record JdDetail(
        Long id, String title, String companyName, String location, String employmentType,
        Float experienceRequiredYears, String educationRequirement, String salaryText, String applicationUrl,
        List<String> requiredSkills, List<String> preferredSkills, List<String> responsibilities,
        List<String> keywords, String aiSummary, String rawText, Instant createdAt
    ) {}
}
