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

## Google Drive setup (optional — enables file upload)

Resume file upload (a resume's original PDF/DOCX) stores the file on **your own Google
Drive**, not on the backend — Render's free web services have no persistent disk, and this
avoids paying for object storage. It's opt-in: without this setup, everything else works
(manual resume entry, matching, tracker) — you just won't have an "Upload File" option on a
resume's detail page.

1. **Create a Google Cloud project** (or reuse one) at [console.cloud.google.com](https://console.cloud.google.com).
2. **Enable the Google Drive API**: APIs & Services → Library → search "Google Drive API" → Enable.
3. **Create a service account**: APIs & Services → Credentials → Create Credentials → Service
   Account. Give it any name (e.g. `resumeintel-uploader`). No roles needed at the project level.
4. **Generate a key**: open the service account → Keys → Add Key → Create new key → JSON.
   This downloads a `.json` file — treat it like a password, never commit it.
5. **Create a folder in your own Google Drive** (e.g. "ResumeIntel Files") and **share it**
   with the service account: right-click the folder → Share → paste the service account's
   email (looks like `resumeintel-uploader@your-project.iam.gserviceaccount.com`, found in
   the downloaded JSON as `client_email`) → give it **Editor** access.
6. **Get the folder ID**: open the folder in Drive, copy the ID from the URL —
   `drive.google.com/drive/folders/`**`THIS_PART`**.
7. **Base64-encode the JSON key** (the backend reads it as one env var, not a file):
   ```
   base64 -i service-account-key.json | tr -d '\n'
   ```
   (On Windows: `certutil -encode service-account-key.json tmp.b64` then strip the header/footer lines.)
8. Set two env vars on the **backend** service:
   - `GOOGLE_DRIVE_CREDENTIALS_BASE64` — the base64 string from step 7
   - `GOOGLE_DRIVE_FOLDER_ID` — the folder ID from step 6

   Locally, put these in `backend/.env`. On Render, the Blueprint (`render.yaml`) already
   declares both with `sync: false`, which makes Render prompt you for the values once when
   you create the Blueprint instance — nothing to hardcode in the YAML itself.

Uploaded files land in that Drive folder, viewable/shareable via a `webViewLink` that gets
stored in Postgres (only the link — never the file bytes).

## Deploy to Render

1. Push this repo to GitHub.
2. In Render: **New → Blueprint**, point it at the repo. Render reads `render.yaml` and
   provisions three things: `resumeintel-db` (Postgres), `resumeintel-backend` (Docker web
   service), `resumeintel-frontend` (static site).
3. `render.yaml` hardcodes each service's expected URL into the other
   (`CORS_ALLOWED_ORIGINS` on the backend, `VITE_API_BASE_URL` on the frontend) based on the
   service names — Render uses the service name as the subdomain when it's free. **Check this
   after the first deploy**: open each service in the Render dashboard and confirm its actual
   URL matches what's in `render.yaml` (`https://resumeintel-backend.onrender.com` and
   `https://resumeintel-frontend.onrender.com`). If either name was taken in your account,
   Render will have appended a random suffix — update the corresponding env var on the *other*
   service to the real URL and redeploy that one service. (This can't be fully automated: a
   static site's JS runs in the visitor's browser, which needs the public URL, not Render's
   private-network `fromService` linkage — which is also why it isn't used here.)
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
  immediately part of the matching pool. Attach the original PDF/DOCX and it's stored on your
  own Google Drive (see "Google Drive setup" below) — the link is saved, not the file.
- **Application tracker**: Kanban-style board, status changes persist to Postgres immediately.
- **Dashboard**: live stats and resume-usage breakdown computed from real data.

## What's still stubbed (same honesty as the mobile/desktop app)

- **PDF/DOCX auto-parsing.** File upload itself is done (see "Google Drive setup" above) —
  you can attach the original file to a resume and it's stored on your Drive. What's still
  missing is automatically *reading* that file to pre-fill skills/projects/experience; you
  still enter those by hand via the form. Real extraction needs a library decision (Apache
  PDFBox / Tika on the Java side) — worth doing once the manual-entry flow feels limiting.
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
