package id.almezi.simplemoneytracker_kt.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import id.almezi.simplemoneytracker_kt.R
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuRadius
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import id.almezi.simplemoneytracker_kt.ui.designsystem.formatRupiah
import id.almezi.simplemoneytracker_kt.ui.designsystem.typeAccent

@Composable
fun TransactionCard(
    categoryId: String,
    categoryName: String,
    typeLabel: String,
    amount: Long,
    type: TransactionType,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    buttonHeight: Dp = SakuSize.rowButtonHeight,
    testTag: String = TestTags.COMPONENT_CARD_TRANSACTION,
) {
    Surface(
        modifier = modifier.fillMaxWidth().testTag(testTag + "_" + categoryId),
        shape = RoundedCornerShape(SakuRadius.card),
        color = SakuTheme.colors.surface,
        contentColor = SakuTheme.colors.text,
        border = BorderStroke(SakuSpace.hairline, SakuTheme.colors.border),
    ) {
        Column(modifier = Modifier.padding(SakuSpace.cardInner + SakuSpace.tight)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SakuSpace.cardInner),
            ) {
                CategoryAvatar(categoryId = categoryId, label = categoryName)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = categoryName,
                        style = SakuTheme.text.bodyStrong,
                        color = SakuTheme.colors.text,
                        maxLines = 1,
                    )
                    Text(
                        text = typeLabel,
                        style = SakuTheme.text.typeLabel,
                        color = SakuTheme.colors.textMuted,
                        maxLines = 1,
                    )
                }
                Text(
                    text = formatRupiah(amount),
                    style = SakuTheme.text.amountList,
                    color = typeAccent(type),
                    textAlign = TextAlign.End,
                )
            }
            Box(modifier = Modifier.padding(top = SakuSpace.cardInner))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
            ) {
                SecondaryButton(
                    text = stringResource(R.string.action_ubah),
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    height = buttonHeight,
                    testTag = testTag + "_edit_" + categoryId,
                )
                DestructiveButton(
                    text = stringResource(R.string.delete),
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    height = buttonHeight,
                    testTag = testTag + "_delete_" + categoryId,
                )
            }
        }
    }
}