package id.almezi.simplemoneytracker_kt.ui.ringkasan

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import id.almezi.simplemoneytracker_kt.R
import id.almezi.simplemoneytracker_kt.SimpleMoneyTrackerApp
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.components.EmptyState
import id.almezi.simplemoneytracker_kt.ui.components.MonthSwitcher
import id.almezi.simplemoneytracker_kt.ui.components.PrimaryButton
import id.almezi.simplemoneytracker_kt.ui.components.SakuCard
import id.almezi.simplemoneytracker_kt.ui.components.SecondaryButton
import id.almezi.simplemoneytracker_kt.ui.components.SectionLabel
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuRadius
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import id.almezi.simplemoneytracker_kt.ui.designsystem.formatCategoryShare
import id.almezi.simplemoneytracker_kt.ui.designsystem.formatMonthLabel
import id.almezi.simplemoneytracker_kt.ui.designsystem.formatRupiah
import id.almezi.simplemoneytracker_kt.ui.designsystem.formatSavingsRate
import id.almezi.simplemoneytracker_kt.ui.designsystem.monthOf
import id.almezi.simplemoneytracker_kt.ui.getCategoryNameRes

@Composable
fun RingkasanScreen(
    modifier: Modifier = Modifier,
    onAddTransaction: () -> Unit = {},
    onOpenAnggaran: () -> Unit = {},
    onOpenKelolaKategori: () -> Unit = {},
    viewModel: RingkasanViewModel = viewModel(
        factory = RingkasanViewModel.Factory(
            (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container.transactionRepository,
            (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container.clock
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SakuTheme.colors.bg)
            .testTag(TestTags.RINGKASAN_SCREEN),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = SakuSpace.screenHorizontal)
                .padding(top = SakuSpace.screenHorizontal, bottom = SakuSize.navReserve + SakuSpace.screenBottom),
            verticalArrangement = Arrangement.spacedBy(SakuSpace.section),
        ) {
            MonthSwitcher(
                label = formatMonthLabel(context, uiState.month),
                onPrevious = viewModel::previousMonth,
                onNext = viewModel::nextMonth,
                nextEnabled = uiState.month < monthOf(viewModel.currentMonthMillis()),
                testTag = TestTags.RINGKASAN_MONTH_SWITCHER,
            )

            if (!uiState.hasTransactions) {
                EmptyState(
                    title = stringResource(R.string.ringkasan_empty_title),
                    body = stringResource(R.string.ringkasan_empty_body),
                    modifier = Modifier.testTag(TestTags.RINGKASAN_EMPTY_STATE),
                    primaryAction = {
                        PrimaryButton(
                            text = stringResource(R.string.ringkasan_empty_action),
                            onClick = onAddTransaction,
                            testTag = TestTags.RINGKASAN_EMPTY_ACTION,
                        )
                    },
                )
                Spacer(modifier = Modifier.height(SakuSize.navReserve))
            } else {
                BalanceCard(uiState = uiState)

                SavingsRow(income = uiState.income, expense = uiState.expense)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
                ) {
                    SecondaryButton(
                        text = stringResource(R.string.ringkasan_anggaran),
                        onClick = onOpenAnggaran,
                        modifier = Modifier.weight(1f),
                        testTag = TestTags.RINGKASAN_ANGGARAN_BUTTON,
                    )
                    SecondaryButton(
                        text = stringResource(R.string.kelola_kategori),
                        onClick = onOpenKelolaKategori,
                        modifier = Modifier.weight(1f),
                        testTag = TestTags.RINGKASAN_KELOLA_BUTTON,
                    )
                }

                ComparisonCard(income = uiState.income, expense = uiState.expense)

                BreakdownCard(
                    title = stringResource(R.string.ringkasan_breakdown_income),
                    rows = uiState.incomeByCategory,
                    typeTotal = uiState.income,
                    accent = SakuTheme.colors.income,
                    testTag = TestTags.RINGKASAN_BREAKDOWN_INCOME,
                )

                BreakdownCard(
                    title = stringResource(R.string.ringkasan_breakdown_expense),
                    rows = uiState.expenseByCategory,
                    typeTotal = uiState.expense,
                    accent = SakuTheme.colors.expense,
                    testTag = TestTags.RINGKASAN_BREAKDOWN_EXPENSE,
                )
            }
        }
    }
}

@Composable
private fun BalanceCard(uiState: RingkasanUiState) {
    SakuCard(
        modifier = Modifier.fillMaxWidth().testTag(TestTags.RINGKASAN_BALANCE_CARD),
        shape = RoundedCornerShape(SakuRadius.summaryCard),
    ) {
        Column {
            SectionLabel(text = stringResource(R.string.ringkasan_saldo))
            Spacer(modifier = Modifier.height(SakuSpace.tight))
            Text(
                text = formatRupiah(uiState.balance),
                style = SakuTheme.text.amountLarge,
                color = when {
                    !uiState.hasTransactions -> SakuTheme.colors.textMuted
                    uiState.isNegative -> SakuTheme.colors.expense
                    else -> SakuTheme.colors.income
                },
                modifier = Modifier.testTag(TestTags.RINGKASAN_BALANCE_VALUE),
            )
            Spacer(modifier = Modifier.height(SakuSpace.cardInner))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
            ) {
                SubTile(
                    label = stringResource(R.string.ringkasan_pemasukan),
                    amount = uiState.income,
                    tint = SakuTheme.colors.incomeSurfaceStrong,
                    accent = SakuTheme.colors.income,
                    testTag = TestTags.RINGKASAN_TOTALS_CARD + "_income",
                    modifier = Modifier.weight(1f),
                )
                SubTile(
                    label = stringResource(R.string.ringkasan_pengeluaran),
                    amount = uiState.expense,
                    tint = SakuTheme.colors.expenseSurface,
                    accent = SakuTheme.colors.expense,
                    testTag = TestTags.RINGKASAN_TOTALS_CARD + "_expense",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SubTile(
    label: String,
    amount: Long,
    tint: Color,
    accent: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(SakuRadius.button),
        color = tint,
        contentColor = SakuTheme.colors.text,
    ) {
        Column(modifier = Modifier.padding(SakuSpace.cardInner)) {
            Text(text = label, style = SakuTheme.text.caption, color = SakuTheme.colors.textMuted)
            Spacer(modifier = Modifier.height(SakuSpace.tight))
            Text(text = formatRupiah(amount), style = SakuTheme.text.amountList, color = accent, maxLines = 1)
        }
    }
}

@Composable
private fun SavingsRow(income: Long, expense: Long) {
    val hasIncome = income > 0L
    SakuCard(
        modifier = Modifier.fillMaxWidth().testTag(TestTags.RINGKASAN_SAVINGS_ROW),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.ringkasan_tingkat_tabungan),
                    style = SakuTheme.text.body,
                    color = SakuTheme.colors.textSecondary,
                )
                if (!hasIncome) {
                    Text(
                        text = stringResource(R.string.ringkasan_saving_hint),
                        style = SakuTheme.text.caption,
                        color = SakuTheme.colors.textMuted,
                    )
                }
            }
            Text(
                text = formatSavingsRate(income, expense),
                style = SakuTheme.text.amountList,
                color = when {
                    !hasIncome -> SakuTheme.colors.textMuted
                    expense > income -> SakuTheme.colors.expense
                    else -> SakuTheme.colors.income
                },
                modifier = Modifier.testTag(TestTags.RINGKASAN_SAVINGS_VALUE),
            )
        }
    }
}

@Composable
private fun ComparisonCard(income: Long, expense: Long) {
    val max = maxOf(income, expense, 1L)
    SakuCard(modifier = Modifier.fillMaxWidth().testTag(TestTags.RINGKASAN_COMPARISON)) {
        Column {
            SectionLabel(text = stringResource(R.string.ringkasan_breakdown_vs))
            Spacer(modifier = Modifier.height(SakuSpace.cardInner))
            BarRow(
                label = stringResource(R.string.ringkasan_pemasukan),
                fraction = income.toFloat() / max.toFloat(),
                amount = income,
                accent = SakuTheme.colors.income,
            )
            Spacer(modifier = Modifier.height(SakuSpace.cardInner))
            BarRow(
                label = stringResource(R.string.ringkasan_pengeluaran),
                fraction = expense.toFloat() / max.toFloat(),
                amount = expense,
                accent = SakuTheme.colors.expense,
            )
        }
    }
}

@Composable
private fun BreakdownCard(
    title: String,
    rows: List<CategoryBreakdown>,
    typeTotal: Long,
    accent: Color,
    testTag: String
) {
    if (rows.isEmpty()) return
    SakuCard(modifier = Modifier.fillMaxWidth().testTag(testTag)) {
        Column {
            SectionLabel(text = title)
            Spacer(modifier = Modifier.height(SakuSpace.cardInner))
            rows.forEach { row ->
                BarRow(
                    label = stringResource(getCategoryNameRes(row.categoryId)),
                    fraction = if (typeTotal <= 0L) 0f else (row.total.toFloat() / typeTotal.toFloat()),
                    amount = row.total,
                    accent = accent,
                    trailing = formatCategoryShare(row.total, typeTotal),
                )
                Spacer(modifier = Modifier.height(SakuSpace.cardInner))
            }
        }
    }
}

@Composable
private fun BarRow(
    label: String,
    fraction: Float,
    amount: Long,
    accent: Color,
    trailing: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = SakuTheme.text.bodySmall,
                color = SakuTheme.colors.textSecondary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            if (trailing != null) {
                Text(
                    text = trailing,
                    style = SakuTheme.text.caption,
                    color = SakuTheme.colors.textMuted,
                )
                Spacer(modifier = Modifier.width(SakuSpace.tight))
            }
            Text(text = formatRupiah(amount), style = SakuTheme.text.amountList, color = accent, maxLines = 1)
        }
        Spacer(modifier = Modifier.height(SakuSpace.tight))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SakuSize.barHeight)
                .background(SakuTheme.colors.barTrack, RoundedCornerShape(SakuRadius.bar)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(SakuSize.barHeight)
                    .background(accent, RoundedCornerShape(SakuRadius.bar)),
            )
        }
    }
}
