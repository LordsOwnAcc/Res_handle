package com.resumeintel.repository;

import com.resumeintel.model.ResumeJdMatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ResumeJdMatchRepository extends JpaRepository<ResumeJdMatchEntity, Long> {
    List<ResumeJdMatchEntity> findByJdIdOrderByOverallScoreDesc(Long jdId);
    List<ResumeJdMatchEntity> findByResumeIdOrderByComputedAtDesc(Long resumeId);
    Optional<ResumeJdMatchEntity> findByResumeIdAndJdId(Long resumeId, Long jdId);
}
