package id.almezi.simplemoneytracker_kt.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import id.almezi.simplemoneytracker_kt.SakuContainer
import id.almezi.simplemoneytracker_kt.SakuStrings
import id.almezi.simplemoneytracker_kt.data.backup.rememberBackupFileStore
import id.almezi.simplemoneytracker_kt.data.backup.rememberFilePicker
import id.almezi.simplemoneytracker_kt.ui.anggaran.AnggaranFormScreen
import id.almezi.simplemoneytracker_kt.ui.anggaran.AnggaranScreen
import id.almezi.simplemoneytracker_kt.ui.cadangan.CadanganScreen
import id.almezi.simplemoneytracker_kt.ui.cadangan.CadanganViewModel
import id.almezi.simplemoneytracker_kt.ui.components.SakuNavItem
import id.almezi.simplemoneytracker_kt.ui.daftar.DaftarScreen
import id.almezi.simplemoneytracker_kt.ui.kategori.KelolaKategoriScreen
import id.almezi.simplemoneytracker_kt.ui.kategori.TambahKategoriScreen
import id.almezi.simplemoneytracker_kt.ui.ringkasan.RingkasanScreen
import id.almezi.simplemoneytracker_kt.ui.tambah.TambahScreen
import id.almezi.simplemoneytracker_kt.ui.ubah.UbahTransaksiScreen
import id.almezi.simplemoneytracker_kt.ui.designsystem.formatShortDate

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

/**
 * The tab matching [destination], or null when the destination is not a tab.
 *
 * `destination` is a `NavDestination`, never a `Screen`, so it cannot be
 * compared against the routes directly. An earlier version took `Any?` and
 * tested `destination is Screen`, which is never true — that silently hid the
 * bottom bar on both platforms.
 *
 * `NavDestination.hasRoute(route: String, arguments: Bundle?)` is a *member*,
 * and Kotlin resolves members ahead of the reified
 * `NavDestination.hasRoute<T>()` extension, so the generic form is not
 * reachable by name. Matching on the serialised route name is the workaround.
 * `Screen.serializer().descriptor.serialName` derives that name from the type,
 * so the comparison stays in sync with the sealed interface instead of being a
 * hand-copied string that can rot.
 */
fun bottomTabFor(destination: NavDestination?): Screen? {
    val route = destination?.route ?: return null
    return bottomNavItems.firstOrNull { it.route.routeName() == route }?.route
}

/**
 * The serialised route name navigation records for a typed route.
 *
 * Named `routeName` rather than `serialName`, and the serializer lookup
 * `routeSerializer` rather than `serializer`. A helper called `Any.serializer()`
 * shadows `kotlinx.serialization.serializer()` for every file in this package —
 * including tests — which is exactly the kind of trap that costs an hour.
 */
private fun Screen.routeSerializer(): KSerializer<*> =
    when (this) {
        is Screen.Tambah -> Screen.Tambah.serializer()
        is Screen.Daftar -> Screen.Daftar.serializer()
        is Screen.Ringkasan -> Screen.Ringkasan.serializer()
        else -> error("routeName() is only defined for the three bottom tabs")
    }

private fun Screen.routeName(): String = routeSerializer().descriptor.serialName

/**
 * The graph connects all screens from the shared `:composeApp` module.
 * Backup (`Cadangan`) is now fully shared — the platform-specific file
 * picker (`FilePicker`) and the export mechanism (`BackupFileStore`) are
 * provided through `AppContainer`, so no slot is needed (docs/09 §5.3).
 */
@Composable
fun NavGraph(
    navController: NavHostController,
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
            val container = SakuContainer
            val viewModel: CadanganViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        CadanganViewModel(
                            container.backupSerializer,
                            container.backupPreferences,
                            container.transactionRepository,
                            container.categoryRepository,
                            container.clock,
                        )
                    }
                },
            )
            val uiState by viewModel.uiState.collectAsState()
            val scope = rememberCoroutineScope()
            val filePicker = rememberFilePicker()
            val backupFileStore = rememberBackupFileStore()
            val lastBackupDate = uiState.lastBackupMillis?.let { formatShortDate(it) }
            // An export failure must be a toast, not a crash: this scope runs on
            // main, and an uncaught exception in it takes the app down.
            fun export(fileName: String, content: suspend () -> String, mimeType: String) {
                scope.launch {
                    runCatching {
                        backupFileStore.shareText(fileName, content(), mimeType)
                    }
                        .onSuccess { path -> viewModel.markBackupDone(path) }
                        .onFailure { viewModel.notifyExportFailed() }
                }
            }
            CadanganScreen(
                transactionCount = uiState.transactionCount,
                accountCount = uiState.accountCount,
                categoryCount = uiState.categoryCount,
                lastBackupDate = lastBackupDate,
                toast = uiState.toast,
                onToastDismiss = viewModel::clearToast,
                onNavigateBack = { navController.popBackStack() },
                onExportCsv = {
                    export("saku-cadangan.csv", { viewModel.buildCsv() }, "text/csv")
                },
                onExportJson = {
                    export("saku-cadangan.json", { viewModel.buildJson() }, "application/json")
                },
                onImportBackup = {
                    filePicker.present { raw ->
                        if (raw != null) viewModel.import(raw)
                    }
                },
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