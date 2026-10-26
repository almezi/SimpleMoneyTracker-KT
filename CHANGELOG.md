# Changelog

All notable changes to SimpleMoneyTracker-KT are documented here.

## [Unreleased] — Release script (2026-10-27)

### Added
- `release.sh` at the repo root: one command bumps `versionCode`/`versionName`,
  builds the unsigned release APK into `release/` via `:app:copyReleaseApk`,
  and refreshes the README download section (link, version, size, SHA-256,
  date, branch) while moving the superseded APK into the previous-artifacts
  table. Safe to re-run with the same version and `--no-build` to finish a
  partially applied release. CHANGELOG and the README prose note stay
  human-owned.

## [Unreleased] — Category search on the budget form (2026-10-27)

### Added
- The "Pilih kategori" sheet on Anggaran baru / Ubah anggaran now has a
  search field under the title that filters the expense categories by
  name, case-insensitive, with "Kategori tidak ditemukan" when nothing
  matches (`UI_SPEC.md` §6.6 updated). New tags:
  `anggaran_form_category_search` / `anggaran_form_category_search_empty`.

## [Unreleased] — Automation bug fixes (2026-10-26, `bugs/BUG_Found_05`/`06`)

### Fixed
- Bottom-nav labels no longer clip mid-word at large font scales: they
  truncate with an ellipsis (`TextOverflow.Ellipsis`), and the same hardening
  covers the month-switcher label and screen-header titles
  (`bugs/BUG_Found_06.md`).
- Transaction-card rows (Daftar/Ubah) no longer clip mid-word at large font
  scales either: the category name and type subtitle truncate with an
  ellipsis (`bugs/BUG_Found_06.md` follow-up).

### Changed
- Built-in (Bawaan) categories no longer offer rename: their rows in Kelola
  kategori show only the hide action, on both the Pemasukan and Pengeluaran
  tabs. Edit and delete remain on custom categories (`UI_SPEC.md` §6.5
  updated to match).
- Kelola kategori now observes the database instead of reloading once per own
  mutation: a category added from Tambah kategori (or written by an import)
  appears in the list as soon as you return to the screen, without switching
  tabs or re-entering, and hide/rename/delete still apply in place.

### Not a bug
- `bugs/BUG_Found_05.md` closed: the Tambah form composes and saves correctly
  in landscape; the "missing" note field and save button were off-screen nodes
  filtered from the automation dump. Re-verification and harness
  recommendations recorded in the bug file.

## [Unreleased] — P1+P2+P3 fixes from the 2026-10-26 code review (`docs/11-code-review-2026-10-26.md`)

### Fixed
- Backup export no longer writes the file on the main thread and can no longer
  crash the app: Android writes on `Dispatchers.IO`, iOS writes off-main and
  reports a failed `createFileAtPath`, and `NavGraph` wraps both exports in
  `runCatching`.
- The Cadangan screen now shows toasts — export saved/failed and import
  result/error — using strings that already existed but were never rendered.
- The import picker no longer offers CSV: `UI_SPEC.md` §7 excludes CSV import,
  yet both pickers accepted it and the parse always failed. Pickers, the error
  string, `README.md` and `UI_SPEC.md` now agree that import is JSON-only.
  Deleted the production-dead `BackupSerializer.parseBackupDate`.
- `TambahViewModel` no longer leaks a Room collector per type switch; the
  previous collector is cancelled like in `UbahTransaksiViewModel`.
- Bottom-tab navigation restores tab state (`launchSingleTop` + `restoreState`),
  so scroll position and the selected month survive a tab switch. To keep data
  fresh with restored ViewModels, `Daftar`, `Ringkasan`, and `Anggaran` now
  observe the database: a transaction changed elsewhere updates the open screen
  instead of the list snapping back to the current month after any write.
- Every remaining database mutation (`UbahTransaksi`, `AnggaranForm`,
  `TambahKategori`, `KelolaKategori`) is wrapped in `runCatching`, so a failed
  write degrades to "nothing happened" instead of crashing via
  `viewModelScope`.
- `TambahKategoriViewModel.save` has an `isSaving` guard: a double-tap used to
  pass the duplicate-name check twice and insert the same name twice.
- First-run seeding is guarded: a failed seed write (disk full, corrupt
  database) can no longer leave the app on the loading screen forever.
- The Cadangan status card shows the real account and category counts instead
  of hard-coded "2 akun · 6 kategori".
- `BudgetRepository.saveBudget` runs its read-then-write in one transaction and
  owns the timestamps completely; the budget form passes only user data.
- iOS import takes security-scoped access around the picked file (reads failed
  on physical devices without it), presents from the top-most view controller,
  and export fails loudly instead of silently when no presenter exists.
- CSV export quotes fields containing newlines, so a multi-line note can no
  longer split a row in Excel.

### Changed
- The Cadangan detail sheet uses the shared `SakuBottomSheet`, which re-arms
  `testTagsAsResourceId` for automation, and its close button carries the
  existing `component_sheet_button_cancel` tag. The back icon's
  contentDescription comes from the new `action_back` string instead of a
  hard-coded "Kembali".
- Deleted dead code: `Category.displayLabel` (unused, ignored its parameter).
- **P3 hygiene pass** (findings 14–20 of the review):
  - `copyReleaseApk` now copies `assembleRelease` output (it used to publish a
    debug APK under a release name; the checked-in `SimpleMoneyTrackerKt-V2.3.apk`
    was such a build and is replaced by a genuine release artifact — see the
    README download table for the fresh SHA-256). The disabled release
    optimization now carries its rationale as a comment.
  - `:app` trimmed to what `MainActivity` needs: AppCompat, Material
    Components, Compose material3, the icon pack, tooling previews and the
    duplicate test dependencies are gone; the theme now parents to framework
    `android:Theme.Material.NoActionBar`. Release APK drops from 28.7 MB to
    18.8 MB.
  - Android Auto Backup now explicitly **excludes** the database and
    preferences from cloud backup and device transfer — financial data never
    leaves the device through a system channel; the app's own JSON export is
    the supported path (matching the Cadangan screen's warning).
  - `BackupSerializer.JAKARTA` delegates to the single `SakuFormat.JAKARTA`;
    `UI_SPEC.md` §8 records the Asia/Jakarta pinning as a deliberate product
    decision.
  - Nits: removed `SakuContainer`'s self-import; a second `present()` on the
    Android file picker now cancels the stale callback instead of overwriting
    it; the Daftar/Ringkasan month arrows stop at `MonthPickerSheet`'s floor
    (year − 10) instead of walking back unbounded; `UbahTransaksiViewModel`
    applies the keep-or-clear category rule synchronously on type switch.
  - Deferred: ViewModel/DAO test suites (finding 19 — needs a
    `kotlinx-coroutines-test` dependency add), the `KelolaKategoriViewModel`
    reactive conversion (correct today; noted in review finding 20), and the
    Cadangan color-token migration (finding 7 remainder).

## [Unreleased] — PR #13: Fix undetected button in automation

### Added
- `Modifier.automationTestTags()` expect/actual extension that re-arms `testTagsAsResourceId` on Android for compositions rooted outside `MainActivity` (bottom sheets, dialogs); no-op on iOS.
- `release/SimpleMoneyTrackerKT-v2.2.apk` release artifact.

### Fixed
- Test tags were missing from the accessibility hierarchy in separate window compositions, so automation could not resolve buttons in `SakuBottomSheet`, `SakuDatePicker`, and the category rename `AlertDialog`. Re-armed the flag at each root.
- Category rename dialog error text now renders under the input field.
- Refined the rename dialog field with explicit text/cursor styling and spacing for error messages.

### Changed
- Simplified `Saku.xcscheme` by removing unused test, profile, analyze, and archive actions; kept build and debug launch settings. Added scheme management entries in `xcschememanagement.plist`.
