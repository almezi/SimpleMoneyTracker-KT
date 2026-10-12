# 5. CI/CD & Release Runbook

Purpose: document how a build is produced and, if the app is ever published,
how it gets to a store — step by step, with nothing assumed.

Verified against branch `feature/improve-design-2.0`, HEAD `8c7dc3a`.

---

## 5.1 Current state: no CI/CD exists

| Item | Status |
|---|---|
| GitHub Actions | **None.** No `.github/` directory |
| Bitrise | None |
| Fastlane | None. No `fastlane/`, no `Gemfile` |
| CircleCI | None |
| Codemagic | None |
| Jenkinsfile | None |
| SonarQube / lint gate | None |
| Any `.yml` / `.yaml` at the repo root | None |

**The build is currently manual.** Everything below is run by a developer on a
local machine. The pipeline section at the end describes what would need to be
built — it is a proposal, not a description of something running.

---

## 5.2 Versioning rules

Versioning lives in `app/build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 1
    versionName = "1.0"
}
```

| Field | Rule |
|---|---|
| `versionCode` | Monotonically increasing **integer**, unique per uploaded APK. Google Play rejects a reused code. Currently `1` |
| `versionName` | Human-readable label shown in the store. Currently `"1.0"` |
| Naming convention | Semantic: `MAJOR.MINOR` for a release, `-rc1`, `-beta2` for pre-releases |
| Increment policy | `MINOR` for a new release, `MAJOR` for a data-model or category-ID change that could break an import, `PATCH` for a bugfix. `versionCode` increments on **every** build, including internal QA builds |
| Bump location | `app/build.gradle.kts` only. There is no separate `version.properties` or Gradle `versionName` property |

There are **no git tags** in the repository. Adding one at the release commit
is recommended so `v1.0` is traceable:

```bash
git tag -a v1.0 -m "Release 1.0" <commit>
git push origin v1.0
```

---

## 5.3 Manual release procedure

This is the process that produced the shipped artifact.

### Step 1 — Pre-flight

```bash
git status                                  # must be clean
git branch --show-current                   # confirm the right branch
git fetch origin && git log --oneline origin/main..HEAD
./gradlew clean
```

Checklist:

- [ ] Working tree is clean; no stray `.DS_Store` or IDE files staged
- [ ] The branch is the one intended for release
- [ ] `versionCode` incremented and `versionName` set
- [ ] `UI_SPEC.md` §8 "Completed" is accurate

### Step 2 — Build and verify

```bash
./gradlew clean assembleDebug
./gradlew testDebugUnitTest
./gradlew lint
```

Checklist:

- [ ] `assembleDebug` succeeds
- [ ] `testDebugUnitTest` passes — currently `SakuFormatTest` and
      `CategoryFilterTest`
- [ ] `lint` produces no new errors
- [ ] `connectedAndroidTest` passes **if** a device is attached. Note: the only
      instrumented test is the template placeholder, so this currently proves
      almost nothing. Manual QA is the real gate.

### Step 3 — Manual QA on a physical device

Install and run against a device, not the emulator, and clear state first:

```bash
./gradlew installDebug
adb shell pm clear id.almezi.simplemoneytracker_kt
```

This walks `UI_SPEC.md` §6.1 through §6.7:

- [ ] Tambah: save income and expense; helper text changes correctly; save
      clears amount/category/note but keeps account and date
- [ ] Tambah: switching type clears an invalid category and keeps amount + note
- [ ] Daftar: month navigation; "next" disabled in the current month; day
      grouping and net totals
- [ ] Daftar: delete sheet → confirm → toast with undo; undo restores the row
- [ ] Ringkasan: balance, savings rate (`—` when income is zero), per-category
      breakdown shares
- [ ] Ringkasan: empty month shows the empty state and hides the cards
- [ ] Ubah transaksi: pre-filled; "Simpan perubahan" disabled until something
      changes
- [ ] Kelola kategori: toggle type, rename, hide, delete a custom category and
      confirm the transactions move to "Lainnya"
- [ ] Tambah kategori: name validation, uniqueness per type, colour preview
- [ ] Anggaran: create, edit, delete; status thresholds Aman / Hampir habis /
      Lewat batas; the bar never exceeds 100%
- [ ] Cadangan: export CSV, export JSON, import the JSON back; duplicate
      skipping message
- [ ] Amount limits: 1 rupiah accepted, 0 rejected, 999.999.999.999 accepted,
      a 13th digit rejected
- [ ] Date: no future date selectable; month boundaries inclusive
- [ ] Dark theme only; 48 dp minimum touch targets

### Step 4 — Produce the artifact

```bash
./gradlew clean assembleRelease
ls -lh app/build/outputs/apk/release/
cp app/build/outputs/apk/release/app-release-unsigned.apk release/SimpleMoneyTrackerKT-v1.0.apk
shasum -a 256 release/SimpleMoneyTrackerKT-v1.0.apk
```

Checklist:

- [ ] The APK is **unsigned** — expected, because there is no `signingConfigs`
- [ ] Checksum recorded
- [ ] Size and build date recorded in `README.md`

The current shipped artifact, per `README.md`:

| | |
|---|---|
| File | `release/SimpleMoneyTrackerKT-v1.0.apk` |
| Version | 1.0 (`versionCode` 1) |
| Build type | Release, unsigned |
| Min / target SDK | 24 / 37 |
| Size | 13.99 MiB (14.67 MB) |
| SHA-256 | `fd7080e690a9b99212a85019e761d5793f0ef6226bba83bc3f16bb9e03e59b54` |
| Built on | 2026-10-11 |

The APK is ~14 MB because `buildTypes.release { optimization { enable = false } }`
disables R8 and resource shrinking.

### Step 5 — Publish (optional, not yet possible)

Blocked: no signing config, no Play account. See
[04-credentials-access-register.md](04-credentials-access-register.md) §4.3–4.4.
Once those exist:

```bash
# 1. Sign with apksigner
apksigner sign --ks saku-upload.jks --ks-key-alias saku-upload \
  --out app-release-signed.apk app-release-unsigned.apk

# 2. Verify
apksigner verify --verbose app-release-signed.apk

# 3. Align (only if using zipalign explicitly)
zipalign -p -f 4 app-release-unsigned.apk app-release-aligned.apk

# 4. Upload to the internal testing track
java -jar bundletool.jar build-apks \
  --bundle=app-release.aab --output=app-release.apks --ks=…
```

Store checklist:

- [ ] `versionCode` higher than every previously uploaded build
- [ ] AAB built from the same commit (`./gradlew bundleRelease`)
- [ ] Signed with the upload key
- [ ] Release notes written
- [ ] Internal testing track first; smoke-test the install from Play
- [ ] Promote to closed → open → production
- [ ] Play Console pre-launch report reviewed

---

## 5.4 Release checklist (condensed)

```bash
# Everything below, in order
git status --porcelain                     # empty
./gradlew clean
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lint
./gradlew connectedAndroidTest             # if device attached
./gradlew installDebug                     # then manual QA per UI_SPEC §6
adb shell pm clear id.almezi.simplemoneytracker_kt
./gradlew clean assembleRelease
cp app/build/outputs/apk/release/app-release-unsigned.apk release/SimpleMoneyTrackerKT-v1.0.apk
shasum -a 256 release/SimpleMoneyTrackerKT-v1.0.apk
git commit -am "release: 1.0 (build 2)"
git tag -a v1.0 -m "Release 1.0"
git push origin HEAD --tags
```

---

## 5.5 Test strategy today

| Level | Tool | Coverage |
|---|---|---|
| JVM unit | JUnit 4 | `SakuFormatTest` (currency formatting), `CategoryFilterTest` (picker search) |
| Instrumented | AndroidJUnitRunner + Espresso | **Placeholder only** — asserts the package name |
| UI automation | External suite (not in this repo) | Driven purely by test tags from `TestTags.kt` |

**There is no meaningful automated test coverage of the UI.** The app is
verified by hand against `UI_SPEC.md`. `UI_SPEC.md` §8 lists this as a known
gap.

`TESTABILITY.md` defines five seed sets (`seed_empty`, `seed_month_mixed`,
`seed_excluded_categories`, `seed_month_boundaries`,
`seed_same_day_ordering`) and two debug hooks (`test_seed`, `test_now` intent
extras) — **none of them are implemented**. An automation suite cannot yet
seed data or pin the clock; today it must construct state through the UI, or
use `adb shell pm clear` for the empty case.

---

## 5.6 Proposed CI/CD pipeline

**Not implemented.** A sensible minimal version, given the project has zero
server-side concerns:

### GitHub Actions — `.github/workflows/build.yml`

```yaml
name: Build

on:
  push:
    branches: [main, develop, 'feature/**']
  pull_request:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - uses: android-actions/setup-android@v3

      - name: Unit tests
        run: ./gradlew testDebugUnitTest

      - name: Lint
        run: ./gradlew lintDebug

      - name: Assemble debug
        run: ./gradlew assembleDebug

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/*.apk
```

Notes for whoever builds it:

- Configuration cache is on, so a cache keyed on
  `gradle/libs.versions.toml` is worth adding.
- No instrumented tests run in CI until real ones exist. If they do, an
  emulator action is needed — `reactivecircus/android-emulator-runner`.
- **Never put signing credentials in the repository.** For a Play upload,
  use GitHub Actions secrets, and prefer Google Play App Signing's upload-key
  reset process over anything stored locally.

### Fastlane (optional, for store submission)

Not present. If Play submission is automated later, the lane would wrap
`bundleRelease` → `apksigner sign` → `supply`, with `key.json` supplied from CI
secrets. There is no `Fastfile` or `Gemfile` today.

### Keeping the current process viable

If CI is not worth it for a single-developer test target, the manual procedure
in §5.3 is the process. Document the version bump and the checksum; that is the
part that matters for reproducibility.

---

## 5.7 Known gaps in the release path

1. **No CI.** Every step is manual and unverified beyond the run itself.
2. **No signing.** The release APK cannot be installed as produced.
3. **No versioning automation.** `versionCode` is hand-maintained.
4. **No git tags.** v1.0 is not marked in history.
5. **No UI test coverage.** `connectedAndroidTest` proves nothing.
6. **No debug hooks.** Seeding and fake clock are specified in
   `TESTABILITY.md` but not implemented, which blocks deterministic automation.
7. **R8 disabled.** No shrinking, no obfuscation, ~14 MB artifact. Enabling it
   would need `keepRules` verified first, especially around Room entities and
   `org.json` usage.
8. **Export is not reachable by the user.** No `FileProvider` and no share
   sheet, and `getExternalFilesDir` is hidden from the file picker on
   Android 11+. Backups are effectively trapped on the device — a data-durability
   problem that matters more than it looks for a finance app.
9. **`allowBackup="true"`** means Android cloud backup will include the Room
   database. Deliberate or not, it is worth an explicit decision.

---

## See also

- [03-codebase-setup.md](03-codebase-setup.md) — build and test commands
- [04-credentials-access-register.md](04-credentials-access-register.md) — signing credentials that must exist first
- `UI_SPEC.md` — the manual QA checklist this runbook points at