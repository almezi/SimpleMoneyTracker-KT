package id.almezi.simplemoneytracker_kt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.almezi.simplemoneytracker_kt.navigation.NavGraph
import id.almezi.simplemoneytracker_kt.navigation.Screen
import id.almezi.simplemoneytracker_kt.navigation.bottomNavItems
import id.almezi.simplemoneytracker_kt.navigation.bottomNavRoutes
import id.almezi.simplemoneytracker_kt.ui.components.SakuBottomNav
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SakuTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SakuTheme.colors.bg)
                ) {
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