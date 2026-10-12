# AGENTS.md

Standing instructions for any AI coding tool working on this repo. Read UI_SPEC.md, CATEGORIES.md and TESTABILITY.md first. Do not ask for or read BUGS.md.

## What this is
A small, local-only personal money tracker, built purely as a test target for mobile automation. Not a product. Keep it small.

## Stack
- Kotlin Multiplatform + Compose Multiplatform (official template)
- Room KMP or SQLDelight, `kotlinx-datetime`
- Android first; iOS comes later

## Architecture rules
- MVVM: UI -> ViewModel (StateFlow) -> Repository -> DB
- Manual DI. No Hilt, no Koin
- No network layer, no login, no analytics
- Injectable clock; never call the system clock directly
- Strings in resource files (Indonesian first); no hard-coded UI text

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
```