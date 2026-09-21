# SimpleMoneyTracker-KT — Project Instructions

## What This Is
An Android money tracking app written in Kotlin using Jetpack Compose or traditional Views (TBD).

## Tech Stack
- Kotlin
- Android (minSdk 24, targetSdk 37)
- Gradle with version catalog
- Material Components theme

## Getting Started
1. Open project in Android Studio
2. Ensure `local.properties` points to valid Android SDK
3. Sync Gradle
4. Run `./gradlew build` to verify

## Development Workflow
- Write code in `app/src/main/`
- Add dependencies in `app/build.gradle.kts`
- Add string resources in `app/src/main/res/values/strings.xml`
- Add themes/styles in `app/src/main/res/values/themes.xml`
- Add colors in `app/src/main/res/values/colors.xml`
- Unit tests: `app/src/test/`
- Instrumented tests: `app/src/androidTest/`

## Rules
- Follow official Kotlin code style
- Use version catalog aliases for dependencies
- Do not modify `settings.gradle.kts` without updating `gradle.properties`
