# ScriptureFlow

Offline-first Android Bible reader (Kotlin + Jetpack Compose) with local KJV assets, local full-text search with highlights, and Room-backed bookmarks/highlights. Includes a minimal FastAPI backend for health checking.

## Repo layout

- `CONTRACT.md` — canonical API + model contract
- `android/` — Android project (pre-existing template). This repo only adds/updates source under `android/app/src/**` and `android/README.md`.
- `backend/` — minimal FastAPI app (health endpoint)

## Backend (FastAPI)

### Endpoint

- `GET /health` (no auth)
  - **200 OK**
  - JSON body:
    ```json
    {"status":"ok"}
    ```

### Run locally

From repo root:

```bash
python -m venv .venv
source .venv/bin/activate  # (Windows: .venv\Scripts\activate)

pip install fastapi uvicorn
uvicorn backend.app.main:app --reload
```

Test it:

```bash
curl http://127.0.0.1:8000/health
# {"status":"ok"}
```

### Error shape (non-2xx)

All backend non-2xx responses must use the canonical error payload from `CONTRACT.md`:

```json
{
  "error": {
    "code": "bad_request",
    "message": "Human-readable message",
    "request_id": null
  }
}
```

## Android app

### What’s included

- Offline-first Bible loading from local assets JSON
- Drawer navigation: book list → chapter/verse selection, plus links to Search/Bookmarks/Highlights/Settings
- Reader settings stored locally (DataStore Preferences):
  - `font_size_sp` (float)
  - `line_height_multiplier` (float)
  - `font_style` ("serif" | "sans")
  - `theme_mode` ("light" | "dark" | "system")
  - `text_alignment` ("start" | "center" | "justify")
- Local search:
  - case-insensitive
  - partial-word matches
  - multi-token queries require all tokens to match
  - returns `match_ranges` that index into the original `verse.text`
- Room persistence:
  - `bookmarks` table (unique `verse_id`)
  - `highlights` table (unique `verse_id`)
- Deterministic “Verse of the Day” (stable for a given local calendar date)

### Assets

KJV sample data is expected at:

- `android/app/src/main/assets/bibles/kjv_sample.json`

This is a small subset for development/testing. You can replace it later with a full KJV file following the same schema the parser expects.

### Build/run

Open the `android/` folder in Android Studio and run the `app` configuration.

Notes:
- This repo uses a pre-existing Android template; Gradle/wrapper files are already present in `android/`.
- Source code for ScriptureFlow lives under the `com.scriptureflow` package in `android/app/src/main/java/`.

See `android/README.md` for Android-specific dependency notes and test commands.

## Testing

### Android unit tests

The project includes unit tests for:
- Search algorithm behavior
- Room DAO CRUD interactions

Run from `android/`:

```bash
./gradlew test
```

### Backend health check test

CI is expected to run a minimal check that `GET /health` returns `200` and `{ "status": "ok" }`.

## Contract-first development

Before changing routes, JSON fields, or database table/column names, update `CONTRACT.md`.

Key invariants (from contract):
- No login/auth (session strategy is `none`)
- Offline-first: Bible text must load from local assets; search must not require network
- No tracking/analytics
- Deterministic Verse of the Day per local calendar date
- Room uniqueness: one bookmark and one highlight per verse (`verse_id` primary key)
