import { useRef, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { api, EducationLevel, ProjectInput } from "../api/client";
import { ArrowLeft, FileText, Briefcase, Code2, Sparkles, Upload, Plus, Trash2, AlertTriangle } from "lucide-react";

interface ProjectDraft { name: string; technologies: string; description: string; }
const EMPTY_PROJECT: ProjectDraft = { name: "", technologies: "", description: "" };

export default function NewResumePage() {
  const navigate = useNavigate();
  const fileRef = useRef<HTMLInputElement>(null);

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [targetRoles, setTargetRoles] = useState("");
  const [skills, setSkills] = useState("");
  const [education, setEducation] = useState<EducationLevel>("NONE");
  const [projects, setProjects] = useState<ProjectDraft[]>([{ ...EMPTY_PROJECT }]);
  const [experienceMonths, setExperienceMonths] = useState("");
  const [contact, setContact] = useState<string>("");

  const [importedFile, setImportedFile] = useState<File | null>(null);
  const [importing, setImporting] = useState(false);
  const [warnings, setWarnings] = useState<string[]>([]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleImport(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    setImporting(true);
    setError(null);
    try {
      const d = await api.resumes.parse(file);
      // Fill the form — the user reviews and edits everything before anything is saved.
      setName(d.name ? `${d.name} — Resume` : file.name.replace(/\.[^.]+$/, ""));
      setDescription(d.description);
      setTargetRoles(d.targetRoles.join(", "));
      setSkills(d.skills.map((s) => s.skillName).join(", "));
      setEducation(d.educationLevel);
      setExperienceMonths(d.experienceMonths ? String(d.experienceMonths) : "");
      setProjects(
        d.projects.length
          ? d.projects.map((p) => ({ name: p.name, technologies: p.technologies.join(", "), description: p.description ?? "" }))
          : [{ ...EMPTY_PROJECT }]
      );
      setContact([d.email, d.phone, d.github, d.linkedin].filter(Boolean).join("  ·  "));
      setWarnings(d.warnings);
      setImportedFile(file);
    } catch (err: any) {
      setError(err.message || "Couldn't read that file");
    } finally {
      setImporting(false);
      if (fileRef.current) fileRef.current.value = "";
    }
  }

  function updateProject(i: number, patch: Partial<ProjectDraft>) {
    setProjects((prev) => prev.map((p, idx) => (idx === i ? { ...p, ...patch } : p)));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError(null);
    const csv = (v: string) => v.split(",").map((s) => s.trim()).filter(Boolean);
    try {
      const projectInputs: ProjectInput[] = projects
        .filter((p) => p.name.trim())
        .map((p) => ({ name: p.name.trim(), technologies: csv(p.technologies), description: p.description }));
      const resume = await api.resumes.create({
        name,
        description,
        educationLevel: education,
        targetRoles: csv(targetRoles),
        skills: csv(skills).map((skillName) => ({ skillName })),
        projects: projectInputs,
        experience: Number(experienceMonths) > 0
          ? [{ company: "—", role: "—", durationMonths: Number(experienceMonths), responsibilities: "", technologies: [] }]
          : [],
      });

      // Attach the original file to Google Drive if it was imported (best-effort: the resume
      // is already saved, so a Drive problem shouldn't lose the user's work).
      if (importedFile) {
        try { await api.resumes.uploadDocument(resume.id, importedFile); } catch { /* shown on detail page instead */ }
      }
      navigate(`/resumes/${resume.id}`);
    } catch (err: any) {
      setError(err.message || "Failed to save resume");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="p-6 md:p-10 max-w-2xl mx-auto animate-fade-in">
      <Link to="/resumes" className="btn-ghost -ml-1 mb-4"><ArrowLeft size={14} /> All resumes</Link>
      <h1 className="text-2xl font-bold tracking-tight mb-1">New Resume</h1>
      <p className="text-sm text-ink-400 mb-6">Import a PDF or DOCX to pre-fill everything, or type it in yourself.</p>

      {/* Import */}
      <label className="card p-6 mb-5 flex flex-col items-center text-center cursor-pointer border-dashed hover:border-brand-300 transition-colors">
        <div className="w-11 h-11 rounded-2xl bg-brand-50 dark:bg-brand-500/10 flex items-center justify-center mb-3">
          <Upload size={19} className="text-brand-600 dark:text-brand-300" />
        </div>
        <div className="font-semibold text-sm">{importing ? "Reading your resume…" : importedFile ? `Imported: ${importedFile.name}` : "Import from PDF or DOCX"}</div>
        <div className="text-xs text-ink-400 mt-1">We'll extract skills, projects and experience. You review everything before saving.</div>
        <input ref={fileRef} type="file" accept=".pdf,.docx,.txt" className="hidden" onChange={handleImport} disabled={importing} />
      </label>

      {warnings.length > 0 && (
        <div className="card p-4 mb-5 border-amber-200 dark:border-amber-500/20 bg-amber-50/60 dark:bg-amber-500/5">
          <div className="flex items-center gap-1.5 text-amber-700 dark:text-amber-400 font-semibold text-xs mb-1.5"><AlertTriangle size={13} /> CHECK BEFORE SAVING</div>
          <ul className="text-xs text-ink-600 dark:text-ink-300 space-y-1 list-disc pl-4">{warnings.map((w) => <li key={w}>{w}</li>)}</ul>
          {contact && <p className="text-xs text-ink-400 mt-2">Detected contact: {contact}</p>}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-5">
        <div className="card p-6 space-y-5">
          <FormHeader icon={FileText} title="Basics" />
          <Field label="Name" required><input className="input" value={name} onChange={(e) => setName(e.target.value)} placeholder="Java Backend v2" required /></Field>
          <Field label="Description"><textarea className="input" rows={3} value={description} onChange={(e) => setDescription(e.target.value)} placeholder="What this resume targets" /></Field>
        </div>

        <div className="card p-6 space-y-5">
          <FormHeader icon={Briefcase} title="Targeting" />
          <Field label="Target roles" hint="comma-separated"><input className="input" value={targetRoles} onChange={(e) => setTargetRoles(e.target.value)} placeholder="Java Developer, Backend Developer, SDE" /></Field>
          <Field label="Skills" hint="comma-separated"><input className="input" value={skills} onChange={(e) => setSkills(e.target.value)} placeholder="Java, Spring Boot, PostgreSQL, Docker" /></Field>
          <Field label="Highest education">
            <select className="input" value={education} onChange={(e) => setEducation(e.target.value as EducationLevel)}>
              <option value="NONE">Not specified</option><option value="DIPLOMA">Diploma</option>
              <option value="BACHELORS">Bachelor's</option><option value="MASTERS">Master's</option><option value="PHD">PhD</option>
            </select>
          </Field>
        </div>

        <div className="card p-6 space-y-5">
          <FormHeader icon={Code2} title="Projects (evidence for skill matches)" />
          {projects.map((p, i) => (
            <div key={i} className="space-y-3 pb-5 border-b border-ink-100 dark:border-white/[0.06] last:border-0 last:pb-0">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-ink-400">PROJECT {i + 1}</span>
                {projects.length > 1 && (
                  <button type="button" onClick={() => setProjects(projects.filter((_, idx) => idx !== i))} className="text-ink-400 hover:text-red-500"><Trash2 size={14} /></button>
                )}
              </div>
              <Field label="Name"><input className="input" value={p.name} onChange={(e) => updateProject(i, { name: e.target.value })} placeholder="Gita API" /></Field>
              <Field label="Technologies" hint="comma-separated"><input className="input" value={p.technologies} onChange={(e) => updateProject(i, { technologies: e.target.value })} placeholder="Spring Boot, PostgreSQL" /></Field>
              <Field label="Description"><textarea className="input" rows={2} value={p.description} onChange={(e) => updateProject(i, { description: e.target.value })} /></Field>
            </div>
          ))}
          <button type="button" className="btn-ghost" onClick={() => setProjects([...projects, { ...EMPTY_PROJECT }])}><Plus size={14} /> Add project</button>
        </div>

        <div className="card p-6 space-y-5">
          <FormHeader icon={Sparkles} title="Experience" />
          <Field label="Total experience" hint="months"><input className="input" type="number" min="0" value={experienceMonths} onChange={(e) => setExperienceMonths(e.target.value)} placeholder="0" /></Field>
        </div>

        {error && <p className="text-sm text-red-500 px-1">{error}</p>}
        <button type="submit" disabled={saving || !name} className="btn-primary w-full py-3">{saving ? "Saving…" : "Save Resume"}</button>
      </form>
    </div>
  );
}

function FormHeader({ icon: Icon, title }: { icon: any; title: string }) {
  return (
    <div className="flex items-center gap-2 pb-1 border-b border-ink-100 dark:border-white/[0.06]">
      <Icon size={15} className="text-brand-500" strokeWidth={2} />
      <h2 className="text-[13px] font-bold uppercase tracking-wide text-ink-500 dark:text-ink-400">{title}</h2>
    </div>
  );
}

function Field({ label, required, hint, children }: { label: string; required?: boolean; hint?: string; children: React.ReactNode }) {
  return (
    <label className="block">
      <span className="label">{label}{required && <span className="text-red-500"> *</span>}{hint && <span className="text-ink-400 font-normal"> ({hint})</span>}</span>
      <div className="mt-1.5">{children}</div>
    </label>
  );
}
