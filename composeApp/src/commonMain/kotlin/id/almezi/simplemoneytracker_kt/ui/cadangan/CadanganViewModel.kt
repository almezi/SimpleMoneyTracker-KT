package id.almezi.simplemoneytracker_kt.ui.cadangan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.almezi.simplemoneytracker_kt.data.Clock
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ImportResult(
    val added: Int,
    val skipped: Int
)

data class CadanganUiState(
    val transactionCount: Int = 0,
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
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(CadanganUiState())
    val uiState: StateFlow<CadanganUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(lastBackupMillis = readLastBackup()) }
        refreshCount()
    }

    private fun readLastBackup(): Long? =
        preferences.getLong(KEY_LAST_BACKUP).takeIf { it > 0L }

    fun todayMillis(): Long = clock.currentTimeMillis()

    /** @param path the displayable location the picker/store reported. */
    fun markBackupDone(path: String) {
        val now = clock.currentTimeMillis()
        preferences.putLong(KEY_LAST_BACKUP, now)
        _uiState.update { it.copy(lastBackupMillis = now, toast = path) }
    }

    fun clearToast() = _uiState.update { it.copy(toast = null) }

    fun clearImportResult() = _uiState.update { it.copy(importResult = null, importError = false) }

    private fun refreshCount() {
        viewModelScope.launch {
            _uiState.update { it.copy(transactionCount = transactionRepository.countTransactions()) }
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
                _uiState.update { it.copy(importing = false, importError = true) }
                return@launch
            }
            val existing = transactionRepository.getAllTransactionsOnce()
            val existingKeys = existing.map { duplicateKey(it) }.toSet()
            var skipped = 0
            val fresh = ArrayList<Transaction>()
            val currentTime = clock.currentTimeMillis()
            parsed.transactions.forEach { backupTx ->
                val tx = backupTx.toTransaction(currentTime)
                if (duplicateKey(tx) in existingKeys) {
                    skipped++
                } else {
                    fresh.add(tx)
                }
            }
            // The file carries four lists, not one. Importing only the
            // transactions brought the money back and silently left the custom
            // category names and the budgets behind, which is the data loss the
            // Cadangan screen warns about (`UI_SPEC.md:119`).
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
                            importResult = ImportResult(fresh.size, skipped)
                        )
                    }
                    refreshCount()
                }
                .onFailure {
                    _uiState.update { state -> state.copy(importing = false, importError = true) }
                }
        }
    }

    /**
     * Date, type, amount, category, account and note all matching. Mirrors
     * `UI_SPEC.md` section 6.7; docs/09 item 5.5 requires this to behave
     * identically on both platforms, which it now does because there is exactly
     * one implementation.
     */
    private fun duplicateKey(tx: Transaction): String = listOf(
        tx.dateEpochMillis,
        tx.type,
        tx.amount,
        tx.categoryId,
        tx.accountId,
        tx.note.orEmpty()
    ).joinToString("|")
}