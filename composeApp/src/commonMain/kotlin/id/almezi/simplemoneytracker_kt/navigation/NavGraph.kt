package id.almezi.simplemoneytracker_kt.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import id.almezi.simplemoneytracker_kt.ui.anggaran.AnggaranFormScreen
import id.almezi.simplemoneytracker_kt.ui.anggaran.AnggaranScreen
import id.almezi.simplemoneytracker_kt.ui.components.SakuNavItem
import id.almezi.simplemoneytracker_kt.ui.daftar.DaftarScreen
import id.almezi.simplemoneytracker_kt.ui.kategori.KelolaKategoriScreen
import id.almezi.simplemoneytracker_kt.ui.kategori.TambahKategoriScreen
import id.almezi.simplemoneytracker_kt.ui.ringkasan.RingkasanScreen
import id.almezi.simplemoneytracker_kt.ui.tambah.TambahScreen
import id.almezi.simplemoneytracker_kt.ui.ubah.UbahTransaksiScreen
import id.almezi.simplemoneytracker_kt.SakuStrings

/**
 * Routes as serializable objects rather than string templates.
 *
 * `androidx.navigation:navigation-compose` is Android-only. The JetBrains
 * multiplatform build (`org.jetbrains.androidx.navigation`) uses `@Serializable`
 * route objects, which also removes the hand-written `route()` builders and
 * ARG_* constants the old string-based version needed.
 */
@Serializable
sealed interface Screen {
    @Serializable
    data object Tambah : Screen

    @Serializable
    data object Daftar : Screen

    @Serializable
    data object Ringkasan : Screen

    @Serializable
    data object KelolaKategori : Screen

    @Serializable
    data object TambahKategori : Screen

    @Serializable
    data object Anggaran : Screen

    @Serializable
    data object Cadangan : Screen

    @Serializable
    data class UbahTransaksi(val transactionId: Long) : Screen

    @Serializable
    data class AnggaranForm(val categoryId: String? = null) : Screen
}

/** The three root tabs. [testKey] values are fixed: they form the test tags. */
val bottomNavItems: List<SakuNavItem<Screen>> = listOf(
    SakuNavItem(Screen.Tambah, SakuStrings.nav_tambah, Icons.Default.Add, "tambah"),
    SakuNavItem(Screen.Daftar, SakuStrings.nav_transaksi, Icons.AutoMirrored.Filled.List, "daftar"),
    SakuNavItem(Screen.Ringkasan, SakuStrings.nav_ringkasan, Icons.Default.Summarize, "ringkasan"),
)

/** The tab that matches [destination], or null when the destination is not a tab. */
fun bottomTabFor(destination: Any?): Screen? =
    bottomNavItems.firstOrNull { destination is Screen && it.route == destination }?.route

/**
 * @param cadanganContent the Cadangan (backup) destination.
 *
 * Backup is still Android-only: it uses the Storage Access Framework, `Uri`,
 * and `java.io` (see docs/09 section 5.3). Rather than force those into
 * commonMain, the shared graph accepts the destination as a slot and each
 * platform supplies its own. iOS supplies a placeholder until Stage 5 ports
 * export/import to `UIDocumentPickerViewController`.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    cadanganContent: @Composable (() -> Unit, () -> Unit) -> Unit,
    startScreen: Screen = Screen.Tambah,
) {
    NavHost(navController = navController, startDestination = startScreen) {
        composable<Screen.Tambah> {
            TambahScreen(onOpenKelolaKategori = { navController.navigate(Screen.KelolaKategori) })
        }

        composable<Screen.Daftar> {
            DaftarScreen(
                onAddTransaction = { navController.navigate(Screen.Tambah) },
                onEditTransaction = { id -> navController.navigate(Screen.UbahTransaksi(id)) },
                onOpenBackup = { navController.navigate(Screen.Cadangan) },
            )
        }

        composable<Screen.Ringkasan> {
            RingkasanScreen(
                onAddTransaction = { navController.navigate(Screen.Tambah) },
                onOpenAnggaran = { navController.navigate(Screen.Anggaran) },
                onOpenKelolaKategori = { navController.navigate(Screen.KelolaKategori) },
            )
        }

        composable<Screen.KelolaKategori> {
            KelolaKategoriScreen(
                onBack = { navController.popBackStack() },
                onAddCategory = { navController.navigate(Screen.TambahKategori) },
            )
        }

        composable<Screen.TambahKategori> {
            TambahKategoriScreen(
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }

        composable<Screen.Anggaran> {
            AnggaranScreen(
                onBack = { navController.popBackStack() },
                onAddBudget = { navController.navigate(Screen.AnggaranForm()) },
                onEditBudget = { id -> navController.navigate(Screen.AnggaranForm(id)) },
            )
        }

        composable<Screen.AnggaranForm> { entry ->
            AnggaranFormScreen(
                categoryIdArg = entry.toRoute<Screen.AnggaranForm>().categoryId,
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }

        composable<Screen.Cadangan> {
            cadanganContent(
                { navController.popBackStack() },
                { navController.navigate(Screen.Tambah) },
            )
        }

        composable<Screen.UbahTransaksi> { entry ->
            UbahTransaksiScreen(
                transactionId = entry.toRoute<Screen.UbahTransaksi>().transactionId,
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
    }
}