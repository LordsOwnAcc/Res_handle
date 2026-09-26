# ResumeIntel — Web Stack (Spring Boot + Postgres + React)

A web version of the Resume Intelligence & Job Application Manager: one Postgres-backed
Spring Boot API, and one React website that talks to it. Deployable together on Render.

```
resumeintel-web/
├── backend/     Spring Boot 3 + Postgres (Flyway migrations, REST API)
├── frontend/    React + Vite + Tailwind (calls the backend over HTTP)
├── render.yaml  Blueprint: provisions Postgres + backend + frontend together
└── docker-compose.yml   Local Postgres only, for dev
```

## Run it locally

**1. Postgres**
```
docker compose up -d
```

**2. Backend** (needs JDK 21 + Maven)
```
cd backend
cp .env.example .env      # edit if your local Postgres differs
mvn spring-boot:run
```
Flyway applies `V1__init.sql` automatically on startup — no manual migration step. API comes
up on `http://localhost:8080`; check `http://localhost:8080/api/health`.

**3. Frontend** (needs Node 20+)
```
cd frontend
cp .env.example .env
npm install
npm run dev
```
Opens on `http://localhost:5173` and talks to the backend via `VITE_API_BASE_URL`.

## Deploy to Render

1. Push this repo to GitHub.
2. In Render: **New → Blueprint**, point it at the repo. Render reads `render.yaml` and
   provisions three things: `resumeintel-db` (Postgres), `resumeintel-backend` (Docker web
   service), `resumeintel-frontend` (static site).
3. **Important manual step:** Render's automatic env-var linking between a static site and a
   web service (`fromService`) doesn't reliably resolve to a full `https://` URL in practice.
   After the first deploy:
   - Copy the backend's actual URL (Render dashboard → `resumeintel-backend` → shows
     `https://resumeintel-backend-xxxx.onrender.com`).
   - Set that as `VITE_API_BASE_URL` on the **frontend** service's environment variables, then
     trigger a redeploy (Vite bakes env vars in at build time, so this needs a rebuild, not
     just a restart).
   - Copy the frontend's actual URL the same way, and set it as `CORS_ALLOWED_ORIGINS` on the
     **backend** service, then redeploy the backend too.
4. Flyway migrates the schema automatically on the backend's first boot — nothing to run by
   hand against the Render Postgres instance.

Both services are on Render's free tier in `render.yaml` — free web services spin down after
inactivity and take ~30–60s to wake on the next request, which you'll notice on first load
after a quiet period. Bump `plan:` to a paid tier if that matters for your use case.

## What's wired end-to-end and working

- **Find Best Resume**: paste a JD on the website → backend parses it (offline heuristic, no
  API key needed) → every resume in Postgres gets scored by the same six-factor matching
  engine from the original spec → ranked results with expandable evidence render on the page.
- **Resume management**: add resumes with skills/projects/experience through a form; they're
  immediately part of the matching pool.
- **Application tracker**: Kanban-style board, status changes persist to Postgres immediately.
- **Dashboard**: live stats and resume-usage breakdown computed from real data.

## What's still stubbed (same honesty as the mobile/desktop app)

- **PDF/DOCX upload + parsing.** The form-based resume entry works fully; there's no file
  upload yet. Real parsing needs a library decision (Apache PDFBox / Tika on the Java side)
  and object storage for the original files (S3-compatible bucket — Render doesn't give you
  persistent disk on free web services) — worth doing once you know where files should live.
- **Cloud AI (OpenAI/Gemini).** `AIProvider` is the seam; `HeuristicAIProvider` is the only
  implementation. Add a new `@Service` implementing the same interface, wire your API key via
  a Render environment variable (never commit it), and mark it `@Primary` to swap it in.
- **Auth.** This is currently single-user with no login — anyone with the backend URL can hit
  every endpoint. Fine for personal use behind an obscure URL; not fine if you're going to
  share this. Adding Spring Security + a login page is the next real piece of work if this
  goes further than just you.
- **Mind-map/graph view, resume versioning diff UI, Ctrl+K global search** — same as noted in
  the KMP app's README: the data model supports all three (every relationship is a real FK),
  none of the three has a UI built yet.

## Extending safely

- Add a feature by extending a `service/` class first, then the controller, then the React
  page — never let a page call the matching engine or Postgres directly.
- If you eventually add cloud AI, keep `HeuristicAIProvider` around as the default/fallback —
  it's what keeps the app usable without anyone's API key.
