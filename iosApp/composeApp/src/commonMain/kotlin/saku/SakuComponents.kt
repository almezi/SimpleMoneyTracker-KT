// Real Saku components, adapted from ui/components/SakuButtons.kt and
// ui/components/CategoryChips.kt. Layout, tokens, and test tags are unchanged;
// only the package and the string resource accessor differ.

package saku

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp

// ---------- buttons ----------

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = SakuSize.secondaryButtonHeight,
    testTag: String = "",
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(height)
            .then(if (testTag.isBlank()) Modifier else Modifier.testTag(testTag)),
        shape = RoundedCornerShape(SakuRadius.button),
        colors = ButtonDefaults.buttonColors(
            containerColor = SakuTheme.colors.surfaceRaised,
            contentColor = SakuTheme.colors.text,
        ),
        border = BorderStroke(SakuSpace.hairline, SakuTheme.colors.border),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp()),
    ) {
        Text(text = text, style = SakuTheme.text.buttonSecondary)
    }
}

private fun Int.dp() = androidx.compose.ui.unit.Dp(this.toFloat())

@Composable
fun DestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = SakuSize.secondaryButtonHeight,
    testTag: String = "",
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(height)
            .then(if (testTag.isBlank()) Modifier else Modifier.testTag(testTag)),
        shape = RoundedCornerShape(SakuRadius.button),
        colors = ButtonDefaults.buttonColors(
            containerColor = SakuTheme.colors.danger,
            contentColor = Color.White,
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp()),
    ) {
        Text(text = text, style = SakuTheme.text.buttonSecondary)
    }
}

// ---------- category marks ----------

fun categoryInitials(label: String): String {
    val words = label.trim().split(" ").filter { it.isNotBlank() }
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words[0].take(2).uppercase()
        else -> (words[0].take(1) + words[1].take(1)).uppercase()
    }
}

@Composable
fun CategoryMark(categoryId: String, modifier: Modifier = Modifier, size: Dp = SakuSize.mark) {
    Surface(
        modifier = modifier.size(size).testTag("mark_" + categoryId),
        shape = RoundedCornerShape(SakuRadius.mark),
        color = SakuCategoryPalette.colorFor(categoryId),
        content = {},
    )
}

@Composable
fun CategoryAvatar(categoryId: String, label: String, modifier: Modifier = Modifier) {
    val color = SakuCategoryPalette.colorFor(categoryId)
    Box(
        modifier = modifier.size(SakuSize.avatar).testTag("avatar_" + categoryId),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.size(SakuSize.avatar),
            shape = RoundedCornerShape(SakuRadius.avatar),
            color = color.copy(alpha = 0.18f),
            content = {},
        )
        Text(text = categoryInitials(label), style = SakuTheme.text.bodyStrong, color = color)
    }
}

// ---------- transaction card ----------

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
    testTag: String = "component_card_transaction",
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
                    text = stringResource(Res.string.action_ubah),
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    height = buttonHeight,
                    testTag = testTag + "_edit_" + categoryId,
                )
                DestructiveButton(
                    text = stringResource(Res.string.delete),
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    height = buttonHeight,
                    testTag = testTag + "_delete_" + categoryId,
                )
            }
        }
    }
}