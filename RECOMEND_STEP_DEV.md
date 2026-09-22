# Step Recommendation from Mobile Developer

## Analysis Summary
 
The project currently has Slice 1 (Data Layer) and Slice 2 (Tambah Screen with Validation + Save) completed. The build passes with Kotlin 2.4.20, KSP 2.3.12, and AGP 9.4.1. **Phase 1 is DONE.** Slices 3 (Daftar) and 4 (Ringkasan) are not started. Navigation does not exist yet.

---

## Phase 1 — Finish Tambah Screen (Validation + Save) — ✅ DONE

| Step | Task | Status |
|------|------|--------|
| 1 | Add `lifecycle-viewmodel-compose` dependency in `app/build.gradle.kts` | ✅ Done |
| 2 | Create `TambahViewModel.kt` — holds `TambahUiState` StateFlow (type, amount, selectedCategoryId, dateMillis, note, errors, isSaved) | ✅ Done |
| 3 | Add validation logic inside ViewModel: empty/zero amount → error, no category selected → error | ✅ Done |
| 4 | Wire save: ViewModel calls `Repository.insertTransaction()` via `Clock.currentTimeMillis()` for `createdAt` | ✅ Done |
| 5 | Connect `TambahScreen` to `TambahViewModel` using `viewModel()` | ✅ Done |
| 6 | Add error UI to TambahScreen (show `TAMBAH_ERROR_AMOUNT` / `TAMBAH_ERROR_CATEGORY` Text) | ✅ Done |
| 7 | Fix typo line 26: "Tambak" → "Tambah Pengeluaran" | ✅ Done |

---

## Phase 2 — Daftar (List Screen)

| Step | Task | Why |
|------|------|-----|
| 8 | Create `DaftarViewModel.kt` — reads all transactions from `TransactionDao.getAll()` as StateFlow | MVVM pattern |
| 9 | Create `DaftarScreen.kt` — LazyColumn listing transactions, grouped by date | Core list screen per slice order |
| 10 | Add `DAFTAR_SCREEN` tag on screen root, `DAFTAR_ITEM` on each transaction row | TestTags rule: every interactive/asserted element gets a tag |
| 11 | Add tags for list item elements (date header, amount, category, note, delete button) | Testability |

---

## Phase 3 — Ringkasan (Summary Screen)

| Step | Task | Why |
|------|------|-----|
| 12 | Create `RingkasanViewModel.kt` — aggregates totals by type (pemasukan/pengeluaran), by category | MVVM pattern |
| 13 | Create `RingkasanScreen.kt` — totals for pemasukan/pengeluaran, category breakdown | Core summary screen per slice order |
| 14 | Add `RINGKASAN_SCREEN` tag on screen root and tags for summary cards | Testability |

---

## Phase 4 — Navigation (Cross-Cutting)

| Step | Task | Why |
|------|------|-----|
| 15 | Add `navigation-compose` dependency in `app/build.gradle.kts` | NavHost for multi-screen app |
| 16 | Create `NavGraph.kt` with routes: `tambah`, `daftar`, `ringkasan` | Screen routing |
| 17 | Add bottom navigation bar in `MainActivity` tying ViewModels to screens | Standard mobile navigation pattern |
| 18 | Move `TambahScreen` behind nav route, remove direct call from `MainActivity` | Currently hardcoded — not scalable |

---

## Recommended Execution Order

1. **Phase 1** (Steps 1–7): Finish Tambah — validation + save
2. **Phase 2** (Steps 8–11): Daftar list screen
3. **Phase 3** (Steps 12–14): Ringkasan summary screen
4. **Phase 4** (Steps 15–18): Navigation — can be done alongside each slice

---

## Architecture Notes

- **MVVM**: UI → ViewModel (StateFlow) → Repository → DB
- **Manual DI**: No Hilt, no Koin — dependencies injected via constructor
- **Injectable Clock**: `Clock` interface with `RealClock` impl; ViewModel receives `Clock` via Repository/AppContainer
- **No network, no login, no analytics**
- **Strings in resource files** (Indonesian first), no hard-coded UI text
- **Only tags defined in `TestTags.kt`** — never rename, remove, or invent tags
