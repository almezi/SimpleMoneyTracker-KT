package id.almezi.simplemoneytracker_kt.ui.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class SakuColors(
    val bg: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val border: Color,
    val borderStrong: Color,
    val text: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textPlaceholder: Color,
    val textDisabled: Color,
    val primary: Color,
    val primaryText: Color,
    val accentText: Color,
    val accentSurface: Color,
    val income: Color,
    val incomeSurface: Color,
    val incomeSurfaceStrong: Color,
    val incomeBorder: Color,
    val incomeSelected: Color,
    val expense: Color,
    val expenseSurface: Color,
    val expenseBorder: Color,
    val expenseButton: Color,
    val warning: Color,
    val danger: Color,
    val dangerText: Color,
    val dangerSurface: Color,
    val dangerBorder: Color,
    val scrim: Color,
    val dragHandle: Color,
    val barTrack: Color,
)

val SakuDarkColors = SakuColors(
    bg = Color(0xFF0E0E12),
    surface = Color(0xFF17171D),
    surfaceRaised = Color(0xFF1C1C26),
    border = Color(0xFF26262E),
    borderStrong = Color(0xFF33334A),
    text = Color(0xFFF2F2F5),
    textSecondary = Color(0xFFD6D6DE),
    textMuted = Color(0xFF9A9AA8),
    textPlaceholder = Color(0xFF8A8A98),
    textDisabled = Color(0xFF4A4A56),
    primary = Color(0xFF5B4BD6),
    primaryText = Color(0xFFFFFFFF),
    accentText = Color(0xFFA99CFF),
    accentSurface = Color(0xFF241F45),
    income = Color(0xFF6EE7A0),
    incomeSurface = Color(0xFF121A16),
    incomeSurfaceStrong = Color(0xFF15231C),
    incomeBorder = Color(0xFF3A7D5C),
    incomeSelected = Color(0xFF1B2E25),
    expense = Color(0xFFF0708A),
    expenseSurface = Color(0xFF2E1A21),
    expenseBorder = Color(0xFF6B2C3A),
    expenseButton = Color(0xFFB8405A),
    warning = Color(0xFFF6C453),
    danger = Color(0xFFB3373F),
    dangerText = Color(0xFFFFB3B3),
    dangerSurface = Color(0xFF2A1518),
    dangerBorder = Color(0xFF5A2A31),
    scrim = Color(0xB3050508),
    dragHandle = Color(0xFF3A3A48),
    barTrack = Color(0xFF26262E),
)

val LocalSakuColors = staticCompositionLocalOf { SakuDarkColors }

object SakuCategoryPalette {
    val swatches: List<Color> = listOf(
        Color(0xFFF0708A),
        Color(0xFFFB923C),
        Color(0xFFF6C453),
        Color(0xFF5FD3E6),
        Color(0xFF7C8CFF),
        Color(0xFFC4A3FF),
        Color(0xFF9A9AA8),
        Color(0xFFC9A25A),
    )

    val default = Color(0xFFC4A3FF)

    fun colorFor(categoryId: String): Color =
        swatches[(categoryId.hashCode() and Int.MAX_VALUE) % swatches.size]
}