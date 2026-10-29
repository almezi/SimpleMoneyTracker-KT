# AGENTS.md

Standing instructions for any AI coding tool working on this repo. Read UI_SPEC.md, CATEGORIES.md and TESTABILITY.md first. Do not ask for or read BUGS.md.

## Doc layout (do not reorganize)

Root contract files stay at root: `README.md`, `LICENSE`, `AGENTS.md`,
`UI_SPEC.md`, `CATEGORIES.md`, `TESTABILITY.md`. GitHub and AI tools expect
them there, and `AGENTS.md` plus `docs/` reference them by bare name.
`improve/` stays at root (`docs/` references `improve/...` paths). Detail
docs live in `docs/`. Do not consolidate everything under `docs/`.

## What this is
A small, local-only personal money tracker, built purely as a test target for mobile automation. Not a product. Keep it small.

## Stack
- Android and iOS through Compose Multiplatform. The shared code lives in the
  `:composeApp` module (`commonMain`); `:app` is the Android application and
  depends on it; `iosApp/` is the thin Swift host.
- Room KMP or SQLDelight, `kotlinx-datetime`
- Android first; iOS ships from the same `commonMain` (see `docs/09-ios-migration-plan.md`)

## Architecture rules
- MVVM: UI -> ViewModel (StateFlow) -> Repository -> DB
- Manual DI. No Hilt, no Koin
- No network layer, no login, no analytics
- Injectable clock; never call the system clock directly
- Strings live in `composeApp/shared-strings/strings.xml` (Indonesian first); no
  hard-coded UI text. Read them in code as `SakuStrings.<name>`, which is
  GENERATED into `composeApp` commonMain by the `:composeApp:generateSakuStrings`
  Gradle task. Never edit the generated `SakuStrings.kt`. Never write a string
  literal in UI code.

## Testability rules (strict)
- Only use tags defined in `TestTags.kt`. Never rename, remove or invent tags
- Tag format: `<screen>_<type>_<name>`
- Every interactive or asserted element gets a tag from `TestTags.kt`
- Debug-only hooks (seeding, fake clock) must not exist in release builds

## Scope rules
- Do not add features outside UI_SPEC.md
- Do not add settings, editing of categories, multi-wallet or multi-currency
- Do not introduce bugs. Any bug work is done separately by hand on another branch

## Current Development Status

All slices have been completed:
1. Data layer + category file (completed)
2. Add screen (completed)
3. Daftar (list) (completed)
4. Ringkasan (summary) (completed)

Navigation has been implemented to connect all screens.

## Slice order
1. Data layer + category file
2. Add screen
3. Daftar (list)
4. Ringkasan (summary)

Finish and verify one slice before starting the next.

## Build and run
Adjust after the Stage 0 spike:
```
./gradlew installDebug        # install on emulator/device
adb shell pm clear <package>  # reset app data
./iosApp/build-app.sh         # iOS debug build (simulator)
./iosApp/release.sh           # iOS release .app -> release/ios/Saku-v<version>.app
```