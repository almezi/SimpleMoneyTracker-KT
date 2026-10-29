# BUG_Found_05.md

## Identity

| Field | Value |
|---|---|
| Reported by | ZCode agent (Appium MCP run, Phase 3.D) |
| Reported at | 2026-10-26T13:20:00+07:00 |
| Branch | features/phase3 |
| Commit SHA | 14d1ec6 |

## Test Context

| Field | Value |
|---|---|
| Phase | 3 |
| Sub-phase | 3.D — Rotation & Config Change |
| Test case ID | TC-ROT-01 (finding surfaced during the pass; case itself passes on round trip) |
| Spec file | src/tests/app/rotation.spec.ts |

## Bug Details

**Title:** Tambah form is unsavable in landscape — note field and save button are not composed

**Description:** Rotating the Tambah screen to landscape retains amount and category, but the note field (`tambah_input_note`) and the save button (`tambah_button_save`) are absent from the accessibility tree entirely — `scroll_to_element` reports end-of-scrollable-content, i.e. they are not composed, not merely off-screen. A user on a landscape device cannot add a note or save the transaction without rotating back to portrait.

**Steps to Reproduce:**
1. Open Tambah, fill amount and category.
2. Rotate the device/emulator to landscape.
3. Attempt to scroll to the note field or locate the save button.

**Actual Result:** Note field and save button do not exist in the UI tree; the visible form ends after the date row. No way to save in landscape.

**Expected Result:** The full form (note + save) is reachable in landscape, either laid out on screen or scrollable.

**Attachment:** MCP page-source dumps 2026-10-26 (portrait tree contains `tambah_input_note`/`tambah_button_save`; landscape tree contains neither; `scroll_to_element` → "end of scrollable content").

**Severity:** Minor
**Priority:** P3

## Environment

| Field | Value |
|---|---|
| Device/Emulator | emulator-5554 (Pixel 10 profile) |
| Platform | Android 17 emulator |
| Appium version | 3.8.0 (uiautomator2) |
| App build | SimpleMoneyTrackerKT-v2.2.apk |
| Android API level | 17 (release) |

## Fix Ownership

**APP CODE (confirmed by the live Appium MCP re-run on 2026-10-26).** The
landscape rotation drops `tambah_input_note` + `tambah_button_save` from the
composed tree; automation, driver and tag layers proven clean by the re-run.
Fix belongs in the app repo (Compose Tambah landscape layout). This workspace
holds the test harness only — no harness fix applies.

## Additional Notes — fix details (app repo)

- Rotation round trip itself is safe: all filled values are retained when portrait returns (TC-ROT-01 passes).
- Related test cases: TC-ROT-01, TC-INC/TC-EXP save flows (landscape only).
- Workaround: rotate back to portrait before saving.

### Live re-run evidence (2026-10-26, Appium MCP, emulator-5554, app v2.2)

Repro on a fresh session via `set_orientation`:

1. Portrait page-source: `tambah_screen`, `tambah_input_amount`,
   `tambah_input_note`, `tambah_helper`, `tambah_button_save` composed.
2. LANDSCAPE: `tambah_input_note` + `tambah_button_save` + `tambah_helper`
   ABSENT. Amount + category pager composed. ScrollView scrollable.
3. PORTRAIT back: all present again.

Conclusion: app-side compose defect on landscape recomposition. Automation
harness not at fault. App-side fix area: full Tambah column (note + save) must
compose under landscape, not only portrait.

## Resolution — CLOSED 2026-10-26: not an app bug (automation-visibility artifact)

**Disposition: closed without an app change, per the re-verification below.
The note field and save button are composed the whole time; they are only
invisible to the a11y dump while off-screen.**

Code re-review first contradicted the report: `TransactionForm.kt` composes
note + save **unconditionally** in a single `verticalScroll` Column
(`composeApp/.../ui/transaction/TransactionForm.kt:124-266`), and the codebase
contains zero orientation branching (no `LocalConfiguration`, no
`BoxWithConstraints`, no `WindowSizeClass` anywhere). So "not composed" was
never plausible from code — the original evidence was page-source-only, with
no screenshot.

### Controlled re-run (2026-10-26, emulator-5554, debug build of this branch)

All evidence via plain `adb` (`uiautomator dump` + `screencap`), no harness:

1. **Portrait baseline** — all 10 `tambah_*` tags in the dump, incl.
   `tambah_input_note`, `tambah_helper`, `tambah_button_save`.
2. Filled Rp 150.000 + category Gaji, keyboard dismissed → all tags present.
3. `user_rotation 1` (landscape) → **original signature reproduced**: dump
   lists toggle/amount/category/kelola-link and account+date (cut at the
   screen edge, bounds ending at y=1080); `tambah_input_note`, `tambah_helper`,
   `tambah_button_save` ABSENT.
4. Screenshot shows why: the form simply continues **below the fold** (the
   Tunai / 26 Okt row is visible at the bottom edge). Values retained.
5. Two ordinary upward swipes → `tambah_input_note` [195,333][2245,459] and
   `tambah_button_save` [195,659][2245,796] present in the dump AND on screen.
6. **End-to-end save in landscape**: typed a note, tapped Simpan transaksi —
   form reset (post-save state), DB gained the row (`pemasukan|150000`, most
   recent `createdAt`), Daftar MASUK total went 77.000 → 227.000.

### Root cause of the false positive

Android reports `isVisibleToUser=false` for a11y nodes outside the viewport;
both plain `uiautomator dump` and the uiautomator2 driver (default
`allowInvisibleElements=false`) drop such nodes. In landscape the form
overflows below the fold, so note/helper/save are off-screen — and therefore
dump-invisible — until scrolled. The original session's `scroll_to_element`
could not resolve the filtered elements and reported "end of scrollable
content", which made them look uncomposed. They were composed the entire time.

### Harness recommendations (test workspace, not this repo)

- Set uiautomator2 setting `allowInvisibleElements=true` for rotation specs,
  or gesture-scroll before asserting off-screen fields.
- Dismiss the keyboard before rotating for stable dumps.

Expected result as written ("reachable in landscape, laid out on screen or
scrollable") is met by the scrollable form; UI_SPEC needs no landscape
section. Session evidence screenshots: `/tmp/qa/repro1..repro6*.png`
(session-local; every step above is re-runnable in ~3 minutes).
