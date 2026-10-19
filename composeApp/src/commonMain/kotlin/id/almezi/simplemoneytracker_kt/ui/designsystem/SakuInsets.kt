package id.almezi.simplemoneytracker_kt.ui.designsystem

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Keeps content clear of the status bar and any display cutout.
 *
 * [WindowInsets.safeDrawing] covers both platforms (Android status bar, iOS
 * notch/Dynamic Island). Only top + horizontal: bottom stays with each
 * screen's footer and the bottom bar's own insets (see [safeAreaBottom]), so
 * padding is not applied twice.
 */
@Composable
fun Modifier.safeAreaTop(): Modifier = windowInsetsPadding(
    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
)

/**
 * Keeps content clear of the system navigation bar and the iOS home
 * indicator. Android 15+ draws edge-to-edge, so every scroll container adds
 * this on top of its design bottom reserve. Bottom only, so it composes with
 * [safeAreaTop] without double-counting horizontal insets.
 */
@Composable
fun Modifier.safeAreaBottom(): Modifier = windowInsetsPadding(
    WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
)