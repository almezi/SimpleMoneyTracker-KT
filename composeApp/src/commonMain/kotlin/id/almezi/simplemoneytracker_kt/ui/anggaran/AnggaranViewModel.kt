package id.almezi.simplemoneytracker_kt.ui.anggaran

import androidx.lifecycle.ViewModel
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
import kotlinx.coroutines.flow.combine
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
    val pendingDelete: BudgetRow? = null
) {
    val isEmpty: Boolean get() = rows.isEmpty()
    val totalFraction: Float
        get() = if (totalLimit <= 0L) 0f else (totalSpent.toFloat() / totalLimit.toFloat()).coerceIn(0f, 1f)
}

/**
 * Assembles the budget rows from three lookups instead of two queries per budget.
 *
 * A budget whose category no longer exists is dropped, and one with no spend in
 * the month shows zero. Rows are ordered by how much of the budget is used.
 */
fun budgetRows(
    budgets: List<Budget>,
    categoriesById: Map<String, Category>,
    spentByCategory: Map<String, Long>
): List<BudgetRow> = budgets.mapNotNull { budget ->
    val category = categoriesById[budget.categoryId] ?: return@mapNotNull null
    val spent = spentByCategory[budget.categoryId] ?: 0L
    BudgetRow(
        category = category,
        limit = budget.limitAmount,
        spent = spent,
        status = BudgetStatus.of(spent, budget.limitAmount)
    )
}.sortedByDescending { it.fraction }

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
        viewModelScope.launch {
            // Either side changing — a budget edited here, a transaction edited
            // elsewhere — must move the spend numbers. Required now that tab
            // state is restored instead of the ViewModel being recreated on
            // every tab switch. The transactions flow only re-triggers; the
            // spend itself still comes from the one grouped SQL query.
            combine(
                budgetRepository.getAllBudgets(),
                transactionRepository.getAllTransactions(),
            ) { budgets, _ -> budgets }.collect { budgets ->
                recompute(_uiState.value.month, budgets)
            }
        }
    }

    fun refresh() {
        val month = _uiState.value.month
        viewModelScope.launch {
            recompute(month, budgetRepository.getAllBudgets().first())
        }
    }

    private suspend fun recompute(month: SakuMonth, budgets: List<Budget>) {
        val rows = budgetRows(
            budgets = budgets,
            categoriesById = categoryRepository.getAllOnce().associateBy { it.id },
            spentByCategory = transactionRepository.expenseSpentByCategory(
                month.startMillis(),
                month.endMillisExclusive(),
            )
        )
        // A newer month selection superseded this recompute while it was reading.
        if (_uiState.value.month != month) return
        _uiState.update {
            it.copy(
                month = month,
                rows = rows,
                totalLimit = rows.sumOf { row -> row.limit },
                totalSpent = rows.sumOf { row -> row.spent }
            )
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

}
