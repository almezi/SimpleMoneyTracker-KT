# UI_SPEC

What the app does. Nothing about tests, tags or bugs lives here.
Companion files: CATEGORIES.md (category list), TESTABILITY.md (tags and debug hooks; the app-building AI must also read it).

## 1. Purpose and scope

A small, local-only personal money tracker (Indonesian UI). One wallet, IDR only, no login, no network, no accounts. It is a test target, so keep it small. Do not add features that are not in this file.

## 2. Product rules

- **Amount:** whole rupiah only, min 1, max 999.999.999.
- **Dates:** no future dates. Uses the device timezone. Time comes from an injectable clock, never the system clock directly.
- **Categories:** fixed list with stable IDs, no user editing (see CATEGORIES.md). Changing the transaction type resets the chosen category.
- **Totals:** categories flagged `countsInTotals = false` (Transfer Masuk, Investasi as expense, Tabungan) are shown in the list but excluded from all totals.
- **Sorting:** list is sorted by date descending, then by created time descending.
- **Month filter:** covers the 1st through the last day of the month, inclusive.
- **Delete:** always asks for confirmation first.
- **Wallet:** single wallet, IDR only.

## 3. Data model

Transaction
- `id` (unique)
- `type`: INCOME or EXPENSE
- `categoryId` (stable ID from CATEGORIES.md)
- `amount`: whole rupiah, Long
- `date`: local date
- `note`: optional text, max 100 characters [ASSUMPTION]
- `createdAt`: instant, from the injectable clock

## 4. Navigation

Three screens, reached from a bottom navigation bar: **Tambah**, **Daftar**, **Ringkasan**. App opens on Tambah. Navigation implemented with Jetpack Navigation Compose.

## 5. Design language

Bento grid tiles: 2 columns, 8dp gap, 14dp corner radius. All UI text lives in string resource files (Indonesian first). Currency is shown as `Rp` with dot thousands separators, for example `Rp1.250.000`.

## 6. Screens

### 6.1 Tambah (add / edit)

Title: "Tambah Transaksi" (edit mode: "Ubah Transaksi").

Fields, top to bottom:
1. Type toggle: "Pengeluaran" | "Pemasukan" (default Pengeluaran)
2. Amount: label "Nominal", numeric input, shown with dot separators as the user types
3. Category: label "Kategori", opens a picker (grouped for expense, flat for income). Placeholder "Pilih kategori"
4. Date: label "Tanggal", opens a date picker, defaults to today, future dates not selectable
5. Note: label "Catatan (opsional)"
6. Button: "Simpan"

Behavior:
- Switching type clears the selected category.
- "Simpan" validates all fields. On success it saves, returns to Daftar and shows the new item.
- Edit mode pre-fills all fields and saves over the existing transaction (`createdAt` unchanged).

Validation messages:
- Amount empty or below 1: "Nominal minimal Rp1"
- Amount above the max: "Nominal maksimal Rp999.999.999"
- No category: "Pilih kategori"
- Future date (if entered another way): "Tanggal tidak boleh di masa depan"

### 6.2 Daftar (list)

Title: "Daftar Transaksi".
- Month selector at the top: previous / next arrows and the month label, for example "September 2026". Defaults to the current month.
- Transaction rows: category name, note (if any), date, amount. Income is prefixed `+`, expense `-`.
- Rows for categories with `countsInTotals = false` show a small label "Tidak dihitung".
- Tap a row: opens edit (6.1).
- Delete action on each row: opens the confirmation dialog.
- Empty state: "Belum ada transaksi bulan ini".

Delete dialog:
- Title "Hapus transaksi?", message "Transaksi ini akan dihapus permanen."
- Buttons "Batal" and "Hapus". "Batal" changes nothing.

### 6.3 Ringkasan (summary)

Title: "Ringkasan". Same month selector as Daftar. Bento tiles (2 columns):
- "Pemasukan": total income of the month
- "Pengeluaran": total expense of the month
- "Selisih": income minus expense
- "Transaksi": count of transactions counted in totals [ASSUMPTION]
- Below the tiles: expense per category group, sorted by amount descending [ASSUMPTION]

Only transactions with `countsInTotals = true` are included. Empty month: all tiles show `Rp0` and the group list shows "Belum ada data".

## 7. Out of scope

Login or PIN (maybe later), multi-wallet, multi-currency, category editing, budgets, recurring transactions, export/import, charts, cloud sync, notifications.

## 8. Current Development Status

### Completed
- Data layer with Room database
- Category management with fixed list
- Tambah screen with validation and save
- Daftar screen with transaction list
- Ringkasan screen with summary
- Navigation with bottom navigation bar
- Test tags for all interactive elements

### Architecture
- MVVM: UI → ViewModel (StateFlow) → Repository → DB
- Manual DI: No Hilt, no Koin
- Injectable clock: Never call system clock directly
- Testability: All UI elements use test tags from TestTags.kt

### Next Steps
- Month selector filter for Daftar and Ringkasan screens
- Delete confirmation dialog
- Edit transaction functionality
- Theme customization