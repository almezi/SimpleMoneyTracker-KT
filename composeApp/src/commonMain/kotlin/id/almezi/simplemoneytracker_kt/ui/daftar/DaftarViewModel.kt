package id.almezi.simplemoneytracker_kt.ui.daftar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.almezi.simplemoneytracker_kt.data.CategoryRepository
import id.almezi.simplemoneytracker_kt.data.Clock
import id.almezi.simplemoneytracker_kt.data.Transaction
import id.almezi.simplemoneytracker_kt.data.TransactionRepository
import id.almezi.simplemoneytracker_kt.data.countedTransactions
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuMonth
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import id.almezi.simplemoneytracker_kt.ui.designsystem.dateOf
import id.almezi.simplemoneytracker_kt.ui.designsystem.monthOf
import id.almezi.simplemoneytracker_kt.ui.displayColorFlow
import id.almezi.simplemoneytracker_kt.ui.displayNameFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L

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
    private val categoryRepository: CategoryRepository,
    private val clock: Clock
) : ViewModel() {

    private val currentMonth = monthOf(clock.currentTimeMillis())

    private val _uiState = MutableStateFlow(DaftarUiState(month = currentMonth))
    val uiState: StateFlow<DaftarUiState> = _uiState.asStateFlow()

    /** Category id to the name the user gave it, for the row and the delete sheet. */
    val categoryNames: StateFlow<Map<String, String>> = categoryRepository.displayNameFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyMap())

    /** Category id to the colour the user picked, for the row's avatar. */
    val categoryColors: StateFlow<Map<String, String>> = categoryRepository.displayColorFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyMap())

    init {
        viewModelScope.launch {
            // Rebuild whatever month the user is looking at, so a database
            // change from another screen refreshes the list in place instead of
            // snapping it back to the month the app started in.
            repository.getAllTransactions().collect { all -> rebuild(_uiState.value.month, all) }
        }
    }

    fun currentMonthMillis(): Long = clock.currentTimeMillis()

    // Matches MonthPickerSheet's floor (today.year - 10); the arrows used to
    // be unbounded and could walk back to year 1.
    fun previousMonth() {
        val previous = _uiState.value.month.minusMonth()
        if (previous < SakuMonth(currentMonth.year - 10, 1)) return
        shiftMonth(previous)
    }

    fun nextMonth() {
        val next = _uiState.value.month.plusMonth()
        if (next > currentMonth) return
        shiftMonth(next)
    }

    fun selectMonth(month: SakuMonth) {
        if (month > currentMonth) return
        shiftMonth(month)
    }

    /** Month moves synchronously (the label flips at once); the data follows. */
    private fun shiftMonth(month: SakuMonth) {
        _uiState.update { it.copy(month = month) }
        viewModelScope.launch { rebuild(month, repository.getAllTransactionsOnce()) }
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
        viewModelScope.launch {
            // runCatching so a failed undo cannot take the screen down with it.
            runCatching { repository.restoreTransaction(transaction) }
        }
    }

    fun clearUndo() {
        _uiState.update { it.copy(undoCandidate = null) }
    }

    private suspend fun rebuild(month: SakuMonth, all: List<Transaction>) {
        val start = month.startMillis()
        val end = month.endMillisExclusive()
        val categories = categoryRepository.getAllOnce()
        // Every row of the month is listed — the user recorded it, so it stays
        // visible — but only counting categories move the numbers
        // (`UI_SPEC.md:16`).
        val inMonth = all.filter { it.dateEpochMillis in start until end }
            .sortedByDescending { it.dateEpochMillis }
        val counted = countedTransactions(all, start, end, categories)
        val countedIds = counted.mapTo(mutableSetOf()) { it.id }
        val days = inMonth
            .groupBy { dateOf(it.dateEpochMillis) }
            .map { (_, txs) ->
                TransactionDay(
                    dateMillis = txs.maxOf { it.dateEpochMillis },
                    netTotal = txs.filter { it.id in countedIds }.sumOf { signedAmount(it) },
                    transactions = txs.sortedByDescending { it.createdAt }
                )
            }
            .sortedByDescending { it.dateMillis }
        // A newer selection superseded this rebuild while it was reading; drop
        // the result instead of overwriting the month the user is on now.
        if (_uiState.value.month != month) return
        _uiState.update {
            it.copy(
                month = month,
                days = days,
                incomeTotal = counted.filter { tx -> tx.type == TransactionType.Income.storageValue }.sumOf { it.amount },
                expenseTotal = counted.filter { tx -> tx.type == TransactionType.Expense.storageValue }.sumOf { it.amount }
            )
        }
    }

    private fun signedAmount(transaction: Transaction): Long =
        if (transaction.type == TransactionType.Income.storageValue) {
            transaction.amount
        } else {
            -transaction.amount
        }

}