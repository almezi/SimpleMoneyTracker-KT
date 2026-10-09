package id.almezi.simplemoneytracker_kt.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.R
import id.almezi.simplemoneytracker_kt.ui.designsystem.SAKU_AMOUNT_PLACEHOLDER
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuRadius
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import id.almezi.simplemoneytracker_kt.ui.designsystem.formatRupiahFromDigits
import id.almezi.simplemoneytracker_kt.ui.designsystem.sanitizeAmountInput
import id.almezi.simplemoneytracker_kt.ui.designsystem.typeAccent

private val FieldPadding = 16.dp

@Composable
fun AmountInput(
    digits: String,
    onDigitsChange: (String) -> Unit,
    type: TransactionType,
    modifier: Modifier = Modifier,
    label: String? = null,
    isError: Boolean = false,
    testTag: String = TestTags.COMPONENT_INPUT_AMOUNT,
) {
    val accent = typeAccent(type)
    val formatted = if (digits.isEmpty()) "" else formatRupiahFromDigits(digits)
    var fieldValue by remember(digits) {
        mutableStateOf(TextFieldValue(formatted, TextRange(formatted.length)))
    }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(focused, digits) {
        if (focused) {
            val current = if (digits.isEmpty()) "" else formatRupiahFromDigits(digits)
            fieldValue = TextFieldValue(current, TextRange(current.length))
        }
    }
    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            SectionLabel(text = label)
            Spacer(modifier = Modifier.height(SakuSpace.tight))
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            shape = RoundedCornerShape(SakuRadius.amountInput),
            color = SakuTheme.colors.surface,
            border = BorderStroke(
                SakuSpace.hairline,
                if (isError) SakuTheme.colors.danger else SakuTheme.colors.border,
            ),
        ) {
            Box(
                modifier = Modifier.padding(horizontal = FieldPadding, vertical = SakuSpace.cardInner),
            ) {
                BasicTextField(
                    value = fieldValue,
                    onValueChange = { next ->
                        val clean = sanitizeAmountInput(next.text)
                        val rendered = if (clean.isEmpty()) "" else formatRupiahFromDigits(clean)
                        fieldValue = TextFieldValue(rendered, TextRange(rendered.length))
                        onDigitsChange(clean)
                    },
                    singleLine = true,
                    textStyle = SakuTheme.text.amountMedium.copy(
                        color = if (digits.isEmpty()) SakuTheme.colors.textPlaceholder else accent,
                        textAlign = TextAlign.Start,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focused = it.isFocused },
                    decorationBox = { inner ->
                        if (digits.isEmpty()) {
                            Text(
                                text = SAKU_AMOUNT_PLACEHOLDER,
                                style = SakuTheme.text.amountMedium,
                                color = SakuTheme.colors.textPlaceholder,
                            )
                        }
                        inner()
                    },
                )
            }
        }
    }
}

@Composable
fun SelectField(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    testTag: String = TestTags.COMPONENT_FIELD_SELECT,
) {
    Column(modifier = modifier) {
        if (label != null) {
            SectionLabel(text = label)
            Spacer(modifier = Modifier.height(SakuSpace.tight))
        }
        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            shape = RoundedCornerShape(SakuRadius.input),
            color = SakuTheme.colors.surface,
            contentColor = SakuTheme.colors.text,
            border = BorderStroke(SakuSpace.hairline, SakuTheme.colors.border),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = SakuSize.fieldHeight)
                    .padding(horizontal = FieldPadding),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
                ) {
                    if (leadingIcon != null) leadingIcon()
                    Text(
                        text = if (value.isEmpty()) placeholder else value,
                        style = SakuTheme.text.bodyStrong,
                        color = if (value.isEmpty()) {
                            SakuTheme.colors.textPlaceholder
                        } else {
                            SakuTheme.colors.text
                        },
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                    )
                    if (trailingIcon != null) trailingIcon()
                }
            }
        }
    }
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    testTag: String = TestTags.COMPONENT_FIELD_SEARCH,
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
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = SakuSize.fieldHeight)
                .padding(horizontal = FieldPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.cd_cari_kategori),
                tint = SakuTheme.colors.textMuted,
            )
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = SakuTheme.text.body.copy(color = SakuTheme.colors.text),
                    cursorBrush = SolidColor(SakuTheme.colors.accentText),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = SakuTheme.text.body,
                                color = SakuTheme.colors.textPlaceholder,
                            )
                        }
                        inner()
                    },
                )
            }
        }
    }
}

@Composable
fun NoteField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    maxLength: Int = 60,
    testTag: String = TestTags.COMPONENT_FIELD_NOTE,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(SakuRadius.input),
        color = SakuTheme.colors.surface,
        border = BorderStroke(SakuSpace.hairline, SakuTheme.colors.border),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = SakuSize.fieldHeight)
                .padding(horizontal = FieldPadding),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = { if (it.length <= maxLength) onValueChange(it) },
                singleLine = true,
                textStyle = SakuTheme.text.body.copy(color = SakuTheme.colors.text),
                cursorBrush = SolidColor(SakuTheme.colors.accentText),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = SakuTheme.text.body,
                            color = SakuTheme.colors.textPlaceholder,
                        )
                    }
                    inner()
                },
            )
        }
    }
}