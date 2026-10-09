package id.almezi.simplemoneytracker_kt.data

import android.content.Context
import androidx.room.Room

class AppContainer(context: Context) {
    private val database: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "simplemoneytracker.db"
    ).addMigrations(*AppDatabase.ALL_MIGRATIONS).build()

    val transactionDao: TransactionDao = database.transactionDao()
    val categoryDao: CategoryDao = database.categoryDao()
    val accountDao: AccountDao = database.accountDao()
    val clock: Clock = RealClock()

    val transactionRepository: TransactionRepository = TransactionRepository(
        transactionDao,
        categoryDao,
        accountDao
    )
}

class RealClock : Clock {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
