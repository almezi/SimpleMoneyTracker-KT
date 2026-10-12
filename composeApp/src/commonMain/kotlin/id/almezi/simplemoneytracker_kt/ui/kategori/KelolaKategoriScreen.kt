package id.almezi.simplemoneytracker_kt.ui.kategori

import id.almezi.simplemoneytracker_kt.format

import id.almezi.simplemoneytracker_kt.SakuContainer

import id.almezi.simplemoneytracker_kt.SakuStrings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.components.CategoryMark
import id.almezi.simplemoneytracker_kt.ui.components.ConfirmDeleteSheet
import id.almezi.simplemoneytracker_kt.ui.components.DestructiveButton
import id.almezi.simplemoneytracker_kt.ui.components.HelperText
import id.almezi.simplemoneytracker_kt.ui.components.PrimaryButton
import id.almezi.simplemoneytracker_kt.ui.components.SakuCard
import id.almezi.simplemoneytracker_kt.ui.components.ScreenHeader
import id.almezi.simplemoneytracker_kt.ui.components.SecondaryButton
import id.almezi.simplemoneytracker_kt.ui.components.SegmentedOption
import id.almezi.simplemoneytracker_kt.ui.components.SegmentedToggle
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuRadius
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import id.almezi.simplemoneytracker_kt.ui.designsystem.safeAreaTop
import id.almezi.simplemoneytracker_kt.ui.categoryDisplayName

@Composable
fun KelolaKategoriScreen(
    onBack: () -> Unit,
    onAddCategory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // SakuContainer is a @Composable getter, so it must be read here, in the
    // composable body. An `initializer { }` block is not composable and
    // cannot call it.
    val container = SakuContainer
    val viewModel: KelolaKategoriViewModel = viewModel(
        factory = viewModelFactory { initializer { KelolaKategoriViewModel(
            container.categoryRepository,
            container.clock,
        ) } },
    )

    val uiState by viewModel.uiState.collectAsState()
    var renaming by remember { mutableStateOf<CategoryRow?>(null) }
    var renameText by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SakuTheme.colors.bg)
            .testTag(TestTags.KATEGORI_SCREEN),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeAreaTop()
                .padding(horizontal = SakuSpace.screenHorizontal)
                .padding(top = SakuSpace.screenHorizontal),
        ) {
            ScreenHeader(title = SakuStrings.kategori_title, onBack = onBack)

            Spacer(modifier = Modifier.height(SakuSpace.screenHorizontal))

            SegmentedToggle(
                options = listOf(
                    SegmentedOption(SakuStrings.type_pengeluaran, TransactionType.Expense),
                    SegmentedOption(SakuStrings.type_pemasukan, TransactionType.Income),
                ),
                selectedIndex = if (uiState.type == TransactionType.Expense) 0 else 1,
                onSelect = { index ->
                    viewModel.updateType(
                        if (index == 1) TransactionType.Income else TransactionType.Expense
                    )
                },
                testTag = TestTags.KATEGORI_TOGGLE_TYPE,
            )

            Spacer(modifier = Modifier.height(SakuSpace.section))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(SakuSpace.cardInner),
            ) {
                items(uiState.rows, key = { it.category.id }) { row ->
                    CategoryRowCard(
                        row = row,
                        onRename = {
                            renaming = row
                            renameText = row.displayName.orEmpty()
                        },
                        onToggleHidden = { viewModel.setHidden(row.category.id, !row.isHidden) },
                        onDelete = { viewModel.requestDelete(row) },
                    )
                }
                item(key = "hint") {
                    HelperText(
                        text = SakuStrings.kategori_hint,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = SakuSpace.tight)
                            .testTag(TestTags.KATEGORI_HINT),
                    )
                }
                item(key = "add") {
                    Column(modifier = Modifier.padding(top = SakuSpace.chipGap, bottom = SakuSize.navReserve)) {
                        PrimaryButton(
                            text = SakuStrings.kategori_form_new,
                            onClick = onAddCategory,
                            testTag = TestTags.KATEGORI_ADD_BUTTON,
                        )
                    }
                }
            }
        }

        if (renaming != null) {
            RenameCategoryDialog(
                initialName = renameText,
                onDismiss = { renaming = null },
                onConfirm = { name ->
                    renaming?.let { viewModel.rename(it.category.id, name) }
                    renaming = null
                },
            )
        }

        val pending = uiState.pendingDelete
        if (pending != null) {
            val categoryName = categoryDisplayName(pending.row.category)
            ConfirmDeleteSheet(
                title = SakuStrings.kategori_delete_title.format(categoryName),
                body = if (pending.usageCount == 0) {
                    SakuStrings.kategori_delete_unused
                } else {
                    SakuStrings.kategori_delete_used.format(pending.usageCount,
                        pending.fallbackName.ifBlank { categoryName })
                },
                onConfirm = viewModel::confirmDelete,
                onDismissRequest = viewModel::cancelDelete,
            )
        }
    }
}

@Composable
private fun CategoryRowCard(
    row: CategoryRow,
    onRename: () -> Unit,
    onToggleHidden: () -> Unit,
    onDelete: () -> Unit,
) {
    val name = categoryDisplayName(row.category)
    SakuCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TestTags.KATEGORI_ROW + "_" + row.category.id)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryMark(categoryId = row.category.id, size = SakuSize.mark)
                Spacer(modifier = Modifier.width(SakuSpace.tight * 2))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = SakuTheme.text.bodyStrong,
                        color = SakuTheme.colors.text,
                    )
                    Text(
                        text = buildString {
                            append(
                                if (row.isDefault) SakuStrings.kategori_bawaan
                                    else SakuStrings.kategori_buatan_sendiri
                            )
                            append(" · ")
                            append(SakuStrings.kategori_dipakai.format(row.usageCount))
                        },
                        style = SakuTheme.text.caption,
                        color = SakuTheme.colors.textMuted,
                    )
                }
            }
            Spacer(modifier = Modifier.height(SakuSpace.cardInner))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
            ) {
                SecondaryButton(
                    text = SakuStrings.action_ubah,
                    onClick = onRename,
                    modifier = Modifier.weight(1f),
                    height = SakuSize.rowButtonHeight,
                    testTag = TestTags.KATEGORI_ROW_EDIT + "_" + row.category.id,
                )
                SecondaryButton(
                    text = if (row.isHidden) SakuStrings.kategori_tampilkan else SakuStrings.kategori_sembunyikan,
                    onClick = onToggleHidden,
                    modifier = Modifier.weight(1f),
                    height = SakuSize.rowButtonHeight,
                    testTag = TestTags.KATEGORI_ROW_HIDE + "_" + row.category.id,
                )
                if (!row.isDefault) {
                    DestructiveButton(
                        text = SakuStrings.delete,
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        height = SakuSize.rowButtonHeight,
                        testTag = TestTags.KATEGORI_ROW_DELETE + "_" + row.category.id,
                    )
                }
            }
        }
    }
}

@Composable
private fun RenameCategoryDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialName) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(SakuStrings.kategori_rename_title) },
        text = {
            androidx.compose.foundation.text.BasicTextField(
                value = text,
                onValueChange = { if (it.length <= CATEGORY_NAME_MAX) text = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag(TestTags.KATEGORI_FORM_NAME),
                decorationBox = { inner ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                SakuTheme.colors.surface,
                                RoundedCornerShape(SakuRadius.input)
                            )
                            .padding(SakuSpace.chipGap)
                    ) {
                        if (text.isEmpty()) {
                            Text(
                                text = SakuStrings.kategori_rename_hint,
                                style = SakuTheme.text.body,
                                color = SakuTheme.colors.textPlaceholder,
                            )
                        }
                        inner()
                    }
                },
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = { onConfirm(text) },
                modifier = Modifier.testTag(TestTags.KATEGORI_FORM_SAVE),
            ) { Text(SakuStrings.action_ubah) }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(SakuStrings.cancel)
            }
        },
    )
}
