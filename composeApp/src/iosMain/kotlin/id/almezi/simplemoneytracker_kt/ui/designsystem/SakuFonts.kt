package id.almezi.simplemoneytracker_kt.ui.designsystem

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import id.almezi.simplemoneytracker_kt.Res
import id.almezi.simplemoneytracker_kt.dm_mono_medium
import id.almezi.simplemoneytracker_kt.dm_mono_regular
import id.almezi.simplemoneytracker_kt.dm_sans_bold
import id.almezi.simplemoneytracker_kt.dm_sans_medium
import id.almezi.simplemoneytracker_kt.dm_sans_regular
import id.almezi.simplemoneytracker_kt.dm_sans_semibold

@Composable
actual fun sakuFontSans(): FontFamily = FontFamily(
    Font(Res.font.dm_sans_regular, FontWeight.Normal),
    Font(Res.font.dm_sans_medium, FontWeight.Medium),
    Font(Res.font.dm_sans_semibold, FontWeight.SemiBold),
    Font(Res.font.dm_sans_bold, FontWeight.Bold),
)

@Composable
actual fun sakuFontMono(): FontFamily = FontFamily(
    Font(Res.font.dm_mono_regular, FontWeight.Normal),
    Font(Res.font.dm_mono_medium, FontWeight.Medium),
)
