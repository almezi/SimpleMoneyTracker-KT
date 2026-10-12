# 4. Credentials, Accounts & Access Register

Purpose: record who owns what, where credentials live, and which systems have
access — so an engineer can onboard or rotate access safely.

Verified against branch `feature/improve-design-2.0`, HEAD `8c7dc3a`.

---

## 4.1 Read this first

**This app is not published and has no third-party integrations.** It is a
local-only, offline personal finance tracker with no backend, no accounts, and
no network calls — `AndroidManifest.xml` declares zero permissions, including
no `INTERNET`.

As a result, most of what a conventional access register covers **does not
exist yet**. Rather than leave those rows blank or fill them with speculation,
each section below states the current status precisely and gives the concrete
setup steps required to bring the item into existence.

### Summary

| Domain | Status | Action needed to activate |
|---|---|---|
| Google Play Console | **Does not exist** | Create a developer account; claim the app id |
| Apple App Store Connect | **Does not exist** | Not applicable — no iOS target |
| Android upload keystore | **Does not exist** | Generate one; add a `signingConfigs` block |
| iOS certificates / provisioning | **Does not exist** | Not applicable — no iOS target |
| Firebase | **Does not exist** | Not needed unless push or crash reporting is added |
| FCM / APNs | **Does not exist** | Not needed |
| Crashlytics | **Does not exist** | Not needed |
| Analytics | **Does not exist** | Explicitly forbidden by `AGENTS.md` |
| Payment gateways | **Does not exist** | Explicitly out of scope |
| GitHub repository | **Exists** | — |
| GitHub package registry | Not used | — |
| Debug keystore | Exists (auto-generated per machine) | — |

---

## 4.2 Ownership

| Item | Value |
|---|---|
| **Maintainer / owner** | `almezi` — GitHub remote `github.com/almezi/SimpleMoneyTracker-KT.git` |
| **Package identifier** | `id.almezi.simplemoneytracker_kt` |
| **Application ID (store-facing)** | Same as the package id |
| **Signing identity** | None defined |
| **Team / organisation** | Single developer; no organisation in the repository |

Granting a new person access means adding them as a collaborator on the GitHub
repository. There is no other system to provision.

---

## 4.3 Store portals

### Google Play Console — NOT SET UP

| Question | Answer |
|---|---|
| Does the developer account exist? | Unknown / presumed not created. No `fastlane`, no `google-services.json`, no service-account key in the repo |
| Is the app listed? | No. No `versionCode` greater than `1` has been uploaded |
| Does the developer account exist in the repo? | No — developer accounts live outside the repository by design |

**To activate:**

1. Create a Google Play developer account at
   `play.google.com/console` and pay the one-time fee.
2. **Decide the account type early.** A personal account (max 20 testers,
   internal testing only, requires a closed test with 12 testers for 14 days
   before production) vs. an organisation account (full access, requires a
   D-U-N-S and a verified organisation). This decision is hard to reverse.
3. Create the app entry with package name `id.almezi.simplemoneytracker_kt`
   and the uploaded artefact from
   [05-ci-cd-release-runbook.md](05-ci-cd-release-runbook.md).
4. Create an upload key and record it in §4.4.
5. Complete the Data safety form. Accurate answers: **no data collected, no
   data shared, data is stored on-device only, no encryption at rest beyond
   Android platform defaults.**

### Apple App Store Connect — NOT APPLICABLE

There is no iOS target in the build. `app/build.gradle.kts` applies only
`com.android.application`; there is no multiplatform plugin, no iOS source set,
and no Xcode project. `TESTABILITY.md` lists iOS as an unverified Stage 0
question. Nothing to register here until a cross-platform build exists.

---

## 4.4 Signing

### Android upload / release key — NOT GENERATED

**The release APK is unsigned.** `app/build.gradle.kts` declares no
`signingConfigs` block. `./gradlew assembleRelease` produces
`app-release-unsigned.apk`, which Android will refuse to install. The
`release/SimpleMoneyTrackerKT-v1.0.apk` artifact in the repository is likewise
unsigned, as stated in `README.md`.

The only signing key that currently exists is the **debug keystore**, generated
automatically by the Android SDK at `~/.android/debug.keystore`. It is
machine-local, never committed, and must never be used for a release.

**To generate the release key (when publishing becomes the goal):**

```bash
keytool -genkeypair -v \
  -keystore ~/secure/saku-upload.jks \
  -alias saku-upload \
  -keyalg RSA -keysize 4096 -validity 10000 \
  -storetype JKS
```

Then:

1. Store the `.jks` in a password manager's secure file store, **not** in the
   repository.
2. Record the alias and passwords in the same password manager entry.
3. Put only the *paths and key aliases* in `local.properties` (already
   git-ignored):

   ```properties
   saku.storeFile=/absolute/path/to/saku-upload.jks
   saku.storePassword=…
   saku.keyAlias=saku-upload
   saku.keyPassword=…
   ```

4. Add a `signingConfigs` block to `app/build.gradle.kts` that reads those
   properties, and wire it to the `release` build type.
5. Add a local-only template such as `keystore.properties.example` with empty
   values so the shape is discoverable without leaking anything.

**Loss of the upload key means you can never update the app on Play.** Treat it
as the single most important credential in this project. Google Play App
Signing does provide a recovery path, but it requires a support request.

### iOS distribution certificates and provisioning profiles

Not applicable — no iOS target. See §4.3.

---

## 4.5 Third-party services

None. Enumerated below with the reason each is absent, so a future engineer
knows which would require a new credential and which would violate the rules.

| Service | Status | Notes |
|---|---|---|
| Firebase | **Not used** | No `google-services.json`, no `firebase-*` dependency. Would be needed only for push or crash reporting |
| FCM (push) | **Not used** | `POST_NOTIFICATIONS` permission is not declared. No notifications are a feature — see `UI_SPEC.md` §7 "Out of scope" |
| APNs (push) | **Not used** | iOS only |
| Crashlytics | **Not used** | No dependency. Manual QA covers a personal-scale app |
| Analytics | **Not used — forbidden** | `AGENTS.md`: "No network layer, no login, no analytics" |
| Firebase Auth | **Not used — forbidden** | `UI_SPEC.md` §7 lists login/PIN as out of scope. The app has exactly one user: whoever holds the phone |
| Payment gateways | **Not used — forbidden** | No commerce exists in the product |
| Cloud sync | **Not used — forbidden** | `UI_SPEC.md` §7. Data stays on device; export/import is the backup mechanism |
| Remote config | **Not used** | The app is self-contained |
| Map / location SDKs | **Not used** | No location features. Note `SakuDatePickerDialog` uses the **platform** `DatePickerDialog`, so no location permission is needed for the date picker |

### Telemetry actually emitted

None. There is no logging framework in the dependency list, no analytics
initialisation in `SimpleMoneyTrackerApp.onCreate()`, and no HTTP client
anywhere in the source tree.

### If a third-party service is ever added

`AGENTS.md` requires that any such change be reflected in `UI_SPEC.md` first —
features outside the spec do not get built. Then:

1. Add the credential reference here in the same commit.
2. Keep the secret out of git: `local.properties`, `gradle.properties` from
   environment variables, or `google-services.json` git-ignored with
   `google-services.json.example` committed.
3. Never commit a `.jks`, `.p12`, `.mobileprovision`, service-account JSON key
   or API key.
4. Re-check whether the app should still declare zero permissions.

---

## 4.6 Local development secrets

| Secret | Location | Committed? | Notes |
|---|---|---|---|
| SDK path | `local.properties` → `sdk.dir` | **No** — git-ignored (twice) | Machine-specific |
| Debug keystore | `~/.android/debug.keystore` | **No** | Auto-generated; default password `android` |
| Gradle user home | `~/.gradle/` | **No** | Holds the dependency cache |
| Build cache | `.gradle/`, `build/`, `app/build/` | **No** | Git-ignored |

**Nothing sensitive exists in this repository.** Confirm before assuming
otherwise:

```bash
git ls-files | xargs -I{} sh -c 'file -b "{}" | grep -qi "private key\|PKCS" && echo {}'
```

---

## 4.7 Access-change procedure

1. **Grant:** add the person as a GitHub collaborator. If they need CI
   credentials later, create a scoped account at that time — do not share a
   personal token.
2. **Record:** update §4.2 with the new maintainer.
3. **Revoke:** remove the collaborator in GitHub settings, then rotate the
   upload keystore if they had access to it.
4. **Rotate the upload key:** generate a new key, keep the Play listing's
   previous key valid until the new one has been used successfully, then
   request removal of the old one via Play App Signing.

---

## 4.8 Open items for the owner

- [ ] Decide whether to publish at all, given the app is a test target rather
      than a product
- [ ] Create a Google Play developer account and record which type
- [ ] Generate and store an upload keystore if publishing is chosen
- [ ] Add `signingConfigs` to `app/build.gradle.kts`
- [ ] Decide on a LICENSE file — the repository has none
- [ ] Consider adding a CODEOWNERS file

---

## See also

- [05-ci-cd-release-runbook.md](05-ci-cd-release-runbook.md) — how a build gets made and shipped
- [02-tech-stack-dependencies.md](02-tech-stack-dependencies.md) — the absence of any HTTP or analytics dependency