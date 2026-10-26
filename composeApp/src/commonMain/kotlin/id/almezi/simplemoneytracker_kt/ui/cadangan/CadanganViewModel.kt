package id.almezi.simplemoneytracker_kt.ui.cadangan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.almezi.simplemoneytracker_kt.SakuStrings
import id.almezi.simplemoneytracker_kt.data.CategoryRepository
import id.almezi.simplemoneytracker_kt.data.Clock
import id.almezi.simplemoneytracker_kt.format
import id.almezi.simplemoneytracker_kt.data.Transaction
import id.almezi.simplemoneytracker_kt.data.TransactionRepository
import id.almezi.simplemoneytracker_kt.data.backup.BackupPreferences
import id.almezi.simplemoneytracker_kt.data.backup.BackupSerializer
import id.almezi.simplemoneytracker_kt.data.backup.KEY_LAST_BACKUP
import id.almezi.simplemoneytracker_kt.data.backup.toAccount
import id.almezi.simplemoneytracker_kt.data.backup.toBudget
import id.almezi.simplemoneytracker_kt.data.backup.toCategory
import id.almezi.simplemoneytracker_kt.data.backup.toTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ImportResult(
    val added: Int,
    val skipped: Int
)

data class CadanganUiState(
    val transactionCount: Int = 0,
    val accountCount: Int = 0,
    val categoryCount: Int = 0,
    val lastBackupMillis: Long? = null,
    val importing: Boolean = false,
    val importResult: ImportResult? = null,
    val importError: Boolean = false,
    val toast: String? = null
)

/**
 * The Cadangan (backup) screen's state.
 *
 * Moved out of the Android-only `:app` module by docs/09 Stage 5. The only
 * platform-shaped dependency left is [BackupPreferences]; the file picker and
 * the share sheet are supplied by the screen through the `FilePicker` /
 * `BackupFileStore` seams.
 */
class CadanganViewModel(
    private val serializer: BackupSerializer,
    private val preferences: BackupPreferences,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(CadanganUiState())
    val uiState: StateFlow<CadanganUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(lastBackupMillis = readLastBackup()) }
        refreshCounts()
    }

    private fun readLastBackup(): Long? =
        preferences.getLong(KEY_LAST_BACKUP).takeIf { it > 0L }

    fun todayMillis(): Long = clock.currentTimeMillis()

    /** @param path the displayable location the picker/store reported. */
    fun markBackupDone(path: String) {
        val now = clock.currentTimeMillis()
        preferences.putLong(KEY_LAST_BACKUP, now)
        _uiState.update { it.copy(lastBackupMillis = now, toast = SakuStrings.cadangan_export_saved.format(path)) }
    }

    /** Export threw before the share sheet could be presented. */
    fun notifyExportFailed() =
        _uiState.update { it.copy(toast = SakuStrings.cadangan_export_error) }

    fun clearToast() = _uiState.update { it.copy(toast = null) }

    fun clearImportResult() = _uiState.update { it.copy(importResult = null, importError = false) }

    private fun refreshCounts() {
        viewModelScope.launch {
            val accounts = transactionRepository.getAccounts().first()
            val categories = categoryRepository.getAllOnce()
            _uiState.update {
                it.copy(
                    transactionCount = transactionRepository.countTransactions(),
                    accountCount = accounts.size,
                    categoryCount = categories.size,
                )
            }
        }
    }

    suspend fun buildJson(): String = serializer.toJson(serializer.build())

    suspend fun buildCsv(): String = serializer.toCsv(serializer.build())

    fun import(rawText: String) {
        if (_uiState.value.importing) return
        _uiState.update { it.copy(importing = true, importError = false, importResult = null) }
        viewModelScope.launch {
            val parsed = runCatching { serializer.parseJson(rawText) }.getOrNull()
            if (parsed == null) {
                _uiState.update {
                    it.copy(
                        importing = false,
                        importError = true,
                        toast = SakuStrings.cadangan_import_error,
                    )
                }
                return@launch
            }
            val existing = transactionRepository.getAllTransactionsOnce()
            val currentTime = clock.currentTimeMillis()
            val (fresh, skipped) = splitOnDuplicates(
                parsed.transactions,
                existing.map { duplicateKey(it) }.toSet(),
                currentTime,
            )
            // The file carries four lists. Importing only the transactions left
            // the custom category names and budgets behind on restore.
            runCatching {
                transactionRepository.restoreImport(
                    categories = parsed.categories.map { it.toCategory() },
                    accounts = parsed.accounts.map { it.toAccount() },
                    budgets = parsed.budgets.map { it.toBudget(currentTime) },
                    transactions = fresh
                )
            }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            importing = false,
                            importResult = ImportResult(fresh.size, skipped),
                            toast = SakuStrings.cadangan_import_result.format(fresh.size, skipped),
                        )
                    }
                    refreshCounts()
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            importing = false,
                            importError = true,
                            toast = SakuStrings.cadangan_import_error,
                        )
                    }
                }
        }
    }

    /**
     * Date, type, amount, category, account and note all matching. Mirrors
     * `UI_SPEC.md` section 6.7; docs/09 item 5.5 requires this to behave
     * identically on both platforms, which it now does because there is exactly
     * one implementation.
     */
    private fun duplicateKey(tx: Transaction): String = duplicateKeyOf(tx)
}

/**
 * Splits a backup's transactions into the ones to insert and the count to skip.
 *
 * A row repeats when it matches a stored row *or* an earlier row of the same
 * file: checking only the database let a hand-merged file insert every copy.
 *
 * @return the fresh transactions and how many were skipped.
 */
internal fun splitOnDuplicates(
    backupTransactions: List<id.almezi.simplemoneytracker_kt.data.backup.BackupTransaction>,
    existingKeys: Set<String>,
    currentTime: Long
): Pair<List<Transaction>, Int> {
    val seen = HashSet<String>(existingKeys)
    val fresh = ArrayList<Transaction>()
    var skipped = 0
    backupTransactions.forEach { backupTx ->
        val tx = backupTx.toTransaction(currentTime)
        if (seen.add(duplicateKeyOf(tx))) fresh.add(tx) else skipped++
    }
    return fresh to skipped
}

internal fun duplicateKeyOf(tx: Transaction): String = listOf(
    tx.dateEpochMillis,
    tx.type,
    tx.amount,
    tx.categoryId,
    tx.accountId,
    tx.note.orEmpty()
).joinToString("|")