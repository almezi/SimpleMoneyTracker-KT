// Adapted from the Android app's ui/designsystem/SakuTheme.kt.
// Changes for KMP: package renamed, R.string -> Res.string.

package saku

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

object SakuTheme {
    val colors: SakuColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSakuColors.current

    val text: SakuTextStyles
        @Composable get() = SakuTextStyles
}

enum class TransactionType(val storageValue: String) {
    Income("pemasukan"),
    Expense("pengeluaran");

    companion object {
        fun fromStorage(value: String): TransactionType =
            if (value == Income.storageValue) Income else Expense
    }
}

@Composable
fun SakuTheme(content: @Composable () -> Unit) {
    val colors = SakuDarkColors
    CompositionLocalProvider(LocalSakuColors provides colors) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = colors.primary,
                onPrimary = colors.primaryText,
                secondary = colors.accentText,
                onSecondary = colors.bg,
                background = colors.bg,
                onBackground = colors.text,
                surface = colors.surface,
                onSurface = colors.text,
                surfaceVariant = colors.surfaceRaised,
                onSurfaceVariant = colors.textSecondary,
                error = colors.danger,
                onError = colors.primaryText,
                outline = colors.border,
                outlineVariant = colors.border,
            ),
            typography = SakuTypography,
            shapes = Shapes(
                extraSmall = RoundedCornerShape(SakuRadius.mark),
                small = RoundedCornerShape(SakuRadius.buttonSmall),
                medium = RoundedCornerShape(SakuRadius.input),
                large = RoundedCornerShape(SakuRadius.card),
                extraLarge = RoundedCornerShape(SakuRadius.sheet),
            ),
            content = content,
        )
    }
}

@Composable
fun typeAccent(type: TransactionType): Color =
    when (type) {
        TransactionType.Income -> SakuTheme.colors.income
        TransactionType.Expense -> SakuTheme.colors.expense
    }

const val SAKU_AMOUNT_PLACEHOLDER = "Rp 0"
const val SAKU_NO_DATA = "—"