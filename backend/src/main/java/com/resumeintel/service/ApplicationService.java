package com.resumeintel.service;

import com.resumeintel.dto.ApplicationDtos.*;
import com.resumeintel.model.ApplicationEntity;
import com.resumeintel.model.ApplicationStatus;
import com.resumeintel.model.CompanyEntity;
import com.resumeintel.repository.ApplicationRepository;
import com.resumeintel.repository.CompanyRepository;
import com.resumeintel.service.matching.MatchResult;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ApplicationService {
    private final ApplicationRepository appRepo;
    private final CompanyRepository companyRepo;
    private final MatchingService matchingService;

    public ApplicationService(ApplicationRepository appRepo, CompanyRepository companyRepo, MatchingService matchingService) {
        this.appRepo = appRepo;
        this.companyRepo = companyRepo;
        this.matchingService = matchingService;
    }

    @Transactional
    public ApplicationEntity create(CreateApplicationRequest req) {
        CompanyEntity company = companyRepo.findByNameIgnoreCase(req.companyName())
            .orElseGet(() -> {
                CompanyEntity c = new CompanyEntity();
                c.setName(req.companyName());
                return companyRepo.save(c);
            });

        ApplicationEntity app = new ApplicationEntity();
        app.setCompanyId(company.getId());
        app.setJdId(req.jdId());
        app.setResumeId(req.resumeId());
        app.setJobTitle(req.jobTitle());
        app.setStatus(req.status() != null ? req.status() : ApplicationStatus.SAVED);
        app.setDateApplied(Instant.now());

        if (req.jdId() != null) {
            // Snapshot the match score at apply time — spec requirement: this score is never
            // silently recomputed later, so an application's history stays accurate to what
            // was known when it was created.
            try {
                MatchResult result = matchingService.matchOne(req.resumeId(), req.jdId());
                app.setMatchScoreAtApplyTime(result.overall());
            } catch (Exception ignored) { /* JD or resume data incomplete — leave score null */ }
        }

        return appRepo.save(app);
    }

    public List<ApplicationEntity> listAll() { return appRepo.findAllByOrderByDateAppliedDesc(); }
    public List<ApplicationEntity> listForResume(Long resumeId) { return appRepo.findByResumeIdOrderByDateAppliedDesc(resumeId); }

    @Transactional
    public ApplicationEntity updateStatus(Long id, ApplicationStatus status) {
        ApplicationEntity app = appRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Application " + id + " not found"));
        app.setStatus(status);
        return appRepo.save(app);
    }

    public long countByStatus(ApplicationStatus status) { return appRepo.countByStatus(status); }
}
