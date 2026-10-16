package id.almezi.simplemoneytracker_kt

import android.app.Application
import id.almezi.simplemoneytracker_kt.data.AppContainer
import id.almezi.simplemoneytracker_kt.data.BackupRepository
import id.almezi.simplemoneytracker_kt.data.CategorySeed
import id.almezi.simplemoneytracker_kt.data.createAppContainer
import id.almezi.simplemoneytracker_kt.data.seedIfNeeded
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuCategoryPalette
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SimpleMoneyTrackerApp : Application() {

    private val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * The shared object graph now lives in the :composeApp KMP module.
     *
     * `backupRepository` is still Android-only (Context, Uri, java.io) and is
     * wired up here. Its multiplatform port is Stage 5 of the iOS plan.
     */
    val container: AppContainer by lazy { createAppContainer(this) }

    val backupRepository: BackupRepository by lazy {
        BackupRepository(
            container.transactionRepository,
            container.categoryRepository,
            container.budgetRepository,
            container.accountDao
        )
    }

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        container.seedIfNeeded(
            scope = applicationScope,
            categoryColors = CategorySeed.all.associate { seed ->
                seed.id to SakuCategoryPalette.hexFor(seed.id)
            }
        ) {
            _isReady.value = true
        }
    }
}