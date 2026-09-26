package com.resumeintel.service;

import com.resumeintel.dto.JdDtos.*;
import com.resumeintel.model.*;
import com.resumeintel.repository.*;
import com.resumeintel.service.ai.AIProvider;
import com.resumeintel.service.matching.JdProfile;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobDescriptionService {
    private final JobDescriptionRepository jdRepo;
    private final JdSkillRepository jdSkillRepo;
    private final CompanyRepository companyRepo;
    private final AIProvider aiProvider;

    public JobDescriptionService(JobDescriptionRepository jdRepo, JdSkillRepository jdSkillRepo,
                                  CompanyRepository companyRepo, AIProvider aiProvider) {
        this.jdRepo = jdRepo;
        this.jdSkillRepo = jdSkillRepo;
        this.companyRepo = companyRepo;
        this.aiProvider = aiProvider;
    }

    @Transactional
    public JobDescriptionEntity importFromText(String rawText) {
        AIProvider.ParsedJd parsed = aiProvider.parseJobDescription(rawText);

        Long companyId = null;
        if (parsed.company() != null && !parsed.company().isBlank()) {
            CompanyEntity company = companyRepo.findByNameIgnoreCase(parsed.company())
                .orElseGet(() -> {
                    CompanyEntity c = new CompanyEntity();
                    c.setName(parsed.company());
                    return companyRepo.save(c);
                });
            companyId = company.getId();
        }

        JobDescriptionEntity jd = new JobDescriptionEntity();
        jd.setCompanyId(companyId);
        jd.setTitle(parsed.title() != null ? parsed.title() : "Untitled role");
        jd.setRawText(rawText);
        jd.setLocation(parsed.location() != null ? parsed.location() : "");
        jd.setEmploymentType(parsed.employmentType() != null ? parsed.employmentType() : "");
        jd.setExperienceRequiredYears(parsed.experienceRequiredYears());
        jd.setEducationRequirement(EducationLevel.valueOf(parsed.education()));
        jd.setSalaryText(parsed.salaryText() != null ? parsed.salaryText() : "");
        jd.setRequiredSkills(String.join(",", parsed.requiredSkills()));
        jd.setPreferredSkills(String.join(",", parsed.preferredSkills()));
        jd.setResponsibilities(String.join("\n", parsed.responsibilities()));
        jd.setKeywords(String.join(",", parsed.keywords()));
        jd.setAiSummary(parsed.summary());
        jd.setCreatedAt(Instant.now());
        jdRepo.save(jd);

        for (String s : parsed.requiredSkills()) jdSkillRepo.save(skillRow(jd.getId(), s, "required"));
        for (String s : parsed.preferredSkills()) jdSkillRepo.save(skillRow(jd.getId(), s, "preferred"));

        return jd;
    }

    private JdSkillEntity skillRow(Long jdId, String skill, String importance) {
        JdSkillEntity e = new JdSkillEntity();
        e.setJdId(jdId);
        e.setSkillName(skill);
        e.setImportance(importance);
        return e;
    }

    public List<JobDescriptionEntity> listAll() { return jdRepo.findAllByOrderByCreatedAtDesc(); }

    public JobDescriptionEntity get(Long id) {
        return jdRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("JD " + id + " not found"));
    }

    public JdProfile buildProfile(Long jdId) {
        JobDescriptionEntity jd = get(jdId);
        List<JdSkillEntity> skills = jdSkillRepo.findByJdId(jdId);
        List<String> required = skills.stream().filter(s -> s.getImportance().equals("required")).map(JdSkillEntity::getSkillName).toList();
        List<String> preferred = skills.stream().filter(s -> s.getImportance().equals("preferred")).map(JdSkillEntity::getSkillName).toList();
        if (required.isEmpty() && !jd.getRequiredSkills().isBlank()) required = splitCsv(jd.getRequiredSkills());
        if (preferred.isEmpty() && !jd.getPreferredSkills().isBlank()) preferred = splitCsv(jd.getPreferredSkills());

        return new JdProfile(
            jdId, required, preferred, jd.getExperienceRequiredYears(),
            jd.getEducationRequirement().name(),
            jd.getResponsibilities() == null || jd.getResponsibilities().isBlank() ? List.of() : List.of(jd.getResponsibilities().split("\n")),
            splitCsv(jd.getKeywords())
        );
    }

    private static List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return List.of(csv.split(",")).stream().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }
}
