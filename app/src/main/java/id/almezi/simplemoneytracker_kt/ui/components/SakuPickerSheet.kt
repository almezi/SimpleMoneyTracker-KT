package id.almezi.simplemoneytracker_kt.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuRadius
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme

data class SakuPickerOption(
    val id: String,
    val label: String,
    val mark: (@Composable () -> Unit)? = null,
)

@Composable
fun SakuPickerSheet(
    title: String,
    options: List<SakuPickerOption>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = TestTags.COMPONENT_PICKER_SHEET,
) {
    SakuBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier.testTag(testTag)) {
        Text(
            text = title,
            style = SakuTheme.text.bodyStrong,
            color = SakuTheme.colors.text,
        )
        Spacer(modifier = Modifier.height(SakuSpace.cardInner))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
        ) {
            options.forEach { option ->
                PickerRow(
                    option = option,
                    selected = option.id == selectedId,
                    onClick = {
                        onSelect(option.id)
                        onDismissRequest()
                    },
                    testTag = testTag + "_option_" + option.id,
                )
            }
        }
    }
}

@Composable
private fun PickerRow(
    option: SakuPickerOption,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(SakuRadius.input),
        color = if (selected) SakuTheme.colors.accentSurface else Color.Transparent,
        contentColor = if (selected) SakuTheme.colors.accentText else SakuTheme.colors.textSecondary,
        border = BorderStroke(SakuSpace.hairline, SakuTheme.colors.border),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(SakuSize.minTouchTarget)
                .padding(horizontal = SakuSpace.cardInner + SakuSpace.tight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
        ) {
            if (option.mark != null) option.mark()
            Text(
                text = option.label,
                style = SakuTheme.text.body,
                color = if (selected) SakuTheme.colors.accentText else SakuTheme.colors.text,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = SakuTheme.colors.accentText,
                )
            }
        }
    }
}