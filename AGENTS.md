# AGENTS.md

See `README.md` for project overview, structure, and build commands. See `PLAN.md` for the roadmap. See `DECISIONS.md` for architecture decisions. See `TESTABILITY.md` for testing strategy. See `BUGS.md` for known issues.

## Standing Instructions
1. Read `UI_SPEC.md` and `TESTABILITY.md` before starting any task
2. Do not read `BUGS.md`

## Architecture Rules
- MVVM: UI -> ViewModel (StateFlow) -> Repository -> DB
- Manual DI. No Hilt, no Koin
- No network layer, no login, no analytics
- Injectable clock; never call the system clock directly
- Strings in resource files (Indonesian first); no hard-coded UI text

## Testability Rules (strict)
- Only use tags defined in `TestTags.kt`. Never rename, remove or invent tags
- Tag format: `<screen>_<type>_<name>`
- Every interactive or asserted element gets a tag from `TestTags.kt`
- Debug-only hooks (seeding, fake clock) must not exist in release builds

## Scope Rules
- Do not add features outside `UI_SPEC.md`
- Do not add settings, editing of categories, multi-wallet or multi-currency
- Do not introduce bugs. Any bug work is done separately by hand on another branch

## Slice Order
1. Data layer + category file
2. Add screen
3. Daftar (list)
4. Ringkasan (summary)

Finish and verify one slice before starting the next.

## Build and Run
Adjust after the Stage 0 spike:
```
./gradlew installDebug        # install on emulator/device
adb shell pm clear <package>  # reset app data
```