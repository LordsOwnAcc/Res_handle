import { NavLink } from "react-router-dom";
import { ReactNode } from "react";

const NAV_ITEMS = [
  { to: "/", label: "Home", icon: "🏠" },
  { to: "/resumes", label: "Resumes", icon: "📄" },
  { to: "/jobs", label: "Find Best Resume", icon: "🎯" },
  { to: "/applications", label: "Applications", icon: "📋" },
];

export function NavShell({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-screen flex text-slate-900 dark:text-slate-100">
      {/* Desktop sidebar */}
      <aside className="hidden md:flex md:flex-col w-64 shrink-0 border-r border-black/5 dark:border-white/5 px-4 py-6">
        <div className="flex items-center gap-2 px-2 mb-8">
          <div className="w-8 h-8 rounded-lg bg-brand-600 flex items-center justify-center text-white font-bold">R</div>
          <span className="font-semibold text-lg">ResumeIntel</span>
        </div>
        <nav className="flex flex-col gap-1">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === "/"}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors ${
                  isActive ? "bg-brand-50 text-brand-700 dark:bg-white/10 dark:text-brand-200" : "hover:bg-black/5 dark:hover:bg-white/5"
                }`
              }
            >
              <span>{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <main className="flex-1 pb-20 md:pb-0 overflow-y-auto">{children}</main>

      {/* Mobile bottom nav */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 bg-white dark:bg-surface-800 border-t border-black/5 dark:border-white/5 flex justify-around py-2">
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === "/"}
            className={({ isActive }) =>
              `flex flex-col items-center gap-0.5 px-3 py-1 text-xs ${isActive ? "text-brand-600" : "text-slate-500"}`
            }
          >
            <span className="text-lg">{item.icon}</span>
            {item.label.split(" ")[0]}
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
