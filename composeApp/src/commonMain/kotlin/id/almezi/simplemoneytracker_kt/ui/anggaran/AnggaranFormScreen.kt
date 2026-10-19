package id.almezi.simplemoneytracker_kt.ui.anggaran

import id.almezi.simplemoneytracker_kt.format

import id.almezi.simplemoneytracker_kt.SakuContainer

import id.almezi.simplemoneytracker_kt.SakuStrings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
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
import id.almezi.simplemoneytracker_kt.ui.designsystem.safeAreaBottom
import id.almezi.simplemoneytracker_kt.ui.designsystem.safeAreaTop
import id.almezi.simplemoneytracker_kt.ui.categoryDisplayName
import id.almezi.simplemoneytracker_kt.ui.getCategoryName

@Composable
fun AnggaranFormScreen(
    categoryIdArg: String?,
    onDone: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // SakuContainer is a @Composable getter, so it must be read here, in the
    // composable body. An `initializer { }` block is not composable and
    // cannot call it.
    val container = SakuContainer
    val viewModel: AnggaranFormViewModel = viewModel(
        factory = viewModelFactory { initializer { AnggaranFormViewModel(
            container.budgetRepository,
            container.categoryRepository,
            container.clock,
            categoryIdArg,
        ) } },
    )

    val uiState by viewModel.uiState.collectAsState()
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
                .safeAreaTop()
                .safeAreaBottom()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SakuSpace.screenHorizontal)
                .padding(top = SakuSpace.screenHorizontal, bottom = SakuSpace.screenBottom),
            verticalArrangement = Arrangement.spacedBy(SakuSpace.screenHorizontal),
        ) {
            ScreenHeader(
                title = if (uiState.editing) SakuStrings.anggaran_form_edit else SakuStrings.anggaran_form_new,
                onBack = onBack,
            )

            SelectField(
                value = uiState.categories
                    .firstOrNull { it.id == uiState.selectedCategoryId }
                    ?.let { categoryDisplayName(it) }
                    .orEmpty(),
                onClick = { if (!uiState.categoryLocked) showCategorySheet = true },
                label = SakuStrings.anggaran_form_category,
                placeholder = SakuStrings.anggaran_pilih_kategori,
                trailingIcon = if (uiState.categoryLocked) null else {
                    {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = SakuStrings.cd_open_picker,
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
                label = SakuStrings.anggaran_form_limit,
                testTag = TestTags.ANGGARAN_FORM_LIMIT,
            )

            HelperText(
                text = (if (uiState.editing) SakuStrings.anggaran_form_note_edit
                        else SakuStrings.anggaran_form_note_new)
                    .format(formatMonthLabel(month)),
                modifier = Modifier.testTag(TestTags.ANGGARAN_FORM_NOTE),
            )

            SwitchRow(
                label = SakuStrings.anggaran_form_switch,
                checked = uiState.alertAt80,
                onCheckedChange = viewModel::setAlert,
                testTag = TestTags.ANGGARAN_FORM_SWITCH,
            )

            PrimaryButton(
                text = SakuStrings.anggaran_form_save,
                onClick = viewModel::save,
                enabled = uiState.canSave,
                testTag = TestTags.ANGGARAN_FORM_SAVE,
            )

            if (uiState.editing) {
                DestructiveTextButton(
                    text = SakuStrings.anggaran_form_delete,
                    onClick = viewModel::requestDelete,
                    testTag = TestTags.ANGGARAN_FORM_DELETE,
                )
            }
        }

        if (showCategorySheet) {
            SakuPickerSheet(
                title = SakuStrings.anggaran_pilih_kategori,
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
                ?: getCategoryName(budget.categoryId)
            ConfirmDeleteSheet(
                title = SakuStrings.anggaran_delete_title.format(categoryName),
                body = SakuStrings.anggaran_delete_body.format(formatRupiah(budget.limitAmount),
                    categoryName
                ),
                onConfirm = viewModel::confirmDelete,
                onDismissRequest = viewModel::cancelDelete,
                testTag = TestTags.ANGGARAN_FORM_DELETE_SHEET,
            )
        }
    }
}