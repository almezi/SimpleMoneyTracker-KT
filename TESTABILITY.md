# TESTABILITY

The contract between the app and the automation suite. You own this file; the AI implements it exactly and never changes it. Everything here is DRAFT until you approve it.

## Locator rule
Never locate by visible text. Locate only by test tag (accessibility ID).

## Tag naming
`<screen>_<type>_<name>`, lower snake case, all defined in one file: `TestTags.kt`. Never generated, renamed or invented by the AI.

Screen prefixes: `nav`, `add`, `list`, `summary`, `dialog`.

## Tag list (draft)

### Navigation
- `nav_tab_add`, `nav_tab_list`, `nav_tab_summary`

### Tambah
- `add_toggle_expense`, `add_toggle_income`
- `add_input_amount`, `add_input_note`
- `add_button_category`, `add_button_date`, `add_button_save`
- `add_text_error_amount`, `add_text_error_category`, `add_text_error_date`
- `add_item_category_<categoryId>` (picker rows, for example `add_item_category_inc_salary`)
- `add_text_selected_category`

### Daftar
- `list_button_month_prev`, `list_button_month_next`, `list_text_month`
- `list_item_transaction_<transactionId>`
- `list_text_amount_<transactionId>`, `list_text_category_<transactionId>`, `list_text_date_<transactionId>`
- `list_button_delete_<transactionId>`
- `list_text_excluded_<transactionId>` (the "Tidak dihitung" label)
- `list_text_empty`

### Delete dialog
- `dialog_button_delete_confirm`, `dialog_button_delete_cancel`

### Ringkasan
- `summary_button_month_prev`, `summary_button_month_next`, `summary_text_month`
- `summary_tile_income`, `summary_tile_expense`, `summary_tile_balance`, `summary_tile_count`
- `summary_item_group_<groupId>`
- `summary_text_empty`

Tile and amount tags must expose a machine-readable value (raw number, not only the formatted "Rp" string) so tests can assert without parsing text. Decide how (content description vs a separate tag) during the spike.

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