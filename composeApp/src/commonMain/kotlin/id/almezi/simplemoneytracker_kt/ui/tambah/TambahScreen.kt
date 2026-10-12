package id.almezi.simplemoneytracker_kt.ui.tambah

import id.almezi.simplemoneytracker_kt.SakuContainer

import id.almezi.simplemoneytracker_kt.SakuStrings

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.components.SakuToast
import id.almezi.simplemoneytracker_kt.ui.components.SakuToastHost
import id.almezi.simplemoneytracker_kt.ui.components.PrimaryButtonTone
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import id.almezi.simplemoneytracker_kt.ui.transaction.TransactionForm
import id.almezi.simplemoneytracker_kt.ui.transaction.TransactionFormActions
import id.almezi.simplemoneytracker_kt.ui.transaction.TransactionFormState

@Composable
fun TambahScreen(
    modifier: Modifier = Modifier,
    onOpenKelolaKategori: () -> Unit = {},
) {
    // SakuContainer is a @Composable getter, so it must be read here, in the
    // composable body. An `initializer { }` block is not composable and
    // cannot call it.
    val container = SakuContainer
    val viewModel: TambahViewModel = viewModel(
        factory = viewModelFactory { initializer { TambahViewModel(
            container.transactionRepository,
            container.clock,
        ) } },
    )

    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    var toast by remember { mutableStateOf<SakuToast?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            toast = when (event) {
                TambahEvent.Saved -> SakuToast(text = SakuStrings.toast_transaksi_disimpan)
                TambahEvent.SaveFailed -> SakuToast(text = SakuStrings.toast_gagal_menyimpan)
            }
        }
    }

    val isExpense = uiState.type == TransactionType.Expense
    val helperText = when (TambahHelper.of(uiState)) {
        TambahHelper.None -> null
        TambahHelper.MissingAmount -> SakuStrings.tambah_helper_amount
        TambahHelper.MissingCategory -> SakuStrings.tambah_helper_category
        TambahHelper.MissingBoth -> SakuStrings.tambah_helper_both
    }

    Box(modifier = modifier) {
        TransactionForm(
            state = TransactionFormState(
                type = uiState.type,
                amountDigits = uiState.amountDigits,
                categories = categories,
                selectedCategoryId = uiState.selectedCategoryId,
                accounts = accounts,
                selectedAccountId = uiState.selectedAccountId,
                dateMillis = uiState.dateMillis,
                note = uiState.note
            ),
            actions = TransactionFormActions(
                onTypeChange = viewModel::updateType,
                onAmountChange = viewModel::updateAmount,
                onCategoryChange = viewModel::updateCategory,
                onAccountChange = viewModel::updateAccount,
                onDateChange = viewModel::updateDate,
                onNoteChange = viewModel::updateNote
            ),
            header = null,
            saveLabel = if (isExpense) SakuStrings.tambah_simpan_pengeluaran else SakuStrings.tambah_simpan_transaksi,
            saveEnabled = uiState.canSave,
            saveTone = if (isExpense) PrimaryButtonTone.Expense else PrimaryButtonTone.Brand,
            helperText = helperText,
            showKelolaKategoriLink = true,
            showQuickCategoryChips = false,
            onOpenKelolaKategori = onOpenKelolaKategori,
            maxDateMillis = viewModel.todayMillis(),
            bottomReserve = SakuSize.navReserve + SakuSpace.screenBottom,
            onSave = viewModel::save,
            testTag = TestTags.TAMBAH_BUTTON_SAVE,
        )

        SakuToastHost(toast = toast, onDismiss = { toast = null })
    }
}
