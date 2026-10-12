package id.almezi.simplemoneytracker_kt

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import id.almezi.simplemoneytracker_kt.navigation.NavGraph
import id.almezi.simplemoneytracker_kt.navigation.bottomNavItems
import id.almezi.simplemoneytracker_kt.navigation.bottomNavRoutes
import id.almezi.simplemoneytracker_kt.ui.components.SakuBottomNav
import id.almezi.simplemoneytracker_kt.ui.components.SakuLoadingScreen
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuMotion
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SakuTheme {
                val app = LocalContext.current.applicationContext as SimpleMoneyTrackerApp
                val isReady by app.isReady.collectAsState()

                var showLoading by remember { mutableStateOf(true) }
                val loadingStartedAt = remember { SystemClock.elapsedRealtime() }
                LaunchedEffect(isReady) {
                    if (isReady) {
                        val elapsed = SystemClock.elapsedRealtime() - loadingStartedAt
                        val remaining = SakuMotion.loadingMinimumMillis - elapsed
                        if (remaining > 0) delay(remaining)
                        showLoading = false
                    }
                }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SakuTheme.colors.bg)
                ) {
                    if (showLoading) {
                        SakuLoadingScreen()
                    } else {
                        NavGraph(navController = navController)

                        if (currentRoute in bottomNavRoutes) {
                            SakuBottomNav(
                                items = bottomNavItems,
                                currentRoute = currentRoute,
                                onSelect = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }
        }
    }
}