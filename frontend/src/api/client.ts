// Single seam between the UI and the backend. Base URL comes from an env var so the same
// build works against localhost in dev and the deployed Render backend in production —
// see .env.example and render.yaml.
const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: { "Content-Type": "application/json", ...(options?.headers || {}) },
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({ error: res.statusText }));
    throw new Error(body.error || `Request failed: ${res.status}`);
  }
  if (res.status === 204) return undefined as T;
  return res.json();
}

// ---- Types (mirrors backend DTOs) ----
export type EducationLevel = "NONE" | "DIPLOMA" | "BACHELORS" | "MASTERS" | "PHD";
export type ApplicationStatus =
  | "SAVED" | "PREPARING" | "APPLIED" | "ASSESSMENT" | "INTERVIEW" | "TECHNICAL_INTERVIEW"
  | "HR" | "OFFER" | "REJECTED" | "WITHDRAWN" | "ON_HOLD";

export interface SkillInput { skillName: string; category?: string; }
export interface ProjectInput { name: string; technologies: string[]; description?: string; impact?: string; link?: string; }
export interface ExperienceInput { company: string; role?: string; durationMonths?: number; responsibilities?: string; technologies?: string[]; }

export interface ResumeSummary {
  id: number; name: string; versionLabel: string; versionGroupKey: string;
  description: string; targetRoles: string[]; lastModifiedAt: string;
}
export interface ResumeDetail extends ResumeSummary {
  targetIndustries: string[]; educationLevel: EducationLevel; customNotes: string;
  skills: SkillInput[]; projects: ProjectInput[]; experience: ExperienceInput[]; createdAt: string;
  documentUrl: string | null; documentFileName: string | null;
}
export interface CreateResumeRequest {
  name: string; versionLabel?: string; versionGroupKey?: string; description?: string;
  targetRoles?: string[]; targetIndustries?: string[]; educationLevel?: EducationLevel;
  customNotes?: string; skills?: SkillInput[]; projects?: ProjectInput[]; experience?: ExperienceInput[];
}

export interface ParsedResumeDraft {
  name: string; email: string; phone: string; github: string; linkedin: string; description: string;
  targetRoles: string[]; educationLevel: EducationLevel; experienceMonths: number;
  skills: SkillInput[]; projects: ProjectInput[]; warnings: string[];
}

export interface JdSummary {
  id: number; title: string; companyName: string | null; location: string;
  experienceRequiredYears: number | null; requiredSkills: string[]; aiSummary: string; createdAt: string;
}
export interface JdDetail extends JdSummary {
  employmentType: string; educationRequirement: EducationLevel; salaryText: string; applicationUrl: string;
  preferredSkills: string[]; responsibilities: string[]; keywords: string[]; rawText: string;
}

export interface SkillEvidence { skill: string; foundIn: string[]; }
export interface MatchBreakdown {
  strongMatches: SkillEvidence[]; partialMatches: SkillEvidence[]; missing: string[];
  potentialIssues: string[]; disclaimer: string;
}
export interface MatchResult {
  resumeId: number; jdId: number; overall: number; requiredMatch: number; preferredMatch: number;
  experienceMatch: number; projectMatch: number; educationMatch: number; keywordMatch: number;
  breakdown: MatchBreakdown;
}
export interface MatchResultResponse { result: MatchResult; resumeName: string; }

export interface ApplicationSummary {
  id: number; companyName: string | null; jobTitle: string; resumeName: string;
  status: ApplicationStatus; dateApplied: string | null; matchScoreAtApplyTime: number | null;
}
export interface CreateApplicationRequest {
  companyName: string; jobTitle: string; jdId?: number; resumeId: number; status?: ApplicationStatus;
}

export interface DashboardStats {
  totalApplications: number; active: number; interviews: number; offers: number;
  resumeCount: number; jdCount: number; resumeUsage: { resumeName: string; count: number }[];
}

// ---- API surface ----
export const api = {
  resumes: {
    list: () => request<ResumeSummary[]>("/api/resumes"),
    get: (id: number) => request<ResumeDetail>(`/api/resumes/${id}`),
    create: (body: CreateResumeRequest) => request<ResumeDetail>("/api/resumes", { method: "POST", body: JSON.stringify(body) }),
    update: (id: number, body: CreateResumeRequest) => request<ResumeDetail>(`/api/resumes/${id}`, { method: "PUT", body: JSON.stringify(body) }),
    // Reads a PDF/DOCX/TXT and returns an editable DRAFT — nothing is saved by this call.
    parse: async (file: File): Promise<ParsedResumeDraft> => {
      const formData = new FormData();
      formData.append("file", file);
      const res = await fetch(`${BASE_URL}/api/resumes/parse`, { method: "POST", body: formData });
      if (!res.ok) {
        const body = await res.json().catch(() => ({ error: res.statusText }));
        throw new Error(body.error || `Couldn't read file: ${res.status}`);
      }
      return res.json();
    },
    // Separate from `request()` — file uploads use multipart/form-data, not JSON, so this
    // must NOT set a Content-Type header (the browser sets the correct multipart boundary
    // itself when given a FormData body).
    uploadDocument: async (resumeId: number, file: File): Promise<{ documentId: number; fileName: string; viewLink: string }> => {
      const formData = new FormData();
      formData.append("file", file);
      const res = await fetch(`${BASE_URL}/api/documents/upload/${resumeId}`, { method: "POST", body: formData });
      if (!res.ok) {
        const body = await res.json().catch(() => ({ error: res.statusText }));
        throw new Error(body.error || `Upload failed: ${res.status}`);
      }
      return res.json();
    },
  },
  jds: {
    list: () => request<JdSummary[]>("/api/jds"),
    get: (id: number) => request<JdDetail>(`/api/jds/${id}`),
    import: (rawText: string) => request<JdDetail>("/api/jds/import", { method: "POST", body: JSON.stringify({ rawText }) }),
  },
  match: {
    bestResumes: (jdId: number) => request<MatchResultResponse[]>(`/api/match/best-resumes/${jdId}`),
    one: (resumeId: number, jdId: number) => request<MatchResultResponse>(`/api/match/${resumeId}/${jdId}`),
  },
  applications: {
    list: () => request<ApplicationSummary[]>("/api/applications"),
    create: (body: CreateApplicationRequest) => request<ApplicationSummary>("/api/applications", { method: "POST", body: JSON.stringify(body) }),
    updateStatus: (id: number, status: ApplicationStatus) =>
      request<ApplicationSummary>(`/api/applications/${id}/status`, { method: "PATCH", body: JSON.stringify({ status }) }),
  },
  dashboard: {
    stats: () => request<DashboardStats>("/api/dashboard"),
  },
};
