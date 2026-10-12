package id.almezi.simplemoneytracker_kt.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuMotion
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuRadius
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme

@Composable
private fun SakuButtonSurface(
    onClick: () -> Unit,
    enabled: Boolean,
    height: Dp,
    containerColor: Color,
    contentColor: Color,
    shape: Shape,
    border: BorderStroke?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) SakuMotion.pressScale else 1f,
        animationSpec = tween(SakuMotion.pressMillis),
        label = "sakuPressScale",
    )
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = border,
        interactionSource = interactionSource,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = SakuSpace.cardInner),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

enum class PrimaryButtonTone {
    Brand,
    Expense,
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tone: PrimaryButtonTone = PrimaryButtonTone.Brand,
    testTag: String = TestTags.COMPONENT_BUTTON_PRIMARY,
) {
    val containerColor = when (tone) {
        PrimaryButtonTone.Brand -> SakuTheme.colors.primary
        PrimaryButtonTone.Expense -> SakuTheme.colors.expenseButton
    }
    SakuButtonSurface(
        onClick = onClick,
        enabled = enabled,
        height = SakuSize.primaryButtonHeight,
        containerColor = if (enabled) containerColor else SakuTheme.colors.surface,
        contentColor = if (enabled) SakuTheme.colors.primaryText else SakuTheme.colors.textDisabled,
        shape = RoundedCornerShape(SakuRadius.button),
        border = if (enabled) null else BorderStroke(SakuSpace.hairline, SakuTheme.colors.border),
        modifier = modifier.fillMaxWidth().testTag(testTag),
    ) {
        Text(text = text, style = SakuTheme.text.buttonPrimary, textAlign = TextAlign.Center)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = SakuSize.secondaryButtonHeight,
    testTag: String = TestTags.COMPONENT_BUTTON_SECONDARY,
) {
    SakuButtonSurface(
        onClick = onClick,
        enabled = enabled,
        height = height,
        containerColor = SakuTheme.colors.surfaceRaised,
        contentColor = if (enabled) SakuTheme.colors.textSecondary else SakuTheme.colors.textDisabled,
        shape = RoundedCornerShape(SakuRadius.button),
        border = BorderStroke(SakuSpace.hairline, SakuTheme.colors.borderStrong),
        modifier = modifier.testTag(testTag),
    ) {
        Text(
            text = text,
            style = SakuTheme.text.buttonSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
fun DestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = SakuSize.secondaryButtonHeight,
    testTag: String = TestTags.COMPONENT_BUTTON_DESTRUCTIVE,
) {
    SakuButtonSurface(
        onClick = onClick,
        enabled = enabled,
        height = height,
        containerColor = SakuTheme.colors.dangerSurface,
        contentColor = if (enabled) SakuTheme.colors.dangerText else SakuTheme.colors.textDisabled,
        shape = RoundedCornerShape(SakuRadius.button),
        border = BorderStroke(SakuSpace.hairline, SakuTheme.colors.dangerBorder),
        modifier = modifier.testTag(testTag),
    ) {
        Text(
            text = text,
            style = SakuTheme.text.buttonSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
fun DestructiveSheetButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = TestTags.COMPONENT_SHEET_BUTTON_CONFIRM,
) {
    SakuButtonSurface(
        onClick = onClick,
        enabled = true,
        height = SakuSize.secondaryButtonHeight,
        containerColor = SakuTheme.colors.danger,
        contentColor = SakuTheme.colors.primaryText,
        shape = RoundedCornerShape(SakuRadius.button),
        border = null,
        modifier = modifier.testTag(testTag),
    ) {
        Text(
            text = text,
            style = SakuTheme.text.buttonSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
fun DestructiveTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = TestTags.COMPONENT_BUTTON_TEXT_DANGER,
) {
    SakuButtonSurface(
        onClick = onClick,
        enabled = enabled,
        height = SakuSize.minTouchTarget,
        containerColor = Color.Transparent,
        contentColor = if (enabled) SakuTheme.colors.dangerText else SakuTheme.colors.textDisabled,
        shape = RoundedCornerShape(SakuRadius.button),
        border = null,
        modifier = modifier.fillMaxWidth().testTag(testTag),
    ) {
        Text(
            text = text,
            style = SakuTheme.text.buttonSecondary,
            textAlign = TextAlign.Center,
            color = if (enabled) SakuTheme.colors.dangerText else SakuTheme.colors.textDisabled,
        )
    }
}

@Composable
fun HelperText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = SakuTheme.colors.textMuted,
) {
    Text(text = text, style = SakuTheme.text.helper, color = color, modifier = modifier)
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = SakuTheme.text.sectionLabel,
        color = SakuTheme.colors.textMuted,
        modifier = modifier,
    )
}