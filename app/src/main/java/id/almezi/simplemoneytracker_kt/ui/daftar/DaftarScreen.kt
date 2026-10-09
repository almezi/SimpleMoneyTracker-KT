package id.almezi.simplemoneytracker_kt.ui.daftar

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import id.almezi.simplemoneytracker_kt.data.Transaction
import id.almezi.simplemoneytracker_kt.ui.components.ConfirmDeleteSheet
import id.almezi.simplemoneytracker_kt.ui.components.EmptyState
import id.almezi.simplemoneytracker_kt.ui.components.MonthSwitcher
import id.almezi.simplemoneytracker_kt.ui.components.PrimaryButton
import id.almezi.simplemoneytracker_kt.ui.components.SakuCard
import id.almezi.simplemoneytracker_kt.ui.components.SakuToast
import id.almezi.simplemoneytracker_kt.ui.components.SakuToastHost
import id.almezi.simplemoneytracker_kt.ui.components.SecondaryButton
import id.almezi.simplemoneytracker_kt.ui.components.SectionLabel
import id.almezi.simplemoneytracker_kt.ui.components.TransactionCard
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuMotion
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import id.almezi.simplemoneytracker_kt.ui.designsystem.dayName
import id.almezi.simplemoneytracker_kt.ui.designsystem.formatMonthLabel
import id.almezi.simplemoneytracker_kt.ui.designsystem.formatRupiah
import id.almezi.simplemoneytracker_kt.ui.designsystem.formatShortDate
import id.almezi.simplemoneytracker_kt.ui.designsystem.monthOf
import id.almezi.simplemoneytracker_kt.ui.getCategoryNameRes

@Composable
fun DaftarScreen(
    modifier: Modifier = Modifier,
    onAddTransaction: () -> Unit = {},
    onOpenBackup: () -> Unit = {},
    onEditTransaction: (Long) -> Unit = {},
    viewModel: DaftarViewModel = viewModel(
        factory = DaftarViewModel.Factory(
            (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container.transactionRepository,
            (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container.clock
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val undoToast = uiState.undoCandidate?.let {
        SakuToast(
            text = context.getString(R.string.toast_transaksi_dihapus),
            actionLabel = context.getString(R.string.action_urungkan),
            onAction = viewModel::undoDelete,
            durationMillis = SakuMotion.undoToastMillis
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SakuTheme.colors.bg)
            .testTag(TestTags.DAFTAR_SCREEN),
    ) {
        if (uiState.isEmpty) {
            DaftarEmptyState(
                monthLabel = formatMonthLabel(context, uiState.month),
                nextEnabled = uiState.month < monthOf(viewModel.currentMonthMillis()),
                onPrevious = viewModel::previousMonth,
                onNext = viewModel::nextMonth,
                onAddTransaction = onAddTransaction,
                onOpenBackup = onOpenBackup,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = SakuSpace.screenHorizontal)
                    .padding(bottom = SakuSize.navReserve + SakuSpace.screenBottom),
                verticalArrangement = Arrangement.spacedBy(SakuSpace.section),
            ) {
                item(key = "header") {
                    DaftarHeader(
                        monthLabel = formatMonthLabel(context, uiState.month),
                        nextEnabled = uiState.month < monthOf(viewModel.currentMonthMillis()),
                        onPrevious = viewModel::previousMonth,
                        onNext = viewModel::nextMonth,
                        incomeTotal = uiState.incomeTotal,
                        expenseTotal = uiState.expenseTotal,
                        onOpenBackup = onOpenBackup,
                        modifier = Modifier.padding(top = SakuSpace.screenHorizontal)
                    )
                }
                items(uiState.days, key = { it.dateMillis }) { day ->
                    DayGroup(day = day, onEdit = onEditTransaction, onDelete = viewModel::requestDelete)
                }
            }
        }

        SakuToastHost(
            toast = undoToast,
            onDismiss = viewModel::clearUndo,
        )

        val pending = uiState.pendingDelete
        if (pending != null) {
            ConfirmDeleteSheet(
                title = stringResource(R.string.delete_sheet_title),
                body = stringResource(
                    R.string.delete_sheet_body,
                    stringResource(getCategoryNameRes(pending.categoryId)),
                    formatRupiah(pending.amount),
                    formatShortDate(context, pending.dateEpochMillis)
                ),
                onConfirm = viewModel::confirmDelete,
                onDismissRequest = viewModel::cancelDelete,
            )
        }
    }
}

@Composable
private fun DaftarHeader(
    monthLabel: String,
    nextEnabled: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    incomeTotal: Long,
    expenseTotal: Long,
    onOpenBackup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        MonthSwitcher(
            label = monthLabel,
            onPrevious = onPrevious,
            onNext = onNext,
            nextEnabled = nextEnabled,
            testTag = TestTags.DAFTAR_MONTH_SWITCHER,
        )
        Spacer(modifier = Modifier.height(SakuSpace.screenHorizontal))
        SummaryStrip(incomeTotal = incomeTotal, expenseTotal = expenseTotal)
        Spacer(modifier = Modifier.height(SakuSpace.chipGap))
        SecondaryButton(
            text = stringResource(R.string.daftar_backup_button),
            onClick = onOpenBackup,
            modifier = Modifier.fillMaxWidth(),
            testTag = TestTags.DAFTAR_BACKUP_BUTTON,
        )
    }
}

@Composable
private fun SummaryStrip(incomeTotal: Long, expenseTotal: Long) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap)) {
        SummaryTile(
            label = stringResource(R.string.daftar_masuk),
            amount = incomeTotal,
            accent = SakuTheme.colors.income,
            muted = incomeTotal == 0L,
            testTag = TestTags.DAFTAR_SUMMARY_MASUK,
            modifier = Modifier.weight(1f),
        )
        SummaryTile(
            label = stringResource(R.string.daftar_keluar),
            amount = expenseTotal,
            accent = SakuTheme.colors.expense,
            muted = expenseTotal == 0L,
            testTag = TestTags.DAFTAR_SUMMARY_KELUAR,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryTile(
    label: String,
    amount: Long,
    accent: Color,
    muted: Boolean,
    testTag: String,
    modifier: Modifier = Modifier
) {
    SakuCard(
        modifier = modifier.testTag(testTag),
        color = SakuTheme.colors.surface,
        border = BorderStroke(SakuSpace.hairline, SakuTheme.colors.border),
    ) {
        Column {
            SectionLabel(text = label)
            Spacer(modifier = Modifier.height(SakuSpace.tight))
            Text(
                text = formatRupiah(amount),
                style = SakuTheme.text.amountSmall,
                color = if (muted) SakuTheme.colors.textMuted else accent,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DayGroup(
    day: TransactionDay,
    onEdit: (Long) -> Unit,
    onDelete: (Transaction) -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SakuSpace.cardInner),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.DAFTAR_DAY_HEADER + "_" + day.dateMillis),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SectionLabel(text = dayName(context, day.dateMillis))
                Text(
                    text = formatShortDate(context, day.dateMillis),
                    style = SakuTheme.text.bodySmall,
                    color = SakuTheme.colors.textSecondary,
                )
            }
            Text(
                text = formatRupiah(day.netTotal),
                style = SakuTheme.text.amountList,
                color = if (day.netTotal < 0L) SakuTheme.colors.expense else SakuTheme.colors.income,
            )
        }
        day.transactions.forEach { transaction ->
            TransactionCard(
                categoryId = transaction.categoryId,
                categoryName = stringResource(getCategoryNameRes(transaction.categoryId)),
                typeLabel = stringResource(
                    if (transaction.type == "pemasukan") R.string.type_pemasukan else R.string.type_pengeluaran
                ),
                amount = transaction.amount,
                type = TransactionType.fromStorage(transaction.type),
                onEdit = { onEdit(transaction.id) },
                onDelete = { onDelete(transaction) },
                testTag = TestTags.DAFTAR_ITEM + "_" + transaction.id,
            )
        }
    }
}

@Composable
private fun DaftarEmptyState(
    monthLabel: String,
    nextEnabled: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onAddTransaction: () -> Unit,
    onOpenBackup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = SakuSpace.screenHorizontal)
            .padding(top = SakuSpace.screenHorizontal, bottom = SakuSize.navReserve),
        verticalArrangement = Arrangement.Top,
    ) {
        MonthSwitcher(
            label = monthLabel,
            onPrevious = onPrevious,
            onNext = onNext,
            nextEnabled = nextEnabled,
            testTag = TestTags.DAFTAR_MONTH_SWITCHER,
        )
        Spacer(modifier = Modifier.height(SakuSpace.section))
        EmptyState(
            title = stringResource(R.string.daftar_empty_title),
            body = stringResource(R.string.daftar_empty_body),
            modifier = Modifier.testTag(TestTags.DAFTAR_EMPTY_STATE),
            primaryAction = {
                PrimaryButton(
                    text = stringResource(R.string.daftar_empty_primary),
                    onClick = onAddTransaction,
                    testTag = TestTags.DAFTAR_EMPTY_STATE + "_primary",
                )
            },
            secondaryAction = {
                SecondaryButton(
                    text = stringResource(R.string.daftar_empty_secondary),
                    onClick = onOpenBackup,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = TestTags.DAFTAR_EMPTY_STATE + "_secondary",
                )
            },
        )
    }
}
