package id.almezi.simplemoneytracker_kt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.ReadOnlyComposable
import id.almezi.simplemoneytracker_kt.data.AppContainer

/**
 * Platform-independent access to the shared object graph.
 *
 * Replaces the Android-only pattern that every screen used to reach the
 * container:
 *
 *     (LocalContext.current.applicationContext as SimpleMoneyTrackerApp).container
 *
 * That cast cannot compile in commonMain, and it also leaked the Android
 * Application type into every screen. `SakuTheme` is provided this at the root,
 * so screens just read [SakuContainer].
 *
 * Manual DI is preserved, as `AGENTS.md` requires: no Hilt, no Koin, no
 * service locator.
 */
val LocalSakuContainer = staticCompositionLocalOf<AppContainer> {
    error("No AppContainer provided. Wrap the content in SakuApp() or provide " +
        "LocalSakuContainer explicitly.")
}

/** The shared dependency graph. */
val SakuContainer: AppContainer
    @Composable
    @ReadOnlyComposable
    get() = LocalSakuContainer.current

/**
 * Provides the container to a subtree. Call once at the root of each platform.
 *
 * Android wraps the MainActivity content; iOS wraps the ComposeUIViewController
 * content.
 */
@Composable
fun ProvideSakuContainer(container: AppContainer, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSakuContainer provides container, content = content)
}