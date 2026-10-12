package id.almezi.simplemoneytracker_kt.ui.kategori

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.almezi.simplemoneytracker_kt.data.Category
import id.almezi.simplemoneytracker_kt.data.CategoryRepository
import id.almezi.simplemoneytracker_kt.data.Clock
import id.almezi.simplemoneytracker_kt.ui.categoryDisplayName
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import id.almezi.simplemoneytracker_kt.ui.matchesCategoryQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryRow(
    val category: Category,
    val usageCount: Int
) {
    val isDefault: Boolean get() = category.isDefault
    val isHidden: Boolean get() = category.isHidden
    val displayName: String? get() = category.customName
}

data class DeleteCategoryRequest(
    val row: CategoryRow,
    val usageCount: Int,
    val fallbackName: String
)

data class KelolaKategoriUiState(
    val type: TransactionType = TransactionType.Income,
    val query: String = "",
    val rows: List<CategoryRow> = emptyList(),
    val pendingDelete: DeleteCategoryRequest? = null
) {
    val visibleRows: List<CategoryRow> get() = filterCategoryRows(rows, query)
}

/** Keeps rows whose display name contains [query], case- and space-insensitive. */
fun filterCategoryRows(rows: List<CategoryRow>, query: String): List<CategoryRow> =
    rows.filter { matchesCategoryQuery(categoryDisplayName(it.category), query) }

class KelolaKategoriViewModel(
    private val categoryRepository: CategoryRepository,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(KelolaKategoriUiState())
    val uiState: StateFlow<KelolaKategoriUiState> = _uiState.asStateFlow()

    init {
        reload(_uiState.value.type)
    }

    fun todayMillis(): Long = clock.currentTimeMillis()

    fun updateType(type: TransactionType) {
        if (type == _uiState.value.type) return
        _uiState.update { it.copy(type = type) }
        reload(type)
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun setHidden(categoryId: String, hidden: Boolean) {
        viewModelScope.launch {
            categoryRepository.setHidden(categoryId, hidden)
            reload(_uiState.value.type)
        }
    }

    fun rename(categoryId: String, name: String) {
        viewModelScope.launch {
            categoryRepository.rename(categoryId, name)
            reload(_uiState.value.type)
        }
    }

    fun requestDelete(row: CategoryRow) {
        viewModelScope.launch {
            val usage = categoryRepository.usageCount(row.category.id)
            val fallback = fallbackCategory(row.category.type)
            _uiState.update {
                it.copy(
                    pendingDelete = DeleteCategoryRequest(row, usage, fallback?.customName.orEmpty())
                )
            }
        }
    }

    fun cancelDelete() = _uiState.update { it.copy(pendingDelete = null) }

    fun confirmDelete() {
        val request = _uiState.value.pendingDelete ?: return
        _uiState.update { it.copy(pendingDelete = null) }
        viewModelScope.launch {
            fallbackCategory(request.row.category.type)?.let { fallback ->
                categoryRepository.deleteCustomCategoryAtomically(request.row.category.id, fallback.id)
            }
            reload(_uiState.value.type)
        }
    }

    private suspend fun fallbackCategory(type: String): Category? =
        categoryRepository.getByTypeOnce(type)
            .firstOrNull { it.id == "exp_other" || it.id == "inc_other" }
            ?: categoryRepository.getByTypeOnce(type).lastOrNull()

    private fun reload(type: TransactionType) {
        viewModelScope.launch {
            val categories = categoryRepository.getByTypeOnce(type.storageValue)
            val rows = categories.map { category ->
                CategoryRow(category, categoryRepository.usageCount(category.id))
            }
            _uiState.update { it.copy(rows = rows) }
        }
    }

}