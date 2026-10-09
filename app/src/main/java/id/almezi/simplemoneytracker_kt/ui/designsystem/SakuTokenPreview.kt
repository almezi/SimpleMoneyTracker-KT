package id.almezi.simplemoneytracker_kt.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private fun Color.hex(): String =
    String.format("#%06X", 0xFFFFFF and toArgb())

@Composable
private fun Swatch(name: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(28.dp)
                .background(color, RoundedCornerShape(SakuRadius.mark)),
        )
        Spacer(modifier = Modifier.width(SakuSpace.chipGap))
        Text(
            text = name + "  " + color.hex(),
            style = SakuTheme.text.caption,
            color = SakuTheme.colors.text,
        )
    }
}

@Preview(name = "Tokens", showBackground = true, backgroundColor = 0xFF0E0E12)
@Composable
private fun ColorTokensPreview() {
    val colors = SakuDarkColors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(SakuSpace.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(SakuSpace.tight),
    ) {
        listOf(
            Triple("bg", colors.bg, SakuTheme.text.screenTitle),
            Triple("surface", colors.surface, SakuTheme.text.body),
            Triple("surfaceRaised", colors.surfaceRaised, SakuTheme.text.body),
            Triple("border", colors.border, SakuTheme.text.body),
            Triple("borderStrong", colors.borderStrong, SakuTheme.text.body),
            Triple("primary", colors.primary, SakuTheme.text.body),
            Triple("accentText", colors.accentText, SakuTheme.text.body),
            Triple("accentSurface", colors.accentSurface, SakuTheme.text.body),
            Triple("text", colors.text, SakuTheme.text.body),
            Triple("textSecondary", colors.textSecondary, SakuTheme.text.body),
            Triple("textMuted", colors.textMuted, SakuTheme.text.body),
            Triple("textDisabled", colors.textDisabled, SakuTheme.text.body),
            Triple("income", colors.income, SakuTheme.text.body),
            Triple("incomeSurface", colors.incomeSurface, SakuTheme.text.body),
            Triple("incomeBorder", colors.incomeBorder, SakuTheme.text.body),
            Triple("incomeSelected", colors.incomeSelected, SakuTheme.text.body),
            Triple("expense", colors.expense, SakuTheme.text.body),
            Triple("expenseButton", colors.expenseButton, SakuTheme.text.body),
            Triple("expenseSurface", colors.expenseSurface, SakuTheme.text.body),
            Triple("expenseBorder", colors.expenseBorder, SakuTheme.text.body),
            Triple("warning", colors.warning, SakuTheme.text.body),
            Triple("danger", colors.danger, SakuTheme.text.body),
            Triple("dangerSurface", colors.dangerSurface, SakuTheme.text.body),
            Triple("dangerText", colors.dangerText, SakuTheme.text.body),
            Triple("scrim", colors.scrim, SakuTheme.text.body),
            Triple("dragHandle", colors.dragHandle, SakuTheme.text.body),
            Triple("primaryText", colors.primaryText, SakuTheme.text.body),
            Triple("barTrack", colors.barTrack, SakuTheme.text.body),
        ).forEach { (name, color, style) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(28.dp)
                        .background(color, RoundedCornerShape(SakuRadius.mark)),
                )
                Spacer(modifier = Modifier.width(SakuSpace.chipGap))
                Text(text = name, style = style, color = colors.textSecondary)
                Spacer(modifier = Modifier.width(SakuSpace.chipGap))
                Text(text = color.hex(), style = SakuTheme.text.caption, color = colors.textMuted)
            }
        }
    }
}

@Preview(name = "Typography", showBackground = true, backgroundColor = 0xFF0E0E12)
@Composable
private fun TypographyPreview() {
    val colors = SakuTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bg)
            .padding(SakuSpace.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
    ) {
        Text("Tambah transaksi", style = SakuTheme.text.screenTitle, color = colors.text)
        Text("KATEGORI", style = SakuTheme.text.sectionLabel, color = colors.textMuted)
        Text("Makan & Minum", style = SakuTheme.text.body, color = colors.text)
        Text("Makan & Minum", style = SakuTheme.text.bodyStrong, color = colors.text)
        Text("Simpan transaksi", style = SakuTheme.text.buttonPrimary, color = colors.text)
        Text("Isi jumlah dulu", style = SakuTheme.text.helper, color = colors.textMuted)
        Text("212.121", style = SakuTheme.text.amountLarge, color = colors.text)
        Text("212.121", style = SakuTheme.text.amountMedium, color = colors.expense)
        Text("−Rp 212.121", style = SakuTheme.text.amountList, color = colors.income)
        Text("OKTOBER 2026", style = SakuTheme.text.monthLabel, color = colors.accentText)
    }
}

@Preview(name = "Palette", showBackground = true, backgroundColor = 0xFF0E0E12)
@Composable
private fun CategoryPalettePreview() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SakuTheme.colors.bg)
            .padding(SakuSpace.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
    ) {
        SakuCategoryPalette.swatches.forEachIndexed { index, color ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp)
                        .background(color, RoundedCornerShape(SakuRadius.avatar)),
                )
                Spacer(modifier = Modifier.height(SakuSpace.tight))
                Text(
                    text = color.hex(),
                    style = SakuTheme.text.caption,
                    color = SakuTheme.colors.textMuted,
                )
            }
        }
    }
}

@Preview(name = "Spacing and radius", showBackground = true, backgroundColor = 0xFF0E0E12)
@Composable
private fun DimensPreview() {
    val colors = SakuTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bg)
            .padding(SakuSpace.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
    ) {
        listOf(
            "screenHorizontal" to SakuSpace.screenHorizontal,
            "section" to SakuSpace.section,
            "cardInner" to SakuSpace.cardInner,
            "chipGap" to SakuSpace.chipGap,
        ).forEach { (name, value) ->
            Text(text = name + " " + value.value + "dp", style = SakuTheme.text.caption, color = colors.textMuted)
        }
        listOf(
            "input" to SakuRadius.input,
            "card" to SakuRadius.card,
            "summaryCard" to SakuRadius.summaryCard,
            "button" to SakuRadius.button,
            "sheet" to SakuRadius.sheet,
            "navBar" to SakuRadius.navBar,
        ).forEach { (name, value) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(32.dp)
                        .background(colors.surfaceRaised, RoundedCornerShape(value)),
                )
                Spacer(modifier = Modifier.width(SakuSpace.chipGap))
                Text(text = name, style = SakuTheme.text.caption, color = colors.textMuted)
            }
        }
    }
}