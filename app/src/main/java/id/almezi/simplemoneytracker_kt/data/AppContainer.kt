package id.almezi.simplemoneytracker_kt.data

import android.content.Context
import androidx.room.Room

class AppContainer(context: Context) {
    private val database: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "simplemoneytracker.db"
    ).build()

    val transactionDao: TransactionDao = database.transactionDao()
    val categoryDao: CategoryDao = database.categoryDao()
    val clock: Clock = RealClock()

    val transactionRepository: TransactionRepository = TransactionRepository(
        transactionDao,
        categoryDao
    )
}

class RealClock : Clock {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
