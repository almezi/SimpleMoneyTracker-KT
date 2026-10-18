# Code Audit — whole repository

Date: 2026-10-17
Scope: whole repo (`:app`, `:composeApp`, Gradle config, tests, docs)
Method: read-only. No file was changed. Every claim verified by reading the
source and grepping the whole tree (including tests, fixtures, config, strings).
Baseline: branch `feature/add-accessbility-id`, commit `49685e0`.

**What this repo does:** Saku is a small, offline-only Indonesian money tracker
(Kotlin Multiplatform + Compose; Android is the main target, iOS is a thin Swift
host over the same shared UI). One wallet, IDR only, no login, no network: you
add income/expense rows into a local Room (SQLite) database, then read them back
as a monthly list, a monthly summary, per-category budgets, and a JSON/CSV
backup. Two Gradle modules matter: `:composeApp` (all screens, ViewModels, DAOs,
migrations) and `:app` (Android shell + manifest).

**Load I assumed:** one person, one device, one process, no server. Tens of
transactions per month, a few hundred to a few thousand rows total, ~36
categories, a handful of budgets. No second writer, so races only matter inside
one app session. I judged performance against that (N+1 queries are cheap here,
a crash or a wrong total is not).

## Must fix

### 1. Excluded categories are counted in every total
`TransactionDao.kt:37-58`, `DaftarViewModel.kt:117-118`, `RingkasanViewModel.kt:70-73`, `Category.kt:11`

- **What this is:** The product rule says categories flagged `countsInTotals = false` (Transfer Masuk, Investasi as expense, Tabungan) are excluded from all totals (`UI_SPEC.md:16`).
- **Problem:** `countsInTotals` is written at seed time and never read anywhere — `grep` finds it in only two places, the field declaration and `CategoryRepository.kt:33` setting it to `true` for new categories. The `SUM` queries and `DaftarViewModel.rebuild` add up every row of the month. Put one Rp 1.000.000 row in "Transfer Masuk" and nothing else in October: Daftar shows "Masuk Rp 1.000.000", Ringkasan shows SALDO Rp 1.000.000 and "100,0%" savings rate, where the spec wants 0 and the empty state.
- **Fix:** Filter in SQL — `... FROM transactions t JOIN categories c ON c.categoryId = t.categoryId WHERE c.countsInTotals = 1` in the four DAO queries, and add the same condition to `rebuild`. One column already exists; no new table, no new layer.
- **If we skip it:** The app's headline numbers are wrong for anyone who records a transfer or a savings move, which is exactly what those three categories are for.

### 2. Switching type on Tambah throws away the amount
`TambahViewModel.kt:99-105`

- **What this is:** The type toggle should keep the amount and the note, and clear only a category that does not fit the new type (`UI_SPEC.md:14`).
- **Problem:** `updateType` sets `amountDigits = ""`. There *is* a regression test for this (`composeApp/src/iosTest/.../TransactionFormTypeSwitchTest.kt:42-112`), but it drives `TransactionForm` with a local state holder that "mirrors `TambahViewModel.updateType`" assuming the amount survives — so it passes while the real screen resets the field. Type Rp 5.000, tap "Pengeluaran", the amount is gone.
- **Fix:** Delete `amountDigits = ""` from `updateType` (one line). Then point the test at `TambahViewModel` so it can actually fail.
- **If we skip it:** Every mistap of the toggle costs the typed amount, and the test that was written to protect this keeps lying.

### 3. Import silently drops the categories, budgets and accounts it exported
`CadanganViewModel.kt:81-117`, `BackupSerializer.kt:47-71`, `BackupData.kt:23-29`

- **What this is:** JSON backup contains transactions, categories, budgets and accounts; import is the only restore path and the spec says import never deletes anything (`UI_SPEC.md:119`).
- **Problem:** `import` reads `parsed.transactions` and nothing else. Budgets, custom categories and accounts in the file are parsed and discarded. Export a file while you have a "Makanan" budget and a custom "Kopi" category, clear the app data (`adb shell pm clear`), import: the transactions come back, the budget is gone, "Kopi" is gone, and its rows now show the generic "Kategori" label.
- **Fix:** In `import`, insert the file's custom categories (INSERT OR IGNORE, the DAO strategy already exists) and budgets before the transactions, in the same `insertTransactions` transaction. Accounts are already seeded, so IGNORE is a no-op; skip anything else.
- **If we skip it:** A backup restores money but loses the budgets and category names that make it readable — the very data loss the backup screen warns about.

### 4. The date picker lets you pick tomorrow
`SakuDatePicker.kt:47-50,90`, callers at `TambahScreen.kt:97` and `UbahTransaksiScreen.kt:96`

- **What this is:** "No future dates" (`UI_SPEC.md:13`). `maxMillis` is meant to be today.
- **Problem:** Both callers pass `clock.currentTimeMillis()` — the current instant — and `isSelectableDate` allows `utcTimeMillis <= maxMillis + 24h`. The picker hands over UTC midnights, so with the local time at 10:00 Jakarta on 17 Oct (03:00 UTC) the bound becomes 18 Oct 03:00 UTC and 18 Oct 00:00 UTC passes. Any time from 07:00 Jakarta onwards, tomorrow is selectable.
- **Fix:** Pass today's Jakarta date as the UTC midnight of that date and drop the one-day slack, or compare dates instead of millis. Small change in `SakuDatePickerDialog` plus the two call sites.
- **If we skip it:** For most of the day a user can post a transaction into the future, which then shows under a month the app says is not reachable yet.

## Should fix

### 5. Renamed and custom category names never appear in the list, the delete sheet or the summary
`DaftarScreen.kt:157,284`, `UbahTransaksiScreen.kt:115`, `RingkasanScreen.kt:313`

- **What this is:** `getCategoryName(id)` (`CategoryUiMapper.kt:8-49`) maps ids to default string resources only. `categoryDisplayName(category)` (`CategoryUiMapper.kt:51`) is the one that honours `customName`.
- **Problem:** The four places above use `getCategoryName` from a bare `categoryId`. So a custom "Kopi" renders as `SakuStrings.tambah_category_label` — the literal word "Kategori" — in the Transaksi row, in its delete confirmation, and in the Ringkasan breakdown; a renamed "Gaji" → "Gajian" keeps showing "Gaji" in all four.
- **Fix:** Give `DaftarViewModel`/`UbahTransaksiViewModel` the category rows (or expose a small map id → display name from `CategoryRepository`) and pass `categoryDisplayName` at those four call sites.
- **If we skip it:** Renaming or creating a category looks like it half-worked: correct on the manage screen, wrong everywhere the money is shown.

### 6. The colour a user picks for a category is never displayed
`SakuColor.kt:100-101`, `CategoryChips.kt:55`, `Category.colorHex` at `Category.kt:13`

- **What this is:** `TambahKategoriScreen` offers eight swatches and stores the choice in `Category.colorHex`.
- **Problem:** Marks and avatars are drawn with `SakuCategoryPalette.colorFor(categoryId)` — a hash of the id, not the stored colour. `colorHex` is only ever written (seed, create, export) and read by nothing. Pick the yellow swatch and the category's square is whatever the hash gives. The live preview is wrong in a different way: `TambahKategoriScreen.kt:174` passes the *hex string* as `categoryId`, so the preview mark hashes the text "#F6C453".
- **Fix:** Make `CategoryMark`/`CategoryAvatar` take the colour (or the `Category`) and fall back to `colorFor(id)` when it is null; pass the real hex to the preview.
- **If we skip it:** A visible control that does nothing, and no way to tell two same-hash categories apart in the picker.

### 7. Undo after delete can crash on a reused row id
`DaftarViewModel.kt:82-92`, `TransactionDao.kt:63-64`

- **What this is:** Delete shows a 5-second "Urungkan" toast; undo re-inserts the deleted row, id included.
- **Problem:** `@Insert` on `transactions` has no conflict strategy, so it is ABORT. SQLite reuses the highest free rowid: delete transaction 42, add a new transaction (it gets 42), tap "Urungkan" → insert of id 42 → constraint violation inside `viewModelScope`, uncaught → crash. Navigating away and back restarts the 5-second window, so the sequence is reachable by hand.
- **Fix:** Make `restoreTransaction` insert with `OnConflictStrategy.IGNORE` and clear the undo candidate when nothing was inserted, or insert with `id = 0` so undo always creates a fresh row.
- **If we skip it:** A crash triggered by two ordinary actions, plus a lost "undo".

### 8. Editing a transaction whose category is hidden silently clears the category
`UbahTransaksiViewModel.kt:136-148`

- **What this is:** The edit form offers `getVisibleCategoriesByType`, and drops the selection when the current id is not in that list.
- **Problem:** Hidden categories keep their history by design (`CATEGORIES.md:24`), but the id is not in the visible list, so `takeIf` nulls it. Hide "Tabungan", open a Tabungan transaction: the category field is empty, the row shows a placeholder, and "Simpan perubahan" is disabled until a different category is chosen.
- **Fix:** Keep the original category when it belongs to the transaction's type even if hidden — one extra check, or load `getByTypeOnce` for the edit path.
- **If we skip it:** Editing only the note or the date of an old transaction forces the user to re-categorise it.

### 9. Renaming a category accepts an empty or duplicate name
`KelolaKategoriScreen.kt:175-183`, `KelolaKategoriViewModel.kt:76-81`

- **What this is:** Category names are 1-20 characters and unique per type, case-insensitive (`CATEGORIES.md:25`).
- **Problem:** `rename` trims and writes with no validation; the dialog's only guard is a max length. Clear the field and confirm: `customName` becomes `""`, which is non-null, so `categoryDisplayName` returns "" and the category is nameless in the manage list and the picker. Duplicates are also allowed — `countWithName` (`CategoryDao.kt:23`) only compares `customName`, which is NULL for unrenamed defaults, so a custom "Gaji" can be created next to the default.
- **Fix:** Validate in `rename` (reuse `countWithName` plus an empty check) and surface the existing `kategori_duplicate` message in the dialog.
- **If we skip it:** A blank row the user cannot identify, and two identical names that break the "referenced by ID" assumption anyway.

### 10. The delete-category sheet names the wrong target
`KelolaKategoriViewModel.kt:86-91`, `KelolaKategoriScreen.kt:191-196`

- **What this is:** Deleting a custom category moves its transactions to "Lainnya" and the sheet must say so.
- **Problem:** `fallbackName` is `fallback?.customName.orEmpty()`. The fallback (`exp_other` / `inc_other`, `KelolaKategoriViewModel.kt:108-111`) is a default category, so `customName` is null and the name becomes empty; the screen then falls back with `ifBlank { categoryName }`, which is the name of the category being deleted. Delete 12-transaction "Kopi" and the sheet reads "12 transaksi … dipindah ke Kopi".
- **Fix:** Use `categoryDisplayName(fallback)` when building the request and drop the `ifBlank` guess.
- **If we skip it:** The confirmation text states the opposite of what happens, right before an irreversible move.

### 11. Importing a file that repeats a transaction inserts every copy
`CadanganViewModel.kt:91-102`

- **What this is:** A transaction is a duplicate when date, type, amount, category, account and note all match (`UI_SPEC.md:119`).
- **Problem:** Only `existingKeys` (the rows already in the database) is consulted; `fresh` is never used as a key set. A file listing the same row twice reports "2 ditambahkan" and stores two identical transactions.
- **Fix:** Track keys as you append — `if (key in existingKeys || !seen.add(key))` — a two-line change.
- **If we skip it:** Re-importing a hand-merged file doubles rows, which then have to be deleted one by one.

### 12. Two test tags embed a user-editable display name and disagree with TESTABILITY.md
`BudgetProgressRow.kt:72,141,148`; `DaftarScreen.kt:290` + `TransactionCard.kt:44`

- **What this is:** The automation contract fixes tag shapes: `anggaran_row_<categoryId>` (`TESTABILITY.md:57`), `daftar_item_<transactionId>` (`TESTABILITY.md:48`).
- **Problem:** `BudgetProgressRow` appends `categoryName` to the tag the caller already suffixed with the id, so the real tag is `anggaran_row_exp_other_Lainnya` — and it changes the moment a category is renamed, taking spaces and `&` with it. `TransactionCard` appends the category id, giving `daftar_item_42_exp_other` where the doc promises `daftar_item_42`.
- **Fix:** Suffix with the stable id only (pass `categoryId` into `BudgetProgressRow`), and stop appending in `TransactionCard`.
- **If we skip it:** Renaming a category breaks the suite silently, which is the one thing `AGENTS.md` forbids.

## Nice to have

### 13. The budget screen runs two queries per budget row, twice per open
`AnggaranViewModel.kt:65-67`, `AnggaranScreen.kt:70` plus `init` at `:55-57`

- **What this is:** For each budget: load the category, then one `SUM` for the month.
- **Problem:** With ten budgets that is twenty round trips, and the whole `refresh()` runs again from `LaunchedEffect(Unit)` right after `init`. Harmless at this size, wasteful by construction.
- **Fix:** One `GROUP BY categoryId` for the month's spend (the shape already exists as `totalsByCategory`), drop the extra `LaunchedEffect`, and use one query to fetch categories.
- **If we skip it:** Nothing breaks today; it is the first thing to cost time if budgets ever grow.

### 14. Kelola Kategori runs one count query per category row
`KelolaKategoriViewModel.kt:113-121`

- **What this is:** `usageCount` is called once per row, ~36 times per tab switch and after every hide/rename.
- **Problem:** A query per item, on every reload.
- **Fix:** One `SELECT categoryId, COUNT(*) ... GROUP BY categoryId` into a map, same as finding 13.
- **If we skip it:** Fine now, slow later.

### 15. 195 lines of dead Android backup code
`app/src/main/java/id/almezi/simplemoneytracker_kt/data/BackupRepository.kt`, `SimpleMoneyTrackerApp.kt:29-36`

- **What this is:** A second, Android-only backup implementation with its own `BACKUP_VERSION`, `BackupData` and `BackupCategory`, left behind when backup moved to `:composeApp`.
- **Problem:** I grepped the whole tree: the class is referenced only by `SimpleMoneyTrackerApp.backupRepository`, and that property is never read. It duplicates the shared `BackupSerializer` and `BackupData` and would drift.
- **Fix:** Delete the file, the `backupRepository` property and its four imports.
- **If we skip it:** Two backup formats in one repo, one of them untested and unreachable.

### 16. Small dead members
`RingkasanViewModel.kt:23,66,76,18,94`, `AnggaranViewModel.kt:36`, `BudgetRepository.kt:6`, `BudgetDao.kt:24-25`

- **What this is:** `RingkasanUiState.loading` is set but never read; `AnggaranUiState.toast` is never set or read; `BudgetRepository` takes `database` and never uses it; `BudgetDao.update` has no callers; `CategoryBreakdown.sharePercent` is computed and never read (the UI calls `formatCategoryShare`, which rounds — the dead field truncates, so the two formulas disagree).
- **Problem:** Dead state invites copy-paste and misleads readers about what the screen does.
- **Fix:** Delete the five members.
- **If we skip it:** Nothing breaks; the code is just bigger than the app.

### 17. Four dependency entries and two version keys are unused
`gradle/libs.versions.toml:10,16,33-35,39`

- **What this is:** `room` 2.8.4 with `room-runtime`/`room-ktx`/`room-compiler`, and `com.android.support:animated-vector-drawable`.
- **Problem:** They belong to the pre-KMP Android-only module; nothing references them after the move (verified across all `*.kts`).
- **Fix:** Delete the six catalog lines.
- **If we skip it:** Dead versions keep getting bumped and audited for nothing.

### 18. `BackupFileStore.writeText` is only called on iOS
`commonMain/.../backup/BackupFileStore.kt:34`, `androidMain/.../BackupFileStore.kt:56-61`

- **What this is:** The interface exposes "write somewhere durable" as a second export path.
- **Problem:** Android's export goes through `shareText`; nothing on Android calls `writeText`, and the Android body writes to `getExternalFilesDir`, the location the file's own comment (`:22-30`) says Android 11+ hides from the user.
- **Fix:** Remove the interface member, make iOS's implementation private (its `shareText` already calls it).
- **If we skip it:** A documented-as-broken path stays reachable for the next caller.

## Verdict

Not healthy. Two product rules are unimplemented (excluded-category totals, no
future dates), one rule is actively violated by the ViewModel (type switch), and
the only restore path loses budgets and category names — fix 1–4 first, then 5
and 7.

**Lean: ~-225 lines, -4 dependencies possible** (findings 15–18: the whole
`BackupRepository.kt`, the `writeText` seam, the dead state, and the four stale
catalog entries).

## Not checked

- `BUGS.md` (AGENTS.md forbids reading it, so I could not cross-check which findings are already known).
- No build, test or app was run — every finding comes from reading source.
- `app/src/main/res/values/{strings,colors}.xml`, `themes.xml` beyond one line, `iosApp/*.sh`, the Xcode project and `SakuApp.swift`, `docs/*.md`, `improve/*.md`, `third_party/*.py`, the remaining design-system components (`SakuSwitch`, `SegmentedToggle`, `SakuLogo`, `SakuLoadingScreen`, `SakuInsets`, `SakuTheme`, `SakuDimens` values) and `app/src/androidTest`.
- One thing seen but not reported for lack of a certain trigger: the Daftar undo toast is keyed on a freshly built `SakuToast` (with a fresh lambda) each recomposition, so its 5-second timer can restart instead of expiring.
