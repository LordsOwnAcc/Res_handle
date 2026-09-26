package com.resumeintel.controller;

import com.resumeintel.dto.JdDtos.*;
import com.resumeintel.model.JobDescriptionEntity;
import com.resumeintel.repository.CompanyRepository;
import com.resumeintel.repository.JdSkillRepository;
import com.resumeintel.service.JobDescriptionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/jds")
public class JobDescriptionController {
    private final JobDescriptionService jdService;
    private final CompanyRepository companyRepo;
    private final JdSkillRepository jdSkillRepo;

    public JobDescriptionController(JobDescriptionService jdService, CompanyRepository companyRepo, JdSkillRepository jdSkillRepo) {
        this.jdService = jdService;
        this.companyRepo = companyRepo;
        this.jdSkillRepo = jdSkillRepo;
    }

    @GetMapping
    public List<JdSummary> list() {
        return jdService.listAll().stream().map(this::toSummary).toList();
    }

    @GetMapping("/{id}")
    public JdDetail get(@PathVariable Long id) {
        return toDetail(jdService.get(id));
    }

    @PostMapping("/import")
    public JdDetail importJd(@Valid @RequestBody ImportJdRequest req) {
        JobDescriptionEntity jd = jdService.importFromText(req.rawText());
        return toDetail(jd);
    }

    private JdSummary toSummary(JobDescriptionEntity jd) {
        return new JdSummary(jd.getId(), jd.getTitle(), companyName(jd.getCompanyId()), jd.getLocation(),
            jd.getExperienceRequiredYears(), splitCsv(jd.getRequiredSkills()), jd.getAiSummary(), jd.getCreatedAt());
    }

    private JdDetail toDetail(JobDescriptionEntity jd) {
        return new JdDetail(
            jd.getId(), jd.getTitle(), companyName(jd.getCompanyId()), jd.getLocation(), jd.getEmploymentType(),
            jd.getExperienceRequiredYears(), jd.getEducationRequirement().name(), jd.getSalaryText(), jd.getApplicationUrl(),
            splitCsv(jd.getRequiredSkills()), splitCsv(jd.getPreferredSkills()),
            jd.getResponsibilities() == null || jd.getResponsibilities().isBlank() ? List.of() : List.of(jd.getResponsibilities().split("\n")),
            splitCsv(jd.getKeywords()), jd.getAiSummary(), jd.getRawText(), jd.getCreatedAt()
        );
    }

    private String companyName(Long companyId) {
        if (companyId == null) return null;
        return companyRepo.findById(companyId).map(c -> c.getName()).orElse(null);
    }

    private static List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return List.of(csv.split(",")).stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
