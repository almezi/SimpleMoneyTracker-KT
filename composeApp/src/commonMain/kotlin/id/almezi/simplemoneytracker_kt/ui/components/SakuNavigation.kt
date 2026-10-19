package id.almezi.simplemoneytracker_kt.ui.components

import id.almezi.simplemoneytracker_kt.SakuStrings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuRadius
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import id.almezi.simplemoneytracker_kt.ui.designsystem.safeAreaBottom

/**
 * One entry in the bottom bar.
 *
 * The route is generic so the same bar works with any navigation type. It used
 * to be a `String`, which only worked while navigation was Android-only; the
 * multiplatform graph navigates with `@Serializable` route objects instead.
 *
 * [testKey] stays a stable `String` because it forms the test tag
 * (`component_nav_bottom_<testKey>`) and those tags must not change.
 */
data class SakuNavItem<T : Any>(
    val route: T,
    val label: String,
    val icon: ImageVector,
    val testKey: String,
)

@Composable
fun <T : Any> SakuBottomNav(
    items: List<SakuNavItem<T>>,
    currentRoute: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    bottomOffset: Dp = SakuSize.navBarBottomOffset,
    sideMargin: Dp = SakuSize.navBarSideMargin,
    height: Dp = SakuSize.navBarHeight,
    testTag: String = TestTags.COMPONENT_NAV_BOTTOM,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            // Real system-bar inset; bottomOffset alone is a fixed design gap.
            .safeAreaBottom()
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
                .height(height)
                .padding(horizontal = SakuSpace.tight),
            horizontalArrangement = Arrangement.spacedBy(SakuSpace.tight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val selected = item.route == currentRoute
                val tint = if (selected) SakuTheme.colors.accentText else SakuTheme.colors.textMuted
                Surface(
                    onClick = { onSelect(item.route) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag(testTag + "_" + item.testKey),
                    shape = RoundedCornerShape(SakuRadius.navItem),
                    color = if (selected) SakuTheme.colors.accentSurface else Color.Transparent,
                    contentColor = tint,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(
                            horizontal = SakuSpace.tight,
                            vertical = SakuSize.navItemVerticalPadding,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(imageVector = item.icon, contentDescription = null, tint = tint)
                        Text(
                            text = item.label,
                            style = SakuTheme.text.caption,
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
    onLabelClick: (() -> Unit)? = null,
    previousContentDescription: String = SakuStrings.cd_previous_month,
    nextContentDescription: String = SakuStrings.cd_next_month,
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
        val labelModifier = Modifier
            .weight(1f)
            .testTag(testTag + "_label")
        val labelNode = if (onLabelClick != null) {
            labelModifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onLabelClick,
            )
        } else {
            labelModifier
        }
        Text(
            text = label.uppercase(),
            style = SakuTheme.text.monthLabel,
            color = SakuTheme.colors.accentText,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = labelNode,
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
internal fun MonthArrowButton(
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
    contentDescription: String = SakuStrings.cd_back,
    testTag: String = TestTags.COMPONENT_BUTTON_BACK,
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