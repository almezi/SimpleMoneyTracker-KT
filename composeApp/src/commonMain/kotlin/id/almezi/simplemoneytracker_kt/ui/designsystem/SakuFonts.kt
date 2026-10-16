package id.almezi.simplemoneytracker_kt.ui.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

/**
 * Saku's two type families, resolved per platform.
 *
 * `R.font.*` is Android-only, and Compose Multiplatform's own
 * `composeResources/font/` accessors do not resolve for the Android target
 * (see docs/09 section 6.5). Rather than fight the codegen, each platform
 * loads the TTFs the way it prefers:
 *
 *  - androidMain reads `src/androidMain/res/font/` through its own `R.font.*`
 *  - iosMain reads `src/commonMain/composeResources/font/` through
 *    `Res.font.*`, which Compose Multiplatform generates correctly for native
 *
 * The TTFs therefore exist in two places on disk. They are binary assets and
 * byte-identical, so treat the copies as one logical file; collapse them if
 * Compose Multiplatform ever fixes Android accessor generation.
 */
@Composable
expect fun sakuFontSans(): FontFamily

@Composable
expect fun sakuFontMono(): FontFamily
