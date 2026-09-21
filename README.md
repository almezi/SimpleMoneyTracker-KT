# SimpleMoneyTracker-KT

An Android money tracking app built with Kotlin.

## Project Info
- **Package**: `id.almezi.simplemoneytracker_kt`
- **App module**: `app/`
- **Min SDK**: 24, **Target SDK**: 37, **Compile SDK**: 37
- **Java compatibility**: Version 11
- **Gradle**: Version catalog via `libs.versions.toml`

## Key Files
- `app/build.gradle.kts` — App-level build config and dependencies
- `build.gradle.kts` — Project-level build config
- `settings.gradle.kts` — Plugin and repo management
- `gradle.properties` — Gradle JVM args and Kotlin code style
- `app/src/main/AndroidManifest.xml` — App manifest
- `app/src/main/res/values/` — Strings, themes, colors
- `app/src/main/res/values-night/themes.xml` — Night mode theme
- `app/src/main/keepRules/rules.keep` — R8/ProGuard rules

## Build Commands
- Build: `./gradlew build`
- Clean: `./gradlew clean`
- Run tests: `./gradlew test`
- Run instrumented tests: `./gradlew connectedAndroidTest`

## Architecture
[Describe your app architecture, modules, patterns used — e.g., MVVM, Clean Architecture, etc.]

## Conventions
- Kotlin code style: official
- Use `libs.versions.toml` for dependency management
