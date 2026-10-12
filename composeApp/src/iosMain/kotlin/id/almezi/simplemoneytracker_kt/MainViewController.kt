package id.almezi.simplemoneytracker_kt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import id.almezi.simplemoneytracker_kt.data.CategorySeed
import id.almezi.simplemoneytracker_kt.data.applicationScope
import id.almezi.simplemoneytracker_kt.data.createAppContainer
import id.almezi.simplemoneytracker_kt.data.seedIfNeeded
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuCategoryPalette
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import platform.UIKit.UIViewController

/**
 * The iOS entry point, called from `iosApp/`.
 *
 * Mirrors `MainActivity` on Android and nothing more: build the container,
 * provide it, hand off to the shared `SakuApp`. Keeping this file thin is the
 * point of the migration — all UI lives in commonMain.
 *
 * Compose Multiplatform requires a fresh view controller per entry, so this is
 * a function rather than a stored property.
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    App()
}

/**
 * Root composable for iOS.
 *
 * Mirrors the body of `MainActivity.setContent`. Two things are unavoidable
 * platform work: there is no `Context`, so the container comes from
 * `createAppContainer()`, and there is no Application, so readiness is tracked
 * in Compose state rather than a `StateFlow` on the Application.
 */
@Composable
fun App() {
    val container = remember { createAppContainer() }
    val scope = remember { applicationScope() }
    val categoryColors = remember {
        CategorySeed.all.associate { it.id to SakuCategoryPalette.hexFor(it.id) }
    }
    var ready by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        container.seedIfNeeded(
            scope = scope,
            categoryColors = categoryColors,
            onReady = { ready = true },
        )
    }

    ProvideSakuContainer(container = container) {
        SakuTheme {
            SakuApp(
                ready = ready,
            )
        }
    }
}