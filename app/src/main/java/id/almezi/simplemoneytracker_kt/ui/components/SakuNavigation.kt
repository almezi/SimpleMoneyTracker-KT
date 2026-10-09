package id.almezi.simplemoneytracker_kt.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

data class SakuNavItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
)

@Composable
fun SakuBottomNav(
    items: List<SakuNavItem>,
    currentRoute: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    bottomOffset: Dp = SakuSize.navBarBottomOffset,
    sideMargin: Dp = SakuSize.navBarSideMargin,
    height: Dp = SakuSize.navBarHeight,
    testTag: String = TestTags.COMPONENT_NAV_BOTTOM,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = sideMargin)
            .padding(bottom = bottomOffset)
            .height(height)
            .testTag(testTag),
        shape = RoundedCornerShape(SakuRadius.navBar),
        color = SakuTheme.colors.surface,
        contentColor = SakuTheme.colors.text,
        border = BorderStroke(SakuSpace.hairline, SakuTheme.colors.border),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val selected = item.route == currentRoute
                val tint = if (selected) SakuTheme.colors.accentText else SakuTheme.colors.textMuted
                Surface(
                    onClick = { onSelect(item.route) },
                    modifier = Modifier.testTag(testTag + "_" + item.route),
                    shape = RoundedCornerShape(SakuRadius.navItem),
                    color = if (selected) SakuTheme.colors.accentSurface else Color.Transparent,
                    contentColor = tint,
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = SakuSize.navItemHorizontalPadding,
                            vertical = SakuSize.navItemVerticalPadding,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
                    ) {
                        Icon(imageVector = item.icon, contentDescription = null, tint = tint)
                        Text(
                            text = stringResource(item.labelRes),
                            style = SakuTheme.text.buttonSecondary,
                            color = tint,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthSwitcher(
    label: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    nextEnabled: Boolean = true,
    previousContentDescription: String = stringResource(R.string.cd_previous_month),
    nextContentDescription: String = stringResource(R.string.cd_next_month),
    testTag: String = TestTags.COMPONENT_SWITCHER_MONTH,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
    ) {
        MonthArrowButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = previousContentDescription,
            enabled = true,
            onClick = onPrevious,
            testTag = testTag + "_prev",
        )
        Text(
            text = label.uppercase(),
            style = SakuTheme.text.monthLabel,
            color = SakuTheme.colors.accentText,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        MonthArrowButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = nextContentDescription,
            enabled = nextEnabled,
            onClick = onNext,
            testTag = testTag + "_next",
        )
    }
}

@Composable
private fun MonthArrowButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(SakuSize.squareButton)
            .testTag(testTag),
        shape = RoundedCornerShape(SakuRadius.buttonSmall),
        color = SakuTheme.colors.surface,
        contentColor = if (enabled) SakuTheme.colors.textSecondary else SakuTheme.colors.textDisabled,
        border = BorderStroke(SakuSpace.hairline, SakuTheme.colors.border),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.padding(SakuSize.arrowIconPadding),
        )
    }
}

@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = stringResource(R.string.cd_back),
    testTag: String = "component_button_back",
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(SakuSize.squareButton)
            .testTag(testTag),
        shape = RoundedCornerShape(SakuRadius.buttonSmall),
        color = SakuTheme.colors.surface,
        contentColor = SakuTheme.colors.textSecondary,
        border = BorderStroke(SakuSpace.hairline, SakuTheme.colors.border),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = contentDescription,
            modifier = Modifier.padding(SakuSize.arrowIconPadding),
        )
    }
}

@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
    ) {
        if (onBack != null) {
            BackButton(onClick = onBack)
        }
        Text(
            text = title,
            style = SakuTheme.text.screenTitle,
            color = SakuTheme.colors.text,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        if (action != null) action()
    }
}