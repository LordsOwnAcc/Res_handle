package com.resumeintel.service.matching;

import java.util.List;

public record MatchBreakdown(
    List<SkillEvidence> strongMatches,
    List<SkillEvidence> partialMatches,
    List<String> missing,
    List<String> potentialIssues,
    String disclaimer
) {}
