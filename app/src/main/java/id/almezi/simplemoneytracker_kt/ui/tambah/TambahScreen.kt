package id.almezi.simplemoneytracker_kt.ui.tambah

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import id.almezi.simplemoneytracker_kt.R
import id.almezi.simplemoneytracker_kt.SimpleMoneyTrackerApp
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.components.SakuToast
import id.almezi.simplemoneytracker_kt.ui.components.SakuToastHost
import id.almezi.simplemoneytracker_kt.ui.components.PrimaryButtonTone
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import id.almezi.simplemoneytracker_kt.ui.transaction.ExpenseHeaderLabel
import id.almezi.simplemoneytracker_kt.ui.transaction.TransactionForm
import id.almezi.simplemoneytracker_kt.ui.transaction.TransactionFormActions
import id.almezi.simplemoneytracker_kt.ui.transaction.TransactionFormState

@Composable
fun TambahScreen(
    modifier: Modifier = Modifier,
    onOpenKelolaKategori: () -> Unit = {},
    viewModel: TambahViewModel = viewModel(
        factory = TambahViewModel.Factory(
            (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container.transactionRepository,
            (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container.clock
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val context = LocalContext.current
    var toast by remember { mutableStateOf<SakuToast?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            toast = when (event) {
                TambahEvent.Saved -> SakuToast(text = context.getString(R.string.toast_transaksi_disimpan))
                TambahEvent.SaveFailed -> SakuToast(text = context.getString(R.string.toast_gagal_menyimpan))
            }
        }
    }

    val isExpense = uiState.type == TransactionType.Expense
    val helperText = when (TambahHelper.of(uiState)) {
        TambahHelper.None -> null
        TambahHelper.MissingAmount -> stringResource(R.string.tambah_helper_amount)
        TambahHelper.MissingCategory -> stringResource(R.string.tambah_helper_category)
        TambahHelper.MissingBoth -> stringResource(R.string.tambah_helper_both)
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
            header = {
                if (isExpense) {
                    ExpenseHeaderLabel(text = stringResource(R.string.tambah_header_pengeluaran))
                } else {
                    Text(
                        text = stringResource(R.string.tambah_header_pemasukan),
                        style = SakuTheme.text.screenTitle,
                        color = SakuTheme.colors.text,
                        modifier = Modifier.testTag(TestTags.TAMBAH_HEADER)
                    )
                }
            },
            saveLabel = stringResource(
                if (isExpense) R.string.tambah_simpan_pengeluaran else R.string.tambah_simpan_transaksi
            ),
            saveEnabled = uiState.canSave,
            saveTone = if (isExpense) PrimaryButtonTone.Expense else PrimaryButtonTone.Brand,
            helperText = helperText,
            showKelolaKategoriLink = true,
            onOpenKelolaKategori = onOpenKelolaKategori,
            maxDateMillis = viewModel.todayMillis(),
            bottomReserve = SakuSize.navReserve + SakuSpace.screenBottom,
            onSave = viewModel::save,
            testTag = TestTags.TAMBAH_BUTTON_SAVE,
        )

        SakuToastHost(toast = toast, onDismiss = { toast = null })
    }
}
