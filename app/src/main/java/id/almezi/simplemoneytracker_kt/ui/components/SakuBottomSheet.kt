package id.almezi.simplemoneytracker_kt.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import id.almezi.simplemoneytracker_kt.R
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuRadius
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SakuBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier.testTag(TestTags.COMPONENT_SHEET_BOTTOM),
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = SakuRadius.sheet,
            topEnd = SakuRadius.sheet,
        ),
        containerColor = SakuTheme.colors.surfaceRaised,
        contentColor = SakuTheme.colors.text,
        scrimColor = SakuTheme.colors.scrim,
        dragHandle = { SakuSheetDragHandle() },
    ) {
        Column(
            modifier = Modifier.padding(
                start = SakuSize.sheetHorizontalPadding,
                end = SakuSize.sheetHorizontalPadding,
                bottom = SakuSize.sheetBottomPadding,
            ),
        ) {
            content()
        }
    }
}

@Composable
private fun SakuSheetDragHandle() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Spacer(
            modifier = Modifier
                .padding(top = SakuSpace.section)
                .size(width = SakuSize.sheetHandleWidth, height = SakuSize.sheetHandleHeight)
                .background(
                    color = SakuTheme.colors.dragHandle,
                    shape = RoundedCornerShape(SakuRadius.bar),
                ),
        )
    }
}

@Composable
fun ConfirmDeleteSheet(
    title: String,
    body: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    SakuBottomSheet(onDismissRequest = onDismissRequest) {
        Text(text = title, style = SakuTheme.text.bodyStrong, color = SakuTheme.colors.text)
        Spacer(modifier = Modifier.height(SakuSpace.chipGap))
        Text(text = body, style = SakuTheme.text.bodySmall, color = SakuTheme.colors.textSecondary)
        Spacer(modifier = Modifier.height(SakuSpace.screenHorizontal))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
        ) {
            SecondaryButton(
                text = stringResource(R.string.cancel),
                onClick = onDismissRequest,
                modifier = Modifier.weight(1f),
                testTag = TestTags.COMPONENT_SHEET_BUTTON_CANCEL,
            )
            DestructiveSheetButton(
                text = stringResource(R.string.action_ya_hapus),
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun SakuCard(
    modifier: Modifier = Modifier,
    color: Color = SakuTheme.colors.surface,
    border: BorderStroke = BorderStroke(SakuSpace.hairline, SakuTheme.colors.border),
    shape: Shape = RoundedCornerShape(SakuRadius.card),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val padding = SakuSpace.cardInner + SakuSpace.tight
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = color,
            contentColor = SakuTheme.colors.text,
            border = border,
        ) {
            Box(modifier = Modifier.padding(padding)) { content() }
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = color,
            contentColor = SakuTheme.colors.text,
            border = border,
        ) {
            Box(modifier = Modifier.padding(padding)) { content() }
        }
    }
}