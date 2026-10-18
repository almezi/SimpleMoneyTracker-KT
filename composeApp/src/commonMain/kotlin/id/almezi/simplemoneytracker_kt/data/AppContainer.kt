package id.almezi.simplemoneytracker_kt.data

import id.almezi.simplemoneytracker_kt.data.backup.BackupPreferences
import id.almezi.simplemoneytracker_kt.data.backup.BackupSerializer

/**
 * Manual DI root, shared by every platform.
 *
 * Adapted for KMP: no `Context`. The platform-specific `createAppContainer()`
 * functions build the database via a platform-specific path and hand it in.
 *
 * `BackupRepository` is deliberately absent. It still depends on Android
 * `Context`, `Uri`, and `java.io`/`java.text`, so its multiplatform port is
 * Stage 5 work. Android wires it up separately in `SimpleMoneyTrackerApp`.
 */
class AppContainer(
    database: AppDatabase,
    val backupPreferences: BackupPreferences,
    val clock: Clock = RealClock
) {
    val transactionDao: TransactionDao = database.transactionDao()
    val categoryDao: CategoryDao = database.categoryDao()
    val accountDao: AccountDao = database.accountDao()
    val budgetDao: BudgetDao = database.budgetDao()

    val transactionRepository: TransactionRepository = TransactionRepository(
        database,
        transactionDao,
        categoryDao,
        accountDao,
        budgetDao
    )

    val categoryRepository: CategoryRepository = CategoryRepository(
        database,
        categoryDao,
        transactionDao
    )

    val budgetRepository: BudgetRepository = BudgetRepository(
        budgetDao,
        clock
    )

    val backupSerializer: BackupSerializer = BackupSerializer(
        transactionRepository,
        categoryRepository,
        accountDao,
        budgetRepository,
    )
}