# Code Audit — whole repository

Date: 2026-10-17
Scope: whole repo (`:app`, `:composeApp`, Gradle config, tests, docs)
Method: read-only. No file was changed. Every claim verified by reading the
source and grepping the whole tree (including tests, fixtures, config, strings).
Baseline: branch `feature/add-accessbility-id`, commit `49685e0`.

Updated 2026-10-18: the original report's findings are all closed and have been
removed from this file. What is left is what the fixes did **not** reach. The
findings themselves are in the commit history, not here.

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

## Still open

These are the only items carried forward. Everything else in the report is
closed.

### A custom category name can still duplicate an unrenamed default

`CategoryDao.kt` `countWithName`, `CategoryRepository.kt` `rename`

`rename` now refuses an empty name and a name already used in the same type, but
the uniqueness query compares `customName`, which is `NULL` for every category
the user has not renamed. A default's display name exists only as a string
resource, so nothing in SQL can see it: a custom "Gaji" can still be created
next to the built-in Gaji, and both then render as "Gaji".

Closing it needs a different query (join the seeded names in, or compare on a
resolved display name), not a validation change.

### The SQL added in the last round has no test

`countsByCategory`, `expenseTotalsByCategory`, `TransactionDao.insertIfAbsent`,
`TransactionRepository.restoreImport`

`commonTest` has no Room in-memory testing, so these four statements are
covered by reading and by the Kotlin assembled around them, not by a database.
The Kotlin around them is tested (`CategoryRowsTest`, `BudgetRowsTest`,
`BackupRestoreTest`). Adding `androidx.room3:room-testing` plus an in-memory
database test would close this and is the only way to.

### TESTABILITY.md:113-114 documents a tag suffix that no longer exists

`ui/components/TransactionCard.kt`

The card's root tag is now exactly what the caller passes, so the documented
`component_card_transaction_<categoryId>` no longer appears on the root; the
`_edit_<categoryId>` and `_delete_<categoryId>` children still do.
`TESTABILITY.md` is owned by the tester and was not edited.

## Verdict

Nothing from the report is open. `./gradlew :composeApp:testAndroidHostTest`
runs 103 tests with 0 failures, `:app:assembleDebug` builds, and the iOS main
and test compilations pass. The two product rules the report found unimplemented
— excluded-category totals and no future dates — are implemented and tested, as
is the restore path that used to lose budgets and category names.

<details>
<summary>Original verdict, at the 49685e0 baseline</summary>

Not healthy. Two product rules are unimplemented (excluded-category totals, no
future dates), one rule is actively violated by the ViewModel (type switch), and
the only restore path loses budgets and category names — fix 1–4 first, then 5
and 7.

**Lean: ~-225 lines, -4 dependencies possible** (findings 15–18: the whole
`BackupRepository.kt`, the `writeText` seam, the dead state, and the four stale
catalog entries).

</details>

## Not checked

- `BUGS.md` (AGENTS.md forbids reading it, so I could not cross-check which findings are already known).
- No build, test or app was run — every finding comes from reading source.
- `app/src/main/res/values/{strings,colors}.xml`, `themes.xml` beyond one line, `iosApp/*.sh`, the Xcode project and `SakuApp.swift`, `docs/*.md`, `improve/*.md`, `third_party/*.py`, the remaining design-system components (`SakuSwitch`, `SegmentedToggle`, `SakuLogo`, `SakuLoadingScreen`, `SakuInsets`, `SakuTheme`, `SakuDimens` values) and `app/src/androidTest`.
- One thing seen but not reported for lack of a certain trigger: the Daftar undo toast is keyed on a freshly built `SakuToast` (with a fresh lambda) each recomposition, so its 5-second timer can restart instead of expiring.