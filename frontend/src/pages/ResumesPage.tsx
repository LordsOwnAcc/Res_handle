import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, ResumeSummary } from "../api/client";
import { Plus, FileText, ArrowRight } from "lucide-react";

export default function ResumesPage() {
  const [resumes, setResumes] = useState<ResumeSummary[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { api.resumes.list().then(setResumes).finally(() => setLoading(false)); }, []);

  return (
    <div className="p-6 md:p-10 max-w-5xl mx-auto animate-fade-in">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Resumes</h1>
          <p className="text-sm text-ink-400 mt-1">Every version, ready to be matched against a job description.</p>
        </div>
        <Link to="/resumes/new" className="btn-primary">
          <Plus size={16} strokeWidth={2.5} /> New Resume
        </Link>
      </div>

      {loading ? (
        <div className="grid sm:grid-cols-2 gap-4">
          {Array.from({ length: 4 }).map((_, i) => <div key={i} className="skeleton h-28" />)}
        </div>
      ) : resumes.length === 0 ? (
        <div className="card p-14 text-center">
          <div className="w-12 h-12 rounded-2xl bg-brand-50 dark:bg-brand-500/10 flex items-center justify-center mx-auto mb-4">
            <FileText size={20} className="text-brand-600 dark:text-brand-300" />
          </div>
          <p className="text-ink-500 mb-4">No resumes yet.</p>
          <Link to="/resumes/new" className="btn-primary inline-flex">
            <Plus size={16} /> Add your first resume
          </Link>
        </div>
      ) : (
        <div className="grid sm:grid-cols-2 gap-4">
          {resumes.map((r) => (
            <Link key={r.id} to={`/resumes/${r.id}`} className="card-interactive p-5 group">
              <div className="flex items-start justify-between mb-3">
                <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-brand-50 to-brand-100 dark:from-brand-500/15 dark:to-brand-500/5 flex items-center justify-center">
                  <FileText size={17} className="text-brand-600 dark:text-brand-300" strokeWidth={2} />
                </div>
                <ArrowRight size={16} className="text-ink-300 group-hover:text-brand-500 group-hover:translate-x-0.5 transition-all mt-2" />
              </div>
              <div className="font-semibold text-[15px]">{r.name}</div>
              <div className="text-xs text-ink-400 mt-0.5 font-medium">{r.versionLabel}</div>
              {r.targetRoles.length > 0 && (
                <div className="flex flex-wrap gap-1.5 mt-3.5">
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
