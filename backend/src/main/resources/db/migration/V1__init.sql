-- V1__init.sql
-- Same entity model used by the KMP client, adapted to SQL + Spring Data JPA naming.

CREATE TABLE folder (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  parent_folder_id BIGINT REFERENCES folder(id) ON DELETE SET NULL
);

CREATE TABLE tag (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL UNIQUE,
  color_hex TEXT NOT NULL DEFAULT '#6750A4'
);

CREATE TABLE company (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL UNIQUE,
  website TEXT NOT NULL DEFAULT ''
);

CREATE TABLE document (
  id BIGSERIAL PRIMARY KEY,
  file_name TEXT NOT NULL,
  mime_type TEXT NOT NULL,
  storage_path TEXT NOT NULL,
  size_bytes BIGINT NOT NULL DEFAULT 0,
  imported_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE resume (
  id BIGSERIAL PRIMARY KEY,
  folder_id BIGINT REFERENCES folder(id) ON DELETE SET NULL,
  name TEXT NOT NULL,
  version_label TEXT NOT NULL DEFAULT 'v1',
  version_group_key TEXT NOT NULL,
  document_id BIGINT REFERENCES document(id) ON DELETE SET NULL,
  description TEXT NOT NULL DEFAULT '',
  target_roles TEXT NOT NULL DEFAULT '',
  target_industries TEXT NOT NULL DEFAULT '',
  education_level TEXT NOT NULL DEFAULT 'NONE',
  custom_notes TEXT NOT NULL DEFAULT '',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  last_modified_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  is_user_edited BOOLEAN NOT NULL DEFAULT false
);
CREATE INDEX idx_resume_version_group ON resume (version_group_key);
CREATE INDEX idx_resume_search ON resume USING GIN (
  to_tsvector('english', coalesce(name,'') || ' ' || coalesce(description,'') || ' ' || coalesce(custom_notes,''))
);

CREATE TABLE resume_skill (
  id BIGSERIAL PRIMARY KEY,
  resume_id BIGINT NOT NULL REFERENCES resume(id) ON DELETE CASCADE,
  skill_name TEXT NOT NULL,
  category TEXT NOT NULL DEFAULT 'tool',
  evidence_project_id BIGINT
);
CREATE INDEX idx_resume_skill_resume ON resume_skill (resume_id);
CREATE INDEX idx_resume_skill_name ON resume_skill (lower(skill_name));

CREATE TABLE resume_project (
  id BIGSERIAL PRIMARY KEY,
  resume_id BIGINT NOT NULL REFERENCES resume(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  technologies TEXT NOT NULL DEFAULT '',
  description TEXT NOT NULL DEFAULT '',
  impact TEXT NOT NULL DEFAULT '',
  link TEXT NOT NULL DEFAULT ''
);
CREATE INDEX idx_resume_project_resume ON resume_project (resume_id);

CREATE TABLE resume_experience (
  id BIGSERIAL PRIMARY KEY,
  resume_id BIGINT NOT NULL REFERENCES resume(id) ON DELETE CASCADE,
  company TEXT NOT NULL,
  role TEXT NOT NULL,
  duration_months INT NOT NULL DEFAULT 0,
  responsibilities TEXT NOT NULL DEFAULT '',
  technologies TEXT NOT NULL DEFAULT ''
);
CREATE INDEX idx_resume_experience_resume ON resume_experience (resume_id);

CREATE TABLE job_description (
  id BIGSERIAL PRIMARY KEY,
  company_id BIGINT REFERENCES company(id) ON DELETE SET NULL,
  title TEXT NOT NULL,
  raw_text TEXT NOT NULL,
  location TEXT NOT NULL DEFAULT '',
  employment_type TEXT NOT NULL DEFAULT '',
  experience_required_years REAL,
  education_requirement TEXT NOT NULL DEFAULT 'NONE',
  salary_text TEXT NOT NULL DEFAULT '',
  application_url TEXT NOT NULL DEFAULT '',
  required_skills TEXT NOT NULL DEFAULT '',
  preferred_skills TEXT NOT NULL DEFAULT '',
  responsibilities TEXT NOT NULL DEFAULT '',
  keywords TEXT NOT NULL DEFAULT '',
  ai_summary TEXT NOT NULL DEFAULT '',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_jd_search ON job_description USING GIN (
  to_tsvector('english', coalesce(title,'') || ' ' || coalesce(raw_text,''))
);

CREATE TABLE jd_skill (
  id BIGSERIAL PRIMARY KEY,
  jd_id BIGINT NOT NULL REFERENCES job_description(id) ON DELETE CASCADE,
  skill_name TEXT NOT NULL,
  importance TEXT NOT NULL CHECK (importance IN ('required','preferred'))
);
CREATE INDEX idx_jd_skill_jd ON jd_skill (jd_id);

CREATE TABLE application (
  id BIGSERIAL PRIMARY KEY,
  company_id BIGINT REFERENCES company(id) ON DELETE SET NULL,
  jd_id BIGINT REFERENCES job_description(id) ON DELETE SET NULL,
  resume_id BIGINT NOT NULL REFERENCES resume(id) ON DELETE RESTRICT,
  -- RESTRICT: an application must always remember the EXACT resume version used.
  job_title TEXT NOT NULL,
  application_url TEXT NOT NULL DEFAULT '',
  date_applied TIMESTAMPTZ,
  status TEXT NOT NULL DEFAULT 'SAVED' CHECK (status IN (
    'SAVED','PREPARING','APPLIED','ASSESSMENT','INTERVIEW','TECHNICAL_INTERVIEW',
    'HR','OFFER','REJECTED','WITHDRAWN','ON_HOLD'
  )),
  recruiter_contact TEXT NOT NULL DEFAULT '',
  location TEXT NOT NULL DEFAULT '',
  salary_text TEXT NOT NULL DEFAULT '',
  interview_date TIMESTAMPTZ,
  follow_up_date TIMESTAMPTZ,
  notes TEXT NOT NULL DEFAULT '',
  match_score_at_apply_time REAL
);
CREATE INDEX idx_application_resume ON application (resume_id);
CREATE INDEX idx_application_status ON application (status);

CREATE TABLE resume_tag (
  resume_id BIGINT NOT NULL REFERENCES resume(id) ON DELETE CASCADE,
  tag_id BIGINT NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
  PRIMARY KEY (resume_id, tag_id)
);

CREATE TABLE application_tag (
  application_id BIGINT NOT NULL REFERENCES application(id) ON DELETE CASCADE,
  tag_id BIGINT NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
  PRIMARY KEY (application_id, tag_id)
);

CREATE TABLE resume_jd_match (
  id BIGSERIAL PRIMARY KEY,
  resume_id BIGINT NOT NULL REFERENCES resume(id) ON DELETE CASCADE,
  jd_id BIGINT NOT NULL REFERENCES job_description(id) ON DELETE CASCADE,
  overall_score REAL NOT NULL,
  required_match_score REAL NOT NULL,
  preferred_match_score REAL NOT NULL,
  experience_match_score REAL NOT NULL,
  project_match_score REAL NOT NULL,
  education_match_score REAL NOT NULL,
  keyword_match_score REAL NOT NULL,
  breakdown_json JSONB NOT NULL,
  computed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (resume_id, jd_id)
);
CREATE INDEX idx_match_jd ON resume_jd_match (jd_id);
CREATE INDEX idx_match_resume ON resume_jd_match (resume_id);

CREATE TABLE ai_analysis (
  id BIGSERIAL PRIMARY KEY,
  resume_id BIGINT NOT NULL REFERENCES resume(id) ON DELETE CASCADE,
  summary TEXT NOT NULL,
  best_target_roles TEXT NOT NULL DEFAULT '',
  suitable_jd_categories TEXT NOT NULL DEFAULT '',
  strong_skills TEXT NOT NULL DEFAULT '',
  weak_areas TEXT NOT NULL DEFAULT '',
  ats_keywords TEXT NOT NULL DEFAULT '',
  extraction_confidence REAL NOT NULL DEFAULT 0,
  generated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_ai_analysis_resume ON ai_analysis (resume_id);
