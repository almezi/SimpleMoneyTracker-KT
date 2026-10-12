package id.almezi.simplemoneytracker_kt.data.backup

import id.almezi.simplemoneytracker_kt.data.Account
import id.almezi.simplemoneytracker_kt.data.AccountSeed
import id.almezi.simplemoneytracker_kt.data.CategoryRepository
import id.almezi.simplemoneytracker_kt.data.Transaction
import id.almezi.simplemoneytracker_kt.data.TransactionRepository
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json

/**
 * Reads the database into [BackupData] and renders it as JSON or CSV.
 *
 * **Pure shared code, no platform calls.** Every date is rendered through
 * [backupDateString], which converts in `Asia/Jakarta` explicitly. The original
 * Android implementation used `SimpleDateFormat` with a `Calendar` pinned to that
 * zone; both are JVM-only. Getting this wrong by one day would silently corrupt
 * the date of every exported transaction, which is why the zone is applied
 * explicitly rather than inherited from the device.
 */
class BackupSerializer(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountDao: id.almezi.simplemoneytracker_kt.data.AccountDao,
    private val budgetRepository: id.almezi.simplemoneytracker_kt.data.BudgetRepository,
) {

    /**
     * `prettyPrintIndent = "  "` reproduces the two-space indent that
     * `org.json`'s `toString(2)` produced, so backups taken before the
     * multiplatform move stay byte-comparable with ones taken after.
     *
     * `explicitNulls` is left at its default of `true` because the old format
     * wrote `JSONObject.NULL` for a null note rather than omitting the key.
     */
    private val json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    suspend fun build(): BackupData = BackupData(
        transactions = transactionRepository.getAllTransactionsOnce().map { it.toBackup() },
        categories = categoryRepository.getByTypeOnce("pemasukan")
            .plus(categoryRepository.getByTypeOnce("pengeluaran"))
            .filter { !it.isDefault }
            .map {
                BackupCategory(
                    id = it.id,
                    name = it.customName.orEmpty(),
                    type = it.type,
                    colorHex = it.colorHex.orEmpty(),
                    hidden = it.isHidden,
                )
            },
        accounts = accountDao.getAllOnce().map { it.toBackup() },
        budgets = budgetRepository.getAllBudgetsOnce().map {
            BackupBudget(
                categoryId = it.categoryId,
                limit = it.limitAmount,
                alertAt80 = it.alertAt80,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        },
    )

    fun toJson(backup: BackupData): String = json.encodeToString(backup)

    fun parseJson(raw: String): BackupData = json.decodeFromString(raw)

    /**
     * CSV with a UTF-8 BOM.
     *
     * The BOM is deliberate: without it Excel on Windows misreads the file as
     * the local codepage and the Indonesian text comes out as mojibake.
     */
    fun toCsv(backup: BackupData): String = buildString {
        append('﻿')
        append("date,type,amount,category,account,note\n")
        backup.transactions.sortedBy { it.dateEpochMillis }.forEach { tx ->
            append(backupDateString(tx.dateEpochMillis)).append(',')
            append(if (tx.type == "pemasukan") "income" else "expense").append(',')
            append(tx.amount).append(',')
            append(csvEscape(tx.categoryId)).append(',')
            append(csvEscape(tx.accountId)).append(',')
            append(csvEscape(tx.note.orEmpty()))
            append('\n')
        }
    }

    /**
     * `yyyy-MM-dd` in Asia/Jakarta.
     *
     * Public because the round-trip tests need to assert the same conversion the
     * exporter uses, and because a test that reimplemented it would prove nothing.
     */
    fun backupDateString(epochMillis: Long): String {
        val date = Instant.fromEpochMilliseconds(epochMillis)
            .toLocalDateTime(JAKARTA)
            .date
        return "${date.year.toString().padStart(4, '0')}-" +
            "${date.monthNumber.toString().padStart(2, '0')}-" +
            date.dayOfMonth.toString().padStart(2, '0')
    }

    /** Jakarta midnight for a `yyyy-MM-dd` string. The inverse of [backupDateString]. */
    fun parseBackupDate(text: String): Long {
        val parts = text.split("-")
        require(parts.size == 3) { "expected yyyy-MM-dd, got '$text'" }
        return LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            .atStartOfDayIn(JAKARTA)
            .toEpochMilliseconds()
    }

    private fun csvEscape(value: String): String =
        if (value.contains(',') || value.contains('"')) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }

    internal companion object {
        val JAKARTA: TimeZone = TimeZone.of("Asia/Jakarta")
    }
}

internal fun Transaction.toBackup() = BackupTransaction(
    type = type,
    amount = amount,
    categoryId = categoryId,
    accountId = accountId,
    dateEpochMillis = dateEpochMillis,
    note = note,
    createdAt = createdAt,
)

internal fun BackupTransaction.toTransaction(currentTime: Long) = Transaction(
    id = 0,
    type = type,
    amount = amount,
    categoryId = categoryId,
    accountId = accountId.orDefaultAccount(),
    dateEpochMillis = dateEpochMillis,
    note = note,
    createdAt = if (createdAt > 0L) createdAt else currentTime,
)

internal fun Account.toBackup() = BackupAccount(
    id = id,
    name = name,
    sortOrder = sortOrder,
)

/** Missing accounts fall back to the seeded default rather than failing the import. */
internal fun String.orDefaultAccount() = ifBlank { AccountSeed.DEFAULT_ACCOUNT_ID }
