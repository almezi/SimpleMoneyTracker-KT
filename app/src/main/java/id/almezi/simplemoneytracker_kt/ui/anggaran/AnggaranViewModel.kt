package id.almezi.simplemoneytracker_kt.ui.anggaran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.almezi.simplemoneytracker_kt.data.Budget
import id.almezi.simplemoneytracker_kt.data.BudgetRepository
import id.almezi.simplemoneytracker_kt.data.Category
import id.almezi.simplemoneytracker_kt.data.CategoryRepository
import id.almezi.simplemoneytracker_kt.data.Clock
import id.almezi.simplemoneytracker_kt.data.TransactionRepository
import id.almezi.simplemoneytracker_kt.ui.designsystem.BudgetStatus
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuMonth
import id.almezi.simplemoneytracker_kt.ui.designsystem.monthOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BudgetRow(
    val category: Category,
    val limit: Long,
    val spent: Long,
    val status: BudgetStatus
) {
    val fraction: Float
        get() = if (limit <= 0L) 0f else (spent.toFloat() / limit.toFloat()).coerceIn(0f, 1f)
}

data class AnggaranUiState(
    val month: SakuMonth,
    val rows: List<BudgetRow> = emptyList(),
    val totalLimit: Long = 0L,
    val totalSpent: Long = 0L,
    val pendingDelete: BudgetRow? = null,
    val toast: String? = null
) {
    val isEmpty: Boolean get() = rows.isEmpty()
    val totalFraction: Float
        get() = if (totalLimit <= 0L) 0f else (totalSpent.toFloat() / totalLimit.toFloat()).coerceIn(0f, 1f)
}

class AnggaranViewModel(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val clock: Clock
) : ViewModel() {

    private val currentMonth = monthOf(clock.currentTimeMillis())

    private val _uiState = MutableStateFlow(AnggaranUiState(month = currentMonth))
    val uiState: StateFlow<AnggaranUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val month = _uiState.value.month
        viewModelScope.launch {
            val budgets = budgetRepository.getAllBudgets().first()
            val start = month.startMillis()
            val end = month.endMillisExclusive()
            val rows = budgets.mapNotNull { budget ->
                val category = categoryRepository.get(budget.categoryId) ?: return@mapNotNull null
                val spent = transactionRepository.spentForCategory(budget.categoryId, start, end)
                BudgetRow(
                    category = category,
                    limit = budget.limitAmount,
                    spent = spent,
                    status = BudgetStatus.of(spent, budget.limitAmount)
                )
            }.sortedByDescending { it.fraction }
            _uiState.update {
                it.copy(
                    month = month,
                    rows = rows,
                    totalLimit = rows.sumOf { row -> row.limit },
                    totalSpent = rows.sumOf { row -> row.spent }
                )
            }
        }
    }

    fun requestDelete(row: BudgetRow) = _uiState.update { it.copy(pendingDelete = row) }

    fun cancelDelete() = _uiState.update { it.copy(pendingDelete = null) }

    fun confirmDelete() {
        val row = _uiState.value.pendingDelete ?: return
        _uiState.update { it.copy(pendingDelete = null) }
        viewModelScope.launch {
            budgetRepository.deleteBudget(row.category.id)
            refresh()
        }
    }

    class Factory(
        private val budgetRepository: BudgetRepository,
        private val transactionRepository: TransactionRepository,
        private val categoryRepository: CategoryRepository,
        private val clock: Clock
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AnggaranViewModel::class.java)) {
                return AnggaranViewModel(
                    budgetRepository,
                    transactionRepository,
                    categoryRepository,
                    clock
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
