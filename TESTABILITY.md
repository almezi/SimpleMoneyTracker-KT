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
- Android: YES — tags resolve as Compose semantics. Manual QA only; no instrumented tests exist.
- iOS: YES — verified 2026-10-12 via spike (see `iosApp/`).

### iOS Stage 0 result

Compose Multiplatform maps `testTag` to the native iOS
`accessibilityIdentifier`, so Appium's `accessibilityId` locator works on iOS
unchanged. Measured on Compose Multiplatform 1.12.1, iOS 27.0 simulator,
iPhone 18 Pro.

All five tag patterns resolved:

| Pattern | Example tag | Result |
|---|---|---|
| Root container | `tambah_screen` | FOUND |
| Interactive input | `tambah_input_amount` | FOUND |
| Interactive button | `tambah_button_save` | FOUND |
| Lazy list container | `daftar_list` | FOUND |
| List item | `daftar_item` | FOUND |

**The open question is closed: do test tags surface as accessibility IDs?**
Yes. The suite's locator rule needs no change for iOS.

Notes for future test work:

- A `testTag` on a root container **does** surface as an
  `accessibilityIdentifier`. The CMP-9551 concern (tag landing on a synthetic
  child instead of the container) did not reproduce in CMP 1.12.1. Screen-level
  tags are safe.
- The accessibility tree has extra wrapper levels: `OverlayInputView` ->
  synthetic `AccessibilityRoot` -> wrapper element -> your tagged elements.
  Locators must not assume a fixed depth.
- **Tags must be unique.** Repeated items rendered with the same tag produce
  several elements sharing one identifier, which makes them ambiguous to
  locate. Per-item tags must be suffixed with the item id.
- Accessibility tree population is **lazy** - CMP syncs it when accessibility
  services are active. A test that reads the tree immediately after launch may
  see an empty tree.

### ⚠️ OPEN: bottom sheets empty the accessibility tree

Reproduced on CMP 1.12.1, iOS 27.0 simulator. While a `ModalBottomSheet` is
open, walking `UIView.accessibilityElements` from inside the app returns an
`AccessibilityRoot` with **zero children**. Neither the sheet's own elements nor
the underlying screen's are exposed, even though both render correctly on
screen.

This matters: Saku puts a sheet on every delete confirmation
(`ConfirmDeleteSheet`) and on every picker (`SakuPickerSheet`,
`CategoryPickerSheet`). Any automation that must read or tap sheet contents
would find nothing.

**Not yet confirmed as a real blocker.** The observation came from a probe
inside the app process, with no accessibility consumer attached. CMP syncs the
tree lazily, and an XCUITest or Appium session attaches a consumer that
triggers sync. So this may be an artefact of the probe rather than of CMP.

**Must be re-verified with a real accessibility client before relying on it.**
See `docs/09-ios-migration-plan.md` section 4.2. Treat sheet-based flows as
unverified on iOS until then.

### Verified against the real app (Stage 1.5)

Confirmed 2026-10-12 on iPhone 18 Pro / iOS 27.0 with Saku's actual design
system and the real Transaksi screen in `commonMain`. Every tag below resolved
as an `accessibilityIdentifier`:

`daftar_screen`, `daftar_list`, `component_nav_bottom`,
`component_card_transaction_<categoryId>`,
`component_card_transaction_edit_<categoryId>`,
`component_card_transaction_delete_<categoryId>`, `avatar_<categoryId>`.

Screenshot: `docs/screenshots/saku-ios-real.png`. App: `iosApp/`.

### Also noted (not blocking)

- Compose Multiplatform does not apply safe-area insets automatically. Saku's
  screens use Android's `Modifier.statusBarsPadding()`, which is unavailable on
  iOS and needs a cross-platform replacement. The spike screenshot shows the
  title colliding with the status bar for exactly this reason.

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