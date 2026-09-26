package com.resumeintel.repository;

import com.resumeintel.model.ApplicationEntity;
import com.resumeintel.model.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {
    List<ApplicationEntity> findAllByOrderByDateAppliedDesc();
    List<ApplicationEntity> findByResumeIdOrderByDateAppliedDesc(Long resumeId);
    List<ApplicationEntity> findByStatus(ApplicationStatus status);
    long countByStatus(ApplicationStatus status);
}
