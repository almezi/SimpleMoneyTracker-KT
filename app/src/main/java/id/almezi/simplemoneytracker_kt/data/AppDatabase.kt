package id.almezi.simplemoneytracker_kt.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Transaction::class, Category::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
}
