import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api, ResumeDetail } from "../api/client";

export default function ResumeDetailPage() {
  const { id } = useParams();
  const [resume, setResume] = useState<ResumeDetail | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    api.resumes.get(Number(id)).then(setResume).finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div className="p-10 text-slate-500">Loading…</div>;
  if (!resume) return <div className="p-10 text-slate-500">Resume not found.</div>;

  return (
    <div className="p-6 md:p-10 max-w-3xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">{resume.name}</h1>
        <p className="text-sm text-slate-500">{resume.versionLabel}</p>
      </div>

      {resume.description && <Section title="Description"><p className="text-sm">{resume.description}</p></Section>}

      {resume.targetRoles.length > 0 && (
        <Section title="Target Roles">
          <div className="flex flex-wrap gap-2">{resume.targetRoles.map((r) => <span key={r} className="chip">{r}</span>)}</div>
        </Section>
      )}

      <Section title="Skills">
        {resume.skills.length === 0 ? <Empty /> : (
          <div className="flex flex-wrap gap-2">{resume.skills.map((s) => <span key={s.skillName} className="chip">{s.skillName}</span>)}</div>
        )}
      </Section>

      <Section title="Projects">
        {resume.projects.length === 0 ? <Empty /> : (
          <div className="space-y-4">
            {resume.projects.map((p) => (
              <div key={p.name} className="border-l-2 border-brand-200 dark:border-brand-800 pl-4">
                <div className="font-medium text-sm">{p.name}</div>
                {p.technologies.length > 0 && <div className="text-xs text-slate-500 mt-0.5">{p.technologies.join(" · ")}</div>}
                {p.description && <p className="text-sm mt-1.5">{p.description}</p>}
              </div>
            ))}
          </div>
        )}
      </Section>

      <Section title="Experience">
        {resume.experience.length === 0 ? <Empty /> : (
          <div className="space-y-3">
            {resume.experience.map((e, i) => (
              <div key={i} className="text-sm">
                <span className="font-medium">{e.role}</span> at {e.company} — {e.durationMonths} months
              </div>
            ))}
          </div>
        )}
      </Section>
    </div>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="card p-6">
      <h2 className="font-semibold mb-3">{title}</h2>
      {children}
    </div>
  );
}
function Empty() { return <p className="text-sm text-slate-500">Nothing added yet.</p>; }
