package com.resumeintel.repository;

import com.resumeintel.model.ResumeProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResumeProjectRepository extends JpaRepository<ResumeProjectEntity, Long> {
    List<ResumeProjectEntity> findByResumeId(Long resumeId);
    void deleteByResumeId(Long resumeId);
}
