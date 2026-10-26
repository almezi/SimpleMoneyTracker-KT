package id.almezi.simplemoneytracker_kt.data

import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Shared database construction, identical on every platform.
 *
 * Lives in commonMain so the two platforms cannot drift: same migrations, same
 * driver, same query context.
 *
 * `BundledSQLiteDriver` ships the *same* SQLite build for Android and iOS, so
 * the two platforms cannot diverge in SQL behaviour either. `setQueryExecutor`
 * would be the Android-only spelling; `setQueryCoroutineContext` is the
 * multiplatform one.
 *
 * @param dispatcher the platform's blocking-capable dispatcher. This is the one
 *   genuinely platform-specific input, which is why it is a parameter instead
 *   of a duplicated function: Android passes `Dispatchers.IO`, iOS passes
 *   `Dispatchers.Default` because `Dispatchers.IO` is JVM-only and does not
 *   exist on Kotlin/Native.
 */
fun buildDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
    dispatcher: CoroutineDispatcher,
): AppDatabase =
    builder
        .addMigrations(*AppDatabase.ALL_MIGRATIONS)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(dispatcher)
        // FIX BUG_Found_03: every commit lands directly in the main .db file;
        // there is no hot -wal window that storage distress can invalidate
        // and silently discard. TRUNCATE's write cost is irrelevant for a
        // single-user local tracker.
        .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
        .build()