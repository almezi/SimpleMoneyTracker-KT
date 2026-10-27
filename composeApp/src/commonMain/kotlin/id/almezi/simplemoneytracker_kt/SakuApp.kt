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
 * cannot drift apart. Each platform supplies two things and nothing else: the
 * container and the ready flag.
 *
 * @param ready whether the database has opened and seeding has finished.
 */
@Composable
fun SakuApp(
    ready: Boolean,
    modifier: Modifier = Modifier,
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
            NavGraph(navController = navController)

            if (currentTab != null) {
                SakuBottomNav(
                    items = bottomNavItems,
                    currentRoute = currentTab,
                    onSelect = { route ->
                        navController.navigate(route) {
                            // The standard bottom-nav trio: pop the current tab
                            // (saving it), never stack a duplicate of the tab
                            // already on top, and restore the saved tab — so
                            // scroll position and the selected month survive a
                            // tab switch. `restoreState` only works because the
                            // totals ViewModels observe the database instead of
                            // refreshing once per creation.
                            popUpTo(Screen.Tambah) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}