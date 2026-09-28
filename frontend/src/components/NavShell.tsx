import { NavLink } from "react-router-dom";
import { ReactNode } from "react";
import { LayoutDashboard, FileText, Target, KanbanSquare, Sparkles } from "lucide-react";

const NAV_ITEMS = [
  { to: "/", label: "Home", icon: LayoutDashboard },
  { to: "/resumes", label: "Resumes", icon: FileText },
  { to: "/jobs", label: "Find Best Resume", icon: Target },
  { to: "/applications", label: "Applications", icon: KanbanSquare },
];

export function NavShell({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-screen flex bg-ink-50 dark:bg-ink-950 text-ink-900 dark:text-ink-50">
      {/* Desktop sidebar */}
      <aside className="hidden md:flex md:flex-col w-64 shrink-0 border-r border-ink-100 dark:border-white/[0.06] px-3 py-5 bg-white/60 dark:bg-ink-900/40 backdrop-blur-xl">
        <div className="flex items-center gap-2.5 px-2.5 mb-8 mt-1">
          <div className="w-8 h-8 rounded-lg bg-brand-gradient flex items-center justify-center shadow-glow">
            <Sparkles size={16} className="text-white" strokeWidth={2.5} />
          </div>
          <span className="font-bold text-[15px] tracking-tight">ResumeIntel</span>
        </div>

        <p className="section-title px-2.5 mb-2">Workspace</p>
        <nav className="flex flex-col gap-0.5">
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === "/"}
                className={({ isActive }) =>
                  `group relative flex items-center gap-3 rounded-xl px-3 py-2.5 text-[13.5px] font-medium transition-colors ${
                    isActive
                      ? "bg-brand-50 dark:bg-brand-500/10 text-brand-700 dark:text-brand-300"
                      : "text-ink-500 dark:text-ink-400 hover:bg-ink-100/70 dark:hover:bg-white/[0.04] hover:text-ink-900 dark:hover:text-ink-100"
                  }`
                }
              >
                {({ isActive }) => (
                  <>
                    {isActive && <span className="absolute left-0 top-1/2 -translate-y-1/2 h-5 w-[3px] rounded-full bg-brand-gradient" />}
                    <Icon size={17} strokeWidth={2} />
                    {item.label}
                  </>
                )}
              </NavLink>
            );
          })}
        </nav>

        <div className="mt-auto px-2.5 py-3 rounded-xl bg-gradient-to-br from-brand-50 to-accent-400/10 dark:from-brand-500/10 dark:to-accent-500/5 border border-brand-100 dark:border-brand-500/10">
          <p className="text-xs font-semibold text-ink-700 dark:text-ink-200">Backend connected</p>
          <p className="text-[11px] text-ink-500 dark:text-ink-400 mt-0.5">Data syncs across web, desktop & mobile.</p>
        </div>
      </aside>

      <main className="flex-1 pb-20 md:pb-0 overflow-y-auto">{children}</main>

      {/* Mobile bottom nav */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 bg-white/90 dark:bg-ink-900/90 backdrop-blur-xl border-t border-ink-100 dark:border-white/[0.06] flex justify-around py-2 z-20">
        {NAV_ITEMS.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === "/"}
              className={({ isActive }) =>
                `flex flex-col items-center gap-0.5 px-3 py-1 text-[10.5px] font-medium ${isActive ? "text-brand-600 dark:text-brand-300" : "text-ink-400"}`
              }
            >
              <Icon size={19} strokeWidth={2} />
              {item.label.split(" ")[0]}
            </NavLink>
          );
        })}
      </nav>
    </div>
  );
}
