import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, DashboardStats, ApplicationSummary } from "../api/client";

export default function DashboardPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [recent, setRecent] = useState<ApplicationSummary[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([api.dashboard.stats(), api.applications.list()])
      .then(([s, apps]) => { setStats(s); setRecent(apps.slice(0, 6)); })
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="p-6 md:p-10 max-w-6xl mx-auto">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-semibold">Job Search Overview</h1>
          <p className="text-sm text-slate-500 mt-1">Everything about your resumes and applications, in one place.</p>
        </div>
        <Link to="/jobs" className="btn-primary">🎯 Find Best Resume</Link>
      </div>

      {loading ? (
        <div className="text-slate-500">Loading…</div>
      ) : stats ? (
        <>
          <div className="grid grid-cols-2 md:grid-cols-6 gap-4 mb-10">
            <StatCard label="Applications" value={stats.totalApplications} />
            <StatCard label="Active" value={stats.active} />
            <StatCard label="Interviews" value={stats.interviews} />
            <StatCard label="Offers" value={stats.offers} accent />
            <StatCard label="Resumes" value={stats.resumeCount} />
            <StatCard label="JDs saved" value={stats.jdCount} />
          </div>

          <div className="grid md:grid-cols-2 gap-6">
            <div className="card p-6">
              <h2 className="font-semibold mb-4">Resume Usage</h2>
              {stats.resumeUsage.length === 0 ? (
                <p className="text-sm text-slate-500">No applications logged yet.</p>
              ) : (
                <div className="space-y-3">
                  {stats.resumeUsage.map((u) => {
                    const max = Math.max(...stats.resumeUsage.map((x) => x.count), 1);
                    return (
                      <div key={u.resumeName}>
                        <div className="flex justify-between text-sm mb-1">
                          <span>{u.resumeName}</span>
                          <span className="text-slate-500">{u.count} applications</span>
                        </div>
                        <div className="h-1.5 rounded-full bg-black/5 dark:bg-white/10 overflow-hidden">
                          <div className="h-full bg-brand-500 rounded-full" style={{ width: `${(u.count / max) * 100}%` }} />
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            <div className="card p-6">
              <h2 className="font-semibold mb-4">Recent Applications</h2>
              {recent.length === 0 ? (
                <p className="text-sm text-slate-500">No applications yet.</p>
              ) : (
                <div className="divide-y divide-black/5 dark:divide-white/5">
                  {recent.map((a) => (
                    <div key={a.id} className="flex items-center justify-between py-2.5">
                      <div>
                        <div className="text-sm font-medium">{a.jobTitle}</div>
                        <div className="text-xs text-slate-500">{a.companyName ?? "Unknown company"} · {a.status.replace(/_/g, " ")}</div>
                      </div>
                      {a.matchScoreAtApplyTime != null && (
                        <span className="chip">{Math.round(a.matchScoreAtApplyTime * 100)}%</span>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </>
      ) : null}
    </div>
  );
}

function StatCard({ label, value, accent }: { label: string; value: number; accent?: boolean }) {
  return (
    <div className="card p-4">
      <div className={`text-2xl font-bold ${accent ? "text-brand-600" : ""}`}>{value}</div>
      <div className="text-xs text-slate-500 mt-1">{label}</div>
    </div>
  );
}
