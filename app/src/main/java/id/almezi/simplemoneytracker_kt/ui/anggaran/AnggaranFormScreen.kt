package id.almezi.simplemoneytracker_kt.ui.anggaran

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import id.almezi.simplemoneytracker_kt.R
import id.almezi.simplemoneytracker_kt.SimpleMoneyTrackerApp
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.components.AmountInput
import id.almezi.simplemoneytracker_kt.ui.components.ConfirmDeleteSheet
import id.almezi.simplemoneytracker_kt.ui.components.DestructiveTextButton
import id.almezi.simplemoneytracker_kt.ui.components.HelperText
import id.almezi.simplemoneytracker_kt.ui.components.PrimaryButton
import id.almezi.simplemoneytracker_kt.ui.components.SakuPickerOption
import id.almezi.simplemoneytracker_kt.ui.components.SakuPickerSheet
import id.almezi.simplemoneytracker_kt.ui.components.ScreenHeader
import id.almezi.simplemoneytracker_kt.ui.components.SelectField
import id.almezi.simplemoneytracker_kt.ui.components.SwitchRow
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import id.almezi.simplemoneytracker_kt.ui.designsystem.formatMonthLabel
import id.almezi.simplemoneytracker_kt.ui.designsystem.formatRupiah
import id.almezi.simplemoneytracker_kt.ui.designsystem.monthOf
import id.almezi.simplemoneytracker_kt.ui.categoryDisplayName
import id.almezi.simplemoneytracker_kt.ui.getCategoryNameRes

@Composable
fun AnggaranFormScreen(
    categoryIdArg: String?,
    onDone: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnggaranFormViewModel = viewModel(
        factory = AnggaranFormViewModel.Factory(
            (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container.budgetRepository,
            (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container.categoryRepository,
            (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container.clock,
            categoryIdArg
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showCategorySheet by remember { mutableStateOf(false) }
    val month = monthOf(viewModel.todayMillis())

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onDone()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SakuTheme.colors.bg)
            .testTag(TestTags.ANGGARAN_FORM_SCREEN),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = SakuSpace.screenHorizontal)
                .padding(top = SakuSpace.screenHorizontal, bottom = SakuSpace.screenBottom),
            verticalArrangement = Arrangement.spacedBy(SakuSpace.screenHorizontal),
        ) {
            ScreenHeader(
                title = stringResource(
                    if (uiState.editing) R.string.anggaran_form_edit else R.string.anggaran_form_new
                ),
                onBack = onBack,
            )

            SelectField(
                value = uiState.categories
                    .firstOrNull { it.id == uiState.selectedCategoryId }
                    ?.let { categoryDisplayName(it) }
                    .orEmpty(),
                onClick = { if (!uiState.categoryLocked) showCategorySheet = true },
                label = stringResource(R.string.anggaran_form_category),
                placeholder = stringResource(R.string.anggaran_pilih_kategori),
                trailingIcon = if (uiState.categoryLocked) null else {
                    {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.cd_open_picker),
                            tint = SakuTheme.colors.textMuted,
                        )
                    }
                },
                testTag = TestTags.ANGGARAN_FORM_CATEGORY,
            )

            AmountInput(
                digits = uiState.amountDigits,
                onDigitsChange = viewModel::updateAmount,
                type = TransactionType.Expense,
                label = stringResource(R.string.anggaran_form_limit),
                testTag = TestTags.ANGGARAN_FORM_LIMIT,
            )

            HelperText(
                text = stringResource(
                    if (uiState.editing) R.string.anggaran_form_note_edit else R.string.anggaran_form_note_new,
                    formatMonthLabel(context, month)
                ),
                modifier = Modifier.testTag(TestTags.ANGGARAN_FORM_NOTE),
            )

            SwitchRow(
                label = stringResource(R.string.anggaran_form_switch),
                checked = uiState.alertAt80,
                onCheckedChange = viewModel::setAlert,
                testTag = TestTags.ANGGARAN_FORM_SWITCH,
            )

            PrimaryButton(
                text = stringResource(R.string.anggaran_form_save),
                onClick = viewModel::save,
                enabled = uiState.canSave,
                testTag = TestTags.ANGGARAN_FORM_SAVE,
            )

            if (uiState.editing) {
                DestructiveTextButton(
                    text = stringResource(R.string.anggaran_form_delete),
                    onClick = viewModel::requestDelete,
                    testTag = TestTags.ANGGARAN_FORM_DELETE,
                )
            }
        }

        if (showCategorySheet) {
            SakuPickerSheet(
                title = stringResource(R.string.anggaran_pilih_kategori),
                options = uiState.categories.map {
                    SakuPickerOption(id = it.id, label = categoryDisplayName(it))
                },
                selectedId = uiState.selectedCategoryId,
                onSelect = viewModel::selectCategory,
                onDismissRequest = { showCategorySheet = false },
                testTag = TestTags.ANGGARAN_FORM_CATEGORY + "_sheet",
            )
        }

        val budget = uiState.budget
        if (uiState.showDeleteSheet && budget != null) {
            val categoryName = uiState.categories.firstOrNull { it.id == budget.categoryId }
                ?.let { categoryDisplayName(it) }
                ?: stringResource(getCategoryNameRes(budget.categoryId))
            ConfirmDeleteSheet(
                title = stringResource(R.string.anggaran_delete_title, categoryName),
                body = stringResource(
                    R.string.anggaran_delete_body,
                    formatRupiah(budget.limitAmount),
                    categoryName
                ),
                onConfirm = viewModel::confirmDelete,
                onDismissRequest = viewModel::cancelDelete,
            )
        }
    }
}