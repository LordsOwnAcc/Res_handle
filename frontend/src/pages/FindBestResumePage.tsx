import { useState } from "react";
import { api, MatchResultResponse } from "../api/client";
import { Target, Sparkles, CheckCircle2, XCircle, AlertTriangle, ChevronDown, Info } from "lucide-react";

export default function FindBestResumePage() {
  const [jdText, setJdText] = useState("");
  const [loading, setLoading] = useState(false);
  const [results, setResults] = useState<MatchResultResponse[]>([]);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit() {
    setLoading(true);
    setError(null);
    try {
      const jd = await api.jds.import(jdText);
      const matches = await api.match.bestResumes(jd.id);
      setResults(matches);
    } catch (err: any) {
      setError(err.message || "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="p-6 md:p-10 max-w-3xl mx-auto animate-fade-in">
      <div className="flex items-center gap-2.5 mb-2">
        <div className="w-8 h-8 rounded-lg bg-brand-gradient flex items-center justify-center shadow-glow">
          <Target size={15} className="text-white" strokeWidth={2.5} />
        </div>
        <h1 className="text-2xl font-bold tracking-tight">Find Best Resume</h1>
      </div>
      <p className="text-sm text-ink-400 mb-6 ml-[42px]">
        Paste a job description. Every resume in your library is scored across six factors — not just keyword overlap.
      </p>

      <div className="card p-6">
        <textarea
          className="input min-h-[180px] resize-y"
          value={jdText}
          onChange={(e) => setJdText(e.target.value)}
          placeholder="Paste the full job description here…"
        />
        <button onClick={handleSubmit} disabled={!jdText.trim() || loading} className="btn-primary mt-4 w-full sm:w-auto px-8">
          {loading ? (
            <><span className="w-3.5 h-3.5 border-2 border-white/40 border-t-white rounded-full animate-spin" /> Analyzing…</>
          ) : (
            <><Sparkles size={16} /> Find Best Resume</>
          )}
        </button>
        {error && <p className="text-sm text-red-500 mt-2">{error}</p>}
      </div>

      {results.length > 0 && (
        <div className="mt-8 space-y-4">
          <p className="section-title px-1">{results.length} resume{results.length !== 1 ? "s" : ""} ranked</p>
          {results.map((r, i) => <MatchCard key={r.result.resumeId} data={r} rank={i + 1} />)}
        </div>
      )}
    </div>
  );
}

function MatchCard({ data, rank }: { data: MatchResultResponse; rank: number }) {
  const [expanded, setExpanded] = useState(rank === 1);
  const { result, resumeName } = data;
  const pct = Math.round(result.overall * 100);
  const tier = pct >= 75 ? "strong" : pct >= 50 ? "partial" : "weak";
  const ring = tier === "strong" ? "#10b981" : tier === "partial" ? "#f59e0b" : "#ef4444";
  const textColor = tier === "strong" ? "text-emerald-600 dark:text-emerald-400" : tier === "partial" ? "text-amber-600 dark:text-amber-400" : "text-red-500";

  const subScores: [string, number][] = [
    ["Required", result.requiredMatch], ["Preferred", result.preferredMatch],
    ["Experience", result.experienceMatch], ["Projects", result.projectMatch],
    ["Education", result.educationMatch], ["Keywords", result.keywordMatch],
  ];

  return (
    <div className="card p-6 animate-slide-up">
      <div className="flex items-center gap-4">
        <ScoreRing pct={pct} color={ring} />
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2">
            {rank === 1 && <span className="chip-brand !py-0.5 !text-[10px]">TOP MATCH</span>}
            <div className="font-semibold truncate">{resumeName}</div>
          </div>
          <div className={`text-xs font-semibold mt-0.5 ${textColor}`}>
            {tier === "strong" ? "Strong match" : tier === "partial" ? "Partial match" : "Weak match"}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-3 sm:grid-cols-6 gap-3 mt-5">
        {subScores.map(([label, score]) => (
          <div key={label}>
            <div className="h-1.5 rounded-full bg-ink-100 dark:bg-white/[0.06] overflow-hidden mb-1.5">
              <div className="h-full bg-brand-gradient rounded-full transition-all duration-500" style={{ width: `${score * 100}%` }} />
            </div>
            <div className="text-[11px] font-bold">{Math.round(score * 100)}%</div>
            <div className="text-[10px] text-ink-400 font-medium">{label}</div>
          </div>
        ))}
      </div>

      <button onClick={() => setExpanded(!expanded)} className="btn-ghost mt-4 -ml-1">
        {expanded ? "Hide" : "Show"} evidence <ChevronDown size={14} className={`transition-transform ${expanded ? "rotate-180" : ""}`} />
      </button>

      {expanded && (
        <div className="mt-3 space-y-4 text-sm border-t border-ink-100 dark:border-white/[0.06] pt-4">
          {result.breakdown.strongMatches.length > 0 && (
            <div>
              <div className="flex items-center gap-1.5 text-emerald-600 dark:text-emerald-400 font-semibold text-xs mb-2">
                <CheckCircle2 size={13} /> STRONG MATCHES
              </div>
              <div className="space-y-1">
                {result.breakdown.strongMatches.map((m) => (
                  <div key={m.skill} className="flex items-baseline gap-1.5">
                    <span className="font-medium">{m.skill}</span>
                    {m.foundIn.length > 0 && <span className="text-ink-400 text-xs">— {m.foundIn.join(", ")}</span>}
                  </div>
                ))}
              </div>
            </div>
          )}
          {result.breakdown.missing.length > 0 && (
            <div>
              <div className="flex items-center gap-1.5 text-red-500 font-semibold text-xs mb-2">
                <XCircle size={13} /> MISSING
              </div>
              <div className="space-y-1">
                {result.breakdown.missing.map((m) => (
                  <div key={m} className="text-ink-500 dark:text-ink-400">{m} <span className="text-ink-400 text-xs">— not found in resume</span></div>
                ))}
              </div>
            </div>
          )}
          {result.breakdown.potentialIssues.length > 0 && (
            <div>
              <div className="flex items-center gap-1.5 text-amber-600 dark:text-amber-400 font-semibold text-xs mb-2">
                <AlertTriangle size={13} /> POTENTIAL ISSUES
              </div>
              {result.breakdown.potentialIssues.map((issue) => <div key={issue} className="text-ink-500 dark:text-ink-400">{issue}</div>)}
            </div>
          )}
          <div className="flex items-start gap-1.5 text-xs text-ink-400 pt-2 border-t border-ink-100 dark:border-white/[0.06]">
            <Info size={13} className="shrink-0 mt-0.5" /> {result.breakdown.disclaimer}
          </div>
        </div>
      )}
    </div>
  );
}

function ScoreRing({ pct, color }: { pct: number; color: string }) {
  const r = 22, c = 2 * Math.PI * r;
  return (
    <div className="relative w-14 h-14 shrink-0">
      <svg viewBox="0 0 56 56" className="w-14 h-14 -rotate-90">
        <circle cx="28" cy="28" r={r} fill="none" stroke="currentColor" strokeWidth="5" className="text-ink-100 dark:text-white/[0.06]" />
        <circle
          cx="28" cy="28" r={r} fill="none" stroke={color} strokeWidth="5" strokeLinecap="round"
          strokeDasharray={c} strokeDashoffset={c - (pct / 100) * c}
          style={{ transition: "stroke-dashoffset .6s cubic-bezier(.16,1,.3,1)" }}
        />
      </svg>
      <div className="absolute inset-0 flex items-center justify-center text-[13px] font-bold">{pct}%</div>
    </div>
  );
}
