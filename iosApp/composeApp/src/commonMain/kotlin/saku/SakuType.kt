package saku

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

val SakuFontSans: FontFamily
    @Composable get() = FontFamily(
    Font(Res.font.dm_sans_regular, FontWeight.Normal),
    Font(Res.font.dm_sans_medium, FontWeight.Medium),
    Font(Res.font.dm_sans_semibold, FontWeight.SemiBold),
    Font(Res.font.dm_sans_bold, FontWeight.Bold),
)

val SakuFontMono: FontFamily
    @Composable get() = FontFamily(
    Font(Res.font.dm_mono_regular, FontWeight.Normal),
    Font(Res.font.dm_mono_medium, FontWeight.Medium),
)

object SakuTextStyles {
    private val trim = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    )

    val screenTitle: TextStyle
        @Composable get() = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.01).em,
        lineHeightStyle = trim,
    )

    val sectionLabel: TextStyle
        @Composable get() = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.08.em,
    )

    val body: TextStyle
        @Composable get() = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        lineHeightStyle = trim,
    )

    val bodyStrong: TextStyle
        @Composable get() = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        lineHeightStyle = trim,
    )

    val bodySmall: TextStyle
        @Composable get() = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        lineHeightStyle = trim,
    )

    val caption: TextStyle
        @Composable get() = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        lineHeightStyle = trim,
    )

    val helper: TextStyle
        @Composable get() = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        lineHeightStyle = trim,
    )

    val emptyTitle: TextStyle
        @Composable get() = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        lineHeightStyle = trim,
    )

    val buttonPrimary: TextStyle
        @Composable get() = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        lineHeightStyle = trim,
    )

    val buttonSecondary: TextStyle
        @Composable get() = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        lineHeightStyle = trim,
    )

    val toggleOption: TextStyle
        @Composable get() = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        lineHeightStyle = trim,
    )

    val chip: TextStyle
        @Composable get() = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        lineHeightStyle = trim,
    )

    val typeLabel: TextStyle
        @Composable get() = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        lineHeightStyle = trim,
    )

    val monthLabel: TextStyle
        @Composable get() = TextStyle(
        fontFamily = SakuFontMono,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.12.em,
        fontFeatureSettings = "tnum",
        lineHeightStyle = trim,
    )

    val amountLarge: TextStyle
        @Composable get() = TextStyle(
        fontFamily = SakuFontMono,
        fontWeight = FontWeight.Medium,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        fontFeatureSettings = "tnum",
        lineHeightStyle = trim,
    )

    val amountMedium: TextStyle
        @Composable get() = TextStyle(
        fontFamily = SakuFontMono,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontFeatureSettings = "tnum",
        lineHeightStyle = trim,
    )

    val amountSmall: TextStyle
        @Composable get() = TextStyle(
        fontFamily = SakuFontMono,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontFeatureSettings = "tnum",
        lineHeightStyle = trim,
    )

    val amountList: TextStyle
        @Composable get() = TextStyle(
        fontFamily = SakuFontMono,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontFeatureSettings = "tnum",
        lineHeightStyle = trim,
            )
}

val SakuTypography: Typography
    @Composable get() = Typography(
    titleLarge = SakuTextStyles.screenTitle,
    bodyLarge = SakuTextStyles.body,
    bodyMedium = SakuTextStyles.bodySmall,
    bodySmall = SakuTextStyles.caption,
    labelLarge = SakuTextStyles.buttonSecondary,
    labelMedium = SakuTextStyles.typeLabel,
    labelSmall = SakuTextStyles.sectionLabel,
)