package id.almezi.simplemoneytracker_kt.ui.ubah

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.almezi.simplemoneytracker_kt.data.Account
import id.almezi.simplemoneytracker_kt.data.Category
import id.almezi.simplemoneytracker_kt.data.Clock
import id.almezi.simplemoneytracker_kt.data.Transaction
import id.almezi.simplemoneytracker_kt.data.TransactionRepository
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import id.almezi.simplemoneytracker_kt.ui.designsystem.isAmountInRange
import id.almezi.simplemoneytracker_kt.ui.designsystem.parseAmountDigits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UbahUiState(
    val loading: Boolean = true,
    val original: Transaction? = null,
    val type: TransactionType = TransactionType.Income,
    val amountDigits: String = "",
    val selectedCategoryId: String? = null,
    val selectedAccountId: String = "",
    val dateMillis: Long = 0L,
    val note: String = "",
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val showDeleteSheet: Boolean = false,
    val finished: Boolean = false
) {
    val amount: Long get() = parseAmountDigits(amountDigits)
    val hasAmount: Boolean get() = isAmountInRange(amountDigits)

    val hasChanges: Boolean
        get() {
            val source = original ?: return false
            return type.storageValue != source.type ||
                amount != source.amount ||
                selectedCategoryId != source.categoryId ||
                selectedAccountId != source.accountId ||
                dateMillis != source.dateEpochMillis ||
                note.trim() != (source.note ?: "").trim()
        }

    val canSave: Boolean
        get() = original != null && hasAmount && selectedCategoryId != null && hasChanges
}

class UbahTransaksiViewModel(
    private val repository: TransactionRepository,
    private val clock: Clock,
    private val transactionId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(UbahUiState())
    val uiState: StateFlow<UbahUiState> = _uiState.asStateFlow()

    private var categoryJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            val transaction = repository.getTransaction(transactionId)
            _uiState.update { state ->
                state.copy(
                    loading = false,
                    original = transaction,
                    type = transaction?.let { TransactionType.fromStorage(it.type) } ?: TransactionType.Income,
                    amountDigits = transaction?.amount?.toString().orEmpty(),
                    selectedCategoryId = transaction?.categoryId,
                    selectedAccountId = transaction?.accountId.orEmpty(),
                    dateMillis = transaction?.dateEpochMillis ?: clock.currentTimeMillis(),
                    note = transaction?.note.orEmpty()
                )
            }
            observeCategories(_uiState.value.type)
        }
        viewModelScope.launch {
            repository.getAccounts().collect { accounts ->
                _uiState.update { it.copy(accounts = accounts) }
            }
        }
    }

    fun todayMillis(): Long = clock.currentTimeMillis()

    fun updateType(type: TransactionType) {
        if (type == _uiState.value.type) return
        _uiState.update { it.copy(type = type) }
        observeCategories(type)
    }

    fun updateAmount(digits: String) = _uiState.update { it.copy(amountDigits = digits) }

    fun updateCategory(categoryId: String) = _uiState.update { it.copy(selectedCategoryId = categoryId) }

    fun updateAccount(accountId: String) = _uiState.update { it.copy(selectedAccountId = accountId) }

    fun updateDate(millis: Long) = _uiState.update { it.copy(dateMillis = millis) }

    fun updateNote(note: String) = _uiState.update { it.copy(note = note) }

    fun requestDelete() = _uiState.update { it.copy(showDeleteSheet = true) }

    fun cancelDelete() = _uiState.update { it.copy(showDeleteSheet = false) }

    fun confirmDelete() {
        val original = _uiState.value.original ?: return
        _uiState.update { it.copy(showDeleteSheet = false) }
        viewModelScope.launch {
            repository.deleteTransaction(original.id)
            _uiState.update { it.copy(finished = true) }
        }
    }

    fun save() {
        val state = _uiState.value
        val original = state.original ?: return
        if (!state.canSave) return
        viewModelScope.launch {
            repository.updateTransaction(
                original.copy(
                    type = state.type.storageValue,
                    amount = state.amount,
                    categoryId = state.selectedCategoryId ?: original.categoryId,
                    accountId = state.selectedAccountId,
                    dateEpochMillis = state.dateMillis,
                    note = state.note.ifBlank { null }
                )
            )
            _uiState.update { it.copy(finished = true) }
        }
    }

    private fun observeCategories(type: TransactionType) {
        categoryJob?.cancel()
        categoryJob = viewModelScope.launch {
            repository.getCategoriesByType(type.storageValue).collect { categories ->
                _uiState.update { state ->
                    state.copy(
                        categories = categories,
                        selectedCategoryId = state.selectedCategoryId
                            ?.takeIf { id -> categories.any { it.id == id } }
                    )
                }
            }
        }
    }

    class Factory(
        private val repository: TransactionRepository,
        private val clock: Clock,
        private val transactionId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UbahTransaksiViewModel::class.java)) {
                return UbahTransaksiViewModel(repository, clock, transactionId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}