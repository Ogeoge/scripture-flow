# ScriptureFlow Contract

This document is the canonical contract for **ScriptureFlow** across Android (offline-first) and the minimal backend service.

- App name: **ScriptureFlow**
- Platforms: **Android**
- Session strategy: **none** (no login, no auth)
- Primary mode: **Offline-first** (Bible text loaded from local assets; search works without network)

---

## Backend API

### GET `/health`
**Auth:** not required  
**Purpose:** Liveness/health check.

- **Request:** no body
- **Success Response:** `200 OK`
  - Content-Type: `application/json`
  - Body:
    ```json
    {"status":"ok"}
    ```

### Error responses (non-2xx)
All non-2xx responses **must** use the canonical error shape:

```json
{
  "error": {
    "code": "bad_request",
    "message": "Human-readable message",
    "request_id": null
  }
}
```

- `error.code`: stable machine-readable code (e.g., `bad_request`, `not_found`, `internal`)
- `error.message`: human-readable string
- `error.request_id`: string or null

---

## Android / Local Domain Models

All models below are shared conceptually across layers (UI, repository, persistence). JSON field names are as shown.

### `VerseRef`
Canonical verse reference.

```json
{
  "book": "Genesis",
  "chapter": 1,
  "verse": 1
}
```

- `book` (string, required): Book name (e.g., `"Genesis"`)
- `chapter` (int, required): 1-based
- `verse` (int, required): 1-based

### `Verse`
A single Bible verse loaded from local KJV assets and optionally cached for fast search.

```json
{
  "id": "Genesis|1|1",
  "ref": {"book":"Genesis","chapter":1,"verse":1},
  "text": "In the beginning God created the heaven and the earth."
}
```

- `id` (string, required): Stable primary key: `"{book}|{chapter}|{verse}"`
- `ref` (`VerseRef`, required)
- `text` (string, required): Verse text (KJV)

### `MatchRange`
Half-open index range used for highlighting matched substrings.

```json
{ "start": 0, "end": 10 }
```

- `start` (int, required): 0-based inclusive start index in `verse.text`
- `end` (int, required): 0-based exclusive end index in `verse.text`

### `SearchResult`
Local (offline) search result.

```json
{
  "verse": {"id":"Genesis|1|1","ref":{"book":"Genesis","chapter":1,"verse":1},"text":"..."},
  "match_ranges": [{"start":3,"end":14}]
}
```

- `verse` (`Verse`, required)
- `match_ranges` (array of `MatchRange`, required): ranges refer to indices in the **original** `verse.text`

### `Bookmark`
User bookmark stored locally.

```json
{
  "verse_id": "Genesis|1|1",
  "created_at_epoch_ms": 1710000000000
}
```

- `verse_id` (string, required): references `Verse.id`
- `created_at_epoch_ms` (long, required): UTC timestamp

### `Highlight`
User highlight of an entire verse stored locally.

```json
{
  "verse_id": "Genesis|1|1",
  "color_argb": -256,
  "created_at_epoch_ms": 1710000000000
}
```

- `verse_id` (string, required): references `Verse.id`
- `color_argb` (int, required): packed ARGB color
- `created_at_epoch_ms` (long, required): UTC timestamp

### `ReadingPreferences`
User reading settings stored locally via DataStore Preferences.

```json
{
  "font_size_sp": 18.0,
  "line_height_multiplier": 1.3,
  "font_style": "serif",
  "theme_mode": "system",
  "text_alignment": "start"
}
```

- `font_size_sp` (float, required)
- `line_height_multiplier` (float, required)
- `font_style` (string, required): one of `"serif" | "sans"`
- `theme_mode` (string, required): one of `"light" | "dark" | "system"`
- `text_alignment` (string, required): one of `"start" | "center" | "justify"`

---

## Backend Payload Models

### `HealthResponse`
```json
{ "status": "ok" }
```

- `status` (string, required): must be `"ok"`

### `ApiError`
```json
{
  "error": {
    "code": "internal",
    "message": "Something went wrong",
    "request_id": null
  }
}
```

- `error` (object, required)
  - `error.code` (string, required)
  - `error.message` (string, required)
  - `error.request_id` (string|null, required)

---

## Local Persistence (Room / SQLite)

Canonical schema (table and column names) used by Room:

```sql
/* SQLite schema used by Room (canonical table/column names) */

-- Optional: cached verses for fast search (seeded from assets on first run)
CREATE TABLE IF NOT EXISTS verses (
  verse_id TEXT PRIMARY KEY,                 -- "{book}|{chapter}|{verse}"
  book TEXT NOT NULL,
  chapter INTEGER NOT NULL,
  verse INTEGER NOT NULL,
  text TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_verses_book_chapter_verse ON verses(book, chapter, verse);

-- Bookmarks
CREATE TABLE IF NOT EXISTS bookmarks (
  verse_id TEXT PRIMARY KEY,                 -- unique per verse
  created_at_epoch_ms INTEGER NOT NULL
);

-- Highlights (entire verse highlight; one highlight per verse)
CREATE TABLE IF NOT EXISTS highlights (
  verse_id TEXT PRIMARY KEY,                 -- unique per verse
  color_argb INTEGER NOT NULL,
  created_at_epoch_ms INTEGER NOT NULL
);
```

---

## Invariants

1. All clients use the same endpoint paths and JSON fields as defined here.
2. No renaming of routes, fields, or tables without updating this contract.
3. Backend errors must always use the `ApiError` shape for non-2xx responses.
4. No login/auth: session strategy remains **none** and no auth endpoints are introduced unless the contract is updated.
5. Offline-first: Bible text must be loaded from **local assets** on device; search must work without network.
6. No tracking/analytics/ads SDKs or telemetry; no collection of user identifiers.
7. Deterministic Verse of the Day: for a given local calendar date, the selected `Verse.id` is stable across app restarts (seed derived solely from date + local verse corpus).
8. Search behavior:
   - case-insensitive
   - partial-word matches allowed
   - multi-word queries supported where **all tokens** must match within the verse text
   - `match_ranges` refer to indices in the original `verse.text`
9. Room uniqueness:
   - `bookmarks.verse_id` is unique (one bookmark per verse)
   - `highlights.verse_id` is unique (one highlight per verse)
