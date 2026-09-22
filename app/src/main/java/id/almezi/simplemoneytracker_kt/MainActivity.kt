package id.almezi.simplemoneytracker_kt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.almezi.simplemoneytracker_kt.navigation.NavGraph
import id.almezi.simplemoneytracker_kt.navigation.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentRoute == Screen.Tambah.route,
                                onClick = { navController.navigate(Screen.Tambah.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                } },
                                icon = { Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(R.string.tambah_transaksi)
                                ) },
                                label = { Text(stringResource(R.string.tambah_transaksi)) }
                            )
                            NavigationBarItem(
                                selected = currentRoute == Screen.Daftar.route,
                                onClick = { navController.navigate(Screen.Daftar.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                } },
                                icon = { Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = stringResource(R.string.daftar_transaksi)
                                ) },
                                label = { Text(stringResource(R.string.daftar_transaksi)) }
                            )
                            NavigationBarItem(
                                selected = currentRoute == Screen.Ringkasan.route,
                                onClick = { navController.navigate(Screen.Ringkasan.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                } },
                                icon = { Icon(
                                    imageVector = Icons.Default.Summarize,
                                    contentDescription = stringResource(R.string.ringkasan_transaksi)
                                ) },
                                label = { Text(stringResource(R.string.ringkasan_transaksi)) }
                            )
                        }
                    }
                ) { padding ->
                    Surface(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavGraph(navController = navController)
                    }
                }
            }
        }
    }
}
