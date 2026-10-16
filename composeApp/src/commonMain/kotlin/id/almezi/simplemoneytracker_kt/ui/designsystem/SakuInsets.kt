package id.almezi.simplemoneytracker_kt.ui.designsystem

import id.almezi.simplemoneytracker_kt.ui.designsystem.safeAreaTop

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
 * This replaces `Modifier.safeAreaTop()`, which is available in
 * commonMain but is an Android-only concept: on iOS it resolves to zero
 * insets, so every screen using it would have drawn under the notch and the
 * Dynamic Island.
 *
 * [WindowInsets.safeDrawing] covers both platforms:
 *  - Android: the status bar, and horizontal cutout/gesture-nav insets
 *  - iOS: the safe area (notch, status area, landscape cutout)
 *
 * Only the top and horizontal sides are applied. The bottom is left to each
 * screen's footer and to the bottom bar's own `navBarBottomOffset`, so padding
 * is not applied twice.
 */
@Composable
fun Modifier.safeAreaTop(): Modifier = windowInsetsPadding(
    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
)