package id.almezi.simplemoneytracker_kt.ui.tambah

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.almezi.simplemoneytracker_kt.data.Clock
import id.almezi.simplemoneytracker_kt.data.Category
import id.almezi.simplemoneytracker_kt.data.Transaction
import id.almezi.simplemoneytracker_kt.data.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TambahUiState(
    val type: String = "pemasukan",
    val amount: String = "",
    val selectedCategoryId: String? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = "",
    val amountError: Boolean = false,
    val categoryError: Boolean = false,
    val isSaved: Boolean = false
)

class TambahViewModel(
    private val repository: TransactionRepository,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(TambahUiState())
    val uiState: StateFlow<TambahUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        loadCategories("pemasukan")
    }

    private fun loadCategories(type: String) {
        viewModelScope.launch {
            repository.getCategoriesByType(type).collect { _categories.value = it }
        }
    }

    fun updateType(type: String) {
        _uiState.update { it.copy(type = type, selectedCategoryId = null) }
        loadCategories(type)
    }

    fun updateAmount(amount: String) {
        _uiState.update { it.copy(amount = amount, amountError = false) }
    }

    fun updateCategory(categoryId: String) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, categoryError = false) }
    }

    fun updateDate(millis: Long) {
        _uiState.update { it.copy(dateMillis = millis) }
    }

    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun save() {
        val state = _uiState.value
        val amountLong = state.amount.toLongOrNull()
        val hasAmountError = amountLong == null || amountLong <= 0
        val hasCategoryError = state.selectedCategoryId == null

        if (hasAmountError || hasCategoryError) {
            _uiState.update { it.copy(amountError = hasAmountError, categoryError = hasCategoryError) }
            return
        }

        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    type = state.type,
                    amount = amountLong,
                    categoryId = state.selectedCategoryId,
                    dateEpochMillis = state.dateMillis,
                    note = state.note.ifBlank { null },
                    createdAt = clock.currentTimeMillis()
                )
            )
            _uiState.update {
                it.copy(
                    isSaved = true,
                    amount = "",
                    selectedCategoryId = null,
                    note = "",
                    dateMillis = clock.currentTimeMillis(),
                    amountError = false,
                    categoryError = false
                )
            }
            _saveSuccess.value = true
        }
    }

    fun resetSaveState() {
        _saveSuccess.value = false
        _uiState.update { it.copy(isSaved = false) }
    }

    class Factory(
        private val repository: TransactionRepository,
        private val clock: Clock
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TambahViewModel::class.java)) {
                return TambahViewModel(repository, clock) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
