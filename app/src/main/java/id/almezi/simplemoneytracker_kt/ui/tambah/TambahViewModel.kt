package id.almezi.simplemoneytracker_kt.ui.tambah

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.almezi.simplemoneytracker_kt.data.Account
import id.almezi.simplemoneytracker_kt.data.AccountSeed
import id.almezi.simplemoneytracker_kt.data.Clock
import id.almezi.simplemoneytracker_kt.data.Category
import id.almezi.simplemoneytracker_kt.data.Transaction
import id.almezi.simplemoneytracker_kt.data.TransactionRepository
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import id.almezi.simplemoneytracker_kt.ui.designsystem.isAmountInRange
import id.almezi.simplemoneytracker_kt.ui.designsystem.parseAmountDigits
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TambahUiState(
    val type: TransactionType = TransactionType.Income,
    val amountDigits: String = "",
    val selectedCategoryId: String? = null,
    val selectedAccountId: String = AccountSeed.DEFAULT_ACCOUNT_ID,
    val dateMillis: Long = 0L,
    val note: String = "",
    val isSaving: Boolean = false
) {
    val amount: Long get() = parseAmountDigits(amountDigits)
    val hasAmount: Boolean get() = isAmountInRange(amountDigits)
    val hasCategory: Boolean get() = selectedCategoryId != null
    val canSave: Boolean get() = hasAmount && hasCategory && !isSaving
}

enum class TambahHelper {
    None,
    MissingAmount,
    MissingCategory,
    MissingBoth;

    companion object {
        fun of(state: TambahUiState): TambahHelper = when {
            state.hasAmount && state.hasCategory -> None
            state.hasAmount -> MissingCategory
            state.hasCategory -> MissingAmount
            else -> MissingBoth
        }
    }
}

sealed interface TambahEvent {
    data object Saved : TambahEvent
    data object SaveFailed : TambahEvent
}

class TambahViewModel(
    private val repository: TransactionRepository,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TambahUiState(dateMillis = clock.currentTimeMillis())
    )
    val uiState: StateFlow<TambahUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    private val _events = MutableSharedFlow<TambahEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<TambahEvent> = _events.asSharedFlow()

    init {
        observeCategories(_uiState.value.type)
        viewModelScope.launch {
            repository.getAccounts().collect { accounts ->
                _accounts.value = accounts
                if (accounts.none { it.id == _uiState.value.selectedAccountId }) {
                    _uiState.update { state ->
                        state.copy(selectedAccountId = accounts.firstOrNull()?.id ?: AccountSeed.DEFAULT_ACCOUNT_ID)
                    }
                }
            }
        }
    }

    fun todayMillis(): Long = clock.currentTimeMillis()

    fun updateType(type: TransactionType) {
        if (type == _uiState.value.type) return
        val keepCategory = _categories.value
            .firstOrNull { it.id == _uiState.value.selectedCategoryId }
            ?.type == type.storageValue
        _uiState.update {
            it.copy(
                type = type,
                selectedCategoryId = if (keepCategory) it.selectedCategoryId else null
            )
        }
        observeCategories(type)
    }

    fun updateAmount(digits: String) {
        _uiState.update { it.copy(amountDigits = digits) }
    }

    fun updateCategory(categoryId: String) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun updateAccount(accountId: String) {
        _uiState.update { it.copy(selectedAccountId = accountId) }
    }

    fun updateDate(millis: Long) {
        _uiState.update { it.copy(dateMillis = millis) }
    }

    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun save() {
        val state = _uiState.value
        val categoryId = state.selectedCategoryId ?: return
        if (!state.canSave) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching {
                repository.insertTransaction(
                    Transaction(
                        type = state.type.storageValue,
                        amount = state.amount,
                        categoryId = categoryId,
                        accountId = state.selectedAccountId,
                        dateEpochMillis = state.dateMillis,
                        note = state.note.ifBlank { null },
                        createdAt = clock.currentTimeMillis()
                    )
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        amountDigits = "",
                        selectedCategoryId = null,
                        note = "",
                        isSaving = false
                    )
                }
                _events.tryEmit(TambahEvent.Saved)
            }.onFailure {
                _uiState.update { it.copy(isSaving = false) }
                _events.tryEmit(TambahEvent.SaveFailed)
            }
        }
    }

    private fun observeCategories(type: TransactionType) {
        viewModelScope.launch {
            repository.getCategoriesByType(type.storageValue).collect { _categories.value = it }
        }
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