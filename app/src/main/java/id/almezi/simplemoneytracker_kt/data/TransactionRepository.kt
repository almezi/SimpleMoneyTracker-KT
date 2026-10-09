package id.almezi.simplemoneytracker_kt.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val database: AppDatabase,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao
) {
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAll()

    suspend fun getAllTransactionsOnce(): List<Transaction> = transactionDao.getAllOnce()

    suspend fun getTransaction(id: Long): Transaction? = transactionDao.getById(id)

    suspend fun countTransactions(): Int = transactionDao.count()

    suspend fun insertTransaction(tx: Transaction): Long = transactionDao.insert(tx)

    suspend fun restoreTransaction(tx: Transaction): Long = transactionDao.insert(tx)

    suspend fun updateTransaction(tx: Transaction) = transactionDao.update(tx)

    suspend fun deleteTransaction(id: Long) = transactionDao.delete(id)

    suspend fun getCategory(id: String): Category? = categoryDao.getById(id)

    fun getCategoriesByType(type: String): Flow<List<Category>> = categoryDao.getByType(type)

    fun getVisibleCategoriesByType(type: String): Flow<List<Category>> =
        categoryDao.getVisibleByType(type)

    suspend fun insertCategories(vararg categories: Category) = categoryDao.insert(*categories)

    fun getAccounts(): Flow<List<Account>> = accountDao.getAll()

    suspend fun getAccount(id: String): Account? = accountDao.getById(id)

    suspend fun insertAccounts(vararg accounts: Account) = accountDao.insert(*accounts)

    suspend fun insertTransactions(transactions: List<Transaction>) =
        database.withTransaction { transactionDao.insertAll(transactions) }

    suspend fun transactionsInMonth(startMillis: Long, endMillis: Long): List<Transaction> =
        database.withTransaction { transactionDao.getAllOnce() }
            .filter { it.dateEpochMillis in startMillis until endMillis }

    suspend fun totalsByCategory(
        type: String,
        startMillis: Long,
        endMillis: Long
    ): List<CategoryTotal> = transactionDao.totalsByCategory(type, startMillis, endMillis)

    suspend fun netBetween(startMillis: Long, endMillis: Long): Long =
        transactionDao.sumNetBetween(startMillis, endMillis)

    suspend fun incomeBetween(startMillis: Long, endMillis: Long): Long =
        transactionDao.sumIncomeBetween(startMillis, endMillis)

    suspend fun expenseBetween(startMillis: Long, endMillis: Long): Long =
        transactionDao.sumExpenseBetween(startMillis, endMillis)

    suspend fun spentForCategory(categoryId: String, startMillis: Long, endMillis: Long): Long =
        transactionDao.sumAmountForCategoryBetween(categoryId, startMillis, endMillis)

    suspend fun usageCountForCategory(categoryId: String): Int =
        transactionDao.countForCategory(categoryId)
}