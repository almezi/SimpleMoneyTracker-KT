package id.almezi.simplemoneytracker_kt.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuMotion
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuRadius
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import kotlinx.coroutines.delay

@Immutable
data class SakuToast(
    val text: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val durationMillis: Long = SakuMotion.toastMillis,
)

@Composable
fun SakuToastHost(
    toast: SakuToast?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    bottomOffset: Dp = SakuSize.navReserve,
) {
    LaunchedEffect(toast) {
        if (toast != null) {
            delay(toast.durationMillis)
            onDismiss()
        }
    }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        if (toast != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SakuSpace.screenHorizontal)
                    .padding(bottom = bottomOffset)
                    .heightIn(min = SakuSize.minTouchTarget)
                    .testTag(TestTags.COMPONENT_TOAST),
                shape = RoundedCornerShape(SakuRadius.input),
                color = SakuTheme.colors.surfaceRaised,
                contentColor = SakuTheme.colors.text,
                border = BorderStroke(SakuSpace.hairline, SakuTheme.colors.borderStrong),
            ) {
                Row(
                    modifier = Modifier.padding(
                        start = SakuSpace.screenHorizontal - SakuSpace.tight,
                        end = SakuSpace.tight,
                        top = SakuSpace.chipGap,
                        bottom = SakuSpace.chipGap,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
                ) {
                    Text(
                        text = toast.text,
                        style = SakuTheme.text.bodySmall,
                        color = SakuTheme.colors.text,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                    )
                    if (toast.actionLabel != null && toast.onAction != null) {
                        ToastActionButton(
                            label = toast.actionLabel,
                            onClick = {
                                toast.onAction.invoke()
                                onDismiss()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToastActionButton(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .heightIn(min = SakuSize.minTouchTarget)
            .testTag(TestTags.COMPONENT_TOAST + "_action"),
        shape = RoundedCornerShape(SakuRadius.buttonSmall),
        color = SakuTheme.colors.accentSurface,
        contentColor = SakuTheme.colors.accentText,
    ) {
        Box(
            modifier = Modifier.padding(
                horizontal = SakuSpace.cardInner,
                vertical = SakuSpace.chipGap,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = SakuTheme.text.buttonSecondary,
                color = SakuTheme.colors.accentText,
            )
        }
    }
}