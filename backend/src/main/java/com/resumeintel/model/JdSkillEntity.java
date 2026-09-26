package com.resumeintel.model;

import jakarta.persistence.*;

@Entity
@Table(name = "jd_skill")
public class JdSkillEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "jd_id", nullable = false)
    private Long jdId;
    @Column(name = "skill_name", nullable = false)
    private String skillName;
    /** "required" | "preferred" */
    @Column(nullable = false)
    private String importance;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getJdId() { return jdId; }
    public void setJdId(Long jdId) { this.jdId = jdId; }
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public String getImportance() { return importance; }
    public void setImportance(String importance) { this.importance = importance; }
}
