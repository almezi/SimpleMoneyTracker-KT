# Vibe Coding Prompts for the Saku Revamp

Run these prompts in order. Send one at a time. Check the acceptance items in `04-acceptance-checklist.md` after each step before you continue.

Before you start, fill in the two placeholders in the **Project context** prompt. Paste that prompt once at the start of the session. Then send each step as a separate message.

---

## Prompt 0: Project context (send first, once)

```
I am upgrading my existing money tracker app "Saku" (Indonesian, personal finance).

Stack: Kotlin 2.4.20 + Jetpack Compose (Material3), Android-only, single Gradle module `:app`, minSdk 24, package `id.almezi.simplemoneytracker_kt`.
Storage today: Room 2.8.4 via KSP (`data/AppDatabase.kt`, `TransactionDao`, `CategoryDao`, `TransactionRepository`), local SQLite file only. No network layer, no login, no analytics.
Current screens: Tambah (income and expense form), Daftar/Transaksi (list), Ringkasan (summary), wired in `navigation/NavGraph.kt`.

Goal: restyle and extend the app to match a new design. The new design is described in three files I will give you in the next messages: the design system, the screens and rules, and a series of build steps.

Architecture rules for every step:
1. MVVM only: Compose UI -> ViewModel exposing a single immutable StateFlow of UI state -> Repository -> Room DAO. No business logic in composables.
2. Manual DI only. No Hilt, no Koin. Wire new dependencies in `data/AppContainer.kt`.
3. Never call the system clock directly. Use the injected `Clock` from `data/AppContainer.kt`.
4. Do not delete or change the user's stored data. If the schema must change, add a Room `Migration`, bump `version`, write it in `AppDatabase.kt`, and explain it before implementing.
5. Do not add dependencies to `app/build.gradle.kts` or `gradle/libs.versions.toml` without asking me first.
6. Do not add a settings screen, multi-wallet, multi-currency, recurring transactions, transfers, or cloud sync.

UI rules for every step:
7. Use the design tokens I provide as a single Compose theme object. Do not hard-code colors or sizes in composables. The spec gives px; convert them to `dp` for sizes and `sp` for text.
8. All user-visible strings go in `app/src/main/res/values/strings.xml`. No hard-coded text in Kotlin, and use the exact Indonesian copy from the spec.
9. Every interactive or asserted element gets a `testTag` from `TestTags.kt`. Tags live only in that file, follow the `<screen>_<type>_<name>` format, and are reused as-is. If a new screen needs a tag that does not exist yet, add it to `TestTags.kt` first and tell me which ones you added.
10. Do not use emoji as icons. Use the category's colored square, the two-letter avatar, or an icon from `material-icons-extended`.
11. Every money amount is a `Long` in rupiah and displays as "Rp 212.121" (dot thousand separator, no decimals, U+2212 minus sign for negatives).
12. Dates come from `kotlinx-datetime` and format in Indonesian ("9 Okt 2026") and Asia/Jakarta.
13. Design for a 390 dp wide phone first. Every tappable element is at least 48 dp tall (Compose accessibility minimum).
14. Build and verify with `./gradlew installDebug` before telling me a step is done.

Process:
15. Before you write code, list the files you will create or change. Wait for my confirmation.

Acknowledge these rules, then wait for the next message.
```

---

## Prompt 1: Design tokens

```
Step 1 of 10: Design tokens.

Add the attached tokens.css as the global design token file. Import it once at the app root.

Then update the base styles:
- Page background: var(--color-bg)
- Default text color: var(--color-text)
- Font family: DM Sans for UI text, DM Mono for amounts (load both from Google Fonts)
- Set body margin to 0
- Make every button and input use the 44 px minimum touch height

Do not change any screen yet. Show me a single test page that displays each color token and each font size as a labelled swatch so I can check them.

List the files you will change before you start.
```

---

## Prompt 2: Shared components

```
Step 2 of 10: Shared components.

Build these reusable components, following 01-design-system.md exactly:
1. PrimaryButton (height 52 px, disabled state with helper text slot)
2. SecondaryButton (height 44 px)
3. DestructiveButton (card style and sheet style)
4. SegmentedToggle with two variants: income and expense
5. AmountInput (shows "Rp 0" when empty, formats with dot separators as the user types, digits only)
6. CategoryChipGrid (3 columns, selected state uses the type color)
7. SelectField (height 48 px, chevron on the right, opens a bottom sheet list)
8. BottomSheet (drag handle, scrim, slides up 200 ms)
9. BottomNav (three items, active state, floating, 20 px from bottom)
10. MonthSwitcher (next button disabled when the month is the current month)
11. EmptyState (icon slot, title, body, primary and optional secondary action)
12. Switch (48 × 28 px)
13. Toast (bottom, above the nav, auto-dismiss 2 seconds, optional action button)

Add a formatRupiah(amount) helper: 212121 -> "Rp 212.121", -123 -> "−Rp 123" (use the minus sign U+2212).
Add a formatSavingsRate(income, expense) helper using section 4 of 02-screens-and-rules.md.

Write a test page for each component with its states. Do not connect them to the screens yet.

List the files you will create before you start.
```

---

## Prompt 3: Tambah transaksi (income and expense)

```
Step 3 of 10: Tambah screen (both Pemasukan and Pengeluaran).

Rebuild the add-transaction screen using the shared components.

Requirements:
- Use one screen with a SegmentedToggle. Switching type changes the amount color, the category list, the primary button label, and the header label.
  - Pemasukan: header "Tambah transaksi", button "Simpan transaksi", categories Gaji, Freelance, Bonus & THR, Investasi, Hadiah, Lainnya.
  - Pengeluaran: header label "TAMBAH PENGELUARAN" in expense color, button "Simpan Pengeluaran" in the expense button color, categories Makan & Minum, Transportasi, Belanja, Hiburan, Kesehatan, Tagihan, Lainnya.
- Akun (SelectField) and Tanggal (date, format "9 Okt 2026") share one row, each half the width.
- Catatan is optional, max 60 characters.
- Primary button is disabled until amount > 0 and a category is selected. The helper text above the button says what is missing.
- Save writes the transaction to the existing storage with the new data model in 02-screens-and-rules.md section 10. Keep the old transactions readable.
- Switching type keeps the amount and note, and clears the category if it is not valid for the new type.
- Show a toast "Transaksi disimpan" after saving, then clear the amount, category, and note. Keep the account and date.
- Include the "Kelola kategori" link next to the Kategori label on the expense variant. It can go to a placeholder route for now.
- Bottom nav is visible. Reserve 84 px at the bottom so the save button is never under the nav.

Account list: for now, use "Tunai", "BCA", and "GoPay" as seed data. Store them as accounts.

Test: save an income and an expense, then confirm the stored data and the old transactions still load.

List the files you will change before you start.
```

---

## Prompt 4: Transaksi list, edit, and delete

```
Step 4 of 10: Transaksi list.

Build the Transaksi screen.

Requirements:
- Header: MonthSwitcher. The next button is disabled when viewing the current month.
- Summary strip: "Masuk" (income total, green) and "Keluar" (expense total, expense color), side by side.
- Button "Cadangan & ekspor data" below the summary strip. For now it can route to a placeholder.
- Transactions are grouped by day. Day header shows the day name and date on the left, the day's net total in mono on the right. Newest day first.
- Each transaction is a card with the icon square, name, type label, amount, then two buttons: "Ubah" and "Hapus".
- "Ubah" opens the edit screen (Prompt 5).
- "Hapus" opens a BottomSheet: title "Hapus transaksi?", body "[name] sebesar [amount] pada [date] akan dihapus permanen. Tindakan ini tidak bisa dibatalkan.", buttons "Batal" and "Ya, hapus" (destructive). Never delete on the first tap.
- After delete, show a toast "Transaksi dihapus" with the action "Urungkan" for 5 seconds. Undo restores the transaction.
- If the month has no transactions, show the EmptyState: "Belum ada transaksi bulan ini", body "Catat pemasukan atau pengeluaran pertamamu. Semua tersimpan di perangkat ini.", primary button "Tambah transaksi", secondary button "Impor dari cadangan". Hide the day list.
- Bottom nav is visible.

Test: add three transactions across two days, delete one, undo it, then switch to a month with no data.

List the files you will change before you start.
```

---

## Prompt 5: Ubah transaksi

```
Step 5 of 10: Edit transaction screen.

Build the edit screen for an existing transaction. Reuse the Tambah form from Step 3 with these differences:
- Header: back button and title "Ubah transaksi". No bottom nav.
- All fields are pre-filled from the saved transaction. The type toggle shows the saved type.
- Button "Simpan perubahan" is enabled only when something changed. Save updates the stored transaction and returns to Transaksi.
- Text button "Hapus transaksi" (danger text, 44 px tall) at the bottom. Opens the same delete sheet as Step 4.

Test: edit the amount of an expense, save, confirm the list and the summary update. Edit the type from expense to income and confirm the category list changes.

List the files you will change before you start.
```

---

## Prompt 6: Ringkasan (summary)

```
Step 6 of 10: Ringkasan screen.

Build the monthly summary with the rules in 02-screens-and-rules.md section 4.

Requirements:
- MonthSwitcher at the top.
- Balance card: "SALDO BERSIH", balance in DM Mono 34 px. Green if zero or positive, expense color if negative. Two sub-tiles: Pemasukan (green tint) and Pengeluaran (expense tint).
- Savings rate row: label "Tingkat tabungan", value with one decimal and a comma. If income is 0, show "—" with the hint text. Use formatSavingsRate from Step 2.
- Two buttons side by side under the savings row: "Anggaran" (goes to Step 8 screen) and "Kelola kategori" (goes to Step 9 screen). Placeholder routes are fine until then.
- Breakdown cards, in this order:
  1. "Pemasukan vs pengeluaran": two bars, amounts shown.
  2. "Pemasukan per kategori": one row per income category with amount and share, sorted high to low.
  3. "Pengeluaran per kategori": one row per expense category with amount and share, sorted high to low.
- Include the income breakdown. A summary with only expenses is a bug in the old app that this step fixes.
- If there are no transactions in the month, show the empty summary: balance Rp 0 in muted color, hide the savings row, the buttons, and the breakdown cards, and show the EmptyState "Belum ada data untuk diringkas" with the body "Ringkasan akan muncul setelah kamu mencatat transaksi pertama bulan ini." and button "Tambah transaksi".
- Bottom nav visible.

Test: add income only, expense only, and both. Check that the income breakdown appears when there is only income. Check the savings rate with zero income shows "—".

List the files you will change before you start.
```

---

## Prompt 7: Anggaran (budget list)

```
Step 7 of 10: Anggaran screen and budget data.

Add a Budget model (see section 10 of 02-screens-and-rules.md). Store it alongside the existing data.

Build the Anggaran screen:
- Header: back button, title "Anggaran", month label.
- Total card: total spent of total budget in DM Mono, progress bar. Hide when there are no budgets.
- Budget list: one BudgetRow per budget, sorted by percentage used, highest first. Use the status rules in 01-design-system 5.16.
- Each BudgetRow has "Ubah" and "Hapus" buttons. Hapus uses a BottomSheet (title "Hapus anggaran [kategori]?", body from 02-screens-and-rules.md section 2.11).
- Deleting a budget does not change any transaction.
- Bottom button "Atur anggaran baru" (not the same as Ubah).
- If there are no budgets, show the EmptyState "Belum ada anggaran" with the button "Atur anggaran baru".
- Bottom nav is hidden on this screen.

Test: create two budgets, one below 75% used and one over 100%. Check the statuses and colors. Delete one and check that the transactions are still there.

List the files you will change before you start.
```

---

## Prompt 8: Anggaran baru and Ubah anggaran

```
Step 8 of 10: Budget form.

Build one form screen used for both creating and editing a budget.
- Title: "Anggaran baru" when creating, "Ubah anggaran" when editing.
- Kategori: SelectField. Lists expense categories without a budget yet. In edit mode, the category cannot change.
- Batas bulanan: AmountInput with Rp 0 default. Must be greater than 0.
- The helper text under the limit depends on the mode (see 02-screens-and-rules.md section 2.10).
- Switch "Ingatkan saat 80% terpakai", on by default. When on, show a toast or notification when spent reaches 80% of the limit.
- Primary button "Simpan anggaran", disabled until category and limit are set.
- In edit mode only: text button "Hapus anggaran" (danger). Opens the same delete sheet as Step 7.
- Bottom nav hidden.

Test: create a budget, edit its limit, try to create a second budget for the same category (it must be blocked), and delete from the edit screen.

List the files you will change before you start.
```

---

## Prompt 9: Kelola kategori

```
Step 9 of 10: Categories.

Build the category management screen and the category data rules (section 5 of 02-screens-and-rules.md).

Requirements:
- Header: back button, title "Kelola kategori".
- Segmented toggle: Pengeluaran / Pemasukan. Changes the list.
- Each row shows the color square, name, and a sub-line: "Bawaan" or "Buatan sendiri", plus usage count.
- Default categories: "Ubah" (rename) and "Sembunyikan".
- Custom categories: "Ubah", "Sembunyikan", and "Hapus" (danger). The three buttons sit in a grid below the name, each the same width.
- Hidden categories are shown dimmed with "Tampilkan".
- "Hapus" on a custom category opens a BottomSheet: "Hapus kategori [nama]?". If the category is used, the body says how many transactions will move to "Lainnya". If unused, the body says it is not used.
- Deleting a custom category moves its transactions to "Lainnya" of the same type and removes its budget. Do this in one transaction so nothing is left half-done.
- Default categories cannot be deleted. Do not show a delete button for them.
- Button "Tambah kategori" at the bottom, opens the Tambah kategori screen.

Tambah kategori screen:
- Type toggle (default Pengeluaran), name field (1–20 characters, unique within the type, case-insensitive), 6 color swatches (one selected, default purple), live preview chip.
- Button "Simpan kategori", disabled until the name is valid.
- Bottom nav hidden.

Test: create a custom category, use it in a transaction, then delete it and check the transaction moved to "Lainnya". Try a duplicate name and check it is blocked.

List the files you will change before you start.
```

---

## Prompt 10: Cadangan (backup) and final polish

```
Step 10 of 10: Backup and final polish.

Build the Cadangan data screen (section 2.15 and section 7 of 02-screens-and-rules.md).

Requirements:
- Status card: transactions stored on this device, last backup date or "belum pernah".
- Export CSV and Export JSON buttons. Use the formats in section 7. CSV must be UTF-8 with BOM.
- Import section: a file picker for .csv or .json. Import JSON in the Saku format. Skip duplicates (exact match on date, type, amount, category, account, note). Show the result: "12 transaksi ditambahkan, 3 dilewati karena sudah ada."
- Import never deletes existing data.
- Import error: "File tidak valid. Pilih file CSV atau JSON dari Saku." below the import area.
- Warning card at the bottom: "Data hanya tersimpan di browser atau perangkat ini. Jika data dihapus atau ganti perangkat, buat cadangan dulu."
- Link this screen from the "Cadangan & ekspor data" button on Transaksi.

Final polish across all screens:
- Check every screen at 390 px width. Nothing overlaps the bottom nav. Nothing is wider than the screen.
- Check every tappable element is at least 44 px tall.
- Check every amount uses DM Mono and the Rp format.
- Remove any emoji from the UI.
- Check the empty states on Transaksi, Ringkasan, and Anggaran.

Test: export JSON, delete all transactions in a test copy, import the file, and confirm everything comes back. Import the same file again and confirm all rows are skipped as duplicates.

List the files you will change before you start.
```

---

## If something goes wrong

- **The AI changes screens you did not ask for:** reply "Only change [file]. Revert the rest." and repeat the step.
- **The AI invents copy or colors:** reply "Use only the tokens and copy in the spec. Show me where you used a value that is not in the spec."
- **Layout overlaps the bottom nav:** reply "Add 84 px bottom padding to the screen container, and make the content area scroll inside it."
- **Old data disappears:** stop. Reply "Stop. Do not change the storage format. Show me how the old data is read first." Restore from your backup before continuing.
