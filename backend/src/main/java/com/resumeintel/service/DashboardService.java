package com.resumeintel.service;

import com.resumeintel.dto.DashboardDtos.*;
import com.resumeintel.model.ApplicationEntity;
import com.resumeintel.model.ApplicationStatus;
import com.resumeintel.model.ResumeEntity;
import com.resumeintel.repository.ApplicationRepository;
import com.resumeintel.repository.JobDescriptionRepository;
import com.resumeintel.repository.ResumeRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final ApplicationRepository appRepo;
    private final ResumeRepository resumeRepo;
    private final JobDescriptionRepository jdRepo;

    public DashboardService(ApplicationRepository appRepo, ResumeRepository resumeRepo, JobDescriptionRepository jdRepo) {
        this.appRepo = appRepo;
        this.resumeRepo = resumeRepo;
        this.jdRepo = jdRepo;
    }

    private static final Set<ApplicationStatus> INACTIVE = Set.of(
        ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN, ApplicationStatus.OFFER
    );
    private static final Set<ApplicationStatus> INTERVIEW_STAGE = Set.of(
        ApplicationStatus.INTERVIEW, ApplicationStatus.TECHNICAL_INTERVIEW, ApplicationStatus.HR
    );

    public DashboardStats getStats() {
        List<ApplicationEntity> applications = appRepo.findAll();
        List<ResumeEntity> resumes = resumeRepo.findAll();

        long active = applications.stream().filter(a -> !INACTIVE.contains(a.getStatus())).count();
        long interviews = applications.stream().filter(a -> INTERVIEW_STAGE.contains(a.getStatus())).count();
        long offers = applications.stream().filter(a -> a.getStatus() == ApplicationStatus.OFFER).count();

        List<UsageRow> usage = resumes.stream()
            .map(r -> new UsageRow(r.getName(), applications.stream().filter(a -> a.getResumeId().equals(r.getId())).count()))
            .filter(u -> u.count() > 0)
            .sorted(Comparator.comparingLong(UsageRow::count).reversed())
            .collect(Collectors.toList());

        return new DashboardStats(applications.size(), active, interviews, offers, resumes.size(), jdRepo.count(), usage);
    }
}
