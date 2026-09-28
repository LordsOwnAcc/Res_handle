import { useEffect, useRef, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { api, ResumeDetail } from "../api/client";
import { FileUp, ExternalLink, ArrowLeft, Sparkles, Briefcase, Code2, GraduationCap } from "lucide-react";

export default function ResumeDetailPage() {
  const { id } = useParams();
  const [resume, setResume] = useState<ResumeDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  function refresh() {
    if (!id) return;
    return api.resumes.get(Number(id)).then(setResume);
  }
  useEffect(() => { refresh()?.finally(() => setLoading(false)); }, [id]);

  async function handleFileSelected(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file || !id) return;
    setUploading(true);
    setUploadError(null);
    try {
      await api.resumes.uploadDocument(Number(id), file);
      await refresh();
    } catch (err: any) {
      setUploadError(err.message || "Upload failed");
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  }

  if (loading) return <div className="p-10"><div className="skeleton h-40 max-w-3xl mx-auto" /></div>;
  if (!resume) return <div className="p-10 text-ink-400">Resume not found.</div>;

  return (
    <div className="p-6 md:p-10 max-w-3xl mx-auto space-y-5 animate-fade-in">
      <Link to="/resumes" className="btn-ghost -ml-1"><ArrowLeft size={14} /> All resumes</Link>

      <div className="card p-6 bg-gradient-to-br from-white to-brand-50/40 dark:from-ink-850 dark:to-brand-500/[0.03]">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-2xl bg-brand-gradient flex items-center justify-center shadow-glow shrink-0">
            <span className="text-white font-bold text-lg">{resume.name[0]?.toUpperCase()}</span>
          </div>
          <div>
            <h1 className="text-xl font-bold tracking-tight">{resume.name}</h1>
            <p className="text-sm text-ink-400 font-medium">{resume.versionLabel}</p>
          </div>
        </div>
      </div>

      <Section title="Original File" icon={FileUp}>
        {resume.documentUrl ? (
          <div className="flex items-center justify-between gap-3">
            <div className="text-sm min-w-0">
              <span className="font-medium truncate block">{resume.documentFileName}</span>
              <span className="text-ink-400 text-xs">Stored on Google Drive</span>
            </div>
            <a href={resume.documentUrl} target="_blank" rel="noreferrer" className="btn-secondary shrink-0">
              Open <ExternalLink size={14} />
            </a>
          </div>
        ) : (
          <div>
            <p className="text-sm text-ink-400 mb-3">No file attached yet. It's saved to your Google Drive, not this app's server.</p>
            <label className="btn-secondary cursor-pointer inline-flex">
              <FileUp size={15} /> {uploading ? "Uploading…" : "Choose file"}
              <input ref={fileInputRef} type="file" accept=".pdf,.doc,.docx,.txt" onChange={handleFileSelected} disabled={uploading} className="hidden" />
            </label>
            {uploadError && <p className="text-xs text-red-500 mt-2">{uploadError}</p>}
          </div>
        )}
      </Section>

      {resume.description && (
        <Section title="Description" icon={Sparkles}><p className="text-sm text-ink-600 dark:text-ink-300 leading-relaxed">{resume.description}</p></Section>
      )}

      {resume.targetRoles.length > 0 && (
        <Section title="Target Roles" icon={Briefcase}>
          <div className="flex flex-wrap gap-2">{resume.targetRoles.map((r) => <span key={r} className="chip-brand">{r}</span>)}</div>
        </Section>
      )}

      <Section title="Skills" icon={Code2}>
        {resume.skills.length === 0 ? <Empty /> : (
          <div className="flex flex-wrap gap-2">{resume.skills.map((s) => <span key={s.skillName} className="chip">{s.skillName}</span>)}</div>
        )}
      </Section>

      <Section title="Projects" icon={Sparkles}>
        {resume.projects.length === 0 ? <Empty /> : (
          <div className="space-y-4">
            {resume.projects.map((p) => (
              <div key={p.name} className="relative pl-5 before:absolute before:left-0 before:top-1 before:bottom-1 before:w-0.5 before:rounded-full before:bg-brand-gradient">
                <div className="font-semibold text-sm">{p.name}</div>
                {p.technologies.length > 0 && (
                  <div className="flex flex-wrap gap-1.5 mt-1.5">
                    {p.technologies.map((t) => <span key={t} className="chip !py-0.5 !text-[11px]">{t}</span>)}
                  </div>
                )}
                {p.description && <p className="text-sm text-ink-600 dark:text-ink-300 mt-2 leading-relaxed">{p.description}</p>}
              </div>
            ))}
          </div>
        )}
      </Section>

      <Section title="Experience" icon={GraduationCap}>
        {resume.experience.length === 0 ? <Empty /> : (
          <div className="space-y-3">
            {resume.experience.map((e, i) => (
              <div key={i} className="text-sm">
                <span className="font-semibold">{e.role}</span>
                <span className="text-ink-400"> at {e.company} — {e.durationMonths} months</span>
              </div>
            ))}
          </div>
        )}
      </Section>
    </div>
  );
}

function Section({ title, icon: Icon, children }: { title: string; icon: any; children: React.ReactNode }) {
  return (
    <div className="card p-6">
      <h2 className="font-semibold mb-4 flex items-center gap-2 text-[14px] text-ink-700 dark:text-ink-200">
        <Icon size={15} className="text-brand-500" strokeWidth={2} /> {title}
      </h2>
      {children}
    </div>
  );
}
function Empty() { return <p className="text-sm text-ink-400">Nothing added yet.</p>; }
