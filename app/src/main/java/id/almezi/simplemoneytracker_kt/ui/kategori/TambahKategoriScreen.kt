package id.almezi.simplemoneytracker_kt.ui.kategori

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import id.almezi.simplemoneytracker_kt.R
import id.almezi.simplemoneytracker_kt.SimpleMoneyTrackerApp
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.components.CategoryMark
import id.almezi.simplemoneytracker_kt.ui.components.HelperText
import id.almezi.simplemoneytracker_kt.ui.components.PrimaryButton
import id.almezi.simplemoneytracker_kt.ui.components.ScreenHeader
import id.almezi.simplemoneytracker_kt.ui.components.SegmentedOption
import id.almezi.simplemoneytracker_kt.ui.components.SegmentedToggle
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuCategoryPalette
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuRadius
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType

@Composable
fun TambahKategoriScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TambahKategoriViewModel = viewModel(
        factory = TambahKategoriViewModel.Factory(
            (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container.categoryRepository,
            (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container.clock
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onDone()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SakuTheme.colors.bg)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = SakuSpace.screenHorizontal)
            .padding(top = SakuSpace.screenHorizontal, bottom = SakuSpace.screenBottom)
            .testTag(TestTags.KATEGORI_FORM_SCREEN),
        verticalArrangement = Arrangement.spacedBy(SakuSpace.screenHorizontal),
    ) {
        ScreenHeader(title = stringResource(R.string.kategori_form_new), onBack = onBack)

        SegmentedToggle(
            options = listOf(
                SegmentedOption(stringResource(R.string.type_pengeluaran), TransactionType.Expense),
                SegmentedOption(stringResource(R.string.type_pemasukan), TransactionType.Income),
            ),
            selectedIndex = if (uiState.type == TransactionType.Expense) 0 else 1,
            onSelect = { index ->
                viewModel.updateType(
                    if (index == 1) TransactionType.Income else TransactionType.Expense
                )
            },
            testTag = TestTags.KATEGORI_TOGGLE_TYPE,
        )

        Column {
            Text(
                text = stringResource(R.string.kategori_form_name),
                style = SakuTheme.text.sectionLabel,
                color = SakuTheme.colors.textMuted,
            )
            Spacer(modifier = Modifier.height(SakuSpace.chipGap))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SakuSize.fieldHeight)
                    .testTag(TestTags.KATEGORI_FORM_NAME),
                shape = RoundedCornerShape(SakuRadius.input),
                color = SakuTheme.colors.surface,
                border = androidx.compose.foundation.BorderStroke(
                    SakuSpace.hairline,
                    if (uiState.nameError) SakuTheme.colors.danger else SakuTheme.colors.border,
                ),
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = SakuSpace.screenHorizontal - SakuSpace.tight),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    BasicTextField(
                        value = uiState.name,
                        onValueChange = viewModel::updateName,
                        singleLine = true,
                        textStyle = SakuTheme.text.body.copy(color = SakuTheme.colors.text),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        cursorBrush = SolidColor(SakuTheme.colors.accentText),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (uiState.name.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.kategori_rename_hint),
                                    style = SakuTheme.text.body,
                                    color = SakuTheme.colors.textPlaceholder,
                                )
                            }
                            inner()
                        },
                    )
                }
            }
            if (uiState.nameError) {
                Spacer(modifier = Modifier.height(SakuSpace.tight))
                HelperText(
                    text = stringResource(R.string.kategori_duplicate),
                    color = SakuTheme.colors.danger,
                )
            }
        }

        Column {
            Text(
                text = stringResource(R.string.daftar_kategori_label),
                style = SakuTheme.text.sectionLabel,
                color = SakuTheme.colors.textMuted,
            )
            Spacer(modifier = Modifier.height(SakuSpace.chipGap))
            Row(horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap)) {
                SakuCategoryPalette.hexSwatches.forEach { hex ->
                    ColorSwatch(
                        hex = hex,
                        selected = hex.equals(uiState.colorHex, ignoreCase = true),
                        onClick = { viewModel.selectColor(hex) },
                    )
                }
            }
        }

        PreviewChip(name = uiState.name, categoryId = uiState.colorHex)

        PrimaryButton(
            text = stringResource(R.string.kategori_form_save),
            onClick = viewModel::save,
            enabled = uiState.canSave,
            testTag = TestTags.KATEGORI_FORM_SAVE,
        )
    }
}

@Composable
private fun ColorSwatch(hex: String, selected: Boolean, onClick: () -> Unit) {
    val color = runCatching { Color(android.graphics.Color.parseColor(hex)) }
        .getOrDefault(SakuTheme.colors.primary)
    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(SakuSize.mark + SakuSpace.cardInner)
            .testTag(TestTags.KATEGORI_FORM_SWATCH + "_" + hex),
        shape = RoundedCornerShape(SakuRadius.buttonSmall),
        color = if (selected) SakuTheme.colors.accentSurface else Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            SakuSpace.hairline,
            if (selected) SakuTheme.colors.accentText else SakuTheme.colors.borderStrong,
        ),
    ) {
        Box(
            modifier = Modifier.padding(SakuSpace.tight),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.size(SakuSize.mark),
                shape = RoundedCornerShape(SakuRadius.mark),
                color = color,
                content = {},
            )
        }
    }
}

@Composable
private fun PreviewChip(name: String, categoryId: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TestTags.KATEGORI_FORM_PREVIEW),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryMark(categoryId = categoryId.ifBlank { "preview" }, size = SakuSize.mark)
        Spacer(modifier = Modifier.size(SakuSpace.chipGap))
        Text(
            text = name.ifBlank { stringResource(R.string.kategori_form_new) },
            style = SakuTheme.text.chip,
            color = SakuTheme.colors.textSecondary,
        )
    }
}
