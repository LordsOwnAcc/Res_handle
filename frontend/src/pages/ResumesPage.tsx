import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, ResumeSummary } from "../api/client";

export default function ResumesPage() {
  const [resumes, setResumes] = useState<ResumeSummary[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { api.resumes.list().then(setResumes).finally(() => setLoading(false)); }, []);

  return (
    <div className="p-6 md:p-10 max-w-5xl mx-auto">
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-2xl font-semibold">Resumes</h1>
        <Link to="/resumes/new" className="btn-primary">+ New Resume</Link>
      </div>

      {loading ? (
        <div className="text-slate-500">Loading…</div>
      ) : resumes.length === 0 ? (
        <div className="card p-10 text-center text-slate-500">
          No resumes yet. <Link to="/resumes/new" className="text-brand-600 font-medium">Add your first one</Link>.
        </div>
      ) : (
        <div className="grid sm:grid-cols-2 gap-4">
          {resumes.map((r) => (
            <Link key={r.id} to={`/resumes/${r.id}`} className="card p-5 hover:shadow-md transition-shadow">
              <div className="flex items-start justify-between">
                <div>
                  <div className="font-medium">{r.name}</div>
                  <div className="text-xs text-slate-500 mt-0.5">{r.versionLabel}</div>
                </div>
                <span className="text-2xl">📄</span>
              </div>
              {r.targetRoles.length > 0 && (
                <div className="flex flex-wrap gap-1.5 mt-3">
                  {r.targetRoles.slice(0, 3).map((role) => <span key={role} className="chip">{role}</span>)}
                </div>
              )}
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
