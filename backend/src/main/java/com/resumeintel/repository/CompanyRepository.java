package com.resumeintel.repository;

import com.resumeintel.model.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    Optional<CompanyEntity> findByNameIgnoreCase(String name);
}
