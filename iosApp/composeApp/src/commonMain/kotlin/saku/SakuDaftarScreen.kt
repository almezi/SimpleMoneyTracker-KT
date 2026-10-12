// A faithful reproduction of Saku's Transaksi (Daftar) screen, built from the
// REAL design system and REAL components — not placeholders. This is the
// visual-parity check that Stage 1 could not answer.

package saku

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getStringArray
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.height

private data class DemoTx(
    val id: String,
    val categoryId: String,
    val categoryNameRes: StringResource,
    val type: TransactionType,
    val amount: Long,
)

@Composable
fun SakuDaftarScreen() {
    // NOTE: no statusBarsPadding(). That is Android-only and is exactly the
    // bug this spike exists to expose. See the cross-platform insets below.
    val transactions = remember {
        listOf(
            DemoTx("1", "inc_salary", Res.string.cat_inc_salary, TransactionType.Income, 5250000),
            DemoTx("2", "exp_daily_food", Res.string.cat_exp_daily_food, TransactionType.Expense, 35000),
            DemoTx("3", "exp_transport_fuel", Res.string.cat_exp_transport_fuel, TransactionType.Expense, 100000),
            DemoTx("4", "inc_freelance", Res.string.cat_inc_freelance, TransactionType.Income, 1750000),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SakuTheme.colors.bg)
            .sakuSafeArea()
            .testTag("daftar_screen")
    ) {
        Column(Modifier.fillMaxSize()) {

            // Month switcher row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SakuSpace.screenHorizontal, vertical = SakuSpace.section),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = SakuMonth(2026, 10).labelLong(monthsLong()),
                    style = SakuTheme.text.monthLabel,
                    color = SakuTheme.colors.accentText,
                )
                Text(
                    text = "‹   ›",
                    style = SakuTheme.text.monthLabel,
                    color = SakuTheme.colors.textMuted,
                )
            }

            // Summary strip: Masuk / Keluar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SakuSpace.screenHorizontal),
                horizontalArrangement = Arrangement.spacedBy(SakuSpace.chipGap),
            ) {
                SummaryTile(
                    label = stringResource(Res.string.daftar_masuk),
                    value = formatRupiah(7000000),
                    accent = SakuTheme.colors.income,
                    modifier = Modifier.weight(1f),
                )
                SummaryTile(
                    label = stringResource(Res.string.daftar_keluar),
                    value = formatRupiah(135000),
                    accent = SakuTheme.colors.expense,
                    modifier = Modifier.weight(1f),
                )
            }

            Text(
                text = "Sen, 12 Okt 2026",
                style = SakuTheme.text.sectionLabel,
                color = SakuTheme.colors.textMuted,
                modifier = Modifier.padding(
                    start = SakuSpace.screenHorizontal,
                    end = SakuSpace.screenHorizontal,
                    top = SakuSpace.screenBottom,
                    bottom = SakuSpace.cardInner,
                ),
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag("daftar_list"),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = SakuSpace.screenHorizontal,
                    end = SakuSpace.screenHorizontal,
                    bottom = SakuSize.navReserve,
                ),
                verticalArrangement = Arrangement.spacedBy(SakuSpace.cardInner),
            ) {
                items(transactions, key = { it.id }) { tx ->
                    TransactionCard(
                        categoryId = tx.categoryId,
                        categoryName = stringResource(tx.categoryNameRes),
                        typeLabel = stringResource(
                            if (tx.type == TransactionType.Income) Res.string.type_pemasukan
                            else Res.string.type_pengeluaran
                        ),
                        amount = tx.amount,
                        type = tx.type,
                        onEdit = {},
                        onDelete = {},
                    )
                }
            }
        }

        // Floating bottom nav, as in the Android app
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = SakuSpace.screenHorizontal)
                .background(SakuTheme.colors.surface, androidx.compose.foundation.shape.RoundedCornerShape(SakuRadius.navBar))
                .padding(horizontal = SakuSpace.screenHorizontal)
                .testTag("component_nav_bottom"),
            horizontalArrangement = Arrangement.spacedBy(SakuSpace.screenHorizontal),
        ) {
            listOf(
                Res.string.nav_tambah,
                Res.string.nav_transaksi,
                Res.string.nav_ringkasan,
            ).forEachIndexed { i, label ->
                Text(
                    text = stringResource(label),
                    style = SakuTheme.text.toggleOption,
                    color = if (i == 1) SakuTheme.colors.accentText else SakuTheme.colors.textMuted,
                    modifier = Modifier.padding(vertical = SakuSpace.chipPadding),
                )
            }
        }
    }
}

// Matches the real ui/daftar/DaftarScreen.kt SummaryTile exactly.
// The earlier spike version used amountMedium (24 sp) with no maxLines, which
// wrapped "Rp 7.000.000" onto two lines. The real app already uses
// amountSmall + maxLines = 1, so this was a fault in the spike reproduction.
@Composable
private fun SummaryTile(
    label: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color,
    muted: Boolean = false,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(SakuRadius.summaryCard),
        color = SakuTheme.colors.surface,
        border = androidx.compose.foundation.BorderStroke(SakuSpace.hairline, SakuTheme.colors.border),
    ) {
        Column {
            Text(
                text = label,
                style = SakuTheme.text.sectionLabel,
                color = SakuTheme.colors.textMuted,
                modifier = Modifier.padding(horizontal = SakuSpace.cardInner, vertical = SakuSpace.tight),
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(SakuSpace.tight))
            Text(
                text = value,
                style = SakuTheme.text.amountSmall,
                color = if (muted) SakuTheme.colors.textMuted else accent,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(start = SakuSpace.cardInner, end = SakuSpace.cardInner, bottom = SakuSpace.cardInner),
            )
        }
    }
}

/** Reads the month names from compose resources rather than Android R.array. */
@Composable
private fun monthsLong(): List<String> {
    // getStringArray is suspend, so the array is resolved in an effect. Stage 3
    // of the iOS plan replaces all 3 arrays properly; this is the spike's
    // minimal working equivalent.
    var months by remember { mutableStateOf(listOf("...")) }
    LaunchedEffect(Unit) { months = getStringArray(Res.array.months_long) }
    return months
}

/**
 * Cross-platform safe-area inset.
 *
 * Saku's Android screens call Modifier.statusBarsPadding(), which does not
 * exist on iOS. This is the portable replacement being trialled.
 */
@Composable
private fun Modifier.sakuSafeArea(): Modifier =
    this.windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.safeDrawing)