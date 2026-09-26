import { useState } from "react";
import { api, MatchResultResponse } from "../api/client";

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
    <div className="p-6 md:p-10 max-w-3xl mx-auto">
      <h1 className="text-2xl font-semibold mb-2">Find Best Resume</h1>
      <p className="text-sm text-slate-500 mb-6">
        Paste a job description. Every resume in your library is scored against it — required
        skills, preferred skills, experience, project relevance, education, and keywords.
      </p>

      <div className="card p-6">
        <textarea
          className="input min-h-[180px]"
          value={jdText}
          onChange={(e) => setJdText(e.target.value)}
          placeholder="Paste the full job description here…"
        />
        <button onClick={handleSubmit} disabled={!jdText.trim() || loading} className="btn-primary mt-4">
          {loading ? "Analyzing…" : "Find Best Resume"}
        </button>
        {error && <p className="text-sm text-red-600 mt-2">{error}</p>}
      </div>

      {results.length > 0 && (
        <div className="mt-8 space-y-4">
          {results.map((r) => <MatchCard key={r.result.resumeId} data={r} />)}
        </div>
      )}
    </div>
  );
}

function MatchCard({ data }: { data: MatchResultResponse }) {
  const [expanded, setExpanded] = useState(false);
  const { result, resumeName } = data;
  const pct = Math.round(result.overall * 100);
  const color = pct >= 75 ? "text-emerald-600" : pct >= 50 ? "text-amber-600" : "text-red-600";

  const subScores: [string, number][] = [
    ["Required", result.requiredMatch], ["Preferred", result.preferredMatch],
    ["Experience", result.experienceMatch], ["Projects", result.projectMatch],
    ["Education", result.educationMatch], ["Keywords", result.keywordMatch],
  ];

  return (
    <div className="card p-6">
      <div className="flex items-center justify-between">
        <div className="font-medium">{resumeName}</div>
        <div className={`text-2xl font-bold ${color}`}>{pct}%</div>
      </div>
      <div className="flex flex-wrap gap-x-5 gap-y-2 mt-3">
        {subScores.map(([label, score]) => (
          <div key={label} className="text-xs">
            <div className="font-semibold">{Math.round(score * 100)}%</div>
            <div className="text-slate-500">{label}</div>
          </div>
        ))}
      </div>
      <button onClick={() => setExpanded(!expanded)} className="text-sm text-brand-600 font-medium mt-3">
        {expanded ? "Hide details" : "Show details"}
      </button>
      {expanded && (
        <div className="mt-3 space-y-3 text-sm">
          {result.breakdown.strongMatches.length > 0 && (
            <div>
              <div className="text-emerald-600 font-medium mb-1">Strong matches</div>
              {result.breakdown.strongMatches.map((m) => (
                <div key={m.skill}>✓ {m.skill}{m.foundIn.length > 0 && <span className="text-slate-500"> — {m.foundIn.join(", ")}</span>}</div>
              ))}
            </div>
          )}
          {result.breakdown.missing.length > 0 && (
            <div>
              <div className="text-red-600 font-medium mb-1">Missing</div>
              {result.breakdown.missing.map((m) => <div key={m}>✗ {m} — Not found in resume.</div>)}
            </div>
          )}
          {result.breakdown.potentialIssues.map((issue) => (
            <div key={issue} className="text-amber-600">⚠ {issue}</div>
          ))}
          <p className="text-xs text-slate-500 pt-2 border-t border-black/5 dark:border-white/5">{result.breakdown.disclaimer}</p>
        </div>
      )}
    </div>
  );
}
