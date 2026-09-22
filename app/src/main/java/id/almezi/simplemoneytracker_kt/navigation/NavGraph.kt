package id.almezi.simplemoneytracker_kt.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import id.almezi.simplemoneytracker_kt.ui.daftar.DaftarScreen
import id.almezi.simplemoneytracker_kt.ui.ringkasan.RingkasanScreen
import id.almezi.simplemoneytracker_kt.ui.tambah.TambahScreen

sealed class Screen(val route: String) {
    object Tambah : Screen("tambah")
    object Daftar : Screen("daftar")
    object Ringkasan : Screen("ringkasan")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Tambah.route
    ) {
        composable(Screen.Tambah.route) {
            TambahScreen()
        }
        composable(Screen.Daftar.route) {
            DaftarScreen()
        }
        composable(Screen.Ringkasan.route) {
            RingkasanScreen()
        }
    }
}
