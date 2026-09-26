package com.resumeintel.service.matching;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Rule-based, explainable multi-factor matcher — deliberately NOT keyword overlap. Six
 * independently-weighted sub-scores (required skills, preferred skills, experience, project
 * relevance, education, ATS keywords), each computed from a different slice of the resume,
 * combined into one score with evidence. Kept in lockstep with the Kotlin (mobile/desktop
 * client) and TypeScript (earlier prototype) versions of this same algorithm — if you change
 * the weights here, mirror it there, or better, make this backend the single source of truth
 * and have every client just call the API.
 */
@Component
public class MatchingEngine {

    private static final float W_REQUIRED = 0.32f;
    private static final float W_PREFERRED = 0.15f;
    private static final float W_EXPERIENCE = 0.15f;
    private static final float W_PROJECT = 0.18f;
    private static final float W_EDUCATION = 0.10f;
    private static final float W_KEYWORD = 0.10f;

    private static final String DISCLAIMER =
        "This score reflects textual/structural overlap between the resume and JD. It is not a prediction of interview or hiring outcomes.";

    private static final Map<String, Integer> EDU_RANK = Map.of(
        "NONE", 0, "DIPLOMA", 1, "BACHELORS", 2, "MASTERS", 3, "PHD", 4
    );

    public MatchResult match(ResumeProfile resume, JdProfile jd) {
        Set<String> resumeSkillsNorm = resume.skills().stream().map(this::normalize).collect(Collectors.toSet());

        SkillSetScore required = scoreSkillSet(jd.requiredSkills(), resumeSkillsNorm, resume.projects());
        SkillSetScore preferred = scoreSkillSet(jd.preferredSkills(), resumeSkillsNorm, resume.projects());
        float experienceScore = scoreExperience(resume.experienceMonths(), jd.experienceRequiredYears());

        List<String> combinedJdSkills = new ArrayList<>(jd.requiredSkills());
        combinedJdSkills.addAll(jd.preferredSkills());
        float projectScore = scoreProjectRelevance(resume.projects(), combinedJdSkills, jd.responsibilities());

        int jdEduRank = EDU_RANK.getOrDefault(jd.minEducation(), 0);
        int resumeEduRank = EDU_RANK.getOrDefault(resume.educationLevel(), 0);
        float educationScore = jdEduRank == 0 ? 1f : Math.min(1f, resumeEduRank / (float) jdEduRank);

        float keywordScore = scoreKeywordOverlap(jd.keywords(), resume.atsKeywords());

        float overall = required.score * W_REQUIRED + preferred.score * W_PREFERRED + experienceScore * W_EXPERIENCE
            + projectScore * W_PROJECT + educationScore * W_EDUCATION + keywordScore * W_KEYWORD;

        List<String> issues = new ArrayList<>();
        if (jd.experienceRequiredYears() != null && jd.experienceRequiredYears() > 0) {
            float haveYears = resume.experienceMonths() / 12f;
            if (haveYears < jd.experienceRequiredYears()) {
                issues.add(String.format("JD asks for %.1f years experience; resume shows ~%.1f years.",
                    jd.experienceRequiredYears(), haveYears));
            }
        }
        if (jdEduRank > resumeEduRank) {
            issues.add("JD's stated education requirement is higher than what's on this resume.");
        }

        LinkedHashSet<String> missingSet = new LinkedHashSet<>();
        missingSet.addAll(required.missing);
        missingSet.addAll(preferred.missing);
        List<String> missing = new ArrayList<>(missingSet);

        Set<String> strongNames = required.evidence.stream().map(SkillEvidence::skill).collect(Collectors.toSet());
        List<SkillEvidence> strong = new ArrayList<>(required.evidence);
        preferred.evidence.stream().filter(e -> !strongNames.contains(e.skill())).forEach(strong::add);

        MatchBreakdown breakdown = new MatchBreakdown(
            strong.stream().filter(e -> !e.foundIn().isEmpty()).toList(),
            strong.stream().filter(e -> e.foundIn().isEmpty()).toList(),
            missing,
            issues,
            DISCLAIMER
        );

        return new MatchResult(
            resume.resumeId(), jd.jdId(),
            clamp01(overall), required.score, preferred.score, experienceScore, projectScore, educationScore, keywordScore,
            breakdown
        );
    }

    public List<MatchResult> findBestResumes(List<ResumeProfile> resumes, JdProfile jd) {
        return resumes.stream().map(r -> match(r, jd))
            .sorted(Comparator.comparingDouble(MatchResult::overall).reversed())
            .toList();
    }

    private record SkillSetScore(float score, List<SkillEvidence> evidence, List<String> missing) {}

    private SkillSetScore scoreSkillSet(List<String> jdSkills, Set<String> resumeSkills, List<ProjectFact> projects) {
        if (jdSkills.isEmpty()) return new SkillSetScore(1f, List.of(), List.of());
        List<SkillEvidence> evidence = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        int hits = 0;
        for (String raw : jdSkills) {
            String skill = normalize(raw);
            boolean direct = resumeSkills.contains(skill);
            List<String> projectHits = projects.stream()
                .filter(p -> p.technologies().stream().anyMatch(t -> normalize(t).equals(skill))
                    || normalize(p.description()).contains(skill))
                .map(ProjectFact::name)
                .toList();
            if (direct || !projectHits.isEmpty()) {
                hits++;
                evidence.add(new SkillEvidence(raw, projectHits));
            } else {
                missing.add(raw);
            }
        }
        return new SkillSetScore(hits / (float) jdSkills.size(), evidence, missing);
    }

    private float scoreExperience(int resumeMonths, Float jdYearsRequired) {
        if (jdYearsRequired == null || jdYearsRequired <= 0f) return 1f;
        float resumeYears = resumeMonths / 12f;
        if (resumeYears >= jdYearsRequired) return 1f;
        return clamp01(resumeYears / jdYearsRequired);
    }

    private float scoreProjectRelevance(List<ProjectFact> projects, List<String> jdSkills, List<String> responsibilities) {
        if (projects.isEmpty()) return 0f;
        Set<String> jdSkillsNorm = jdSkills.stream().map(this::normalize).collect(Collectors.toSet());
        String respText = normalize(String.join(" ", responsibilities));
        float best = 0f;
        for (ProjectFact p : projects) {
            float techOverlap = jdSkillsNorm.isEmpty() ? 0f :
                (float) p.technologies().stream().map(this::normalize).filter(jdSkillsNorm::contains).count() / jdSkillsNorm.size();
            float respOverlap = respText.isBlank() ? 0f :
                Math.min(5, (int) Arrays.stream(normalize(p.description()).split(" "))
                    .filter(w -> w.length() > 3 && respText.contains(w)).count()) / 5f;
            best = Math.max(best, clamp01(techOverlap * 0.7f + respOverlap * 0.3f));
        }
        return best;
    }

    private float scoreKeywordOverlap(List<String> jdKeywords, List<String> resumeKeywords) {
        if (jdKeywords.isEmpty()) return 1f;
        Set<String> resumeSet = resumeKeywords.stream().map(this::normalize).collect(Collectors.toSet());
        long hits = jdKeywords.stream().filter(k -> resumeSet.contains(normalize(k))).count();
        return hits / (float) jdKeywords.size();
    }

    private String normalize(String s) {
        return s.trim().toLowerCase().replaceAll("[.\\-_/]", " ").replaceAll("\\s+", " ");
    }

    private float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }
}
