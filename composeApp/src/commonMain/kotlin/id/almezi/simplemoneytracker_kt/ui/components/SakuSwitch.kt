package id.almezi.simplemoneytracker_kt.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuRadius
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme

private const val SWITCH_ANIM_MILLIS = 150

@Composable
fun SakuSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = TestTags.COMPONENT_SWITCH,
) {
    val knobOffset by animateDpAsState(
        targetValue = if (checked) {
            SakuSize.switchTrackWidth - SakuSize.switchKnob - SakuSpace.switchPadding * 2
        } else {
            SakuSpace.switchPadding
        },
        animationSpec = tween(SWITCH_ANIM_MILLIS),
        label = "switchKnobOffset",
    )
    Box(
        modifier = modifier
            .height(SakuSize.minTouchTarget)
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .testTag(testTag),
        contentAlignment = Alignment.CenterStart,
    ) {
        Surface(
            modifier = Modifier.size(
                width = SakuSize.switchTrackWidth,
                height = SakuSize.switchTrackHeight,
            ),
            shape = RoundedCornerShape(SakuRadius.switchTrack),
            color = if (checked) SakuTheme.colors.primary else SakuTheme.colors.borderStrong,
            contentColor = Color.White,
        ) {
            Box(
                modifier = Modifier.padding(SakuSpace.switchPadding),
                contentAlignment = Alignment.CenterStart,
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = knobOffset)
                        .size(SakuSize.switchKnob)
                        .background(Color.White, RoundedCornerShape(SakuRadius.knob)),
                )
            }
        }
    }
}

@Composable
fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = TestTags.COMPONENT_SWITCH,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SakuSpace.cardInner),
    ) {
        Text(
            text = label,
            style = SakuTheme.text.body,
            color = SakuTheme.colors.text,
            modifier = Modifier.weight(1f),
        )
        SakuSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            testTag = testTag,
        )
    }
}