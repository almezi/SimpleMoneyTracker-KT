package id.almezi.simplemoneytracker_kt

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import id.almezi.simplemoneytracker_kt.data.AppContainer
import id.almezi.simplemoneytracker_kt.data.AccountSeed
import id.almezi.simplemoneytracker_kt.data.CategorySeed
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuCategoryPalette

class SimpleMoneyTrackerApp : Application() {
    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        applicationScope.launch {
            container.categoryDao.insert(*CategorySeed.all.toTypedArray())
            container.accountDao.insert(*AccountSeed.all.toTypedArray())
            CategorySeed.all.forEach { seed ->
                container.categoryRepository.assignColorIfMissing(seed.id, SakuCategoryPalette.hexFor(seed.id))
            }
            _isReady.value = true
        }
    }
}
