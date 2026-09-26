package com.resumeintel.repository;

import com.resumeintel.model.JobDescriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface JobDescriptionRepository extends JpaRepository<JobDescriptionEntity, Long> {
    List<JobDescriptionEntity> findAllByOrderByCreatedAtDesc();

    @Query("SELECT j FROM JobDescriptionEntity j WHERE lower(j.title) LIKE lower(concat('%', :q, '%')) " +
           "OR lower(j.rawText) LIKE lower(concat('%', :q, '%'))")
    List<JobDescriptionEntity> search(String q);
}
