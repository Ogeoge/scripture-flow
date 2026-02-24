# ScriptureFlow (Android)

This folder is an Android Studio project. ScriptureFlow app code lives under:

- `android/app/src/main/java/com/scriptureflow/**`

This repo uses a pre-existing template; do **not** regenerate Gradle wrapper/build files. Only `android/app/src/**` sources/assets are intended to be added/updated.

## Features (contract-aligned)

- Offline-first: KJV is loaded from local assets (no network required for reading/search).
- Local search:
  - case-insensitive
  - partial-word matching
  - multi-word queries require **all tokens** to match within the verse text
  - returns `match_ranges` referencing indices in the original `verse.text` (start inclusive, end exclusive)
- Room persistence:
  - `bookmarks` table (unique `verse_id`)
  - `highlights` table (unique `verse_id`)
- DataStore Preferences for reading settings:
  - `font_size_sp` (float)
  - `line_height_multiplier` (float)
  - `font_style` = `serif` | `sans`
  - `theme_mode` = `light` | `dark` | `system`
  - `text_alignment` = `start` | `center` | `justify`
- Deterministic “Verse of the Day”: stable per local calendar date.

## Assets

Place the KJV JSON (sample included) at:

- `android/app/src/main/assets/bibles/kjv_sample.json`

The app seeds/caches verses for fast search from this asset on first run.

## Required Android dependencies (add to your Gradle)

This repo does not generate Gradle files; ensure these dependencies exist in your `android/app/build.gradle.kts` (exact versions as per your template/BOM).

### Jetpack Compose + Material 3
- `androidx.activity:activity-compose`
- `androidx.compose.ui:ui`
- `androidx.compose.ui:ui-tooling-preview`
- `androidx.compose.material3:material3`
- `androidx.navigation:navigation-compose`

### Room (SQLite)
- `androidx.room:room-runtime`
- `androidx.room:room-ktx`
- `kapt("androidx.room:room-compiler")` (or KSP equivalent)

### DataStore Preferences
- `androidx.datastore:datastore-preferences`

### Kotlinx Serialization (for asset parsing)
- `org.jetbrains.kotlinx:kotlinx-serialization-json`

## Build & run

Open the `android/` directory in Android Studio and run the `app` configuration.

## Testing

### Unit tests (JVM)
Run from `android/`:

```bash
./gradlew test
```

Included unit tests cover:
- search algorithm behavior (token matching + `match_ranges` correctness)
- Room DAO CRUD (bookmarks/highlights uniqueness by `verse_id`)

### Instrumented tests
If you run on device/emulator:

```bash
./gradlew connectedAndroidTest
```

## Backend note

The Android app is offline-first and does not require the backend. The repository includes a minimal FastAPI service used only for health checks:

- `GET /health` → `200` with JSON `{ "status": "ok" }`

See repo root `README.md` and `CONTRACT.md` for the canonical contract.
