package com.resumeintel.controller;

import com.resumeintel.dto.ApplicationDtos.*;
import com.resumeintel.model.ApplicationEntity;
import com.resumeintel.repository.CompanyRepository;
import com.resumeintel.repository.ResumeRepository;
import com.resumeintel.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final ApplicationService applicationService;
    private final CompanyRepository companyRepo;
    private final ResumeRepository resumeRepo;

    public ApplicationController(ApplicationService applicationService, CompanyRepository companyRepo, ResumeRepository resumeRepo) {
        this.applicationService = applicationService;
        this.companyRepo = companyRepo;
        this.resumeRepo = resumeRepo;
    }

    @GetMapping
    public List<ApplicationSummary> list() {
        return applicationService.listAll().stream().map(this::toSummary).toList();
    }

    @PostMapping
    public ApplicationSummary create(@Valid @RequestBody CreateApplicationRequest req) {
        return toSummary(applicationService.create(req));
    }

    @PatchMapping("/{id}/status")
    public ApplicationSummary updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest req) {
        return toSummary(applicationService.updateStatus(id, req.status()));
    }

    private ApplicationSummary toSummary(ApplicationEntity a) {
        String companyName = a.getCompanyId() != null ? companyRepo.findById(a.getCompanyId()).map(c -> c.getName()).orElse(null) : null;
        String resumeName = resumeRepo.findById(a.getResumeId()).map(r -> r.getName()).orElse("Unknown resume");
        return new ApplicationSummary(a.getId(), companyName, a.getJobTitle(), resumeName, a.getStatus(), a.getDateApplied(), a.getMatchScoreAtApplyTime());
    }
}
