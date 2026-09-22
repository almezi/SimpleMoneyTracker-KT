# SimpleMoneyTracker-KT

A small, local-only personal money tracker built with Kotlin and Jetpack Compose.

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

## Architecture
- **MVVM**: UI → ViewModel (StateFlow) → Repository → DB
- **Manual DI**: No Hilt, no Koin — dependencies injected via constructor
- **Room**: Local database with Kotlin coroutines
- **Navigation**: Jetpack Navigation Compose with bottom navigation bar

## Screens
- **Tambah**: Add new transactions with validation
- **Daftar**: List all transactions grouped by date
- **Ringkasan**: Summary of income, expenses, and totals by category

## Testability
- All UI elements use test tags defined in `TestTags.kt`
- Tags follow format: `<screen>_<type>_<name>`
- Debug hooks for testing: seed data, fake clock, reset state

## Conventions
- Kotlin code style: official
- Use `libs.versions.toml` for dependency management
- Strings in resource files (Indonesian first), no hard-coded UI text
- Injectable clock — never call system clock directly
