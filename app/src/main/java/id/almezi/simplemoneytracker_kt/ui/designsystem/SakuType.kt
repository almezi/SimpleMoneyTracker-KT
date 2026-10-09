package id.almezi.simplemoneytracker_kt.ui.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import id.almezi.simplemoneytracker_kt.R

val SakuFontSans = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
    Font(R.font.dm_sans_semibold, FontWeight.SemiBold),
    Font(R.font.dm_sans_bold, FontWeight.Bold),
)

val SakuFontMono = FontFamily(
    Font(R.font.dm_mono_regular, FontWeight.Normal),
    Font(R.font.dm_mono_medium, FontWeight.Medium),
)

object SakuTextStyles {
    private val trim = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    )

    val screenTitle = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.01).em,
        lineHeightStyle = trim,
    )

    val sectionLabel = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.08.em,
    )

    val body = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        lineHeightStyle = trim,
    )

    val bodyStrong = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        lineHeightStyle = trim,
    )

    val bodySmall = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        lineHeightStyle = trim,
    )

    val caption = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        lineHeightStyle = trim,
    )

    val helper = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        lineHeightStyle = trim,
    )

    val emptyTitle = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        lineHeightStyle = trim,
    )

    val buttonPrimary = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        lineHeightStyle = trim,
    )

    val buttonSecondary = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        lineHeightStyle = trim,
    )

    val toggleOption = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        lineHeightStyle = trim,
    )

    val chip = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        lineHeightStyle = trim,
    )

    val typeLabel = TextStyle(
        fontFamily = SakuFontSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        lineHeightStyle = trim,
    )

    val monthLabel = TextStyle(
        fontFamily = SakuFontMono,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.12.em,
        fontFeatureSettings = "tnum",
        lineHeightStyle = trim,
    )

    val amountLarge = TextStyle(
        fontFamily = SakuFontMono,
        fontWeight = FontWeight.Medium,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        fontFeatureSettings = "tnum",
        lineHeightStyle = trim,
    )

    val amountMedium = TextStyle(
        fontFamily = SakuFontMono,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontFeatureSettings = "tnum",
        lineHeightStyle = trim,
    )

    val amountSmall = TextStyle(
        fontFamily = SakuFontMono,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontFeatureSettings = "tnum",
        lineHeightStyle = trim,
    )

    val amountList = TextStyle(
        fontFamily = SakuFontMono,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontFeatureSettings = "tnum",
        lineHeightStyle = trim,
    )
}

val SakuTypography = Typography(
    titleLarge = SakuTextStyles.screenTitle,
    bodyLarge = SakuTextStyles.body,
    bodyMedium = SakuTextStyles.bodySmall,
    bodySmall = SakuTextStyles.caption,
    labelLarge = SakuTextStyles.buttonSecondary,
    labelMedium = SakuTextStyles.typeLabel,
    labelSmall = SakuTextStyles.sectionLabel,
)