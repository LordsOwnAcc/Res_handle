package com.resumeintel.controller;

import com.resumeintel.service.MatchingService;
import com.resumeintel.service.ResumeService;
import com.resumeintel.service.matching.MatchResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/match")
public class MatchController {
    private final MatchingService matchingService;
    private final ResumeService resumeService;

    public MatchController(MatchingService matchingService, ResumeService resumeService) {
        this.matchingService = matchingService;
        this.resumeService = resumeService;
    }

    /** Spec's headline feature: rank every resume in the DB against one JD, with evidence. */
    @GetMapping("/best-resumes/{jdId}")
    public List<MatchResultResponse> findBestResumes(@PathVariable Long jdId) {
        List<MatchResult> results = matchingService.findBestResumes(jdId);
        return results.stream().map(r -> new MatchResultResponse(r, resumeService.get(r.resumeId()).getName())).toList();
    }

    @GetMapping("/{resumeId}/{jdId}")
    public MatchResultResponse matchOne(@PathVariable Long resumeId, @PathVariable Long jdId) {
        MatchResult r = matchingService.matchOne(resumeId, jdId);
        return new MatchResultResponse(r, resumeService.get(resumeId).getName());
    }

    public record MatchResultResponse(MatchResult result, String resumeName) {}
}
