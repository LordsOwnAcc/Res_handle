import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { api } from "../api/client";
import { ArrowLeft, FileText, Briefcase, Code2, Sparkles } from "lucide-react";

/**
 * Manual entry form. PDF/DOCX auto-extraction isn't wired (see README) — this is the honest,
 * fully-working path: type in what the resume contains and it's immediately usable by the
 * matching engine. The original file can be attached afterward from the detail page.
 */
export default function NewResumePage() {
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [targetRoles, setTargetRoles] = useState("");
  const [skills, setSkills] = useState("");
  const [projectName, setProjectName] = useState("");
  const [projectTech, setProjectTech] = useState("");
  const [projectDesc, setProjectDesc] = useState("");
  const [experienceMonths, setExperienceMonths] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const resume = await api.resumes.create({
        name,
        description,
        targetRoles: targetRoles.split(",").map((s) => s.trim()).filter(Boolean),
        skills: skills.split(",").map((s) => s.trim()).filter(Boolean).map((skillName) => ({ skillName })),
        projects: projectName ? [{
          name: projectName,
          technologies: projectTech.split(",").map((s) => s.trim()).filter(Boolean),
          description: projectDesc,
        }] : [],
        experience: experienceMonths ? [{
          company: "—", role: "—", durationMonths: Number(experienceMonths) || 0, responsibilities: "", technologies: [],
        }] : [],
      });
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
      <p className="text-sm text-ink-400 mb-6">Fill in what this resume covers — it's immediately part of the matching pool.</p>

      <form onSubmit={handleSubmit} className="space-y-5">
        <div className="card p-6 space-y-5">
          <FormHeader icon={FileText} title="Basics" />
          <Field label="Name" required><input className="input" value={name} onChange={(e) => setName(e.target.value)} placeholder="Java Backend v2" required /></Field>
          <Field label="Description"><textarea className="input" rows={3} value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Short description of what this resume targets" /></Field>
        </div>

        <div className="card p-6 space-y-5">
          <FormHeader icon={Briefcase} title="Targeting" />
          <Field label="Target roles" hint="comma-separated"><input className="input" value={targetRoles} onChange={(e) => setTargetRoles(e.target.value)} placeholder="Java Developer, Backend Developer, SDE" /></Field>
          <Field label="Skills" hint="comma-separated"><input className="input" value={skills} onChange={(e) => setSkills(e.target.value)} placeholder="Java, Spring Boot, PostgreSQL, Docker" /></Field>
        </div>

        <div className="card p-6 space-y-5">
          <FormHeader icon={Code2} title="A project (evidence for skill matches)" />
          <Field label="Project name"><input className="input" value={projectName} onChange={(e) => setProjectName(e.target.value)} placeholder="Gita API" /></Field>
          <Field label="Technologies" hint="comma-separated"><input className="input" value={projectTech} onChange={(e) => setProjectTech(e.target.value)} placeholder="Spring Boot, PostgreSQL, REST API" /></Field>
          <Field label="Description"><textarea className="input" rows={2} value={projectDesc} onChange={(e) => setProjectDesc(e.target.value)} /></Field>
        </div>

        <div className="card p-6 space-y-5">
          <FormHeader icon={Sparkles} title="Experience" />
          <Field label="Total experience" hint="months"><input className="input" type="number" value={experienceMonths} onChange={(e) => setExperienceMonths(e.target.value)} placeholder="0" /></Field>
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
