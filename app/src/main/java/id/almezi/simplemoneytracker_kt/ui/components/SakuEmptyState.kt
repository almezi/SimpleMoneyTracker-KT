package id.almezi.simplemoneytracker_kt.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme

@Composable
fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    primaryAction: (@Composable () -> Unit)? = null,
    secondaryAction: (@Composable () -> Unit)? = null,
    testTag: String = TestTags.COMPONENT_STATE_EMPTY,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.height(SakuSpace.screenHorizontal))
        }
        Text(
            text = title,
            style = SakuTheme.text.emptyTitle,
            color = SakuTheme.colors.text,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(SakuSpace.chipGap))
        Text(
            text = body,
            style = SakuTheme.text.bodySmall,
            color = SakuTheme.colors.textMuted,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        if (primaryAction != null || secondaryAction != null) {
            Spacer(modifier = Modifier.height(SakuSpace.screenHorizontal))
            if (primaryAction != null) {
                primaryAction()
                if (secondaryAction != null) {
                    Spacer(modifier = Modifier.height(SakuSpace.chipGap))
                }
            }
            if (secondaryAction != null) {
                secondaryAction()
            }
        }
    }
}