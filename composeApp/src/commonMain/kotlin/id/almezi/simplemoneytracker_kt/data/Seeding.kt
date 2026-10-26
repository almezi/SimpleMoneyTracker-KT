package id.almezi.simplemoneytracker_kt.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Background scope for first-run seeding.
 *
 * On Android the Application class owns this scope; the shared module exposes
 * the same shape so iOS can create an identical one.
 */
fun applicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

/**
 * Seeds the reference data. Safe to call on every launch.
 *
 * Both seeds insert with `OnConflictStrategy.IGNORE`. Colors are assigned only
 * where missing, so a color the user already picked is never overwritten.
 *
 * The seeds are guarded: if the write fails (disk full, corrupted database),
 * [onReady] still fires and the app opens instead of hanging on the loading
 * screen forever. Reference data may be incomplete in that window; the next
 * launch retries, and IGNORE makes a partial seed safe to re-run.
 */
fun AppContainer.seedIfNeeded(
    scope: CoroutineScope,
    categoryColors: Map<String, String>,
    onReady: () -> Unit
) {
    scope.launch {
        runCatching {
            categoryDao.insert(*CategorySeed.all.toTypedArray())
            accountDao.insert(*AccountSeed.all.toTypedArray())
            categoryColors.forEach { (id, hex) -> categoryRepository.assignColorIfMissing(id, hex) }
        }
        onReady()
    }
}