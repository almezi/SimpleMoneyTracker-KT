# SimpleMoneyTracker-KT

A small, local-only personal money tracker built with Kotlin and Jetpack Compose. Indonesian UI, single wallet, IDR only, no network.

## Project Info
- **Package**: `id.almezi.simplemoneytracker_kt`
- **App module**: `app/`
- **Min SDK**: 24, **Target SDK**: 37, **Compile SDK**: 37
- **Java compatibility**: Version 17
- **Gradle**: Version catalog via `libs.versions.toml`

## Key Files
- `app/build.gradle.kts` — App-level build config and dependencies
- `build.gradle.kts` — Project-level build config
- `settings.gradle.kts` — Plugin and repo management
- `gradle.properties` — Gradle JVM args and Kotlin code style
- `app/src/main/AndroidManifest.xml` — App manifest
- `app/src/main/res/values/` — Strings, themes, colors
- `app/src/main/res/values-night/themes.xml` — Night mode theme

## Build Commands
- Build: `./gradlew build`
- Install debug: `./gradlew installDebug`
- Clean: `./gradlew clean`
- Run tests: `./gradlew test`
- Run instrumented tests: `./gradlew connectedAndroidTest`
- Reset app data: `adb shell pm clear id.almezi.simplemoneytracker_kt`

## Architecture
- **MVVM**: UI → ViewModel (StateFlow) → Repository → DB
- **Manual DI**: No Hilt, no Koin — dependencies injected via constructor
- **Room**: Local database with Kotlin coroutines
- **Navigation**: Jetpack Navigation Compose with bottom navigation bar

## Screens
Bottom navigation: **Tambah**, **Transaksi**, **Ringkasan**. Additional destinations reached from those three:

- **Tambah** — Add a transaction. Type toggle, amount, category, account, date, note. No page header; the form starts with the type toggle.
- **Transaksi (Daftar)** — Transactions of the selected month, grouped by day. Month switcher, masuk/keluar totals, backup button, empty state.
- **Ringkasan** — Monthly summary: balance, savings rate, income vs expense, breakdown per category.
- **Ubah Transaksi** — Edit or delete an existing transaction.
- **Kelola Kategori** — List, rename, hide, delete, and create categories.
- **Anggaran** — Monthly budget limits per category, plus the budget form.
- **Cadangan** — Export and import transactions as CSV or JSON.

## Design System
Tokens and components live in `app/src/main/java/.../ui/designsystem/` and `ui/components/`, named `Saku*`. Covers colors, type scale, spacing, radius, sizing, currency/date formatting, cards, fields, buttons, toggles, sheets, toasts, and empty states. Reference docs are in `improve/`.

## Recent UI Work
- Category selection on Tambah moved to a single dropdown picker; the quick category chip row was removed.
- The category picker sheet now has a search field that filters with a case-insensitive like-word match and hides groups with no match.
- The Tambah page header was removed for consistency with the other screens.
- The Transaksi empty state keeps the month switcher at the top so other months can still be browsed.

## Testability
- All UI elements use test tags defined in `TestTags.kt`
- Tags follow format: `<screen>_<type>_<name>`
- Debug hooks for testing: seed data, fake clock, reset state

## Download APK

You can download the pre-built APK from [here](release/SimpleMoneyTrackerKT-v1.0.apk).

## Conventions
- Kotlin code style: official
- Use `libs.versions.toml` for dependency management
- Strings in resource files (Indonesian first), no hard-coded UI text
- Injectable clock — never call system clock directly
