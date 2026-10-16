package id.almezi.simplemoneytracker_kt.ui.transaction

import id.almezi.simplemoneytracker_kt.SakuStrings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.data.Account
import id.almezi.simplemoneytracker_kt.data.Category
import id.almezi.simplemoneytracker_kt.ui.components.AmountInput
import id.almezi.simplemoneytracker_kt.ui.components.CategoryChipItem
import id.almezi.simplemoneytracker_kt.ui.components.CategoryChipRow
import id.almezi.simplemoneytracker_kt.ui.components.CategoryMark
import id.almezi.simplemoneytracker_kt.ui.components.CategoryPickerSheet
import id.almezi.simplemoneytracker_kt.ui.components.HelperText
import id.almezi.simplemoneytracker_kt.ui.components.NoteField
import id.almezi.simplemoneytracker_kt.ui.components.PrimaryButton
import id.almezi.simplemoneytracker_kt.ui.components.PrimaryButtonTone
import id.almezi.simplemoneytracker_kt.ui.components.SakuPickerOption
import id.almezi.simplemoneytracker_kt.ui.components.SakuPickerOptionGroup
import id.almezi.simplemoneytracker_kt.ui.components.SakuPickerSheet
import id.almezi.simplemoneytracker_kt.ui.components.SectionLabel
import id.almezi.simplemoneytracker_kt.ui.components.SegmentedOption
import id.almezi.simplemoneytracker_kt.ui.components.SegmentedToggle
import id.almezi.simplemoneytracker_kt.ui.components.SelectField
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuRadius
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import id.almezi.simplemoneytracker_kt.ui.designsystem.formatShortDate
import id.almezi.simplemoneytracker_kt.ui.designsystem.safeAreaTop
import id.almezi.simplemoneytracker_kt.ui.categoryDisplayName

data class TransactionFormState(
    val type: TransactionType,
    val amountDigits: String,
    val categories: List<Category>,
    val selectedCategoryId: String?,
    val accounts: List<Account>,
    val selectedAccountId: String,
    val dateMillis: Long,
    val note: String
)

data class TransactionFormActions(
    val onTypeChange: (TransactionType) -> Unit,
    val onAmountChange: (String) -> Unit,
    val onCategoryChange: (String) -> Unit,
    val onAccountChange: (String) -> Unit,
    val onDateChange: (Long) -> Unit,
    val onNoteChange: (String) -> Unit
)

@Composable
fun TransactionForm(
    state: TransactionFormState,
    actions: TransactionFormActions,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    saveLabel: String,
    saveEnabled: Boolean,
    helperText: String?,
    saveTone: PrimaryButtonTone = PrimaryButtonTone.Brand,
    showKelolaKategoriLink: Boolean = true,
    showQuickCategoryChips: Boolean = true,
    onOpenKelolaKategori: () -> Unit = {},
    onSave: () -> Unit,
    maxDateMillis: Long,
    bottomReserve: Dp,
    footer: (@Composable () -> Unit)? = null,
    testTag: String
) {
    var showAccountSheet by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var datePickerOpen by remember { mutableStateOf(false) }
    val isExpense = state.type == TransactionType.Expense

    val selectedCategory = state.categories.firstOrNull { it.id == state.selectedCategoryId }
    val selectedCategoryName = selectedCategory?.let { categoryDisplayName(it) } ?: ""

    val quickCategoryIds = if (isExpense) {
        listOf("exp_daily_food", "exp_daily_groceries", "exp_transport_fuel", "exp_daily_household", "exp_other")
    } else {
        listOf("inc_salary", "inc_freelance", "inc_bonus_thr", "inc_other")
    }
    val quickItems = if (showQuickCategoryChips) {
        state.categories
            .filter { it.id in quickCategoryIds }
            .map { CategoryChipItem(id = it.id, label = categoryDisplayName(it)) }
    } else {
        emptyList()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SakuTheme.colors.bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .safeAreaTop()
                .padding(horizontal = SakuSpace.screenHorizontal)
                .padding(top = SakuSpace.screenHorizontal, bottom = bottomReserve),
        ) {
            if (header != null) {
                header()
                Spacer(modifier = Modifier.height(SakuSpace.screenHorizontal))
            }

            SegmentedToggle(
                options = listOf(
                    SegmentedOption(SakuStrings.type_pemasukan, TransactionType.Income),
                    SegmentedOption(SakuStrings.type_pengeluaran, TransactionType.Expense),
                ),
                selectedIndex = if (isExpense) 1 else 0,
                onSelect = { index ->
                    actions.onTypeChange(
                        if (index == 1) TransactionType.Expense else TransactionType.Income
                    )
                },
            )

            Spacer(modifier = Modifier.height(SakuSpace.screenHorizontal))

            AmountInput(
                digits = state.amountDigits,
                onDigitsChange = actions.onAmountChange,
                type = state.type,
                label = SakuStrings.label_jumlah,
                testTag = TestTags.TAMBAH_INPUT_AMOUNT,
            )

            Spacer(modifier = Modifier.height(SakuSpace.screenHorizontal))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionLabel(text = SakuStrings.label_kategori)
                Spacer(modifier = Modifier.weight(1f))
                if (showKelolaKategoriLink) {
                    KelolaKategoriLink(onClick = onOpenKelolaKategori)
                }
            }

            Spacer(modifier = Modifier.height(SakuSpace.cardInner))

            SelectField(
                value = selectedCategoryName,
                onClick = { showCategorySheet = true },
                placeholder = SakuStrings.pilih_kategori_title,
                leadingIcon = selectedCategory?.let { cat ->
                    { CategoryMark(categoryId = cat.id, size = SakuSize.markSmall) }
                },
                trailingIcon = { FieldChevron() },
                modifier = Modifier.fillMaxWidth(),
                testTag = TestTags.TAMBAH_CATEGORY_PAGER,
            )

            if (quickItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(SakuSpace.cardInner))
                CategoryChipRow(
                    items = quickItems,
                    selectedId = state.selectedCategoryId,
                    onSelect = actions.onCategoryChange,
                    type = state.type,
                    testTag = TestTags.TAMBAH_CATEGORY_ITEM,
                )
            }

            Spacer(modifier = Modifier.height(SakuSpace.screenHorizontal))

            Row(modifier = Modifier.fillMaxWidth()) {
                SelectField(
                    value = state.accounts.firstOrNull { it.id == state.selectedAccountId }?.name.orEmpty(),
                    onClick = { showAccountSheet = true },
                    label = SakuStrings.label_akun,
                    placeholder = SakuStrings.pilih_akun_title,
                    trailingIcon = { FieldChevron() },
                    modifier = Modifier.weight(1f),
                    testTag = TestTags.TAMBAH_SELECT_ACCOUNT,
                )
                Spacer(modifier = Modifier.width(SakuSpace.chipGap))
                SelectField(
                    value = formatShortDate(state.dateMillis),
                    onClick = {
                        datePickerOpen = true
                    },
                    label = SakuStrings.label_tanggal,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = SakuTheme.colors.textMuted,
                        )
                    },
                    modifier = Modifier.weight(1f),
                    testTag = TestTags.TAMBAH_SELECT_DATE,
                )
            }

            Spacer(modifier = Modifier.height(SakuSpace.chipGap))

            NoteField(
                value = state.note,
                onValueChange = actions.onNoteChange,
                placeholder = SakuStrings.note_placeholder,
                testTag = TestTags.TAMBAH_INPUT_NOTE,
            )

            Spacer(modifier = Modifier.height(SakuSpace.screenHorizontal))

            if (helperText != null) {
                HelperText(text = helperText, modifier = Modifier.testTag(TestTags.TAMBAH_HELPER))
            } else {
                Spacer(modifier = Modifier.height(SakuSize.minTouchTarget))
            }

            Spacer(modifier = Modifier.height(SakuSpace.chipGap))

            PrimaryButton(
                text = saveLabel,
                onClick = onSave,
                enabled = saveEnabled,
                tone = saveTone,
                testTag = testTag,
            )

            if (footer != null) {
                footer()
            }
        }

        if (showCategorySheet) {
            // One untitled group: custom categories have no groupId, so grouping
            // them would strand them under whatever heading the fallback picks.
            val categoryGroups = listOf(
                SakuPickerOptionGroup(
                    title = "",
                    options = state.categories.map { category ->
                        SakuPickerOption(
                            id = category.id,
                            label = categoryDisplayName(category),
                            mark = { CategoryMark(categoryId = category.id, size = SakuSize.markSmall) }
                        )
                    }
                )
            )

            CategoryPickerSheet(
                title = SakuStrings.pilih_kategori_title,
                groups = categoryGroups,
                selectedId = state.selectedCategoryId,
                onSelect = actions.onCategoryChange,
                onDismissRequest = { showCategorySheet = false },
                testTag = TestTags.TAMBAH_CATEGORY_ITEM,
            )
        }

        if (showAccountSheet) {
            SakuPickerSheet(
                title = SakuStrings.pilih_akun_title,
                options = state.accounts.map { SakuPickerOption(id = it.id, label = it.name) },
                selectedId = state.selectedAccountId,
                onSelect = actions.onAccountChange,
                onDismissRequest = { showAccountSheet = false },
                testTag = TestTags.TAMBAH_ACCOUNT_SHEET,
            )
        }
    }

    if (datePickerOpen) {
        SakuDatePickerDialog(
            initialMillis = state.dateMillis,
            maxMillis = maxDateMillis,
            onDismiss = { datePickerOpen = false },
            onDateSelected = actions.onDateChange,
        )
    }
}

@Composable
fun KelolaKategoriLink(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .height(SakuSize.minTouchTarget)
            .testTag(TestTags.TAMBAH_LINK_KELOLA_KATEGORI),
        shape = RoundedCornerShape(SakuRadius.buttonSmall),
        color = SakuTheme.colors.surfaceRaised,
        contentColor = SakuTheme.colors.accentText,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = SakuSpace.chipGap),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = SakuStrings.kelola_kategori,
                style = SakuTheme.text.buttonSecondary,
                color = SakuTheme.colors.accentText,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FieldChevron() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = SakuStrings.cd_open_picker,
        tint = SakuTheme.colors.textMuted,
        modifier = Modifier.padding(SakuSpace.tight),
    )
}

