package com.resumeintel.repository;

import com.resumeintel.model.ResumeExperienceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResumeExperienceRepository extends JpaRepository<ResumeExperienceEntity, Long> {
    List<ResumeExperienceEntity> findByResumeId(Long resumeId);
    void deleteByResumeId(Long resumeId);
}
