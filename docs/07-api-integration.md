# 7. API Integration Specifications

Purpose: describe how the app exchanges data with anything outside itself.

Verified against branch `feature/improve-design-2.0`, HEAD `8c7dc3a`.

---

## 7.1 There is no backend

Stated plainly, because the usual contents of this document do not apply:

| Item | Status |
|---|---|
| REST / GraphQL / gRPC base URL | **None.** No HTTP client dependency exists |
| `INTERNET` permission | **Not declared** in `AndroidManifest.xml` |
| Authentication headers, tokens, refresh | **None.** No login exists |
| Swagger / OpenAPI | **None** |
| Custom error codes from a server | **None** |
| Webhooks, callbacks | **None** |
| Third-party SDKs making network calls | **None** |

There is no `okhttp`, `retrofit`, `ktor`, `volley` or `HttpURLConnection` call
anywhere in `app/src/main/java`. The app cannot reach the network even by
accident, and `AGENTS.md` explicitly forbids adding a network layer.

---

## 7.2 The only external interface: backup files

What the app *does* integrate with is the **file system**, in both directions,
via the Storage Access Framework. This is the app's entire integration surface,
and it is the piece a future engineer is most likely to need to extend.

```
        ┌─────────────────────────── APP (Cadangan screen) ───────────────────────────┐
        │                                                                            │
  WRITE │  Export CSV ──┐                                                            │
        │              ├─► BackupRepository.buildBackup()                            │
        │  Export JSON ┘     · TransactionRepository.getAllTransactionsOnce()        │
        │                   · CategoryRepository.getByTypeOnce("pemasukan"/"…")      │
        │                     filtered to !isDefault                                 │
        │                   · AccountDao.getAllOnce()                                │
        │                   · BudgetRepository.getAllBudgetsOnce()                   │
        │                          │                                                 │
        │                          ▼                                                 │
        │                    BackupRepository.toJson() / toCsv()                     │
        │                          │                                                 │
        │                          ▼                                                 │
        │        writeFile(context, name, content, mimeType)                         │
        │                          │                                                 │
        │                          ▼                                                 │
        │   getExternalFilesDir(null)/saku-cadangan.csv        ← on-device only      │
        │   (falls back to filesDir/ if external is unavailable)                     │
        └────────────────────────────────────────────────────────────────────────────┘

  READ  ┌─────────────────────────── APP ───────────────────────────────────────────┐
        │  SAF OpenDocument picker  ──►  Uri                                        │
        │                                   │                                        │
        │                                   ▼                                        │
        │                    BackupRepository.readText(context, uri)                 │
        │                        contentResolver.openInputStream(uri)                │
        │                        BufferedReader(InputStreamReader(UTF-8)).readText()│
        │                                   │                                        │
        │                                   ▼                                        │
        │                    BackupRepository.parseJson(raw)                          │
        │                          │ valid  ──► duplicate detection ──► insert       │
        │                          │                                                 │
        │                          └── invalid ──► "File tidak valid."              │
        └────────────────────────────────────────────────────────────────────────────┘
```

---

## 7.3 JSON export format

Produced by `BackupRepository.toJson()`, `BACKUP_VERSION = 1`, indented two
spaces (`toString(2)`).

```jsonc
{
  "version": 1,
  "transactions": [
    {
      "type": "pemasukan",              // "pemasukan" | "pengeluaran"
      "amount": 5250000,                // whole rupiah, Long
      "categoryId": "inc_salary",       // stable category ID, not a display name
      "accountId": "acc_tunai",         // stable account ID
      "dateEpochMillis": 1758092400000, // midnight Asia/Jakarta of the chosen day
      "note": "Gaji Agustus"             // null when absent (JSONObject.NULL)
    }
  ],
  "categories": [                       // custom categories ONLY (!isDefault)
    {
      "id": "cus_peng_1758092400000",
      "name": "Olahraga Panahan",       // from customName
      "type": "pengeluaran",
      "color": "#5FD3E6",
      "hidden": false
    }
  ],
  "budgets": [
    {
      "categoryId": "exp_daily_food",
      "limit": 500000,
      "alertAt80": true
    }
  ],
  "accounts": [
    { "id": "acc_tunai", "name": "Tunai", "sortOrder": 0 },
    { "id": "acc_bca",   "name": "BCA",   "sortOrder": 1 },
    { "id": "acc_gopay", "name": "GoPay", "sortOrder": 2 }
  ]
}
```

### Field reference

| Path | Type | Required on import | Notes |
|---|---|---|---|
| `version` | int | Read but not validated | `BACKUP_VERSION = 1` |
| `transactions[].type` | string | optional, defaults `"pengeluaran"` | Indonesian storage values |
| `transactions[].amount` | number | optional, defaults `0` | |
| `transactions[].categoryId` | string | **required** (`optString` → `""` if missing) | An empty value produces an orphan row — there is no FK constraint |
| `transactions[].accountId` | string | optional, defaults `"acc_tunai"` | |
| `transactions[].dateEpochMillis` | number | optional, defaults `0` | |
| `transactions[].note` | string \| null | optional | `null` preserved as `null` |
| `transactions[].createdAt` | number | **not exported**; read with default `0` | See §7.7 |
| `categories[].id` | string | optional | |
| `categories[].name` | string | optional | |
| `categories[].type` | string | optional, defaults `"pengeluaran"` | |
| `categories[].color` | string | optional | Stored in `colorHex` |
| `categories[].hidden` | boolean | optional, defaults `false` | |
| `budgets[].categoryId` | string | optional | |
| `budgets[].limit` | number | optional, defaults `0` | |
| `budgets[].alertAt80` | boolean | optional, defaults `true` | |
| `accounts[]` | array | **exported but discarded on import** | See §7.7 |

Parsing uses `optString` / `optLong` / `optBoolean` throughout, so a missing
field never throws — it silently takes a default. A missing `transactions` or
`categories` array becomes an empty list.

---

## 7.4 CSV export format

Produced by `BackupRepository.toCsv()`.

- **Encoding:** UTF-8 **with a UTF-8 BOM** (`﻿`, written as the first
  character in `toCsv()`) so Excel opens it correctly
- **Header row:** `date,type,amount,category,account,note`
- **Sort:** transactions sorted by `dateEpochMillis` ascending
- **Row terminator:** `\n`
- **Date format:** `yyyy-MM-dd`, computed in **Asia/Jakarta** via
  `Calendar.getInstance(JAKARTA)`
- **Type values:** `income` / `expense` — note these are **English**, unlike the
  JSON format which uses `pemasukan` / `pengeluaran`
- **Escaping:** values containing `,` or `"` are wrapped in double quotes, with
  inner quotes doubled (`"` → `""`)
- **Categories/accounts** are written as **IDs**, not names

```csv
date,type,amount,category,account,note
2026-09-01,income,5250000,inc_salary,acc_tunai,Gaji Agustus
2026-09-02,expense,35000,exp_daily_food,acc_gopay,"Kopi, susu"
```

| Column | Type | Format |
|---|---|---|
| `date` | string | `yyyy-MM-dd` in Asia/Jakarta |
| `type` | string | `income` or `expense` |
| `amount` | number | Whole rupiah, no separators |
| `category` | string | Category ID |
| `account` | string | Account ID |
| `note` | string | Empty when absent; escaped if it contains `,` or `"` |

**CSV is export-only.** There is no CSV importer, despite the error message
telling users to "Pilih file CSV atau JSON dari Saku." `UI_SPEC.md` §8 records
this as a known gap, and `UI_SPEC.md` §7 lists CSV import as out of scope.
Treat the error string as inaccurate rather than as a specification.

---

## 7.5 Import behaviour

```
User taps "Impor dari cadangan"
  └─ SAF OpenDocument picker ──► Uri (or null if cancelled)
      └─ readText(context, uri)              ← UTF-8, whole file into memory
          └─ import(rawText)
              ├─ guard: already importing → ignore
              ├─ parseJson(rawText) via runCatching
              │     └── null → importError = true
              │                 → "File tidak valid. Pilih file CSV atau JSON dari Saku."
              ├─ build the duplicate-key set from all existing transactions
              ├─ partition the parsed rows into fresh / skipped
              └─ runCatching { transactionRepository.insertTransactions(fresh) }
                    ├─ success → ImportResult(added, skipped)
                    │            → "%1$d transaksi ditambahkan, %2$d dilewati karena sudah ada."
                    │            → refresh the transaction count
                    └─ failure → importError = true
```

### Duplicate detection

A transaction is a duplicate when **all six** of these match exactly:

```
dateEpochMillis | type | amount | categoryId | accountId | note
```

Built as `listOf(...).joinToString("|")` and compared as a `Set<String>`.
Notes use `orEmpty()`, so `null` and `""` are the same. `createdAt` is **not**
part of the key.

### Guarantees

| Guarantee | Implementation |
|---|---|
| Import never deletes | Only `insertTransactions()` is called — no delete, no update |
| Import never overwrites | Fresh rows are inserted; existing rows are untouched |
| Import is all-or-nothing | The bulk insert runs inside `withTransaction` |
| Malformed input is safe | `runCatching` around both parse and insert |

---

## 7.6 Error model

There are no server error codes. Errors are UI-level booleans mapped to
Indonesian strings.

| Condition | State | User-facing string | Resource |
|---|---|---|---|
| Unparseable / non-JSON file | `importError = true` | "File tidak valid. Pilih file CSV atau JSON dari Saku." | `cadangan_import_error` |
| Insert failure | `importError = true` | Same as above | `cadangan_import_error` |
| Import succeeded | `importResult = ImportResult(added, skipped)` | "%1$d transaksi ditambahkan, %2$d dilewati karena sudah ada." | `cadangan_import_result` |
| Import in progress | `importing = true` | "Mengimpor…" | `cadangan_importing` |
| Export written | `toast` = file path | "Cadangan tersimpan di %1$s" | `cadangan_export_saved` |
| Save failed on Tambah | `TambahEvent.SaveFailed` | "Gagal menyimpan. Coba lagi." | `toast_gagal_menyimpan` |

State is cleared explicitly: `clearImportResult()` resets `importResult` and
`importError`; `clearToast()` clears `toast`.

The error message is imprecise — it names CSV as an acceptable choice when the
importer cannot read CSV. If you fix the message, update `strings.xml` and
`UI_SPEC.md` together.

---

## 7.7 Known defects in this interface

These are real and worth knowing before you rely on export/import as a backup
strategy.

| # | Issue | Location | Consequence |
|---|---|---|---|
| 1 | **Accounts are discarded on import.** `BackupData(transactions, categories, emptyList(), budgets)` returns an empty account list regardless of file contents | `BackupRepository.kt:179` | An imported JSON's accounts never reach the database. Transactions fall back to whatever `accountId` they carry |
| 2 | **`createdAt` is not exported.** Written nowhere in `toJson()`; read with `optLong("createdAt", 0L)` | `BackupRepository.kt` | Re-imported transactions get `createdAt = 0` and lose intra-day ordering; they sort last within their day |
| 3 | **Only custom categories are exported** — `buildBackup()` filters `!it.isDefault` | `BackupRepository.kt:43` | A renamed *default* category exports without its rename; custom categories are exported but **not re-imported** — `parseJson` parses them and then drops them |
| 4 | **CSV import does not exist** | — | The CSV error message names a format that cannot be read |
| 5 | **No `FileProvider`, no share sheet.** `writeFile()` targets `getExternalFilesDir(null)` | `BackupRepository.kt:120` | Android 11+ hides this directory from the file picker. **A user cannot practically retrieve their own backup** |
| 6 | **Duplicate detection is value-based, not identity-based.** The `id` is never in the key | `CadanganViewModel.kt:116` | Two genuinely distinct transactions with identical date/type/amount/category/account/note are treated as one; the second is skipped |
| 7 | **Whole file is read into memory** | `BackupRepository.readText` | Fine for personal volumes; would OOM on a large export |

⚠️ Given #5 especially, **the backup feature currently does not deliver the
durability it promises.** `UI_SPEC.md` §8 and `UI_SPEC.md` §6.7 both warn the
user that data lives only on this device — and the documented escape hatch is
partly broken.

---

## 7.8 If a backend is ever needed

Not recommended — `AGENTS.md` forbids a network layer and `UI_SPEC.md` §7 puts
cloud sync out of scope. If a future requirement forces it, the constraints are:

1. **Amend `UI_SPEC.md` first.** Features outside the spec do not get built.
2. **Add the `INTERNET` permission**, which the app currently does not declare.
   Treat this as a deliberate, reviewable change.
3. **Manual DI still applies.** Add the client to `AppContainer`; do not
   introduce Hilt or Koin.
4. **Do not touch `Clock`.** Server time must be handled at the repository
   boundary, not by replacing the injected clock — `AGENTS.md` requires the
   clock stay injectable for testability.
5. **Category IDs are a wire contract.** They are already persisted and appear
   in backup files; the server must speak the same IDs.
6. **No auth tokens in this codebase yet.** Adding them means a real token
   lifecycle, which `TESTABILITY.md`'s fake-clock hook will eventually need to
   control.
7. **Offline-first must be preserved.** The app is local-only by product
   definition; sync would be an addition, not a replacement.

---

## See also

- [06-functional-spec.md](06-functional-spec.md) — the business rules behind these formats
- [04-credentials-access-register.md](04-credentials-access-register.md) — why no API keys exist
- [01-architecture-blueprint.md](01-architecture-blueprint.md) — `BackupRepository` in context