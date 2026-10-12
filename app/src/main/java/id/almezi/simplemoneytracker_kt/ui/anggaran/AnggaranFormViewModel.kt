package id.almezi.simplemoneytracker_kt.ui.anggaran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.almezi.simplemoneytracker_kt.data.Budget
import id.almezi.simplemoneytracker_kt.data.BudgetRepository
import id.almezi.simplemoneytracker_kt.data.Category
import id.almezi.simplemoneytracker_kt.data.CategoryRepository
import id.almezi.simplemoneytracker_kt.data.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnggaranFormUiState(
    val loading: Boolean = true,
    val editing: Boolean = false,
    val budget: Budget? = null,
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val amountDigits: String = "",
    val alertAt80: Boolean = true,
    val showDeleteSheet: Boolean = false,
    val saved: Boolean = false
) {
    val categoryLocked: Boolean get() = editing
    val hasAmount: Boolean get() = amountDigits.toLongOrNull()?.let { it > 0L } ?: false
    val canSave: Boolean get() = selectedCategoryId != null && hasAmount
}

class AnggaranFormViewModel(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val clock: Clock,
    private val categoryIdArg: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AnggaranFormUiState(editing = categoryIdArg != null)
    )
    val uiState: StateFlow<AnggaranFormUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val existing = categoryIdArg?.let { budgetRepository.getBudget(it) }
            val budgets = budgetRepository.getAllBudgetsOnce()
            val budgeted = budgets.map { it.categoryId }.toSet()
            val available = categoryRepository.getByTypeOnce("pengeluaran")
                .filter { it.id !in budgeted || it.id == categoryIdArg }
            _uiState.update {
                it.copy(
                    loading = false,
                    budget = existing,
                    categories = available,
                    selectedCategoryId = existing?.categoryId,
                    amountDigits = existing?.limitAmount?.toString().orEmpty(),
                    alertAt80 = existing?.alertAt80 ?: true
                )
            }
        }
    }

    fun todayMillis(): Long = clock.currentTimeMillis()

    fun selectCategory(categoryId: String) {
        if (_uiState.value.categoryLocked) return
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun updateAmount(digits: String) = _uiState.update { it.copy(amountDigits = digits) }

    fun setAlert(enabled: Boolean) = _uiState.update { it.copy(alertAt80 = enabled) }

    fun save() {
        val state = _uiState.value
        val categoryId = state.selectedCategoryId ?: return
        if (!state.canSave) return
        val amount = state.amountDigits.toLongOrNull() ?: return
        viewModelScope.launch {
            budgetRepository.saveBudget(
                Budget(
                    categoryId = categoryId,
                    limitAmount = amount,
                    alertAt80 = state.alertAt80,
                    createdAt = state.budget?.createdAt ?: clock.currentTimeMillis(),
                    updatedAt = clock.currentTimeMillis()
                )
            )
            _uiState.update { it.copy(saved = true) }
        }
    }

    fun requestDelete() = _uiState.update { it.copy(showDeleteSheet = true) }

    fun cancelDelete() = _uiState.update { it.copy(showDeleteSheet = false) }

    fun confirmDelete() {
        val budget = _uiState.value.budget ?: return
        _uiState.update { it.copy(showDeleteSheet = false) }
        viewModelScope.launch {
            budgetRepository.deleteBudget(budget.categoryId)
            _uiState.update { it.copy(saved = true) }
        }
    }

    class Factory(
        private val budgetRepository: BudgetRepository,
        private val categoryRepository: CategoryRepository,
        private val clock: Clock,
        private val categoryIdArg: String?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AnggaranFormViewModel::class.java)) {
                return AnggaranFormViewModel(
                    budgetRepository,
                    categoryRepository,
                    clock,
                    categoryIdArg
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}