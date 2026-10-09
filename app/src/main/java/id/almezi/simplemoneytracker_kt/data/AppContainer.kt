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

    val budgetDao: BudgetDao = database.budgetDao()

    val transactionRepository: TransactionRepository = TransactionRepository(
        database,
        transactionDao,
        categoryDao,
        accountDao
    )

    val categoryRepository: CategoryRepository = CategoryRepository(
        database,
        categoryDao,
        transactionDao
    )

    val budgetRepository: BudgetRepository = BudgetRepository(
        database,
        budgetDao,
        clock
    )

    val backupRepository: BackupRepository = BackupRepository(
        transactionRepository,
        categoryRepository,
        budgetRepository,
        accountDao
    )
}

class RealClock : Clock {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
