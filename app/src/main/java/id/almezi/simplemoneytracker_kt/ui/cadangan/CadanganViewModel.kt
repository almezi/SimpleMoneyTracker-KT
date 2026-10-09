package id.almezi.simplemoneytracker_kt.ui.cadangan

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.almezi.simplemoneytracker_kt.data.BackupRepository
import id.almezi.simplemoneytracker_kt.data.CategoryRepository
import id.almezi.simplemoneytracker_kt.data.Clock
import id.almezi.simplemoneytracker_kt.data.Transaction
import id.almezi.simplemoneytracker_kt.data.TransactionRepository
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

class CadanganViewModel(
    private val context: Context,
    private val backupRepository: BackupRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(CadanganUiState())
    val uiState: StateFlow<CadanganUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(lastBackupMillis = readLastBackup()) }
        refreshCount()
    }

    private fun preferences() =
        context.getSharedPreferences("saku_backup", Context.MODE_PRIVATE)

    private fun readLastBackup(): Long? =
        preferences().getLong(KEY_LAST_BACKUP, 0L).takeIf { it > 0L }

    private fun writeLastBackup(millis: Long) {
        preferences().edit().putLong(KEY_LAST_BACKUP, millis).apply()
    }

    fun todayMillis(): Long = clock.currentTimeMillis()

    fun markBackupDone(path: String) {
        val now = clock.currentTimeMillis()
        writeLastBackup(now)
        _uiState.update { it.copy(lastBackupMillis = now, toast = path) }
    }

    fun clearToast() = _uiState.update { it.copy(toast = null) }

    fun clearImportResult() = _uiState.update { it.copy(importResult = null, importError = false) }

    private fun refreshCount() {
        viewModelScope.launch {
            _uiState.update { it.copy(transactionCount = transactionRepository.countTransactions()) }
        }
    }

    suspend fun buildJson(): String = backupRepository.toJson(backupRepository.buildBackup())

    suspend fun buildCsv(): String = backupRepository.toCsv(backupRepository.buildBackup())

    fun import(rawText: String) {
        if (_uiState.value.importing) return
        _uiState.update { it.copy(importing = true, importError = false, importResult = null) }
        viewModelScope.launch {
            val parsed = runCatching { backupRepository.parseJson(rawText) }.getOrNull()
            if (parsed == null) {
                _uiState.update { it.copy(importing = false, importError = true) }
                return@launch
            }
            val existing = transactionRepository.getAllTransactionsOnce()
            val existingKeys = existing.map { duplicateKey(it) }.toSet()
            var skipped = 0
            val fresh = ArrayList<Transaction>()
            parsed.transactions.forEach { tx ->
                if (duplicateKey(tx) in existingKeys) {
                    skipped++
                } else {
                    fresh.add(tx)
                }
            }
            runCatching { transactionRepository.insertTransactions(fresh) }
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

    private fun duplicateKey(tx: Transaction): String = listOf(
        tx.dateEpochMillis,
        tx.type,
        tx.amount,
        tx.categoryId,
        tx.accountId,
        tx.note.orEmpty()
    ).joinToString("|")

    class Factory(
        private val context: Context,
        private val backupRepository: BackupRepository,
        private val transactionRepository: TransactionRepository,
        private val categoryRepository: CategoryRepository,
        private val clock: Clock
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CadanganViewModel::class.java)) {
                return CadanganViewModel(
                    context,
                    backupRepository,
                    transactionRepository,
                    categoryRepository,
                    clock
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

private const val KEY_LAST_BACKUP = "last_backup_millis"