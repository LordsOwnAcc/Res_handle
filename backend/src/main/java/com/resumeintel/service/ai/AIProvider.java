package com.resumeintel.service.ai;

import com.resumeintel.service.matching.ProjectFact;
import com.resumeintel.service.matching.ResumeProfile;
import java.util.List;

/**
 * Nothing else in the backend talks to an LLM directly — everything goes through this
 * interface, so swapping HeuristicAIProvider for a real OpenAI/Gemini-backed implementation
 * is a Spring @Bean/@Primary change, not a rewrite of every service that uses AI.
 */
public interface AIProvider {
    String name();
    boolean requiresNetwork();

    ResumeAnalysis summarizeResume(ResumeProfile resume);
    ParsedJd parseJobDescription(String rawText);

    record ResumeAnalysis(
        String summary,
        List<String> bestTargetRoles,
        List<String> suitableJdCategories,
        List<String> strongSkills,
        List<String> weakAreas,
        List<String> atsKeywords,
        float extractionConfidence // certainty of EXTRACTION only, never "hireability"
    ) {}

    record ParsedJd(
        String company,
        String title,
        String location,
        String employmentType,
        Float experienceRequiredYears,
        List<String> requiredSkills,
        List<String> preferredSkills,
        String education, // NONE|DIPLOMA|BACHELORS|MASTERS|PHD
        List<String> responsibilities,
        List<String> keywords,
        String salaryText,
        String summary
    ) {}
}
