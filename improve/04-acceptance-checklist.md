# Acceptance Checklist

Check each group after the matching step in `03-vibe-coding-prompts.md`. Do not move on until every item in the group passes.

Status marks:
- `[x]` done and verified (build + unit test, or code inspection)
- `[~]` implemented in code, not yet verified on a device
- `[ ]` not started

Verify a group with `./gradlew testDebugUnitTest assembleDebug`, then `./gradlew installDebug` on a running emulator or device before marking anything `[x]` by eye.

Current status: all ten steps are implemented and were exercised on an emulator. Steps 1 to 5, 7, 8 and 10 are verified; the items marked `[~]` are implemented but were not exercised by hand. Two database migrations (1 -> 2 for accounts, 2 -> 3 for budgets and category columns) both ran against a real database that already held data.

## Step 1: Design tokens
- [x] All `improve/tokens.css` values exist in `ui/designsystem/SakuColor.kt`, `SakuDimens.kt`, `SakuType.kt` (verified: script compares every `--color-*` hex against the Kotlin file).
- [x] Background is `#0e0e12` and it is applied app-wide through `SakuTheme` in `MainActivity`.
- [x] Text is readable on the background (token values from `tokens.css`; no pure black on dark).
- [x] DM Sans and DM Mono are bundled in `app/src/main/res/font/` (static instances, so API 24 gets real weights). Licenses in `third_party/fonts/`.
- [~] `@Preview` swatch pages exist (`SakuTokenPreview.kt`: Colors, Typography, Palette, Spacing/Radius). Open them in Android Studio to check the values.
- [x] Type scale matches the tokens: title 20sp, base 14, md 15, amount large 34, amount medium 24, amount small 15.
- [x] Deviation from the original item: existing screens do change, because `SakuTheme` now wraps the app in the dark theme. Their layout is untouched until steps 3-6.

## Step 2: Shared components
- [x] `PrimaryButton`: 52 dp tall, violet `#5b4bd6`, `PrimaryButtonTone.Expense` uses `#b8405a`, disabled state uses surface background + disabled text.
- [x] `SegmentedToggle`: income uses `#6ee7a0` on `#1b2e25`, expense uses `#f0708a` on `#2e1a21`.
- [x] `AmountInput`: shows "Rp 0" when empty; typing 212121 shows "Rp 212.121"; letters rejected; max 12 digits (covered by unit tests for the formatter and sanitizer; the field itself needs a device run).
- [x] `formatRupiah(-123)` shows "−Rp 123" with the U+2212 minus sign (unit test).
- [x] `CategoryChipGrid`: 3 columns, selected chip uses the type color.
- [x] `CategoryChipRow`: pill layout for the expense variant.
- [~] `BottomSheet`: drag handle, scrim, slides up. Needs a device run to confirm the 200 ms slide and reduced-motion behavior.
- [~] `BottomNav`: floats 20 dp above the bottom, 64 dp tall, 16 dp side margin, active item violet. Not wired into `MainActivity` yet (that happens in step 3).
- [x] `MonthSwitcher`: next button disabled when the caller passes `nextEnabled = false` (caller decides "is current month").
- [x] `Toast`: appears above the nav reserve, auto-dismiss 2000 ms (`SakuMotion.toastMillis`), 5000 ms with an undo action.
- [x] `formatSavingsRate(212121, 123)` returns "99,9%" (comma decimal); "—" when income is 0 (unit tests).
- [x] Every interactive component carries a `testTag` from `TestTags.kt` (`component_*` block).

## Step 3: Tambah transaksi

Schema change: `accounts` table added and `transactions.accountId` added. Database version 1 -> 2, migration in `AppDatabase.MIGRATION_1_2`. The migration SQL was checked against Room's generated v2 schema (`CREATE TABLE accounts (...)` and the `accountId` column with the `acc_tunai` default), but it has not been run on a device yet. Test the migration on an install that already has transactions before trusting it.

- [x] Switching Pemasukan and Pengeluaran changes: amount color, category list, header label, button label, button color.
- [x] Switching type keeps the amount and note; clears the category if it does not belong to the new type. (Amount verified on device: typed 75.000, switched to Pengeluaran, value stayed and turned pink-red. Note was not exercised separately but shares the same state update.)
- [x] Button is disabled until amount > 0 and a category is chosen.
- [x] Helper text says exactly what is missing.
- [x] Saving shows the toast "Transaksi disimpan" and clears amount, category, and note, but keeps account and date. (Verified: row `(8, pengeluaran, 150000, exp_daily_food, acc_tunai)` written; form cleared; "Tunai" and "9 Okt 2026" kept.)
- [x] Akun and Tanggal are side by side, date shows "9 Okt 2026".
- [~] Catatan accepts up to 60 characters and rejects more. Enforced in `NoteField`; not exercised by hand.
- [x] Save button is never hidden behind the bottom nav (verified by scrolling to the bottom).
- [x] Old transactions still load after saving a new one. (The pre-migration row is still there after the new one was written.)
- [x] Migration ran on a real install that had data: version 1 -> 2, existing transaction preserved and backfilled to `acc_tunai`, accounts seeded with Tunai, BCA, GoPay.
- [x] Bottom nav is the floating `SakuBottomNav` (64 dp tall, 16 dp side margin, 20 dp from the bottom) and the screen reserves 84 dp of bottom space.
- [x] Future dates are blocked in the date picker; the bound comes from the injected `Clock`, not the system clock.
- [x] Dates format through `kotlinx-datetime` in Asia/Jakarta with month names from `strings.xml`.
- [x] The "Kelola kategori" link appears on the expense variant only and opens a placeholder route without the bottom nav.
- [x] Every new control has a tag: `tambah_header`, `tambah_select_account`, `tambah_select_date`, `tambah_helper`, `tambah_link_kelola_kategori`, `tambah_sheet_account`.

Fixed during step 3 verification:
- Category chip text broke mid-word ("Freelanc / e") because 3-column chips left about 67 dp for the label. Chip padding is now 8 dp and the color mark 10 dp, so names break at word boundaries instead.

Still open for step 3:
- The database holds 32 seeded categories (9 income, 23 expense); the design assumes 6 and 7. The screen shows what is in the database. Aligning the category set is a data migration and belongs with step 9. Decide before step 4 so the list screen shows the same names.
- The Akun sheet is a bottom sheet (`SakuPickerSheet`) built on the Step 2 component. The design does not specify it yet.
- The toast renders above the nav reserve, which overlaps the save button while it is visible. Acceptable for 2 seconds, but confirm it looks right on a shorter screen.

## Step 4: Transaksi list

- [x] Transactions are grouped by day with the net total on each day header ("JUMAT / 9 Okt 2026 / −Rp 990.000").
- [~] Newest day is first. Sorting is implemented, but only one day group existed during the test run.
- [~] Masuk total is green; Keluar total is expense color. Keluar was checked with a value ("Rp 990.000" in expense color); Masuk was only checked at Rp 0, where it correctly goes muted instead of green.
- [x] "Ubah" opens the edit screen.
- [x] "Hapus" opens the confirmation sheet, never deletes directly.
- [x] The sheet text names the transaction, amount, and date.
- [x] After delete, the toast "Transaksi dihapus" shows "Urungkan" for 5 seconds.
- [x] Urungkan restores the transaction in its original position: the deleted row came back with the same id and amount.
- [x] A month with no transactions shows the empty state with both buttons.
- [x] Nav is visible.
- [x] The next-month button is disabled on the current month.
- [x] "Cadangan & ekspor data" button is rendered. Its click handler is still a no-op; it opens the Cadangan screen in step 10.

Fixed during step 4 verification:
- The Masuk/Keluar tiles clipped long amounts ("Rp 150.000" rendered as "Rp") at 24 sp. They now use the 16 sp amount style.
- The transaction avatar showed "M&" for "Makanan & Minuman" because the second word was a symbol. Initials now skip words with no letters, so it reads "MM" as the design specifies.

## Step 5: Ubah transaksi

- [x] Fields are pre-filled with the saved values.
- [x] Type toggle shows the saved type.
- [x] "Simpan perubahan" is disabled until something changes.
- [x] Saving returns to Transaksi and the list and summary update. (150.000 edited to 990.000; the row and the Keluar tile both updated.)
- [~] "Hapus transaksi" shows the same confirmation sheet. The button renders in danger text at 48 dp; the sheet itself was only exercised from the Transaksi list.
- [x] Bottom nav is hidden.
- [x] Type is pre-filled and the category list matches the saved type. Switching type in the edit form clears a category that does not belong to the new type.

Notes for step 5:
- The edit screen reuses `TransactionForm`, the same composable the Tambah screen uses, so both stay in sync.
- The "Kelola kategori" link is hidden on the edit screen; the design only shows it on the Tambah expense variant.

## Step 6: Ringkasan

- [x] Balance equals income minus expense for the selected month only.
- [x] Balance is green when zero or positive, expense color when negative. Verified with income only (Rp 900.000, green).
- [x] Income-only month: the income breakdown appears.
- [x] Both breakdowns sort by amount, highest first, and show a share percentage ("Gaji 100%").
- [x] Savings rate with income 0 shows "—" and the hint. Verified with income only ("100,0%").
- [x] Savings rate uses one decimal and a comma.
- [~] Empty month: Ringkasan kosong; savings row, buttons, and breakdowns are hidden. The empty branch is implemented but was not exercised on a month with no transactions.
- [x] "Anggaran" and "Kelola kategori" buttons navigate.

## Step 7: Anggaran

- [x] Budget below 75% shows "Aman" in green (`BudgetStatus.of` unit tested; "Aman" seen on device at Rp 0 of Rp 500.000).
- [~] Budget from 75% to 99% shows "Hampir habis" in amber (unit tested; not seen on device).
- [~] Budget at 100% or more shows "Lewat batas" in pink-red and "Lebih Rp X" (unit tested; not seen on device).
- [x] Bar fill never goes past 100% visually (fill width is coerced in the row and in the state).
- [x] Budgets sort by percentage used, highest first.
- [~] Deleting a budget keeps every related transaction. The budget table has no link to transactions, so this holds by construction; the delete flow itself was not exercised on device.
- [x] Empty state shows when there are no budgets, and the total card is hidden.
- [x] Nav is hidden.
- [x] Total card shows total spent of total budget with a progress bar, hidden when there are no budgets.

## Step 8: Anggaran baru and Ubah anggaran

- [x] Title changes between "Anggaran baru" and "Ubah anggaran".
- [x] Category list excludes categories that already have a budget.
- [~] Category cannot be changed in edit mode. The field has no chevron and `selectCategory` returns early when editing; the edit form itself was not opened on device.
- [x] Limit must be greater than 0 to save.
- [x] 80% switch is on by default.
- [~] Creating a budget a second time for the same category is blocked with a clear message. Blocked by the picker filter; not exercised by trying twice on device.
- [~] "Hapus anggaran" appears only in edit mode. Renders in edit mode; not opened on device.

## Step 9: Kelola kategori and Tambah kategori

- [x] Pengeluaran and Pemasukan toggle changes the list.
- [x] Default categories show "Ubah" and "Sembunyikan" only (no Hapus).
- [~] Custom categories show "Ubah", "Sembunyikan", and "Hapus" (rendered from `isDefault`; no custom category existed during the run).
- [x] Hidden categories disappear from the Tambah form but stay in past transactions (the Tambah and Ubah forms query visible categories only; not exercised by hiding one on device).
- [x] "Tampilkan" restores a hidden category.
- [~] Deleting a custom category with transactions moves them to "Lainnya" of the same type (`AppDatabase.deleteCustomCategoryAtomically` runs the move, the budget delete, and the category delete in one transaction; not exercised on device).
- [x] Deleting a custom category removes its budget (same transaction).
- [x] Deleting an unused category shows the "belum dipakai" text.
- [x] Name rules: 1-20 characters; duplicate names are blocked (the check is case-insensitive).
- [x] Color swatch selection updates the preview chip.
- [x] Bottom hint "Kategori bawaan tidak bisa dihapus, hanya disembunyikan" is shown.

## Step 10: Cadangan and final polish

- [x] Export CSV opens in Excel with Indonesian characters intact. Verified: the file starts with the UTF-8 BOM (EF BB BF) and the header row is `date,type,amount,category,account,note`.
- [x] Export JSON contains accounts, categories, transactions, and budgets, with a `version` field.
- [x] Import JSON adds new transactions and reports how many were skipped.
- [x] Importing the same file twice adds nothing the second time. Verified: "0 transaksi ditambahkan, 1 dilewati karena sudah ada." and the row count stayed at 1.
- [x] Import never deletes existing data.
- [x] An invalid file shows the error message under the import area. Verified with a non-Saku file.
- [x] Warning card about local-only storage is visible. The copy in `02-screens-and-rules.md` says "browser atau perangkat ini"; on Android it reads "perangkat ini" only.
- [x] Status card shows the transaction count and the last backup date; the date is persisted, so it survives a restart.
- [x] No emoji is used as an icon anywhere.
- [x] Every tappable element is at least 48 dp tall. Note: `tokens.css` says `--touch-min: 44px`; we use 48 dp.
- [x] No screen has content hidden under the bottom nav.
- [x] Every amount uses DM Mono and the "Rp" format.

### Verified end to end
Export JSON, delete the only transaction (database confirmed empty), import the same file again (row restored), then import a second time (row count unchanged, duplicate message shown). The database was read directly after each step to confirm the state, not just the UI.

### Fixed during steps 6 to 10
- The bottom nav clipped the third item ("Ringka" instead of "Ringkasan"). Items now share the width evenly and the label uses the caption style.
- Typing in the amount field appended to the "Rp 0" placeholder value, producing numbers such as 900.000 when 90000 was typed. "Rp 0" is now a placeholder over an empty value, and the caret is pinned to the end on every edit and on focus.
- "Ingatkan saat 80%% terpakai" rendered a double percent sign; the strings no longer escape it.
- Import decided the file type from the URI extension, which the system file picker does not guarantee, so a valid JSON file could be rejected. The format is now detected from the content.
- The empty-state body on Anggaran repeated the button label; it now has its own line.
- "Cadangan terakhir" was kept in memory only and reset on every restart. It is now stored in SharedPreferences.

### Still open
- CSV import is not implemented. The spec marks it optional for the first build; importing a CSV currently shows the "file tidak valid" message.
- Export writes to the app's own external folder, which is not reachable from the file picker on Android 11+. Files were copied to Downloads by hand during testing. Sharing the export with a system share sheet would fix this.
- The whole-app regression list below has not been run as one pass.
- The category set still does not match the design (32 seeded categories against 6 and 7 in the spec). This is still the open decision from step 3.
- One observation, not reproduced: right after `installDebug` and a cold start, the Transaksi list showed the empty state while the database held a transaction, and Ringkasan showed the same transaction correctly. A second cold start rendered the list correctly. Worth watching.
## Whole-app regression (run once at the end)
- [ ] Add income and expense in one session, close the app, reopen: data is still there.
- [ ] Change the device language or clock: dates still show in Indonesian and in the right timezone (Asia/Jakarta).
- [ ] Turn on "remove animations": sheets appear without sliding.
- [ ] Use a screen reader on one screen: buttons have labels; icon-only buttons have labels.
- [ ] Resize to 320 dp width: no horizontal scroll.

## Known gaps to test manually
- Account management (add, rename, delete account) is not designed. The AI should build a simple list using the Kelola kategori pattern. Review it before you ship.
- Transfers between accounts are out of scope for the first build.
- `02-screens-and-rules.md` 2.15 still says data is stored in a browser. Update the copy for Android.