# TESTABILITY

The contract between the app and the automation suite. You own this file; the AI implements it exactly and never changes it. Everything here is DRAFT until you approve it.

## Locator rule
Never locate by visible text. Locate only by test tag (accessibility ID).

## Tag naming
`<screen>_<type>_<name>`, lower snake case, all defined in one file: `TestTags.kt`. Never generated, renamed or invented by the AI.

Screen prefixes: `nav`, `add`, `list`, `summary`, `dialog`.

## Current Test Tags Implementation

### Navigation
- `nav_tab_add`, `nav_tab_list`, `nav_tab_summary`

### Tambah
- `tambah_toggle_type_pemasukan`, `tambah_toggle_type_pengeluaran`
- `tambah_input_amount`, `tambah_input_note`
- `tambah_button_category`, `tambah_button_date`, `tambah_button_save`
- `tambah_error_amount`, `tambah_error_category`
- `tambah_category_item_<categoryId>` (picker rows, for example `tambah_category_item_inc_salary`)
- `tambah_selected_category`

### Daftar
- `daftar_button_month_prev`, `daftar_button_month_next`, `daftar_text_month`
- `daftar_item_transaction_<transactionId>`
- `daftar_text_amount_<transactionId>`, `daftar_text_category_<transactionId>`, `daftar_text_date_<transactionId>`
- `daftar_button_delete_<transactionId>`
- `daftar_text_excluded_<transactionId>` (the "Tidak dihitung" label)
- `daftar_text_empty`

### Delete dialog
- `dialog_button_delete_confirm`, `dialog_button_delete_cancel`

### Ringkasan
- `ringkasan_button_month_prev`, `ringkasan_button_month_next`, `ringkasan_text_month`
- `ringkasan_tile_income`, `ringkasan_tile_expense`, `ringkasan_tile_balance`, `ringkasan_tile_count`
- `ringkasan_item_group_<groupId>`
- `ringkasan_text_empty`

All tags follow the format `<screen>_<type>_<name>` and are defined in `TestTags.kt`. Tags for amounts expose raw values for easy assertion.

## Verified on
- Android: __ (Stage 0)
- iOS: __ (Stage 0: do Compose test tags surface as accessibility IDs?)

## Debug tooling (debug builds only, absent from release)

| Need | Mechanism |
|---|---|
| Seed data | Intent extra `test_seed` = seed set name |
| Reset state | `adb shell pm clear <package>` |
| Fake clock | Intent extra `test_now` = ISO local date-time, for example `2026-09-15T10:00` |

Extra names are proposals; confirm or change them and keep this table the single source of truth.

## Seed sets (named, versioned)
- `seed_empty`
- `seed_month_mixed`: several income and expense items across one month
- `seed_excluded_categories`: includes Transfer Masuk, Investasi (expense), Tabungan
- `seed_month_boundaries`: items on the 1st and last day of a month, and on adjacent days
- `seed_same_day_ordering`: several items on one date with different created times

## Rules the suite must respect
See UI_SPEC.md section 2 (amount limits, dates, totals exclusion, sort order, month filter, category reset, delete confirmation). Categories are referenced by ID from CATEGORIES.md.