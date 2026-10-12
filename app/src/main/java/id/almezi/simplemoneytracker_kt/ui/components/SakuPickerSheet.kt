package id.almezi.simplemoneytracker_kt.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import id.almezi.simplemoneytracker_kt.R
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuRadius
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import id.almezi.simplemoneytracker_kt.ui.filterCategoryGroups

data class SakuPickerOption(
    val id: String,
    val label: String,
    val mark: (@Composable () -> Unit)? = null,
)

data class SakuPickerOptionGroup(
    val title: String,
    val options: List<SakuPickerOption>
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
fun CategoryPickerSheet(
    title: String,
    groups: List<SakuPickerOptionGroup>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = TestTags.COMPONENT_PICKER_SHEET,
) {
    var query by remember { mutableStateOf("") }
    val filteredGroups = filterCategoryGroups(groups, query)
    val noResults = filteredGroups.isEmpty()

    SakuBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier.testTag(testTag)) {
        Text(
            text = title,
            style = SakuTheme.text.bodyStrong,
            color = SakuTheme.colors.text,
        )
        Spacer(modifier = Modifier.height(SakuSpace.cardInner))
        SearchField(
            value = query,
            onValueChange = { query = it },
            placeholder = stringResource(R.string.cari_kategori_placeholder),
            testTag = TestTags.TAMBAH_CATEGORY_SEARCH,
        )
        Spacer(modifier = Modifier.height(SakuSpace.cardInner))
        if (noResults) {
            Text(
                text = stringResource(R.string.kategori_tidak_ditemukan),
                style = SakuTheme.text.body,
                color = SakuTheme.colors.textMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SakuSpace.cardInner)
                    .testTag(TestTags.TAMBAH_CATEGORY_SEARCH_EMPTY),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(SakuSpace.tight)
            ) {
                filteredGroups.forEach { group ->
                    if (group.title.isNotEmpty()) {
                        item {
                            Text(
                                text = group.title.uppercase(),
                                style = SakuTheme.text.sectionLabel,
                                color = SakuTheme.colors.accentText,
                                modifier = Modifier.padding(top = SakuSpace.cardInner, bottom = SakuSpace.tight)
                            )
                        }
                    }
                    items(group.options) { option ->
                        PickerRow(
                            option = option,
                            selected = option.id == selectedId,
                            onClick = {
                                onSelect(option.id)
                                onDismissRequest()
                            },
                            testTag = TestTags.TAMBAH_CATEGORY_ITEM + "_" + option.id,
                        )
                    }
                }
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