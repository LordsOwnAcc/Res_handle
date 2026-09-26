package com.resumeintel.service.matching;

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
