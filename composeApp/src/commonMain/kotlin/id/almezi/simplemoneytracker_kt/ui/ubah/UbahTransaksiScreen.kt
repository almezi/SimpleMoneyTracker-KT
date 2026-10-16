package id.almezi.simplemoneytracker_kt.ui.ubah

import id.almezi.simplemoneytracker_kt.format

import id.almezi.simplemoneytracker_kt.SakuContainer

import id.almezi.simplemoneytracker_kt.SakuStrings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.components.ConfirmDeleteSheet
import id.almezi.simplemoneytracker_kt.ui.components.DestructiveTextButton
import id.almezi.simplemoneytracker_kt.ui.components.PrimaryButtonTone
import id.almezi.simplemoneytracker_kt.ui.components.ScreenHeader
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import id.almezi.simplemoneytracker_kt.ui.designsystem.formatRupiah
import id.almezi.simplemoneytracker_kt.ui.designsystem.formatShortDate
import id.almezi.simplemoneytracker_kt.ui.getCategoryName
import id.almezi.simplemoneytracker_kt.ui.transaction.TransactionForm
import id.almezi.simplemoneytracker_kt.ui.transaction.TransactionFormActions
import id.almezi.simplemoneytracker_kt.ui.transaction.TransactionFormState

@Composable
fun UbahTransaksiScreen(
    transactionId: Long,
    onDone: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // SakuContainer is a @Composable getter, so it must be read here, in the
    // composable body. An `initializer { }` block is not composable and
    // cannot call it.
    val container = SakuContainer
    val viewModel: UbahTransaksiViewModel = viewModel(
        factory = viewModelFactory { initializer { UbahTransaksiViewModel(
            container.transactionRepository,
            container.clock,
            transactionId,
        ) } },
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.finished) {
        if (uiState.finished) onDone()
    }

    Box(modifier = modifier.testTag(TestTags.UBAH_SCREEN)) {
        TransactionForm(
            state = TransactionFormState(
                type = uiState.type,
                amountDigits = uiState.amountDigits,
                categories = uiState.categories,
                selectedCategoryId = uiState.selectedCategoryId,
                accounts = uiState.accounts,
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
                ScreenHeader(
                    title = SakuStrings.ubah_header,
                    onBack = onBack,
                    modifier = Modifier.testTag(TestTags.UBAH_HEADER),
                )
            },
            saveLabel = SakuStrings.ubah_save,
            saveEnabled = uiState.canSave,
            saveTone = if (uiState.type == TransactionType.Expense) {
                PrimaryButtonTone.Expense
            } else {
                PrimaryButtonTone.Brand
            },
            helperText = null,
            showKelolaKategoriLink = false,
            maxDateMillis = viewModel.todayMillis(),
            bottomReserve = SakuSpace.screenBottom,
            onSave = viewModel::save,
            testTag = TestTags.UBAH_BUTTON_SAVE,
            footer = {
                Spacer(modifier = Modifier.height(SakuSpace.chipGap))
                DestructiveTextButton(
                    text = SakuStrings.ubah_delete,
                    onClick = viewModel::requestDelete,
                    testTag = TestTags.UBAH_BUTTON_DELETE,
                )
            },
        )

        if (uiState.showDeleteSheet) {
            val original = uiState.original
            if (original != null) {
                ConfirmDeleteSheet(
                    title = SakuStrings.delete_sheet_title,
                    body = SakuStrings.delete_sheet_body.format(getCategoryName(original.categoryId),
                        formatRupiah(original.amount),
                        formatShortDate(original.dateEpochMillis)
                    ),
                    onConfirm = viewModel::confirmDelete,
                    onDismissRequest = viewModel::cancelDelete,
                    testTag = TestTags.UBAH_DELETE_SHEET,
                )
            }
        }
    }
}
