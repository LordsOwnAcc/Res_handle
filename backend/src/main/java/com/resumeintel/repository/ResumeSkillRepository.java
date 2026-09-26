package com.resumeintel.repository;

import com.resumeintel.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResumeSkillRepository extends JpaRepository<ResumeSkillEntity, Long> {
    List<ResumeSkillEntity> findByResumeId(Long resumeId);
    void deleteByResumeId(Long resumeId);
}
