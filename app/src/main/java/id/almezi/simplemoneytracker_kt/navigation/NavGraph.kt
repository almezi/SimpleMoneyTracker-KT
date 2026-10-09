package id.almezi.simplemoneytracker_kt.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import id.almezi.simplemoneytracker_kt.R
import id.almezi.simplemoneytracker_kt.ui.components.SakuNavItem
import id.almezi.simplemoneytracker_kt.ui.daftar.DaftarScreen
import id.almezi.simplemoneytracker_kt.ui.ringkasan.RingkasanScreen
import id.almezi.simplemoneytracker_kt.ui.tambah.KelolaKategoriPlaceholderScreen
import id.almezi.simplemoneytracker_kt.ui.tambah.TambahScreen
import id.almezi.simplemoneytracker_kt.ui.ubah.UbahTransaksiScreen

sealed class Screen(val route: String) {
    object Tambah : Screen("tambah")
    object Daftar : Screen("daftar")
    object Ringkasan : Screen("ringkasan")
    object KelolaKategori : Screen("kelola_kategori")
    object UbahTransaksi : Screen("ubah_transaksi/{transactionId}") {
        const val ARG_TRANSACTION_ID = "transactionId"
        fun route(transactionId: Long) = "ubah_transaksi/$transactionId"
    }
}

val bottomNavItems = listOf(
    SakuNavItem(Screen.Tambah.route, R.string.nav_tambah, Icons.Default.Add),
    SakuNavItem(Screen.Daftar.route, R.string.nav_transaksi, Icons.AutoMirrored.Filled.List),
    SakuNavItem(Screen.Ringkasan.route, R.string.nav_ringkasan, Icons.Default.Summarize)
)

val bottomNavRoutes = setOf(
    Screen.Tambah.route,
    Screen.Daftar.route,
    Screen.Ringkasan.route
)

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Tambah.route
    ) {
        composable(Screen.Tambah.route) {
            TambahScreen(
                onOpenKelolaKategori = { navController.navigate(Screen.KelolaKategori.route) }
            )
        }
        composable(Screen.Daftar.route) {
            DaftarScreen(
                onAddTransaction = { navController.navigate(Screen.Tambah.route) },
                onEditTransaction = { id ->
                    navController.navigate(Screen.UbahTransaksi.route(id))
                }
            )
        }
        composable(Screen.Ringkasan.route) {
            RingkasanScreen()
        }
        composable(
            route = Screen.UbahTransaksi.route,
            arguments = listOf(
                navArgument(Screen.UbahTransaksi.ARG_TRANSACTION_ID) { type = NavType.LongType }
            )
        ) { entry ->
            val id = entry.arguments?.getLong(Screen.UbahTransaksi.ARG_TRANSACTION_ID) ?: 0L
            UbahTransaksiScreen(
                transactionId = id,
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.KelolaKategori.route) {
            KelolaKategoriPlaceholderScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}