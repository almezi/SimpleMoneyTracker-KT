package id.almezi.simplemoneytracker_kt.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import id.almezi.simplemoneytracker_kt.R
import id.almezi.simplemoneytracker_kt.ui.anggaran.AnggaranFormScreen
import id.almezi.simplemoneytracker_kt.ui.anggaran.AnggaranScreen
import id.almezi.simplemoneytracker_kt.ui.cadangan.CadanganScreen
import id.almezi.simplemoneytracker_kt.ui.components.SakuNavItem
import id.almezi.simplemoneytracker_kt.ui.daftar.DaftarScreen
import id.almezi.simplemoneytracker_kt.ui.kategori.KelolaKategoriScreen
import id.almezi.simplemoneytracker_kt.ui.kategori.TambahKategoriScreen
import id.almezi.simplemoneytracker_kt.ui.ringkasan.RingkasanScreen
import id.almezi.simplemoneytracker_kt.ui.tambah.TambahScreen
import id.almezi.simplemoneytracker_kt.ui.ubah.UbahTransaksiScreen

sealed class Screen(val route: String) {
    object Tambah : Screen("tambah")
    object Daftar : Screen("daftar")
    object Ringkasan : Screen("ringkasan")
    object KelolaKategori : Screen("kelola_kategori")
    object TambahKategori : Screen("tambah_kategori")
    object Anggaran : Screen("anggaran")
    object Cadangan : Screen("cadangan")

    object UbahTransaksi : Screen("ubah_transaksi/{transactionId}") {
        const val ARG_TRANSACTION_ID = "transactionId"
        fun route(transactionId: Long) = "ubah_transaksi/$transactionId"
    }

    object AnggaranForm : Screen("anggaran/form?categoryId={categoryId}") {
        const val ARG_CATEGORY_ID = "categoryId"
        fun route(categoryId: String? = null) =
            if (categoryId == null) "anggaran/form" else "anggaran/form?categoryId=$categoryId"
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
                onEditTransaction = { id -> navController.navigate(Screen.UbahTransaksi.route(id)) },
                onOpenBackup = { navController.navigate(Screen.Cadangan.route) },
            )
        }
        composable(Screen.Ringkasan.route) {
            RingkasanScreen(
                onAddTransaction = { navController.navigate(Screen.Tambah.route) },
                onOpenAnggaran = { navController.navigate(Screen.Anggaran.route) },
                onOpenKelolaKategori = { navController.navigate(Screen.KelolaKategori.route) },
            )
        }
        composable(Screen.KelolaKategori.route) {
            KelolaKategoriScreen(
                onBack = { navController.popBackStack() },
                onAddCategory = { navController.navigate(Screen.TambahKategori.route) },
            )
        }
        composable(Screen.TambahKategori.route) {
            TambahKategoriScreen(
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Screen.Anggaran.route) {
            AnggaranScreen(
                onBack = { navController.popBackStack() },
                onAddBudget = { navController.navigate(Screen.AnggaranForm.route()) },
                onEditBudget = { id -> navController.navigate(Screen.AnggaranForm.route(id)) },
            )
        }
        composable(
            route = Screen.AnggaranForm.route,
            arguments = listOf(
                navArgument(Screen.AnggaranForm.ARG_CATEGORY_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            AnggaranFormScreen(
                categoryIdArg = entry.arguments?.getString(Screen.AnggaranForm.ARG_CATEGORY_ID),
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Screen.Cadangan.route) {
            CadanganScreen(
                onBack = { navController.popBackStack() },
                onAddTransaction = { navController.navigate(Screen.Tambah.route) },
            )
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
    }
}