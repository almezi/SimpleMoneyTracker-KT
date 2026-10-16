package id.almezi.simplemoneytracker_kt.ui.components

import id.almezi.simplemoneytracker_kt.SakuStrings

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuMonth
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuRadius
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import id.almezi.simplemoneytracker_kt.ui.designsystem.monthLong

@Composable
fun MonthPickerSheet(
    current: SakuMonth,
    maxMonth: SakuMonth,
    minYear: Int,
    onMonthSelected: (SakuMonth) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = TestTags.COMPONENT_MONTH_PICKER_SHEET,
) {
    val maxYear = maxMonth.year
    var year by remember(current) { mutableStateOf(current.year) }
    SakuBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier.testTag(testTag)) {
        Text(
            text = SakuStrings.pilih_bulan_title,
            style = SakuTheme.text.bodyStrong,
            color = SakuTheme.colors.text,
        )
        Spacer(modifier = Modifier.height(SakuSpace.cardInner))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
        ) {
            MonthArrowButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = SakuStrings.cd_previous_month,
                enabled = year > minYear,
                onClick = { if (year > minYear) year -= 1 },
                testTag = testTag + "_year_prev",
            )
            Text(
                text = year.toString(),
                style = SakuTheme.text.monthLabel,
                color = SakuTheme.colors.accentText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .testTag(testTag + "_year"),
            )
            MonthArrowButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = SakuStrings.cd_next_month,
                enabled = year < maxYear,
                onClick = { if (year < maxYear) year += 1 },
                testTag = testTag + "_year_next",
            )
        }
        Spacer(modifier = Modifier.height(SakuSpace.cardInner))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
        ) {
            (0 until 4).forEach { rowIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
                ) {
                    (1..3).forEach { columnIndex ->
                        val monthNumber = rowIndex * 3 + columnIndex
                        val month = SakuMonth(year, monthNumber)
                        MonthChip(
                            label = monthLong(monthNumber),
                            selected = month == current,
                            enabled = month <= maxMonth,
                            onClick = {
                                onMonthSelected(month)
                                onDismissRequest()
                            },
                            modifier = Modifier.weight(1f),
                            testTag = testTag + "_month_$monthNumber",
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(SakuSpace.cardInner))
    }
}

@Composable
private fun MonthChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(SakuRadius.buttonSmall),
        color = if (selected) SakuTheme.colors.accentSurface else SakuTheme.colors.surface,
        contentColor = when {
            selected -> SakuTheme.colors.accentText
            !enabled -> SakuTheme.colors.textDisabled
            else -> SakuTheme.colors.textSecondary
        },
        border = BorderStroke(
            SakuSpace.hairline,
            if (selected) SakuTheme.colors.accentText else SakuTheme.colors.border,
        ),
    ) {
        Text(
            text = label,
            style = SakuTheme.text.caption,
            color = when {
                selected -> SakuTheme.colors.accentText
                !enabled -> SakuTheme.colors.textDisabled
                else -> SakuTheme.colors.textSecondary
            },
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(
                horizontal = SakuSpace.chipPadding,
                vertical = SakuSpace.cardInner,
            ),
        )
    }
}
