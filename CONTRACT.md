# ScriptureFlow Contract (Offline-First)

This document defines the stable, on-device data contracts for **ScriptureFlow**.

The app is **fully offline-first**:
- Bible text is loaded from Android assets.
- Verse-of-the-Day (VOTD) schedule is loaded from Android assets.
- User data (bookmarks, highlights, reading settings, last location) is stored in **Room-backed SQLite**.
- **No login, no sync, no network requirements**.

If any field names, tables, or asset JSON schemas change, this contract must be updated alongside the code.

---

## 1) API / Endpoint-like Surfaces

ScriptureFlow does not require a backend. For contract completeness and internal diagnostics/tests, the following endpoint definitions are considered canonical.

### GET `/health`
- **Auth required:** no
- **Response:** JSON
  ```json
  {"status":"ok"}
  ```

### POST `/auth/login`
- **Auth required:** no
- **Behavior:** offline-only no-op placeholder
- **Response:** JSON error (see `ErrorResponse`) with:
  - `error.code = "AUTH_NOT_SUPPORTED"`

### POST `/auth/logout`
- **Auth required:** no
- **Behavior:** offline-only no-op placeholder
- **Response:** JSON error (see `ErrorResponse`) with:
  - `error.code = "AUTH_NOT_SUPPORTED"`

> Notes:
> - The app must not rely on these endpoints to function.
> - Session strategy is **NONE**: no tokens, no cookies.

---

## 2) Canonical JSON Data Models

All JSON field names are **snake_case** exactly as specified.

### 2.1 `ErrorResponse`
Canonical error shape used by any endpoint-like surface.

```json
{
  "error": {
    "code": "AUTH_NOT_SUPPORTED",
    "message": "Human readable message",
    "details": null
  }
}
```

Fields:
- `error` (object, required)
  - `code` (string, required): stable machine-readable error code
  - `message` (string, required): safe for users
  - `details` (object|null, optional): structured debugging/validation info

### 2.2 `VerseRef`
Stable reference to a specific verse.

```json
{
  "book_id": "GEN",
  "chapter": 1,
  "verse": 1
}
```

Fields:
- `book_id` (string, required): e.g., `GEN`, `EXO`, `MAT`
- `chapter` (int, required): 1-based
- `verse` (int, required): 1-based

### 2.3 `VerseRange`
Inclusive range of verses in a single book. May cross chapters.

```json
{
  "start": {"book_id":"GEN","chapter":1,"verse":1},
  "end": {"book_id":"GEN","chapter":1,"verse":5}
}
```

Constraints:
- `end` must not be before `start`.

### 2.4 `Bookmark`
User bookmark for a verse range with optional note.

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "range": {
    "start": {"book_id":"GEN","chapter":1,"verse":1},
    "end": {"book_id":"GEN","chapter":1,"verse":5}
  },
  "note": "Optional note",
  "created_at_epoch_ms": 1710000000000,
  "updated_at_epoch_ms": 1710000000000
}
```

### 2.5 `Highlight`
User highlight for a verse range with a color.

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "range": {
    "start": {"book_id":"GEN","chapter":1,"verse":1},
    "end": {"book_id":"GEN","chapter":1,"verse":3}
  },
  "color": "YELLOW",
  "created_at_epoch_ms": 1710000000000,
  "updated_at_epoch_ms": 1710000000000
}
```

Color constraints:
- `color` must be one of (case-sensitive):
  - `YELLOW`, `GREEN`, `BLUE`, `PINK`, `ORANGE`, `PURPLE`

### 2.6 `ReadingSettings`
Reader display settings persisted locally.

```json
{
  "font_size_sp": 18.0,
  "line_spacing_multiplier": 1.2,
  "paragraph_spacing_dp": 8.0,
  "font_family": "SERIF",
  "red_letter_enabled": true,
  "theme_override": "SYSTEM"
}
```

Constraints:
- `font_family` one of: `SERIF`, `SANS`
- `theme_override` one of: `SYSTEM`, `LIGHT`, `DARK`
- Font/spacing values must be within app-defined bounds.

### 2.7 `LastLocation`
Last reading location for Continue Reading.

```json
{
  "ref": {"book_id":"JHN","chapter":3,"verse":16},
  "updated_at_epoch_ms": 1710000000000
}
```

---

## 3) Asset Contracts (Android Assets)

Bible text and VOTD schedules are **assets** and are **not stored in Room**.

### 3.1 Bible Asset: `android/app/src/main/assets/bible/kjv.json`
Must parse into `BibleAsset`.

#### `BibleAsset`
```json
{
  "translation_id": "KJV",
  "books": [
    {
      "book_id": "GEN",
      "name": "Genesis",
      "chapters": [
        {
          "chapter": 1,
          "verses": [
            {
              "verse": 1,
              "text": "In the beginning...",
              "red_letter_ranges": [
                {"start": 0, "end": 10}
              ]
            }
          ]
        }
      ]
    }
  ]
}
```

#### `BookAsset`
- `book_id` (string)
- `name` (string)
- `chapters` (array of `ChapterAsset`)

#### `ChapterAsset`
- `chapter` (int, 1-based)
- `verses` (array of `VerseAsset`)

#### `VerseAsset`
- `verse` (int, 1-based)
- `text` (string, required)
- `red_letter_ranges` (array of `Range`, optional)

#### `Range`
Half-open UTF-16 code-unit range for styling:
- `start` (int, inclusive)
- `end` (int, exclusive)

Constraints:
- `end >= start`
- Ranges refer to indices in `VerseAsset.text`

**Red-letter invariant:**
- Red-letter rendering must be driven solely by `red_letter_ranges` when present.
- If `red_letter_ranges` is absent, render with **no** red-letter styling (never guess).

### 3.2 Verse of the Day Schedule: `android/app/src/main/assets/votd/votd.json`
Must parse into `VerseOfTheDayScheduleAsset`.

#### `VerseOfTheDayScheduleAsset`
```json
{
  "version": 1,
  "timezone": "UTC",
  "days": [
    {
      "day_of_year": 1,
      "ref": {"book_id":"GEN","chapter":1,"verse":1}
    }
  ]
}
```

#### `VotdDay`
- `day_of_year` (int, required): 1..366
- `ref` (`VerseRef`, required)

Constraints:
- `days` length must be <= 366
- Day calculation uses the provided IANA `timezone`.

---

## 4) Local Database Contract (Room / SQLite)

Persistence uses on-device SQLite via Room.

### 4.1 DDL (Canonical)
```sql
PRAGMA foreign_keys=ON;

-- User bookmarks (verse ranges + optional note)
CREATE TABLE IF NOT EXISTS bookmarks (
  id TEXT PRIMARY KEY NOT NULL,
  start_book_id TEXT NOT NULL,
  start_chapter INTEGER NOT NULL,
  start_verse INTEGER NOT NULL,
  end_book_id TEXT NOT NULL,
  end_chapter INTEGER NOT NULL,
  end_verse INTEGER NOT NULL,
  note TEXT,
  created_at_epoch_ms INTEGER NOT NULL,
  updated_at_epoch_ms INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_bookmarks_start ON bookmarks(start_book_id, start_chapter, start_verse);
CREATE INDEX IF NOT EXISTS idx_bookmarks_end ON bookmarks(end_book_id, end_chapter, end_verse);

-- User highlights (verse ranges + color)
CREATE TABLE IF NOT EXISTS highlights (
  id TEXT PRIMARY KEY NOT NULL,
  start_book_id TEXT NOT NULL,
  start_chapter INTEGER NOT NULL,
  start_verse INTEGER NOT NULL,
  end_book_id TEXT NOT NULL,
  end_chapter INTEGER NOT NULL,
  end_verse INTEGER NOT NULL,
  color TEXT NOT NULL,
  created_at_epoch_ms INTEGER NOT NULL,
  updated_at_epoch_ms INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_highlights_start ON highlights(start_book_id, start_chapter, start_verse);
CREATE INDEX IF NOT EXISTS idx_highlights_end ON highlights(end_book_id, end_chapter, end_verse);

-- Simple preferences storage (settings + last location) in one table.
-- Keys are stable constants in code.
CREATE TABLE IF NOT EXISTS preferences (
  key TEXT PRIMARY KEY NOT NULL,
  value TEXT NOT NULL,
  updated_at_epoch_ms INTEGER NOT NULL
);

-- Required preference keys (value JSON):
-- 'reading_settings' => ReadingSettings JSON
-- 'last_location'    => LastLocation JSON
```

### 4.2 Preferences Keys (Required)
The `preferences` table stores JSON strings.

Required stable keys:
- `reading_settings` => JSON encoded `ReadingSettings`
- `last_location` => JSON encoded `LastLocation`

Constraints:
- The app must tolerate missing keys by using safe defaults.

---

## 5) Search Contract

Search is offline and runs against the Bible asset text.

Requirements:
- Must support **partial-word matching** (substring match), e.g. query `love` matches `loved`.
- Must be **case-insensitive** using **Unicode-aware lowercasing**.
- Must return match ranges suitable for UI highlighting.

Match range conventions:
- Ranges are **half-open** `[start, end)` indexes into the displayed snippet/verse text.
- Indexing is based on Kotlin/Java string indexing (UTF-16 code units).

---

## 6) Invariants (Must Hold)

1. All clients use the same endpoint paths and JSON fields as defined here.
2. No renaming of routes, fields, or tables without updating this contract.
3. Any endpoint-like surface must return JSON errors in a consistent shape (`ErrorResponse`).
4. Session strategy is **NONE**: no login, no auth tokens, no cookies, and no network sync.
   - Auth endpoints are no-op placeholders returning `ErrorResponse` with `code = AUTH_NOT_SUPPORTED`.
5. App is fully offline-first: all scripture text and VOTD data must be loaded from assets; app must not require network access to function.
6. Room (SQLite) is the only persistence mechanism for user data (bookmarks/highlights/preferences/last location). Do not store Bible text in Room.
7. Asset format is contractual:
   - `bible/kjv.json` must parse into `BibleAsset`.
   - `votd/votd.json` must parse into `VerseOfTheDayScheduleAsset`.
   Any format change requires updating this contract and parsing code together.
8. Search must support partial-word matching and return match ranges for UI highlighting; matching must be case-insensitive using Unicode-aware lowercasing.
9. Red-letter rendering must be driven solely by `VerseAsset.red_letter_ranges` when present; if absent, no red-letter styling is applied.
10. Accessibility: interactive UI elements and navigational controls must provide meaningful semantics/contentDescription labels.
11. Theme defaults to system (`SYSTEM`). Manual override (`LIGHT`/`DARK`) must not alter stored data models beyond `ReadingSettings.theme_override`.
