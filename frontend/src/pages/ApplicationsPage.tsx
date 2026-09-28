import { useEffect, useState } from "react";
import { api, ApplicationSummary, ApplicationStatus } from "../api/client";
import { KanbanSquare } from "lucide-react";

const STATUSES: ApplicationStatus[] = [
  "SAVED", "PREPARING", "APPLIED", "ASSESSMENT", "INTERVIEW", "TECHNICAL_INTERVIEW",
  "HR", "OFFER", "REJECTED", "WITHDRAWN", "ON_HOLD",
];

const STATUS_COLORS: Record<ApplicationStatus, string> = {
  SAVED: "bg-ink-300", PREPARING: "bg-sky-400", APPLIED: "bg-brand-500",
  ASSESSMENT: "bg-violet-400", INTERVIEW: "bg-amber-400", TECHNICAL_INTERVIEW: "bg-amber-500",
  HR: "bg-orange-400", OFFER: "bg-emerald-500", REJECTED: "bg-red-400",
  WITHDRAWN: "bg-ink-400", ON_HOLD: "bg-yellow-400",
};

export default function ApplicationsPage() {
  const [apps, setApps] = useState<ApplicationSummary[]>([]);
  const [loading, setLoading] = useState(true);

  function refresh() { return api.applications.list().then(setApps); }
  useEffect(() => { refresh().finally(() => setLoading(false)); }, []);

  async function changeStatus(id: number, status: ApplicationStatus) {
    setApps((prev) => prev.map((a) => (a.id === id ? { ...a, status } : a)));
    try {
      await api.applications.updateStatus(id, status);
    } catch {
      refresh();
    }
  }

  const byStatus = STATUSES.map((s) => ({ status: s, items: apps.filter((a) => a.status === s) }));

  return (
    <div className="p-6 md:p-10 animate-fade-in">
      <div className="flex items-center gap-2.5 mb-6 max-w-6xl mx-auto md:mx-0 md:pl-0">
        <div className="w-8 h-8 rounded-lg bg-brand-gradient flex items-center justify-center shadow-glow">
          <KanbanSquare size={15} className="text-white" strokeWidth={2.5} />
        </div>
        <h1 className="text-2xl font-bold tracking-tight">Applications</h1>
      </div>

      {loading ? (
        <div className="text-ink-400 px-6">Loading…</div>
      ) : (
        <div className="flex gap-4 overflow-x-auto pb-4 px-6 md:px-10 -mx-6 md:-mx-10">
          {byStatus.map(({ status, items }) => (
            <div key={status} className="w-64 shrink-0">
              <div className="flex items-center gap-2 mb-3 px-0.5">
                <span className={`w-2 h-2 rounded-full ${STATUS_COLORS[status]}`} />
                <span className="text-xs font-bold uppercase tracking-wide text-ink-500 dark:text-ink-400">{status.replace(/_/g, " ")}</span>
                <span className="text-xs text-ink-300 font-medium">{items.length}</span>
              </div>
              <div className="space-y-2.5">
                {items.map((a) => (
                  <div key={a.id} className="card p-3.5 hover:shadow-elevated transition-shadow duration-200">
                    <div className="text-sm font-semibold leading-snug">{a.jobTitle}</div>
                    <div className="text-xs text-ink-400 mt-0.5">{a.companyName ?? "Unknown company"}</div>
                    {a.matchScoreAtApplyTime != null && (
                      <span className="chip-brand mt-2 inline-block !py-0.5">{Math.round(a.matchScoreAtApplyTime * 100)}% match</span>
                    )}
                    <select
                      className="input mt-2.5 !py-1.5 !px-2.5 text-xs cursor-pointer"
                      value={a.status}
                      onChange={(e) => changeStatus(a.id, e.target.value as ApplicationStatus)}
                    >
                      {STATUSES.map((s) => <option key={s} value={s}>{s.replace(/_/g, " ")}</option>)}
                    </select>
                  </div>
                ))}
                {items.length === 0 && (
                  <div className="h-16 rounded-xl border-2 border-dashed border-ink-100 dark:border-white/[0.06]" />
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
