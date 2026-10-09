package id.almezi.simplemoneytracker_kt.ui.components

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuCategoryPalette
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuRadius
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import id.almezi.simplemoneytracker_kt.ui.designsystem.typeBorder
import id.almezi.simplemoneytracker_kt.ui.designsystem.typeSelectedSurface

data class CategoryChipItem(
    val id: String,
    val label: String,
)

fun categoryInitials(label: String): String {
    val words = label.trim().split(" ").filter { it.any { c -> c.isLetterOrDigit() } }
    return when {
        words.isEmpty() -> ""
        words.size == 1 -> words[0].take(2).uppercase()
        else -> (words[0].take(1) + words[1].take(1)).uppercase()
    }
}

@Composable
fun CategoryMark(
    categoryId: String,
    modifier: Modifier = Modifier,
    size: Dp = SakuSize.mark,
) {
    Surface(
        modifier = modifier
            .size(size)
            .testTag("mark_" + categoryId),
        shape = RoundedCornerShape(SakuRadius.mark),
        color = SakuCategoryPalette.colorFor(categoryId),
        content = {},
    )
}

@Composable
fun CategoryAvatar(
    categoryId: String,
    label: String,
    modifier: Modifier = Modifier,
) {
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
        Text(
            text = categoryInitials(label),
            style = SakuTheme.text.bodyStrong,
            color = color,
        )
    }
}

@Composable
fun CategoryChipGrid(
    items: List<CategoryChipItem>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    type: TransactionType,
    modifier: Modifier = Modifier,
    columns: Int = 3,
    testTag: String = TestTags.COMPONENT_GRID_CATEGORY_CHIP,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
    ) {
        items.chunked(columns).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
            ) {
                rowItems.forEach { item ->
                    CategoryChip(
                        item = item,
                        selected = item.id == selectedId,
                        type = type,
                        onClick = { onSelect(item.id) },
                        modifier = Modifier.weight(1f),
                        height = SakuSize.chipHeight,
                        testTag = testTag + "_" + item.id,
                    )
                }
                repeat(columns - rowItems.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CategoryChipRow(
    items: List<CategoryChipItem>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    type: TransactionType,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    testTag: String = TestTags.COMPONENT_ROW_CATEGORY_CHIP,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
    ) {
        items.chunked(columns).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
            ) {
                rowItems.forEach { item ->
                    CategoryChip(
                        item = item,
                        selected = item.id == selectedId,
                        type = type,
                        onClick = { onSelect(item.id) },
                        modifier = Modifier.weight(1f),
                        height = SakuSize.chipRowHeight,
                        testTag = testTag + "_" + item.id,
                        pill = true,
                    )
                }
                repeat(columns - rowItems.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    item: CategoryChipItem,
    selected: Boolean,
    type: TransactionType,
    onClick: () -> Unit,
    height: Dp,
    testTag: String,
    modifier: Modifier = Modifier,
    pill: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(height)
            .testTag(testTag),
        shape = RoundedCornerShape(if (pill) SakuRadius.buttonSmall else SakuRadius.input),
        color = if (selected) typeSelectedSurface(type) else SakuTheme.colors.surface,
        contentColor = SakuTheme.colors.text,
        border = BorderStroke(
            SakuSpace.hairline,
            if (selected) typeBorder(type) else SakuTheme.colors.border,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = SakuSpace.chipPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipMarkGap),
        ) {
            CategoryMark(categoryId = item.id, size = SakuSize.markSmall)
            Text(
                text = item.label,
                style = SakuTheme.text.chip,
                color = if (selected) SakuTheme.colors.text else SakuTheme.colors.textSecondary,
                maxLines = 2,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
    }
}