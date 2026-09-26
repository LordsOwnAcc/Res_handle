package com.resumeintel.controller;

import com.resumeintel.dto.ResumeDtos.*;
import com.resumeintel.model.ResumeEntity;
import com.resumeintel.repository.ApplicationRepository;
import com.resumeintel.service.ResumeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {
    private final ResumeService resumeService;
    private final ApplicationRepository applicationRepo;

    public ResumeController(ResumeService resumeService, ApplicationRepository applicationRepo) {
        this.resumeService = resumeService;
        this.applicationRepo = applicationRepo;
    }

    @GetMapping
    public List<ResumeSummary> list() {
        return resumeService.listAll().stream().map(r -> new ResumeSummary(
            r.getId(), r.getName(), r.getVersionLabel(), r.getVersionGroupKey(),
            r.getDescription(), splitCsv(r.getTargetRoles()), r.getLastModifiedAt()
        )).toList();
    }

    @GetMapping("/{id}")
    public ResumeDetail get(@PathVariable Long id) {
        return resumeService.toDetail(resumeService.get(id));
    }

    @PostMapping
    public ResumeDetail create(@Valid @RequestBody CreateResumeRequest req) {
        ResumeEntity created = resumeService.create(req);
        return resumeService.toDetail(created);
    }

    @PutMapping("/{id}")
    public ResumeDetail update(@PathVariable Long id, @Valid @RequestBody CreateResumeRequest req) {
        ResumeEntity updated = resumeService.update(id, req);
        return resumeService.toDetail(updated);
    }

    @GetMapping("/{id}/applications")
    public List<Long> applicationIdsUsingResume(@PathVariable Long id) {
        return applicationRepo.findByResumeIdOrderByDateAppliedDesc(id).stream().map(a -> a.getId()).toList();
    }

    private static List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return List.of(csv.split(",")).stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
