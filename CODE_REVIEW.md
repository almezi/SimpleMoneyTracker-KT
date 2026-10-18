# Code Review — Unused Code

Date: 2026-10-11
Scope: `app/src/` (main, test, androidTest)
Baseline commit: `ebe5de5`

**Re-checked 2026-10-18** at `2acac55` on `feature/ios-migration`; see §8.

Status: **fully applied.** Sections 1 and 3 landed in commit `1304841`.
Section 2 is applied in the working tree (see §6).

Method: static scan for imports, declarations, resources and test tags with no
reference anywhere in the module. Every item below was confirmed by direct grep.

---

## 1. Unused imports (7) — APPLIED in `1304841`

| File | Line | Import |
|---|---|---|
| `ui/components/SakuEmptyState.kt` | 3 | `androidx.compose.foundation.layout.Box` |
| `ui/anggaran/AnggaranFormScreen.kt` | 7 | `androidx.compose.foundation.layout.Spacer` |
| `ui/anggaran/AnggaranFormScreen.kt` | 9 | `androidx.compose.foundation.layout.fillMaxWidth` |
| `ui/anggaran/AnggaranFormScreen.kt` | 10 | `androidx.compose.foundation.layout.height` |
| `ui/anggaran/AnggaranViewModel.kt` | 6 | `id.almezi.simplemoneytracker_kt.data.Budget` |
| `ui/cadangan/CadanganViewModel.kt` | 4 | `android.net.Uri` |
| `ui/daftar/DaftarScreen.kt` | 17 | `androidx.compose.material3.Surface` |

Note: `getValue` / `setValue` imports in `SakuSwitch.kt:19`, `SakuButtons.kt:16`
and `BudgetProgressRow.kt:18` look unused to a naive scan but are required by
`by` property delegation. Leave them.

## 2. Dead code — APPLIED in working tree

### 2.1 Unused composables / functions

| Declaration | Note |
|---|---|
| `ui/transaction/TransactionForm.kt:380` `ExpenseHeaderLabel` | never called |
| `ui/components/CategoryChips.kt:86` `CategoryChipGrid` | never called; `CategoryChipRow:124` is the variant actually used |
| `ui/CategoryUiMapper.kt:70` `categoryColorOrNull` | never called |
| `ui/designsystem/SakuFormat.kt:75` `jakartaYearMonth` | never called; `monthOf:124` does the same job |
| `ui/designsystem/SakuTokenPreview.kt:28` `Swatch` | private and never called — `ColorTokensPreview:47` inlines a duplicate of it |

### 2.2 Unused design tokens

| Declaration | Note |
|---|---|
| `ui/designsystem/SakuDimens.kt:60` `SakuSpace.sheetTopPadding` | no references |
| `ui/designsystem/SakuDimens.kt:62` `SakuSize.emptyIcon` | no references |
| `ui/designsystem/SakuDimens.kt:40` `SakuSize.chipHeight` | only referenced by `CategoryChipGrid`; becomes dead once that is removed (cascade) |

### 2.3 Unused data-layer members

| Declaration | Note |
|---|---|
| `data/CategoryDao.kt:42` `setColor` | never called |
| `data/TransactionRepository.kt:28` `getCategory` | pass-through, never called |
| `data/TransactionRepository.kt:30` `getCategoriesByType` | pass-through, never called |
| `data/TransactionRepository.kt:35` `insertCategories` | pass-through, never called |
| `data/TransactionRepository.kt:39` `getAccount` | pass-through, never called |
| `data/TransactionRepository.kt:41` `insertAccounts` | pass-through, never called |
| `data/TransactionRepository.kt:46` `transactionsInMonth` | never called |
| `data/TransactionRepository.kt:56` `netBetween` | never called |
| `data/TransactionRepository.kt:68` `usageCountForCategory` | never called |

## 3. Unused string resources (23) — APPLIED in `1304841`

All in `app/src/main/res/values/strings.xml`. No `R.string.*` reference exists
for any of them in Kotlin or XML.

| Line | Name | Value |
|---|---|---|
| 5 | `tambah_transaksi` | Tambah Transaksi |
| 6 | `daftar_transaksi` | Daftar Transaksi |
| 7 | `ringkasan_transaksi` | Ringkasan |
| 8 | `tambah_screen_title_pemasukan` | Tambah Pemasukan |
| 9 | `tambah_screen_title_pengeluaran` | Tambah Pengeluaran |
| 10 | `tambah_type_pemasukan` | Pemasukan |
| 11 | `tambah_type_pengeluaran` | Pengeluaran |
| 12 | `tambah_amount_label` | Jumlah |
| 14 | `tambah_date_button` | Pilih Tanggal |
| 15 | `tambah_note_label` | Catatan |
| 16 | `tambah_save_button` | Simpan |
| 17 | `tambah_error_amount` | Jumlah tidak valid |
| 18 | `tambah_error_category` | Pilih kategori |
| 19 | `tambah_save_success` | Transaksi berhasil disimpan |
| 21 | `loading` | Memuat… |
| 22 | `delete_dialog_title` | Hapus transaksi? |
| 23 | `delete_dialog_message` | Transaksi ini akan dihapus permanen. |
| 25 | `daftar_empty` | Belum ada transaksi bulan ini |
| 35 | `cd_close` | Tutup |
| 50 | `tambah_header_pemasukan` | Tambah transaksi |
| 51 | `tambah_header_pengeluaran` | Tambah Pengeluaran |
| 65 | `kelola_kategori_placeholder_body` | Kelola kategori akan hadir di langkah berikutnya. |
| 116 | `budget_alert_reached` | %1$s sudah memakai 80% anggaran |

---

## 4. Verified clean

- No `TODO` / `FIXME` / `XXX` / `HACK` markers anywhere in `app/src`
- No commented-out code blocks
- No unused local `val` declarations (spot-checked all multi-declaration blocks)
- Every constant in `TestTags.kt` is referenced — consistent with TESTABILITY.md
- `AppDatabase.kt:22` / `:38` `migrate()` overrides are Room callbacks, not dead code

## 5. False positives — do not remove

- The `@Preview` composables in `ui/designsystem/SakuTokenPreview.kt`
  (`ColorTokensPreview:47`, `TypographyPreview:104`, `CategoryPalettePreview:128`,
  `DimensPreview:157`) have no call sites by design. They are tooling entry points.
- `SakuDarkColors`, `LocalSakuColors`, `SakuFontSans`, `SakuFontMono`,
  `SakuTypography` are referenced by name, not by dot access.
- `BackupRepository.kt:182` `dateString` is called at `BackupRepository.kt:109`. (Stale since `d7a9a45`: that whole file was deleted as dead code — see §8.)
- `SakuLogo.kt:38` `DrawScope.drawLogo` is called at `SakuLogo.kt:34`.
- `SakuFormat.kt:21` `MAX_AMOUNT` is used at `SakuFormat.kt:42`.
- Test classes and `MainActivity` are framework entry points.

---

## 6. Cascade removals

Three further items became dead once §2 was applied, or were missed by the first
scan because of a name collision with a DAO method of the same name. Removed too:

| Declaration | Reason |
|---|---|
| `data/TransactionDao.kt:41` `sumNetBetween` | orphaned when `TransactionRepository.netBetween` was removed |
| `data/CategoryRepository.kt:12` `getByType` | pre-existing dead code; the `Flow` variant is unused, only `getByTypeOnce` is called. Missed initially because `CategoryDao.getByType` shares the name |
| `data/CategoryDao.kt:15` `getByType` | orphaned when `CategoryRepository.getByType` was removed |
| `ui/transaction/TransactionForm.kt:62` `import ...typeAccent` | orphaned when `ExpenseHeaderLabel` was removed |

## 7. Verification after §2

- `./gradlew :app:assembleDebug` — BUILD SUCCESSFUL
- `./gradlew :app:testDebugUnitTest` — BUILD SUCCESSFUL
- Re-ran the unused-import scan: only the three `getValue` false positives remain
  (see §1 note); no new dead declarations or unused tokens

## 8. Re-check 2026-10-18

Baseline moved from `ebe5de5` to `2acac55` (`feature/ios-migration`), after the
fixes for `CODE_AUDIT.md` findings 1–18. Re-grepped every declaration named in
§2 and §6: all absent from the tree, so §2 stands as applied.

Two things changed the document itself:

- **`BackupRepository.kt` is gone.** Deleted in `d7a9a45` as audit finding 15:
  195 lines of Android-only backup code with no caller. The §5 false positive
  that cites it no longer has a file to point at.
- **The audit fixes removed more dead code**, all in the same pass that created
  it, so §2 did not grow:

| Declaration | Removed because |
|---|---|
| `data/TransactionDao.kt` `sumIncomeBetween`, `sumExpenseBetween`, `totalsByCategory` | audit 1 replaced the SQL totals with `MonthTotals.kt` |
| `data/TransactionRepository.kt` `totalsByCategory`, `incomeBetween`, `expenseBetween` | same |
| `data/TransactionRepository.kt` `insertTransactions` | audit 3 replaced it with `restoreImport`; import was its only caller |
| `data/TransactionDao.kt` `sumAmountForCategoryBetween` | audit 13 replaced the per-row spend with `expenseTotalsByCategory` |
| `data/TransactionRepository.kt` `spentForCategory` | same |
| `data/BudgetDao.kt` `update` | audit 16, no callers |
| `ui/ringkasan/RingkasanViewModel.kt` `RingkasanUiState.loading`, `CategoryBreakdown.sharePercent` | audit 16, never read |
| `ui/anggaran/AnggaranViewModel.kt` `AnggaranUiState.toast` | audit 16, never set or read |
| `data/BudgetRepository.kt` `database` | audit 16, constructor argument never used |
| `BackupFileStore.writeText` (interface + Android body; iOS copy made private) | audit 18, Android body wrote to a location its own comment calls unreachable |
| `gradle/libs.versions.toml` `room` 2.8.4 ×3, `animated-vector-drawable` ×2 | audit 17, no `*.kts` referenced them |

Verification at `2acac55`: `:composeApp:testAndroidHostTest` 103 tests, 0
failures; `:app:assembleDebug` builds; iOS main and test compilations pass.

Two open items from that work, recorded in `CODE_AUDIT.md`: the SQL added in
audit 13, 14 and 18 has no test (no Room in-memory testing in this source set),
and `TESTABILITY.md:113-114` still documents a `component_card_transaction`
suffix that finding 12 removed.