package id.almezi.simplemoneytracker_kt.ui.ringkasan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.almezi.simplemoneytracker_kt.data.CategoryRepository
import id.almezi.simplemoneytracker_kt.data.CategoryTotal
import id.almezi.simplemoneytracker_kt.data.Clock
import id.almezi.simplemoneytracker_kt.data.Transaction
import id.almezi.simplemoneytracker_kt.data.TransactionRepository
import id.almezi.simplemoneytracker_kt.data.monthTotals
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuMonth
import id.almezi.simplemoneytracker_kt.ui.designsystem.monthOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import id.almezi.simplemoneytracker_kt.ui.displayNameFlow

private const val STOP_TIMEOUT_MILLIS = 5_000L

data class CategoryBreakdown(
    val categoryId: String,
    val total: Long
)

data class RingkasanUiState(
    val month: SakuMonth,
    val balance: Long = 0L,
    val income: Long = 0L,
    val expense: Long = 0L,
    val incomeByCategory: List<CategoryBreakdown> = emptyList(),
    val expenseByCategory: List<CategoryBreakdown> = emptyList()
) {
    val hasTransactions: Boolean get() = income > 0L || expense > 0L
    val isNegative: Boolean get() = balance < 0L
}

class RingkasanViewModel(
    private val repository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val clock: Clock
) : ViewModel() {

    private val currentMonth = monthOf(clock.currentTimeMillis())

    private val _uiState = MutableStateFlow(RingkasanUiState(month = currentMonth))
    val uiState: StateFlow<RingkasanUiState> = _uiState.asStateFlow()

    /** Category id to the name the user gave it, for the breakdown rows. */
    val categoryNames: StateFlow<Map<String, String>> = categoryRepository.displayNameFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyMap())

    init {
        viewModelScope.launch {
            // Recompute whatever month the user is on whenever the database
            // changes; required now that tab state is restored instead of the
            // ViewModel being recreated on every tab switch.
            repository.getAllTransactions().collect { all -> recompute(_uiState.value.month, all) }
        }
    }

    fun currentMonthMillis(): Long = clock.currentTimeMillis()

    fun previousMonth() = shiftMonth(_uiState.value.month.minusMonth())

    fun nextMonth() {
        val next = _uiState.value.month.plusMonth()
        if (next > currentMonth) return
        shiftMonth(next)
    }

    fun selectMonth(month: SakuMonth) {
        if (month > currentMonth) return
        shiftMonth(month)
    }

    /** Month moves synchronously (the header flips at once); the totals follow. */
    private fun shiftMonth(month: SakuMonth) {
        _uiState.update { it.copy(month = month) }
        viewModelScope.launch { recompute(month, repository.getAllTransactionsOnce()) }
    }

    private suspend fun recompute(month: SakuMonth, all: List<Transaction>) {
        val totals = monthTotals(
            transactions = all,
            startMillis = month.startMillis(),
            endMillis = month.endMillisExclusive(),
            categories = categoryRepository.getAllOnce()
        )
        // A newer selection superseded this recompute while it was reading.
        if (_uiState.value.month != month) return
        _uiState.update {
            it.copy(
                balance = totals.balance,
                income = totals.income,
                expense = totals.expense,
                incomeByCategory = breakdown(totals.incomeByCategory),
                expenseByCategory = breakdown(totals.expenseByCategory)
            )
        }
    }

    // The share of the total is rendered by formatCategoryShare, which rounds;
    // a truncated copy here only ever disagreed with it.
    private fun breakdown(rows: List<CategoryTotal>) =
        rows
            .filter { it.total > 0L }
            .map { row -> CategoryBreakdown(categoryId = row.categoryId, total = row.total) }
            .sortedByDescending { it.total }

}