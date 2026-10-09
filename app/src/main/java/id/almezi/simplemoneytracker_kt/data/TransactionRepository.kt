package id.almezi.simplemoneytracker_kt.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao
) {
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAll()

    suspend fun getAllTransactionsOnce(): List<Transaction> = transactionDao.getAllOnce()

    suspend fun getTransaction(id: Long): Transaction? = transactionDao.getById(id)

    suspend fun insertTransaction(tx: Transaction): Long = transactionDao.insert(tx)

    suspend fun restoreTransaction(tx: Transaction): Long = transactionDao.insert(tx)

    suspend fun updateTransaction(tx: Transaction) = transactionDao.update(tx)

    suspend fun deleteTransaction(id: Long) = transactionDao.delete(id)

    suspend fun getCategory(id: String): Category? = categoryDao.getById(id)

    fun getCategoriesByType(type: String): Flow<List<Category>> = categoryDao.getByType(type)

    suspend fun insertCategories(vararg categories: Category) = categoryDao.insert(*categories)

    fun getAccounts(): Flow<List<Account>> = accountDao.getAll()

    suspend fun getAccount(id: String): Account? = accountDao.getById(id)

    suspend fun insertAccounts(vararg accounts: Account) = accountDao.insert(*accounts)
}
