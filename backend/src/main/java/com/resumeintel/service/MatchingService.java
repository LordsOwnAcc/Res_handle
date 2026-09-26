package com.resumeintel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeintel.model.ResumeEntity;
import com.resumeintel.model.ResumeJdMatchEntity;
import com.resumeintel.repository.ResumeJdMatchRepository;
import com.resumeintel.service.matching.JdProfile;
import com.resumeintel.service.matching.MatchResult;
import com.resumeintel.service.matching.MatchingEngine;
import com.resumeintel.service.matching.ResumeProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class MatchingService {
    private final MatchingEngine engine;
    private final ResumeService resumeService;
    private final JobDescriptionService jdService;
    private final ResumeJdMatchRepository matchRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MatchingService(MatchingEngine engine, ResumeService resumeService,
                            JobDescriptionService jdService, ResumeJdMatchRepository matchRepo) {
        this.engine = engine;
        this.resumeService = resumeService;
        this.jdService = jdService;
        this.matchRepo = matchRepo;
    }

    /** The headline "Find Best Resume" feature: rank every resume in the DB against one JD. */
    @Transactional
    public List<MatchResult> findBestResumes(Long jdId) {
        JdProfile jdProfile = jdService.buildProfile(jdId);
        List<ResumeProfile> profiles = resumeService.listAll().stream()
            .map(ResumeEntity::getId).map(resumeService::buildProfile).toList();
        List<MatchResult> results = engine.findBestResumes(profiles, jdProfile);
        results.forEach(this::cache);
        return results;
    }

    public MatchResult matchOne(Long resumeId, Long jdId) {
        return engine.match(resumeService.buildProfile(resumeId), jdService.buildProfile(jdId));
    }

    private void cache(MatchResult r) {
        try {
            ResumeJdMatchEntity entity = matchRepo.findByResumeIdAndJdId(r.resumeId(), r.jdId()).orElseGet(ResumeJdMatchEntity::new);
            entity.setResumeId(r.resumeId());
            entity.setJdId(r.jdId());
            entity.setOverallScore(r.overall());
            entity.setRequiredMatchScore(r.requiredMatch());
            entity.setPreferredMatchScore(r.preferredMatch());
            entity.setExperienceMatchScore(r.experienceMatch());
            entity.setProjectMatchScore(r.projectMatch());
            entity.setEducationMatchScore(r.educationMatch());
            entity.setKeywordMatchScore(r.keywordMatch());
            entity.setBreakdownJson(objectMapper.writeValueAsString(r.breakdown()));
            entity.setComputedAt(Instant.now());
            matchRepo.save(entity);
        } catch (Exception e) {
            // Caching is best-effort — a failure here shouldn't break the live match response.
            System.err.println("Failed to cache match result: " + e.getMessage());
        }
    }
}
