package id.almezi.simplemoneytracker_kt.ui.kategori

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.almezi.simplemoneytracker_kt.data.Category
import id.almezi.simplemoneytracker_kt.data.CategoryRepository
import id.almezi.simplemoneytracker_kt.data.RenameResult
import id.almezi.simplemoneytracker_kt.data.Clock
import id.almezi.simplemoneytracker_kt.ui.categoryDisplayName
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import id.almezi.simplemoneytracker_kt.ui.matchesCategoryQuery
import kotlinx.coroutines.Job
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
    val renameError: RenameResult? = null,
    val pendingDelete: DeleteCategoryRequest? = null
) {
    val visibleRows: List<CategoryRow> get() = filterCategoryRows(rows, query)
}

/** Keeps rows whose display name contains [query], case- and space-insensitive. */
fun filterCategoryRows(rows: List<CategoryRow>, query: String): List<CategoryRow> =
    rows.filter { matchesCategoryQuery(categoryDisplayName(it.category), query) }

/** The category deleted transactions are moved to. `CATEGORIES.md` names it "Lainnya". */
fun fallbackCategoryId(type: String): String =
    if (type == "pemasukan") "inc_other" else "exp_other"

/** Every category with its usage count, from one grouped query instead of one per row. */
fun categoryRows(categories: List<Category>, usageCounts: Map<String, Int>): List<CategoryRow> =
    categories.map { category -> CategoryRow(category, usageCounts[category.id] ?: 0) }

class KelolaKategoriViewModel(
    private val categoryRepository: CategoryRepository,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(KelolaKategoriUiState())
    val uiState: StateFlow<KelolaKategoriUiState> = _uiState.asStateFlow()

    private var rowsJob: Job? = null

    init {
        observeRows()
    }

    fun todayMillis(): Long = clock.currentTimeMillis()

    fun updateType(type: TransactionType) {
        if (type == _uiState.value.type) return
        _uiState.update { it.copy(type = type) }
        observeRows()
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun setHidden(categoryId: String, hidden: Boolean) {
        viewModelScope.launch {
            // runCatching so a failed write cannot take the screen down with
            // it; the row keeps its previous state and the user can retry.
            runCatching { categoryRepository.setHidden(categoryId, hidden) }
        }
    }

    fun rename(categoryId: String, name: String, onResult: (RenameResult) -> Unit = {}) {
        viewModelScope.launch {
            runCatching { categoryRepository.rename(categoryId, name) }
                .onSuccess { result ->
                    _uiState.update { it.copy(renameError = result.takeIf { it != RenameResult.Renamed }) }
                    onResult(result)
                }
        }
    }

    fun clearRenameError() = _uiState.update { it.copy(renameError = null) }

    fun requestDelete(row: CategoryRow) {
        viewModelScope.launch {
            val usage = categoryRepository.usageCount(row.category.id)
            val fallback = fallbackCategory(row.category.type)
            _uiState.update {
                it.copy(
                    pendingDelete = DeleteCategoryRequest(
                        row,
                        usage,
                        fallback?.let { category -> categoryDisplayName(category) }.orEmpty()
                    )
                )
            }
        }
    }

    fun cancelDelete() = _uiState.update { it.copy(pendingDelete = null) }

    fun confirmDelete() {
        val request = _uiState.value.pendingDelete ?: return
        _uiState.update { it.copy(pendingDelete = null) }
        viewModelScope.launch {
            runCatching {
                fallbackCategory(request.row.category.type)?.let { fallback ->
                    categoryRepository.deleteCustomCategoryAtomically(request.row.category.id, fallback.id)
                }
            }
        }
    }

    private suspend fun fallbackCategory(type: String): Category? =
        categoryRepository.getByTypeOnce(type)
            .firstOrNull { it.id == fallbackCategoryId(type) }
            ?: categoryRepository.getByTypeOnce(type).lastOrNull()

    /**
     * The list observes the database instead of reloading once per mutation:
     * categories added from Tambah kategori (or an import) must appear when
     * this screen sits behind them in the back stack, and hide/rename/delete
     * apply in place. One collector per type, the same cancel-and-relaunch
     * pattern as `UbahTransaksiViewModel`. Room re-emits on every categories
     * write, from this screen or any other. Usage counts are re-queried per
     * emission; they stay one emission stale after a transaction-only write,
     * exactly as with the previous one-shot reload.
     */
    private fun observeRows() {
        rowsJob?.cancel()
        rowsJob = viewModelScope.launch {
            categoryRepository.getByType(_uiState.value.type.storageValue).collect { ofType ->
                val rows = categoryRows(ofType, categoryRepository.usageCounts())
                _uiState.update { it.copy(rows = rows) }
            }
        }
    }

}