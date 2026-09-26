package com.resumeintel.repository;

import com.resumeintel.model.JdSkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JdSkillRepository extends JpaRepository<JdSkillEntity, Long> {
    List<JdSkillEntity> findByJdId(Long jdId);
}
