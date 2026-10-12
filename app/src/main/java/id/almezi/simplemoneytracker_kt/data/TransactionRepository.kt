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

    fun getVisibleCategoriesByType(type: String): Flow<List<Category>> =
        categoryDao.getVisibleByType(type)

    fun getAccounts(): Flow<List<Account>> = accountDao.getAll()

    suspend fun insertTransactions(transactions: List<Transaction>) =
        database.withTransaction { transactionDao.insertAll(transactions) }

    suspend fun totalsByCategory(
        type: String,
        startMillis: Long,
        endMillis: Long
    ): List<CategoryTotal> = transactionDao.totalsByCategory(type, startMillis, endMillis)

    suspend fun incomeBetween(startMillis: Long, endMillis: Long): Long =
        transactionDao.sumIncomeBetween(startMillis, endMillis)

    suspend fun expenseBetween(startMillis: Long, endMillis: Long): Long =
        transactionDao.sumExpenseBetween(startMillis, endMillis)

    suspend fun spentForCategory(categoryId: String, startMillis: Long, endMillis: Long): Long =
        transactionDao.sumAmountForCategoryBetween(categoryId, startMillis, endMillis)
}