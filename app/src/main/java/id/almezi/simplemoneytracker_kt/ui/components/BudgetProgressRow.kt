package id.almezi.simplemoneytracker_kt.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import id.almezi.simplemoneytracker_kt.R
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.designsystem.BudgetStatus
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuRadius
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import id.almezi.simplemoneytracker_kt.ui.designsystem.budgetRemainingAmount
import id.almezi.simplemoneytracker_kt.ui.designsystem.formatRupiah

@Composable
fun BudgetProgressRow(
    categoryName: String,
    spent: Long,
    limit: Long,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    buttonHeight: Dp = SakuSize.rowButtonHeight,
    testTag: String = TestTags.COMPONENT_ROW_BUDGET,
) {
    val status = BudgetStatus.of(spent, limit)
    val accent = when (status) {
        BudgetStatus.Safe -> SakuTheme.colors.income
        BudgetStatus.AlmostGone -> SakuTheme.colors.warning
        BudgetStatus.OverLimit -> SakuTheme.colors.expense
    }
    val statusLabel = when (status) {
        BudgetStatus.Safe -> stringResource(R.string.status_aman)
        BudgetStatus.AlmostGone -> stringResource(R.string.status_hampir_habis)
        BudgetStatus.OverLimit -> stringResource(R.string.status_lewat_batas)
    }
    val remaining = budgetRemainingAmount(limit, spent)
    val remainingText = if (remaining >= 0L) {
        stringResource(R.string.budget_remaining, formatRupiah(remaining))
    } else {
        stringResource(R.string.budget_over, formatRupiah(-remaining))
    }
    val fraction = if (limit <= 0L) 0f else (spent.toFloat() / limit.toFloat()).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(BUDGET_BAR_ANIM_MILLIS),
        label = "budgetBar",
    )

    Surface(
        modifier = modifier.fillMaxWidth().testTag(testTag + "_" + categoryName),
        shape = RoundedCornerShape(SakuRadius.card),
        color = SakuTheme.colors.surface,
        contentColor = SakuTheme.colors.text,
        border = BorderStroke(SakuSpace.hairline, SakuTheme.colors.border),
    ) {
        Column(modifier = Modifier.padding(SakuSpace.cardInner + SakuSpace.tight)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = categoryName,
                    style = SakuTheme.text.bodyStrong,
                    color = SakuTheme.colors.text,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
                Text(
                    text = statusLabel,
                    style = SakuTheme.text.monthLabel,
                    color = accent,
                )
            }
            Box(
                modifier = Modifier
                    .padding(top = SakuSpace.cardInner)
                    .fillMaxWidth()
                    .height(SakuSize.barHeight)
                    .background(SakuTheme.colors.barTrack, RoundedCornerShape(SakuRadius.bar)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFraction)
                        .height(SakuSize.barHeight)
                        .background(accent, RoundedCornerShape(SakuRadius.bar)),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SakuSpace.chipGap),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        R.string.budget_spent_of,
                        formatRupiah(spent),
                        formatRupiah(limit),
                    ),
                    style = SakuTheme.text.caption,
                    color = SakuTheme.colors.textMuted,
                )
                Text(
                    text = remainingText,
                    style = SakuTheme.text.caption,
                    color = if (remaining >= 0L) SakuTheme.colors.textSecondary else accent,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SakuSpace.cardInner),
                horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
            ) {
                SecondaryButton(
                    text = stringResource(R.string.action_ubah),
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    height = buttonHeight,
                    testTag = testTag + "_edit_" + categoryName,
                )
                DestructiveButton(
                    text = stringResource(R.string.delete),
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    height = buttonHeight,
                    testTag = testTag + "_delete_" + categoryName,
                )
            }
        }
    }
}

private const val BUDGET_BAR_ANIM_MILLIS = 200