package com.resumeintel.repository;

import com.resumeintel.model.AIAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AIAnalysisRepository extends JpaRepository<AIAnalysisEntity, Long> {
    List<AIAnalysisEntity> findByResumeIdOrderByGeneratedAtDesc(Long resumeId);
}
