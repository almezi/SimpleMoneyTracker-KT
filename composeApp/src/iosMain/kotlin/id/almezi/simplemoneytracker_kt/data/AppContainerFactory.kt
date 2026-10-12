package id.almezi.simplemoneytracker_kt.data

import androidx.room3.Room
import androidx.room3.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * iOS database path comes from `NSFileManager` + `NSDocumentDirectory`.
 *
 * Takes no arguments — there is no `Context` on iOS.
 */
@OptIn(ExperimentalForeignApi::class)
fun createAppContainer(): AppContainer {
    val documentDirectory: NSURL = requireNotNull(
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
    ) { "Could not resolve NSDocumentDirectory" }

    val dbFilePath = documentDirectory.path + "/simplemoneytracker.db"
    val builder: RoomDatabase.Builder<AppDatabase> =
        Room.databaseBuilder<AppDatabase>(name = dbFilePath)
    // Dispatchers.IO is JVM-only and does not exist on Kotlin/Native.
    return AppContainer(buildDatabase(builder, Dispatchers.Default))
}
