package id.almezi.simplemoneytracker_kt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.almezi.simplemoneytracker_kt.navigation.NavGraph
import id.almezi.simplemoneytracker_kt.navigation.Screen
import id.almezi.simplemoneytracker_kt.navigation.bottomNavItems
import id.almezi.simplemoneytracker_kt.navigation.bottomTabFor
import id.almezi.simplemoneytracker_kt.ui.components.SakuBottomNav
import id.almezi.simplemoneytracker_kt.ui.components.SakuLoadingScreen
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuMotion
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme
import kotlinx.coroutines.delay
import kotlin.time.TimeSource

/**
 * The shared application shell: loading gate, navigation graph, bottom bar.
 *
 * This is the only place that decides what the user sees, so Android and iOS
 * cannot drift apart. Each platform supplies three things and nothing else:
 * the container, the ready flag, and the backup screen.
 *
 * @param ready whether the database has opened and seeding has finished.
 * @param cadanganContent the Cadangan (backup) destination. Backup is still
 *   Android-only — it uses the Storage Access Framework, `Uri` and `java.io` —
 *   so it arrives as a slot rather than living in this module. See docs/09
 *   section 5.3.
 */
@Composable
fun SakuApp(
    ready: Boolean,
    modifier: Modifier = Modifier,
    cadanganContent: @Composable (() -> Unit, () -> Unit) -> Unit,
) {
    var showLoading by remember { mutableStateOf(true) }
    val startedAt = remember { TimeSource.Monotonic.markNow() }

    LaunchedEffect(ready) {
        if (ready) {
            // Hold the loading screen briefly even when the database is already
            // open, so the first frame is not a flash of half-built layout.
            val remaining =
                SakuMotion.loadingMinimumMillis - startedAt.elapsedNow().inWholeMilliseconds
            if (remaining > 0L) delay(remaining)
            showLoading = false
        }
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = bottomTabFor(backStackEntry?.destination)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SakuTheme.colors.bg),
    ) {
        if (showLoading) {
            SakuLoadingScreen()
        } else {
            NavGraph(
                navController = navController,
                cadanganContent = cadanganContent,
            )

            if (currentTab != null) {
                SakuBottomNav(
                    items = bottomNavItems,
                    currentRoute = currentTab,
                    onSelect = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Tambah) { saveState = true }
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}