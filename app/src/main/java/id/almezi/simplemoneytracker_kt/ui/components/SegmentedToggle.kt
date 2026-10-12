package id.almezi.simplemoneytracker_kt.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuRadius
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import id.almezi.simplemoneytracker_kt.ui.designsystem.typeAccent
import id.almezi.simplemoneytracker_kt.ui.designsystem.typeBorder
import id.almezi.simplemoneytracker_kt.ui.designsystem.typeSelectedSurface

data class SegmentedOption(
    val label: String,
    val type: TransactionType,
)

@Composable
fun SegmentedToggle(
    options: List<SegmentedOption>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = TestTags.COMPONENT_TOGGLE_SEGMENTED,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(SakuRadius.input),
        color = SakuTheme.colors.surface,
        border = BorderStroke(SakuSpace.hairline, SakuTheme.colors.border),
    ) {
        Row(
            modifier = Modifier.padding(SakuSpace.tight),
            horizontalArrangement = Arrangement.spacedBy(SakuSpace.tight),
        ) {
            options.forEachIndexed { index, option ->
                val selected = index == selectedIndex
                val contentColor = if (selected) typeAccent(option.type) else SakuTheme.colors.textMuted
                Surface(
                    onClick = { onSelect(index) },
                    modifier = Modifier
                        .weight(1f)
                        .height(SakuSize.minTouchTarget - SakuSpace.tight * 2)
                        .testTag(testTag + "_" + index),
                    shape = RoundedCornerShape(SakuRadius.buttonSmall),
                    color = if (selected) typeSelectedSurface(option.type) else Color.Transparent,
                    contentColor = contentColor,
                    border = if (selected) {
                        BorderStroke(SakuSpace.hairline, typeBorder(option.type))
                    } else {
                        null
                    },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = option.label,
                            style = SakuTheme.text.toggleOption,
                            color = contentColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}