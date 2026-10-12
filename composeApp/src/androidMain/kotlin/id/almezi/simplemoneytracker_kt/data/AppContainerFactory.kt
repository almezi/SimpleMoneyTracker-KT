package id.almezi.simplemoneytracker_kt.data

import androidx.room3.Room
import android.content.Context
import androidx.room3.RoomDatabase
import kotlinx.coroutines.Dispatchers

/**
 * Android needs a `Context` to locate the database file, so the factory takes
 * one. iOS has its own `createAppContainer()` with no parameters — see
 * `AppContainerFactory.kt` in iosMain.
 *
 * Deliberately not expect/actual: the signatures differ, so each platform
 * simply exposes the one that fits it. The part that *is* identical,
 * `buildDatabase`, lives in commonMain and takes the dispatcher as a parameter.
 */
fun createAppContainer(context: Context): AppContainer {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("simplemoneytracker.db")
    val builder: RoomDatabase.Builder<AppDatabase> = Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
    return AppContainer(buildDatabase(builder, Dispatchers.IO))
}
