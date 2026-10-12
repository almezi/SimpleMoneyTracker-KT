package id.almezi.simplemoneytracker_kt.ui.ringkasan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.almezi.simplemoneytracker_kt.data.Clock
import id.almezi.simplemoneytracker_kt.data.TransactionRepository
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuMonth
import id.almezi.simplemoneytracker_kt.ui.designsystem.monthOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryBreakdown(
    val categoryId: String,
    val total: Long,
    val sharePercent: Int
)

data class RingkasanUiState(
    val month: SakuMonth,
    val loading: Boolean = true,
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
    private val clock: Clock
) : ViewModel() {

    private val currentMonth = monthOf(clock.currentTimeMillis())

    private val _uiState = MutableStateFlow(RingkasanUiState(month = currentMonth))
    val uiState: StateFlow<RingkasanUiState> = _uiState.asStateFlow()

    init {
        refresh(currentMonth)
    }

    fun currentMonthMillis(): Long = clock.currentTimeMillis()

    fun previousMonth() {
        refresh(_uiState.value.month.minusMonth())
    }

    fun nextMonth() {
        val next = _uiState.value.month.plusMonth()
        if (next > currentMonth) return
        refresh(next)
    }

    private fun refresh(month: SakuMonth) {
        _uiState.update { it.copy(month = month, loading = true) }
        viewModelScope.launch {
            val start = month.startMillis()
            val end = month.endMillisExclusive()
            val income = repository.incomeBetween(start, end)
            val expense = repository.expenseBetween(start, end)
            val incomeRows = repository.totalsByCategory("pemasukan", start, end)
            val expenseRows = repository.totalsByCategory("pengeluaran", start, end)
            _uiState.update {
                it.copy(
                    loading = false,
                    balance = income - expense,
                    income = income,
                    expense = expense,
                    incomeByCategory = breakdown(incomeRows, income),
                    expenseByCategory = breakdown(expenseRows, expense)
                )
            }
        }
    }

    private fun breakdown(rows: List<id.almezi.simplemoneytracker_kt.data.CategoryTotal>, typeTotal: Long) =
        rows
            .filter { it.total > 0L }
            .map { row ->
                CategoryBreakdown(
                    categoryId = row.categoryId,
                    total = row.total,
                    sharePercent = if (typeTotal <= 0L) 0 else ((row.total * 100.0) / typeTotal).toInt()
                )
            }
            .sortedByDescending { it.total }

    class Factory(
        private val repository: TransactionRepository,
        private val clock: Clock
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RingkasanViewModel::class.java)) {
                return RingkasanViewModel(repository, clock) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}