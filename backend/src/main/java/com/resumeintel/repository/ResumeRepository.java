package com.resumeintel.repository;

import com.resumeintel.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<ResumeEntity, Long> {
    List<ResumeEntity> findAllByOrderByLastModifiedAtDesc();
    List<ResumeEntity> findByVersionGroupKeyOrderByVersionLabel(String versionGroupKey);
    List<ResumeEntity> findByFolderId(Long folderId);

    @Query("SELECT r FROM ResumeEntity r WHERE lower(r.name) LIKE lower(concat('%', :q, '%')) " +
           "OR lower(r.description) LIKE lower(concat('%', :q, '%')) OR lower(r.customNotes) LIKE lower(concat('%', :q, '%'))")
    List<ResumeEntity> search(String q);
}
