package id.almezi.simplemoneytracker_kt.ui.kategori

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.almezi.simplemoneytracker_kt.data.Category
import id.almezi.simplemoneytracker_kt.data.CategoryRepository
import id.almezi.simplemoneytracker_kt.data.Clock
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuCategoryPalette
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val CATEGORY_NAME_MAX = 20

data class TambahKategoriUiState(
    val type: TransactionType = TransactionType.Income,
    val name: String = "",
    val colorHex: String = SakuCategoryPalette.defaultHex,
    val nameError: Boolean = false,
    val saved: Boolean = false
) {
    val trimmedName: String get() = name.trim()
    val nameValid: Boolean get() = isValidCategoryName(name)
    val canSave: Boolean get() = nameValid && !nameError
}

class TambahKategoriViewModel(
    private val categoryRepository: CategoryRepository,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(TambahKategoriUiState())
    val uiState: StateFlow<TambahKategoriUiState> = _uiState.asStateFlow()

    fun todayMillis(): Long = clock.currentTimeMillis()

    fun updateType(type: TransactionType) = _uiState.update { it.copy(type = type, nameError = false) }

    fun updateName(name: String) {
        if (name.length > CATEGORY_NAME_MAX) return
        _uiState.update { it.copy(name = name, nameError = false) }
    }

    fun selectColor(hex: String) = _uiState.update { it.copy(colorHex = hex) }

    fun save() {
        val state = _uiState.value
        if (!state.nameValid) {
            _uiState.update { it.copy(nameError = true) }
            return
        }
        viewModelScope.launch {
            val duplicates = categoryRepository.countWithName(
                type = state.type.storageValue,
                name = state.trimmedName,
                excludeId = NEW_CATEGORY_SENTINEL
            )
            if (duplicates > 0) {
                _uiState.update { it.copy(nameError = true) }
                return@launch
            }
            val id = "cus_" + state.type.storageValue.take(3) + "_" + clock.currentTimeMillis()
            categoryRepository.create(
                id = id,
                type = state.type.storageValue,
                name = state.trimmedName,
                colorHex = state.colorHex,
                createdAt = clock.currentTimeMillis()
            )
            _uiState.update { it.copy(saved = true) }
        }
    }

}

const val NEW_CATEGORY_SENTINEL = "__new__"

fun Category.displayLabel(defaultResId: Int, fallback: String): String =
    customName ?: fallback

/**
 * Category names are 1-20 characters (`CATEGORIES.md:25`). Rename used to write
 * whatever it was given, so clearing the dialog produced a blank row that
 * `categoryDisplayName` could not name.
 */
fun isValidCategoryName(name: String): Boolean = name.trim().length in 1..CATEGORY_NAME_MAX