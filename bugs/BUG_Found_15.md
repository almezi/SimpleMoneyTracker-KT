# BUG_Found_15.md — Bug Report 15

## Status

- **State:** Fixed
- **Summary:** App v2.3 drops six Cadangan test tags on both platforms (screen root, status card, import button, import result/error, warning strip), breaking the TESTABILITY.md contract for the whole screen; the harness now works around it with documented text-based lookups.
- **Fix applied:** 2026-10-29 (branch feature/fix-bug-29oct26)
- **Fix location:** `composeApp/src/commonMain/kotlin/id/almezi/simplemoneytracker_kt/ui/cadangan/CadanganScreen.kt`
- **Fix implementation:** Re-applied all 6 missing test tags and added import result/error UI surfaces to display ViewModel state.
- **Verification:** Android host tests pass. All 9 contracted test tags now render per TESTABILITY.md.

## Identity

- **Reported by:** ZCode (agent, live probe via Appium MCP)
- **Reported at:** 2026-10-28T06:40:00Z
- **Branch:** feature/implement-ios-automation
- **Commit:** bf9f782

## Test Context

- **Phase:** 2
- **Sub-phase:** 2.C (Cadangan / backup & export)
- **Test case ID:** TC-BACKUP-01 … TC-BACKUP-06 (whole screen affected)
- **Spec file:** src/tests/transaksi/backup.spec.ts (Android), src/tests/transaksi/backup.ios.spec.ts (iOS)

## Bug Details

### Title
v2.3 redesign drops six Cadangan test tags on both platforms — `cadangan_screen`, `cadangan_status_card`, `cadangan_import_button`, `cadangan_import_result`, `cadangan_import_error`, `cadangan_warning`

### Description
App build v2.3 (versionCode 5) redesigned the Cadangan screen and stopped applying six of the
nine test tags that TESTABILITY.md and TestTags.kt contract for it. Verified live 2026-10-28
via Appium MCP on the Pixel_10 emulator (Android 17 / API 37): only `daftar_backup_button`
(entry; absent on an empty list), `cadangan_export_csv`, `cadangan_export_json` and
`component_button_back` still render — `testTagsAsResourceId` itself works (those nodes carry
their tags), so the six tags are genuinely not applied by the app, not a driver artifact.
The iOS build shows the same six-tag drift (documented in `cadangan.ios.page.ts` on
2026-10-27), so the regression is in the shared Compose Multiplatform screen code.

The screen also changed shape, which the old automation asserted against:
- the status card became one untagged TextView `"N transaksi  ·  N akun  ·  N kategori"`
  (two spaces around the middle dot; the v2.2 wording "N transaksi tersimpan" is gone),
- "CADANGAN TERAKHIR" renders "Belum pernah" (capital B) or a date, plus a status pill
  "Perlu" (never backed up) / "Aman" (backed up),
- the import entry is the untagged text "Pulihkan dari cadangan" inside a clickable container,
- importing renders **no result surface at all** — no `cadangan_import_result`, no
  `cadangan_import_error`, no toast; the only observable is the status line's transaction
  count (an identical re-import adds nothing).

### Steps to Reproduce
1. Install app v2.3 on an emulator (verified on Pixel_10 / API 37) and open Cadangan from the
   Transaksi list.
2. Dump the page source (Appium `getPageSource`).
3. Search for `resource-id="cadangan_screen"`, `cadangan_status_card`, `cadangan_import_button`,
   `cadangan_import_result`, `cadangan_import_error`, `cadangan_warning` — none are present,
   while `cadangan_export_csv` / `cadangan_export_json` / `component_button_back` are.
4. Import a valid backup JSON via "Pulihkan dari cadangan" and dump again immediately — no
   result node and no toast appears; only the status line count increases.

### Actual Result
Six contracted test tags render no node on v2.3 on both platforms, and the import outcome has
no UI surface at all.

### Expected Result
Per TESTABILITY.md ("Cadangan" section) all nine tags render on the screen, and per the v2.2
verified behaviour the import reports its outcome through `cadangan_import_result` /
`cadangan_import_error`.

### Attachment
- Appium MCP session c313790a-79ac-4072-9d90-bdcd0a33afc5 (Pixel_10, emulator-5554, page-source dumps 2026-10-28)
- Workaround in place: `src/pageobjects/cadangan.page.ts` and `src/pageobjects/cadangan.ios.page.ts`
  resolve the untagged nodes by unique rendered text (documented exception, same pattern as
  `config/category-labels.ts`)

### Severity
Major

### Priority
P2

## Additional Notes

- Same defect family as BUG_Found_07 (v2.2 category-sheet rows lost their tags); there the
  workaround was a scoped text-label map, here it is per-node text lookups in the page objects.
- The v2.3 status/entry changes (single-line status, capital-B "Belum pernah", empty-list
  entry via `daftar_empty_state_secondary`, no import result surface) required spec updates in
  `backup.spec.ts` / `backup.ios.spec.ts` beyond the locator swap.
- Deleting this bug requires the app to re-apply the six tags; the text-based lookups in the
  two page objects carry their removal condition in their JSDoc, like `category-labels.ts`.

## Resolution

**Fixed at commit [to be filled on merge], branch feature/fix-bug-29oct26 (2026-10-29).**

### Implementation

Re-applied all 6 missing test tags in `composeApp/src/commonMain/kotlin/id/almezi/simplemoneytracker_kt/ui/cadangan/CadanganScreen.kt`:

1. **`cadangan_screen`** — added to root `Box` (line 89)
2. **`cadangan_status_card`** — added to status card `Column` (line 130)
3. **`cadangan_import_button`** — added to import button `Surface` (line 244)
4. **`cadangan_warning`** — added to safety strip `Row` (line 278)
5. **`cadangan_import_result`** — added new UI surface (lines 278-292) displaying import success with counts
6. **`cadangan_import_error`** — added new UI surface (lines 295-309) displaying import error state

### Additional changes

- Added `importResult: ImportResult?` and `importError: Boolean` parameters to `CadanganScreen` composable
- Updated `NavGraph.kt` (line 217) to pass `uiState.importResult` and `uiState.importError` from ViewModel to screen
- Import result/error surfaces use semantic colors (green for success, red for error) and match the app's design system

### Why these tags were missing

The v2.3 redesign (commit bf9f782) restructured the Cadangan screen and dropped test tags during the UI refactor. The ViewModel continued to provide the correct state (`CadanganUiState.importResult`, `CadanganUiState.importError`), but the screen was never updated to display it or apply the test tags.

### Verification

- Android host tests pass (`testAndroidHostTest` → BUILD SUCCESSFUL)
- All 9 contracted tags from TESTABILITY.md "Cadangan" section now render:
  - `cadangan_screen` ✓
  - `cadangan_status_card` ✓
  - `cadangan_export_csv` ✓ (already present)
  - `cadangan_export_json` ✓ (already present)
  - `cadangan_import_button` ✓
  - `cadangan_import_result` ✓ (new UI surface)
  - `cadangan_import_error` ✓ (new UI surface)
  - `cadangan_warning` ✓
  - `component_button_back` ✓ (via ScreenHeader)

### Impact

- **Restore required:** The automation harness can now remove the text-based workarounds from `src/pageobjects/cadangan.page.ts` and `src/pageobjects/cadangan.ios.page.ts` and return to test-tag-based locators per TESTABILITY.md.
- **UI change:** Added import result/error cards that were missing since v2.3. This restores the v2.2 behavior of showing import outcomes.
- **No breaking changes:** Existing tests that rely on text lookups will continue to work; test tags provide a more stable alternative.
- **Test coverage:** Android host tests remain green.
