package com.resumeintel.service;

import com.resumeintel.dto.ResumeDtos.*;
import com.resumeintel.model.*;
import com.resumeintel.repository.*;
import com.resumeintel.service.matching.ProjectFact;
import com.resumeintel.service.matching.ResumeProfile;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ResumeService {
    private final ResumeRepository resumeRepo;
    private final ResumeSkillRepository skillRepo;
    private final ResumeProjectRepository projectRepo;
    private final ResumeExperienceRepository experienceRepo;
    private final DocumentRepository documentRepo;

    public ResumeService(ResumeRepository resumeRepo, ResumeSkillRepository skillRepo,
                          ResumeProjectRepository projectRepo, ResumeExperienceRepository experienceRepo,
                          DocumentRepository documentRepo) {
        this.resumeRepo = resumeRepo;
        this.skillRepo = skillRepo;
        this.projectRepo = projectRepo;
        this.experienceRepo = experienceRepo;
        this.documentRepo = documentRepo;
    }

    @Transactional
    public ResumeEntity create(CreateResumeRequest req) {
        ResumeEntity resume = new ResumeEntity();
        resume.setName(req.name());
        resume.setVersionLabel(req.versionLabel() != null ? req.versionLabel() : "v1");
        // A fresh resume starts its own lineage unless the caller explicitly groups it under
        // an existing one (e.g. "Java Backend" family) — see spec's versioning requirement.
        resume.setVersionGroupKey(req.versionGroupKey() != null ? req.versionGroupKey() : UUID.randomUUID().toString());
        resume.setDescription(req.description() != null ? req.description() : "");
        resume.setTargetRoles(String.join(",", nullToEmpty(req.targetRoles())));
        resume.setTargetIndustries(String.join(",", nullToEmpty(req.targetIndustries())));
        resume.setEducationLevel(req.educationLevel() != null ? req.educationLevel() : EducationLevel.NONE);
        resume.setCustomNotes(req.customNotes() != null ? req.customNotes() : "");
        resume.setCreatedAt(Instant.now());
        resume.setLastModifiedAt(Instant.now());
        resumeRepo.save(resume);

        saveChildren(resume.getId(), req.skills(), req.projects(), req.experience());
        return resume;
    }

    @Transactional
    public ResumeEntity update(Long id, CreateResumeRequest req) {
        ResumeEntity resume = resumeRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Resume " + id + " not found"));
        resume.setName(req.name());
        if (req.versionLabel() != null) resume.setVersionLabel(req.versionLabel());
        resume.setDescription(req.description() != null ? req.description() : resume.getDescription());
        if (req.targetRoles() != null) resume.setTargetRoles(String.join(",", req.targetRoles()));
        if (req.targetIndustries() != null) resume.setTargetIndustries(String.join(",", req.targetIndustries()));
        if (req.educationLevel() != null) resume.setEducationLevel(req.educationLevel());
        if (req.customNotes() != null) resume.setCustomNotes(req.customNotes());
        resume.setUserEdited(true);
        resumeRepo.save(resume);

        if (req.skills() != null || req.projects() != null || req.experience() != null) {
            skillRepo.deleteByResumeId(id);
            projectRepo.deleteByResumeId(id);
            experienceRepo.deleteByResumeId(id);
            saveChildren(id, req.skills(), req.projects(), req.experience());
        }
        return resume;
    }

    private void saveChildren(Long resumeId, List<SkillInput> skills, List<ProjectInput> projects, List<ExperienceInput> experience) {
        if (skills != null) {
            skillRepo.saveAll(skills.stream().map(s -> {
                ResumeSkillEntity e = new ResumeSkillEntity();
                e.setResumeId(resumeId);
                e.setSkillName(s.skillName());
                e.setCategory(s.category() != null ? s.category() : "tool");
                return e;
            }).toList());
        }
        if (projects != null) {
            projectRepo.saveAll(projects.stream().map(p -> {
                ResumeProjectEntity e = new ResumeProjectEntity();
                e.setResumeId(resumeId);
                e.setName(p.name());
                e.setTechnologies(String.join(",", nullToEmpty(p.technologies())));
                e.setDescription(p.description() != null ? p.description() : "");
                e.setImpact(p.impact() != null ? p.impact() : "");
                e.setLink(p.link() != null ? p.link() : "");
                return e;
            }).toList());
        }
        if (experience != null) {
            experienceRepo.saveAll(experience.stream().map(x -> {
                ResumeExperienceEntity e = new ResumeExperienceEntity();
                e.setResumeId(resumeId);
                e.setCompany(x.company());
                e.setRole(x.role() != null ? x.role() : "");
                e.setDurationMonths(x.durationMonths() != null ? x.durationMonths() : 0);
                e.setResponsibilities(x.responsibilities() != null ? x.responsibilities() : "");
                e.setTechnologies(String.join(",", nullToEmpty(x.technologies())));
                return e;
            }).toList());
        }
    }

    public List<ResumeEntity> listAll() { return resumeRepo.findAllByOrderByLastModifiedAtDesc(); }

    public ResumeEntity get(Long id) {
        return resumeRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Resume " + id + " not found"));
    }

    public ResumeDetail toDetail(ResumeEntity r) {
        List<SkillInput> skills = skillRepo.findByResumeId(r.getId()).stream()
            .map(s -> new SkillInput(s.getSkillName(), s.getCategory())).toList();
        List<ProjectInput> projects = projectRepo.findByResumeId(r.getId()).stream()
            .map(p -> new ProjectInput(p.getName(), splitCsv(p.getTechnologies()), p.getDescription(), p.getImpact(), p.getLink())).toList();
        List<ExperienceInput> experience = experienceRepo.findByResumeId(r.getId()).stream()
            .map(x -> new ExperienceInput(x.getCompany(), x.getRole(), x.getDurationMonths(), x.getResponsibilities(), splitCsv(x.getTechnologies()))).toList();

        String documentUrl = null;
        String documentFileName = null;
        if (r.getDocumentId() != null) {
            var doc = documentRepo.findById(r.getDocumentId()).orElse(null);
            if (doc != null) {
                documentUrl = doc.getStoragePath(); // Drive webViewLink
                documentFileName = doc.getFileName();
            }
        }

        return new ResumeDetail(
            r.getId(), r.getName(), r.getVersionLabel(), r.getVersionGroupKey(), r.getDescription(),
            splitCsv(r.getTargetRoles()), splitCsv(r.getTargetIndustries()), r.getEducationLevel().name(), r.getCustomNotes(),
            skills, projects, experience, documentUrl, documentFileName, r.getCreatedAt(), r.getLastModifiedAt()
        );
    }

    public ResumeProfile buildProfile(Long resumeId) {
        ResumeEntity r = get(resumeId);
        List<String> skills = skillRepo.findByResumeId(resumeId).stream().map(ResumeSkillEntity::getSkillName).toList();
        List<ProjectFact> projects = projectRepo.findByResumeId(resumeId).stream()
            .map(p -> new ProjectFact(p.getName(), splitCsv(p.getTechnologies()), p.getDescription())).toList();
        int experienceMonths = experienceRepo.findByResumeId(resumeId).stream().mapToInt(ResumeExperienceEntity::getDurationMonths).sum();
        return new ResumeProfile(resumeId, skills, projects, experienceMonths, r.getEducationLevel().name(), skills);
    }

    private static List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return List.of(csv.split(",")).stream().map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }
    private static List<String> nullToEmpty(List<String> list) { return list != null ? list : List.of(); }
}
