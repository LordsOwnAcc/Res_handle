import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";

/**
 * Manual entry form. PDF/DOCX upload+parse isn't wired in this pass (see README) — this is
 * the honest, fully-working path: type in what the resume contains and it's immediately
 * usable by the matching engine.
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
    <div className="p-6 md:p-10 max-w-2xl mx-auto">
      <h1 className="text-2xl font-semibold mb-6">New Resume</h1>
      <form onSubmit={handleSubmit} className="card p-6 space-y-5">
        <Field label="Name" required><input className="input" value={name} onChange={(e) => setName(e.target.value)} placeholder="Java Backend v2" required /></Field>
        <Field label="Description"><textarea className="input" rows={3} value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Short description of what this resume targets" /></Field>
        <Field label="Target roles (comma-separated)"><input className="input" value={targetRoles} onChange={(e) => setTargetRoles(e.target.value)} placeholder="Java Developer, Backend Developer, SDE" /></Field>
        <Field label="Skills (comma-separated)"><input className="input" value={skills} onChange={(e) => setSkills(e.target.value)} placeholder="Java, Spring Boot, PostgreSQL, Docker" /></Field>

        <div className="border-t border-black/5 dark:border-white/5 pt-5">
          <p className="text-sm font-medium mb-3">Add one project (evidence for skill matches)</p>
          <div className="space-y-3">
            <Field label="Project name"><input className="input" value={projectName} onChange={(e) => setProjectName(e.target.value)} placeholder="Gita API" /></Field>
            <Field label="Technologies (comma-separated)"><input className="input" value={projectTech} onChange={(e) => setProjectTech(e.target.value)} placeholder="Spring Boot, PostgreSQL, REST API" /></Field>
            <Field label="Project description"><textarea className="input" rows={2} value={projectDesc} onChange={(e) => setProjectDesc(e.target.value)} /></Field>
          </div>
        </div>

        <Field label="Total experience (months)"><input className="input" type="number" value={experienceMonths} onChange={(e) => setExperienceMonths(e.target.value)} placeholder="0" /></Field>

        {error && <p className="text-sm text-red-600">{error}</p>}
        <button type="submit" disabled={saving || !name} className="btn-primary w-full">{saving ? "Saving…" : "Save Resume"}</button>
      </form>
    </div>
  );
}

function Field({ label, required, children }: { label: string; required?: boolean; children: React.ReactNode }) {
  return (
    <label className="block">
      <span className="text-sm font-medium">{label}{required && <span className="text-red-500"> *</span>}</span>
      <div className="mt-1.5">{children}</div>
    </label>
  );
}
