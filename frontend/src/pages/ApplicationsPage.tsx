import { useEffect, useState } from "react";
import { api, ApplicationSummary, ApplicationStatus } from "../api/client";

const STATUSES: ApplicationStatus[] = [
  "SAVED", "PREPARING", "APPLIED", "ASSESSMENT", "INTERVIEW", "TECHNICAL_INTERVIEW",
  "HR", "OFFER", "REJECTED", "WITHDRAWN", "ON_HOLD",
];

export default function ApplicationsPage() {
  const [apps, setApps] = useState<ApplicationSummary[]>([]);
  const [loading, setLoading] = useState(true);

  function refresh() { return api.applications.list().then(setApps); }
  useEffect(() => { refresh().finally(() => setLoading(false)); }, []);

  async function changeStatus(id: number, status: ApplicationStatus) {
    setApps((prev) => prev.map((a) => (a.id === id ? { ...a, status } : a))); // optimistic
    try {
      await api.applications.updateStatus(id, status);
    } catch {
      refresh(); // revert on failure
    }
  }

  const byStatus = STATUSES.map((s) => ({ status: s, items: apps.filter((a) => a.status === s) }));

  return (
    <div className="p-6 md:p-10">
      <h1 className="text-2xl font-semibold mb-6">Applications</h1>
      {loading ? (
        <div className="text-slate-500">Loading…</div>
      ) : (
        <div className="flex gap-4 overflow-x-auto pb-4">
          {byStatus.map(({ status, items }) => (
            <div key={status} className="w-64 shrink-0">
              <div className="text-sm font-semibold mb-2 text-slate-600 dark:text-slate-300">
                {status.replace(/_/g, " ")} <span className="text-slate-400">({items.length})</span>
              </div>
              <div className="space-y-2">
                {items.map((a) => (
                  <div key={a.id} className="card p-3">
                    <div className="text-sm font-medium">{a.jobTitle}</div>
                    <div className="text-xs text-slate-500">{a.companyName ?? "Unknown company"}</div>
                    {a.matchScoreAtApplyTime != null && (
                      <span className="chip mt-2 inline-block">{Math.round(a.matchScoreAtApplyTime * 100)}% match</span>
                    )}
                    <select
                      className="input mt-2 !py-1 text-xs"
                      value={a.status}
                      onChange={(e) => changeStatus(a.id, e.target.value as ApplicationStatus)}
                    >
                      {STATUSES.map((s) => <option key={s} value={s}>{s.replace(/_/g, " ")}</option>)}
                    </select>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
