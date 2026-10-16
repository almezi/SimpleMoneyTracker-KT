package id.almezi.simplemoneytracker_kt.ui.anggaran

import id.almezi.simplemoneytracker_kt.format

import id.almezi.simplemoneytracker_kt.SakuContainer

import id.almezi.simplemoneytracker_kt.SakuStrings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import id.almezi.simplemoneytracker_kt.ui.components.BudgetProgressRow
import id.almezi.simplemoneytracker_kt.ui.components.ConfirmDeleteSheet
import id.almezi.simplemoneytracker_kt.ui.components.EmptyState
import id.almezi.simplemoneytracker_kt.ui.components.PrimaryButton
import id.almezi.simplemoneytracker_kt.ui.components.SakuCard
import id.almezi.simplemoneytracker_kt.ui.components.ScreenHeader
import id.almezi.simplemoneytracker_kt.ui.components.SectionLabel
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuRadius
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import id.almezi.simplemoneytracker_kt.ui.designsystem.formatMonthLabel
import id.almezi.simplemoneytracker_kt.ui.designsystem.formatRupiah
import id.almezi.simplemoneytracker_kt.ui.designsystem.safeAreaTop
import id.almezi.simplemoneytracker_kt.ui.categoryDisplayName

@Composable
fun AnggaranScreen(
    onBack: () -> Unit,
    onAddBudget: () -> Unit,
    onEditBudget: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // SakuContainer is a @Composable getter, so it must be read here, in the
    // composable body. An `initializer { }` block is not composable and
    // cannot call it.
    val container = SakuContainer
    val viewModel: AnggaranViewModel = viewModel(
        factory = viewModelFactory { initializer { AnggaranViewModel(
            container.budgetRepository,
            container.transactionRepository,
            container.categoryRepository,
            container.clock,
        ) } },
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SakuTheme.colors.bg)
            .testTag(TestTags.ANGGARAN_SCREEN),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .safeAreaTop()
                    .padding(horizontal = SakuSpace.screenHorizontal)
                    .padding(top = SakuSpace.screenHorizontal, bottom = SakuSpace.screenBottom),
                verticalArrangement = Arrangement.spacedBy(SakuSpace.section),
            ) {
                ScreenHeader(
                    title = SakuStrings.anggaran_title,
                    onBack = onBack,
                    action = {
                        Text(
                            text = formatMonthLabel(uiState.month),
                            style = SakuTheme.text.monthLabel,
                            color = SakuTheme.colors.accentText,
                        )
                    },
                )
            }

            if (uiState.isEmpty) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = SakuSpace.screenHorizontal)
                        .padding(bottom = SakuSpace.screenBottom),
                    verticalArrangement = Arrangement.Center,
                ) {
                    EmptyState(
                        title = SakuStrings.anggaran_empty_title,
                        body = SakuStrings.anggaran_empty_body,
                        modifier = Modifier.testTag(TestTags.ANGGARAN_EMPTY_STATE),
                        primaryAction = {
                            PrimaryButton(
                                text = SakuStrings.anggaran_baru,
                                onClick = onAddBudget,
                                testTag = TestTags.ANGGARAN_ADD_BUTTON,
                            )
                        },
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = SakuSpace.screenHorizontal)
                        .testTag(TestTags.ANGGARAN_LIST),
                    verticalArrangement = Arrangement.spacedBy(SakuSpace.cardInner),
                ) {
                    item(key = "total") {
                        SakuCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = SakuSpace.tight)
                                .testTag(TestTags.ANGGARAN_TOTAL_CARD),
                            shape = RoundedCornerShape(SakuRadius.summaryCard),
                        ) {
                            Column {
                                SectionLabel(text = SakuStrings.anggaran_title)
                                Spacer(modifier = Modifier.height(SakuSpace.tight))
                                Text(
                                    text = formatRupiah(uiState.totalSpent),
                                    style = SakuTheme.text.amountLarge,
                                    color = SakuTheme.colors.text,
                                )
                                Text(
                                    text = SakuStrings.anggaran_total_spent.format(formatRupiah(uiState.totalSpent),
                                        formatRupiah(uiState.totalLimit)
                                    ),
                                    style = SakuTheme.text.caption,
                                    color = SakuTheme.colors.textMuted,
                                )
                                Spacer(modifier = Modifier.height(SakuSpace.cardInner))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(SakuSize.barHeight)
                                        .background(
                                            SakuTheme.colors.barTrack,
                                            RoundedCornerShape(SakuRadius.bar)
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(uiState.totalFraction)
                                            .height(SakuSize.barHeight)
                                            .background(
                                                SakuTheme.colors.primary,
                                                RoundedCornerShape(SakuRadius.bar)
                                            )
                                    )
                                }
                            }
                        }
                    }
                    item(key = "rows_header") {
                        Spacer(modifier = Modifier.height(SakuSpace.tight))
                    }
                    items(uiState.rows, key = { it.category.id }) { row ->
                        BudgetProgressRow(
                            categoryName = categoryDisplayName(row.category),
                            spent = row.spent,
                            limit = row.limit,
                            onEdit = { onEditBudget(row.category.id) },
                            onDelete = { viewModel.requestDelete(row) },
                            testTag = TestTags.ANGGARAN_ROW + "_" + row.category.id,
                        )
                    }
                    item(key = "add") {
                        Column(modifier = Modifier.padding(top = SakuSpace.chipGap)) {
                            PrimaryButton(
                                text = SakuStrings.anggaran_baru,
                                onClick = onAddBudget,
                                testTag = TestTags.ANGGARAN_ADD_BUTTON,
                            )
                            Spacer(modifier = Modifier.height(SakuSize.navReserve))
                        }
                    }
                }
            }
        }

        val pending = uiState.pendingDelete
        if (pending != null) {
            ConfirmDeleteSheet(
                title = SakuStrings.anggaran_delete_title.format(categoryDisplayName(pending.category)
                ),
                body = SakuStrings.anggaran_delete_body.format(formatRupiah(pending.limit),
                    categoryDisplayName(pending.category)
                ),
                onConfirm = viewModel::confirmDelete,
                onDismissRequest = viewModel::cancelDelete,
                testTag = TestTags.ANGGARAN_DELETE_SHEET,
            )
        }
    }
}
