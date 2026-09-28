import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, DashboardStats, ApplicationSummary } from "../api/client";
import { Target, Briefcase, Zap, Trophy, FileText, ClipboardList, ArrowRight, TrendingUp } from "lucide-react";

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
    <div className="p-6 md:p-10 max-w-6xl mx-auto animate-fade-in">
      {/* Hero */}
      <div className="relative overflow-hidden rounded-3xl bg-brand-gradient p-7 md:p-9 mb-8 shadow-elevated">
        <div className="absolute -right-16 -top-16 w-64 h-64 rounded-full bg-white/10 blur-2xl" />
        <div className="absolute -right-4 bottom-0 w-40 h-40 rounded-full bg-accent-400/20 blur-2xl" />
        <div className="relative flex flex-col md:flex-row md:items-center justify-between gap-5">
          <div>
            <p className="text-white/70 text-xs font-semibold uppercase tracking-wider mb-1.5">Job Search Overview</p>
            <h1 className="text-2xl md:text-3xl font-bold text-white tracking-tight">Everything in one place.</h1>
            <p className="text-white/75 text-sm mt-1.5 max-w-md">Resumes, job descriptions, and applications — synced across web, desktop, and mobile.</p>
          </div>
          <Link to="/jobs" className="inline-flex items-center gap-2 rounded-xl bg-white text-brand-700 px-5 py-3 text-sm font-bold shadow-lg hover:shadow-xl active:scale-[.98] transition-all whitespace-nowrap">
            <Target size={16} strokeWidth={2.5} /> Find Best Resume
          </Link>
        </div>
      </div>

      {loading ? (
        <div className="grid grid-cols-2 md:grid-cols-6 gap-4">
          {Array.from({ length: 6 }).map((_, i) => <div key={i} className="skeleton h-24" />)}
        </div>
      ) : stats ? (
        <>
          <div className="grid grid-cols-2 md:grid-cols-6 gap-4 mb-8">
            <StatCard icon={Briefcase} label="Applications" value={stats.totalApplications} />
            <StatCard icon={Zap} label="Active" value={stats.active} />
            <StatCard icon={TrendingUp} label="Interviews" value={stats.interviews} />
            <StatCard icon={Trophy} label="Offers" value={stats.offers} accent />
            <StatCard icon={FileText} label="Resumes" value={stats.resumeCount} />
            <StatCard icon={ClipboardList} label="JDs saved" value={stats.jdCount} />
          </div>

          <div className="grid md:grid-cols-2 gap-6">
            <div className="card p-6 animate-slide-up">
              <h2 className="font-semibold mb-5 flex items-center gap-2 text-[15px]">
                <span className="w-1.5 h-1.5 rounded-full bg-brand-500" /> Resume Usage
              </h2>
              {stats.resumeUsage.length === 0 ? (
                <EmptyState text="No applications logged yet." />
              ) : (
                <div className="space-y-4">
                  {stats.resumeUsage.map((u) => {
                    const max = Math.max(...stats.resumeUsage.map((x) => x.count), 1);
                    return (
                      <div key={u.resumeName}>
                        <div className="flex justify-between text-sm mb-1.5">
                          <span className="font-medium">{u.resumeName}</span>
                          <span className="text-ink-400 text-xs font-medium">{u.count} applications</span>
                        </div>
                        <div className="h-2 rounded-full bg-ink-100 dark:bg-white/[0.06] overflow-hidden">
                          <div className="h-full bg-brand-gradient rounded-full transition-all duration-500" style={{ width: `${(u.count / max) * 100}%` }} />
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            <div className="card p-6 animate-slide-up">
              <h2 className="font-semibold mb-5 flex items-center gap-2 text-[15px]">
                <span className="w-1.5 h-1.5 rounded-full bg-accent-500" /> Recent Applications
              </h2>
              {recent.length === 0 ? (
                <EmptyState text="No applications yet." />
              ) : (
                <div className="divide-y divide-ink-100 dark:divide-white/[0.06]">
                  {recent.map((a) => (
                    <div key={a.id} className="flex items-center justify-between py-3 first:pt-0 last:pb-0">
                      <div className="flex items-center gap-3 min-w-0">
                        <div className="w-8 h-8 rounded-lg bg-ink-100 dark:bg-white/[0.06] flex items-center justify-center text-xs font-bold text-ink-500 shrink-0">
                          {(a.companyName ?? "?")[0].toUpperCase()}
                        </div>
                        <div className="min-w-0">
                          <div className="text-sm font-medium truncate">{a.jobTitle}</div>
                          <div className="text-xs text-ink-400 truncate">{a.companyName ?? "Unknown company"} · {a.status.replace(/_/g, " ")}</div>
                        </div>
                      </div>
                      {a.matchScoreAtApplyTime != null && (
                        <span className="chip-brand shrink-0">{Math.round(a.matchScoreAtApplyTime * 100)}%</span>
                      )}
                    </div>
                  ))}
                </div>
              )}
              <Link to="/applications" className="btn-ghost mt-4 -ml-1">
                View all applications <ArrowRight size={14} />
              </Link>
            </div>
          </div>
        </>
      ) : null}
    </div>
  );
}

function StatCard({ icon: Icon, label, value, accent }: { icon: any; label: string; value: number; accent?: boolean }) {
  return (
    <div className="card p-4 hover:shadow-elevated transition-shadow duration-200">
      <div className={`w-8 h-8 rounded-lg flex items-center justify-center mb-3 ${accent ? "bg-accent-500/10 text-accent-600" : "bg-brand-50 dark:bg-brand-500/10 text-brand-600 dark:text-brand-300"}`}>
        <Icon size={15} strokeWidth={2.25} />
      </div>
      <div className="text-2xl font-bold tracking-tight">{value}</div>
      <div className="text-xs text-ink-400 mt-0.5 font-medium">{label}</div>
    </div>
  );
}

function EmptyState({ text }: { text: string }) {
  return <p className="text-sm text-ink-400 py-6 text-center">{text}</p>;
}
