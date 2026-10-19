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
- `composeApp/build.gradle.kts` — Shared KMP module (Android + `iosArm64` + `iosSimulatorArm64`) and the `SakuStrings` generator
- `iosApp/iosApp.xcodeproj` — Xcode project for the iOS app, target and scheme `Saku`
- `iosApp/iosApp/SakuApp.swift` — Swift host that hands Compose's `UIViewController` to UIKit
- `iosApp/build-app.sh`, `iosApp/run-app.sh` — Build the iOS app, then boot, install, launch and screenshot it

## Build Commands
- Build: `./gradlew build`
- Build release APK: `./gradlew assembleRelease`
- Install debug: `./gradlew installDebug`
- Clean: `./gradlew clean`
- Run tests: `./gradlew test`
- Run instrumented tests: `./gradlew connectedAndroidTest`
- Reset app data: `adb shell pm clear id.almezi.simplemoneytracker_kt`
- Build iOS app: `./iosApp/build-app.sh`
- Build, install and launch iOS app: `./iosApp/run-app.sh --build`

iOS has its own section below: [Build and Run on iOS](#build-and-run-on-ios)

## Build and Run on iOS

Saku is a Kotlin Multiplatform app. Every screen lives in `:composeApp`
`commonMain`; `iosApp/` is a thin Swift host that hands a `UIViewController` to
UIKit. There is no second copy of the app for iOS.

**No Apple Developer Program membership, no App Store Connect, and no publishing
are needed to run Saku locally.**

| Target | Needs an Apple ID? | Needs the paid $99/yr program? | Code signing |
|---|---|---|---|
| iOS Simulator | No | No | No — the project sets `CODE_SIGNING_ALLOWED = NO` |
| Physical iPhone / iPad | Yes, a **free** Apple ID is enough | No | Yes, automatic development signing |
| TestFlight / App Store | Yes | Yes | Yes, distribution profile |

A physical device is the only case that needs an Apple ID, and only because iOS
refuses to install unsigned apps on hardware. A free *Personal Team* mints a
throwaway development profile when you press ⌘R. Nothing is ever submitted.

> **Verification status.** The simulator path below was run end-to-end on
> 2026-10-13 (build, install, launch, Room database created, screenshot). The
> device path is written from the project configuration and from a successful
> `./gradlew :composeApp:linkDebugFrameworkIosArm64`, but no physical device has
> been attached to this machine yet, so the signed install step is untested here.

### Requirements

| Requirement | Notes |
|---|---|
| Apple Silicon Mac | `composeApp/build.gradle.kts` declares `iosArm64` and `iosSimulatorArm64` only. Compose Multiplatform 1.12.1 is 64-bit; there is no `iosX64` slice, so an Intel Mac cannot build this. |
| Full Xcode | Installed **and** selected. Command Line Tools alone ship no iOS SDK, and every Kotlin/Native link then fails. |
| A JDK for Gradle | Java 17+. `JAVA_HOME` is honoured; Android Studio's bundled JBR works. |
| Android SDK | Even a pure iOS build configures `:app` and `:composeApp`'s Android target, so Gradle needs `sdk.dir` in `local.properties` (or `ANDROID_HOME`). |
| Network on first build | Kotlin/Native downloads its toolchain and per-target artifacts. Several GB of disk and a first build measured in minutes, not seconds. |
| CocoaPods / `pod install` | **Not needed.** This project uses direct framework integration, which is mutually exclusive with CocoaPods. |

#### 1. Point the toolchain at full Xcode

This is the one setup step that silently breaks everything else:

```bash
xcode-select -p
# /Library/Developer/CommandLineTools   ← wrong, no iOS SDK

sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer
xcodebuild -version                       # must report Xcode 27.x
xcrun --sdk iphonesimulator --show-sdk-version
```

`iosApp/build-app.sh` and `iosApp/run-app.sh` export `DEVELOPER_DIR` themselves,
so they work even before the global selection is fixed — but Xcode and Android
Studio need the switch too. Do it anyway.

### Run in the simulator

Nothing to sign, no account, no configuration.

```bash
# Build Debug for iPhone 18 Pro
# → iosApp/build/DerivedData/Build/Products/Debug-iphonesimulator/Saku.app
./iosApp/build-app.sh

# Build, then boot, install, launch, screenshot
./iosApp/run-app.sh --build "iPhone 18 Pro"

# Explicit configuration and device
./iosApp/build-app.sh Release "iPhone 17"
./iosApp/run-app.sh "iPad Pro 13-inch (M5)"
```

Xcode GUI: open `iosApp/iosApp.xcodeproj`, choose the shared scheme **Saku** and
any simulator, press ⌘R. That runs the same Kotlin build phase.

`run-app.sh` resolves the device by name or UDID, boots it, uninstalls any
previous Saku, installs, launches, waits for Compose to settle, checks that the
Room database exists, and writes `iosApp/build/screenshot.png`.

Manual equivalents:

```bash
xcrun simctl list devices available | grep -E 'iPhone|iPad'
xcrun simctl boot "iPhone 18 Pro" && open -a Simulator

APP=iosApp/build/DerivedData/Build/Products/Debug-iphonesimulator/Saku.app
xcrun simctl install booted "$APP"
xcrun simctl launch booted id.almezi.saku

# Reset app data — the iOS equivalent of `adb shell pm clear`
xcrun simctl uninstall booted id.almezi.saku

# Inspect the database
xcrun simctl get_app_container booted id.almezi.saku data
#   → …/Documents/simplemoneytracker.db
```

Simulators present on this machine (iOS 27.0 runtime, already installed — do not
download one): iPhone 18 Pro, 18 Pro Max, 17, 17e; iPad Pro 13" and 11", iPad Air
13" and 11", iPad mini (A17 Pro), iPad (A16).

### Run on a physical device

iOS only installs a signed bundle on hardware, so a physical device needs a
signing identity. **A free Apple ID is enough** — Apple calls it a *Personal
Team*, it never contacts App Store Connect, and nothing is published.

1. Fix the Xcode selection (above).
2. Xcode → Settings → Accounts → **+** → Apple Account. Sign in with any free
   Apple ID. It appears with its team name in parentheses.
3. Connect the iPhone or iPad by USB, unlock it, tap **Trust This Computer**.
   Xcode then lists it in the scheme's destination menu.
4. Select the device and press ⌘R. Xcode registers the device UDID and creates
   the development profile on first run; that step needs network access.

From the command line, the project must be re-signed explicitly because signing
is switched off in the project file (see the next section):

```bash
export DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme Saku \
  -configuration Debug \
  -sdk iphoneos \
  -destination 'generic/platform=iOS' \
  -derivedDataPath iosApp/build/DerivedData-device \
  CODE_SIGN_STYLE=Automatic \
  CODE_SIGNING_ALLOWED=YES \
  CODE_SIGNING_REQUIRED=YES \
  DEVELOPMENT_TEAM=<TEAMID> \
  -allowProvisioningUpdates \
  build

APP=iosApp/build/DerivedData-device/Build/Products/Debug-iphoneos/Saku.app
xcrun devicectl list devices
xcrun devicectl device install app --device <UDID> "$APP"
xcrun devicectl device process launch --device <UDID> id.almezi.saku
```

`DEVELOPMENT_TEAM` is the 10-character team id shown in Xcode → Settings →
Accounts, or `xcodebuild -showBuildSettings | grep DEVELOPMENT_TEAM` once ⌘R has
set it. `-allowProvisioningUpdates` is what lets Xcode create the profile without
opening App Store Connect.

Free-team limits, so they are not a surprise:

- Development certificates and profiles expire after **7 days**. The app then
  stops launching; press ⌘R again, or re-run the command above, to regenerate
  and reinstall. Nothing else expires and no data is lost.
- 3 apps per device per 7 days. Keep the bundle id `id.almezi.saku`; changing it
  burns a slot.
- No Push, iCloud, App Groups, HealthKit or Wallet. Saku uses none of them — it
  is offline and local-only.
- A Personal Team cannot produce TestFlight or App Store builds. That needs the
  $99/yr program. Nothing in this repo does it.

### Signing settings in the Xcode project

`iosApp/iosApp.xcodeproj` sets `CODE_SIGNING_ALLOWED = NO`,
`CODE_SIGNING_REQUIRED = NO` and an empty `CODE_SIGN_IDENTITY` in both
configurations. That is what lets simulator builds succeed with no identity at
all. Device builds need it reversed; command-line settings override project
settings, so the `xcodebuild` snippet above is enough on its own.

To make plain ⌘R work on a device with no extra flags, change both
configurations in the target (not the project) to:

```
CODE_SIGNING_ALLOWED[sdk=iphonesimulator*] = NO;
CODE_SIGNING_REQUIRED[sdk=iphonesimulator*] = NO;
CODE_SIGN_STYLE = Automatic;
DEVELOPMENT_TEAM = <TEAMID>;
```

Not applied on purpose: the repo deliberately has no signing configuration, and a
team id must not be committed.

### Running the shared tests on iOS

`:composeApp` common tests use `kotlin.test`, so they run on the simulator too:

```bash
xcrun simctl boot "iPhone 18 Pro"
./gradlew :composeApp:iosSimulatorArm64Test   # iOS only
./gradlew :composeApp:allTests               # Android unit tests + iOS simulator
```

### How the Kotlin code gets into the app

The `Saku` target has no CocoaPods and no framework in its UI. Its first build
phase, **Build Kotlin framework**, runs:

```bash
./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
```

That task configures itself from the Xcode environment (`CONFIGURATION`,
`SDK_NAME`, `BUILT_PRODUCTS_DIR`, …), which is why the framework is built *by*
Xcode and never by hand. Running it from a plain shell fails with `Could not infer
iOS target platform`. Do not replace it with a bare `swiftc` invocation.

The framework is **static** (`isStatic = true`), so it is linked into the `Saku`
binary and no `Frameworks/` directory ends up in the app bundle. The same task
copies `compose-resources/` — the Compose resources, including the DM Sans and
DM Mono fonts — into the **root** of the app bundle. If you ever see
`MissingResourceException: Missing resource with path: …/compose-resources/…`, the
resources were not packaged: rebuild through Xcode or `build-app.sh` and confirm
`Saku.app/compose-resources` exists.

### Project settings worth knowing

| Setting | Value |
|---|---|
| Xcode project / scheme | `iosApp/iosApp.xcodeproj`, scheme `Saku` (shared) |
| Bundle id | `id.almezi.saku` |
| Deployment target | iOS 15.0 |
| Devices | iPhone and iPad (`TARGETED_DEVICE_FAMILY = "1,2"`) |
| Version | 1.0 (build 1) |
| Kotlin framework | `ComposeApp`, static, `iosArm64` + `iosSimulatorArm64` |
| Swift host | `iosApp/iosApp/SakuApp.swift` — `AppDelegate` + `SceneDelegate` |
| Signing | disabled in-project; see above for the device overrides |

`SceneDelegate` is required by the iOS 27 SDK's `UIScene` lifecycle. Without it
the app launches to a blank window.

### iOS troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `xcrun: SDK "iphonesimulator" cannot be located`, or `xcodebuild -version` fails | `xcode-select` points at Command Line Tools | `sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer` |
| `MissingXcodeException` / `xcrun` exit code 72 during the Gradle phase | same cause, reached from inside the Xcode script phase | same; the scripts already export `DEVELOPER_DIR` |
| `Could not infer iOS target platform` | `embedAndSignAppleFrameworkForXcode` run from a terminal | let Xcode drive it, or use `:composeApp:linkDebugFrameworkIosSimulatorArm64` if you only want the framework |
| `MissingResourceException: … compose-resources …` | framework built outside Xcode | rebuild via `build-app.sh` or Xcode |
| `Signing for "Saku" requires a development team` | device build with no team | set `DEVELOPMENT_TEAM`, or use automatic signing in the GUI |
| `No profiles for 'id.almezi.saku' were found` | no Apple ID in Xcode, or the flag is missing | add the account in Xcode → Settings → Accounts, and pass `-allowProvisioningUpdates` |
| App runs on the device for a week, then stops | free Personal Team profile expired | rebuild and reinstall |
| Blank white window | `SceneDelegate` removed | it is required on the iOS 27 SDK; do not delete it |
| `SDK location not found` while building iOS only | Android SDK not configured for Gradle | set `sdk.dir` in `local.properties` |

Background and the full migration rationale are in `docs/09-ios-migration-plan.md`.

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

## Cleanup
- Removed unused imports, 23 unused string resources, and the dead code listed in `CODE_REVIEW.md` (unused composables, unused design tokens, unused DAO and repository methods).
- No behaviour change: `./gradlew assembleDebug` and `./gradlew testDebugUnitTest` both pass.

## Testability
- All UI elements use test tags defined in `TestTags.kt`
- Tags follow format: `<screen>_<type>_<name>`
- Debug hooks for testing: seed data, fake clock, reset state

## Download APK

[Download SimpleMoneyTrackerKT-v2.1.apk](release/SimpleMoneyTrackerKT-v2.1.apk)

| | |
|---|---|
| Version | 2.1 (`versionCode` 3) |
| Build type | Release, unsigned |
| Min / target SDK | 24 / 37 |
| Size | 20.67 MiB (21.67 MB) |
| SHA-256 | `e254f7bc5723e2b3a667dac4f1221bf5066efef69db50902c6e59d15f9c74efc` |
| Built on | 2026-10-16 |
| Branch | `feature/add-accessbility-id` |

Previous artifacts:

| File | Version | SHA-256 |
|---|---|---|
| `release/SimpleMoneyTrackerKT-v2.0.apk` | **mislabeled — actually 1.0 (`versionCode` 1)** | `fd7080e690a9b99212a85019e761d5793f0ef6226bba83bc3f16bb9e03e59b54` |

> The `-v2.0.apk` file was renamed from the v1.0 artifact without rebuilding; its
> embedded manifest still reports `versionName 1.0` / `versionCode 1`. It is kept
> only for history and is byte-identical to what the v1.0 runbook recorded.

The APK is **unsigned** — no signing config is defined in `app/build.gradle.kts`.
Android will refuse to install it until it is signed. To install for local testing,
use `./gradlew installDebug` instead, which produces a signed debug build.

Rebuild the release artifact with:

```
./gradlew clean assembleRelease
cp app/build/outputs/apk/release/app-release-unsigned.apk release/SimpleMoneyTrackerKT-v2.1.apk
```

Verify the checksum after rebuilding with `shasum -a 256 release/SimpleMoneyTrackerKT-v2.1.apk`.

## Conventions
- Kotlin code style: official
- Use `libs.versions.toml` for dependency management
- Strings in resource files (Indonesian first), no hard-coded UI text
- Injectable clock — never call system clock directly

## Acknowledging AI Assistance
Parts of this project were built with AI assistance (code, docs, reviews); humans review and own every change.

## License
MIT — see [LICENSE](LICENSE). Free to use as a testing app; feedback and input via issues and pull requests are welcome.
