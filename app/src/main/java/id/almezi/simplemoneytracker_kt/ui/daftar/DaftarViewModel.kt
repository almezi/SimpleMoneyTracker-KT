package id.almezi.simplemoneytracker_kt.ui.daftar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.almezi.simplemoneytracker_kt.data.Clock
import id.almezi.simplemoneytracker_kt.data.Transaction
import id.almezi.simplemoneytracker_kt.data.TransactionRepository
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuMonth
import id.almezi.simplemoneytracker_kt.ui.designsystem.dateOf
import id.almezi.simplemoneytracker_kt.ui.designsystem.monthOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionDay(
    val dateMillis: Long,
    val netTotal: Long,
    val transactions: List<Transaction>
)

data class DaftarUiState(
    val month: SakuMonth,
    val days: List<TransactionDay> = emptyList(),
    val incomeTotal: Long = 0L,
    val expenseTotal: Long = 0L,
    val pendingDelete: Transaction? = null,
    val undoCandidate: Transaction? = null
) {
    val isEmpty: Boolean get() = days.isEmpty()
}

class DaftarViewModel(
    private val repository: TransactionRepository,
    private val clock: Clock
) : ViewModel() {

    private val currentMonth = monthOf(clock.currentTimeMillis())

    private val _uiState = MutableStateFlow(DaftarUiState(month = currentMonth))
    val uiState: StateFlow<DaftarUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllTransactions().collect { all -> rebuild(currentMonth, all) }
        }
    }

    fun currentMonthMillis(): Long = clock.currentTimeMillis()

    fun previousMonth() {
        viewModelScope.launch {
            val month = _uiState.value.month.minusMonth()
            rebuild(month, repository.getAllTransactionsOnce())
        }
    }

    fun nextMonth() {
        val next = _uiState.value.month.plusMonth()
        if (next > currentMonth) return
        viewModelScope.launch {
            rebuild(next, repository.getAllTransactionsOnce())
        }
    }

    fun requestDelete(transaction: Transaction) {
        _uiState.update { it.copy(pendingDelete = transaction) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(pendingDelete = null) }
    }

    fun confirmDelete() {
        val transaction = _uiState.value.pendingDelete ?: return
        _uiState.update { it.copy(pendingDelete = null, undoCandidate = transaction) }
        viewModelScope.launch { repository.deleteTransaction(transaction.id) }
    }

    fun undoDelete() {
        val transaction = _uiState.value.undoCandidate ?: return
        _uiState.update { it.copy(undoCandidate = null) }
        viewModelScope.launch { repository.restoreTransaction(transaction) }
    }

    fun clearUndo() {
        _uiState.update { it.copy(undoCandidate = null) }
    }

    private suspend fun rebuild(month: SakuMonth, all: List<Transaction>) {
        val start = month.startMillis()
        val end = month.endMillisExclusive()
        val inMonth = all.filter { it.dateEpochMillis in start until end }
            .sortedByDescending { it.dateEpochMillis }
        val days = inMonth
            .groupBy { dateOf(it.dateEpochMillis) }
            .map { (_, txs) ->
                TransactionDay(
                    dateMillis = txs.maxOf { it.dateEpochMillis },
                    netTotal = txs.sumOf { signedAmount(it) },
                    transactions = txs.sortedByDescending { it.createdAt }
                )
            }
            .sortedByDescending { it.dateMillis }
        _uiState.update {
            it.copy(
                month = month,
                days = days,
                incomeTotal = inMonth.filter { tx -> tx.type == "pemasukan" }.sumOf { it.amount },
                expenseTotal = inMonth.filter { tx -> tx.type == "pengeluaran" }.sumOf { it.amount }
            )
        }
    }

    private fun signedAmount(transaction: Transaction): Long =
        if (transaction.type == "pemasukan") transaction.amount else -transaction.amount

    class Factory(
        private val repository: TransactionRepository,
        private val clock: Clock
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DaftarViewModel::class.java)) {
                return DaftarViewModel(repository, clock) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}