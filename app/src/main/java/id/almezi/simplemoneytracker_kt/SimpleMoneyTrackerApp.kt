package id.almezi.simplemoneytracker_kt

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import id.almezi.simplemoneytracker_kt.data.AppContainer
import id.almezi.simplemoneytracker_kt.data.CategorySeed

class SimpleMoneyTrackerApp : Application() {
    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        applicationScope.launch {
            container.categoryDao.insert(*CategorySeed.all.toTypedArray())
        }
    }
}
