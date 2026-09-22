package id.almezi.simplemoneytracker_kt.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAll()

    suspend fun getTransaction(id: Long): Transaction? = transactionDao.getById(id)

    suspend fun insertTransaction(tx: Transaction): Long = transactionDao.insert(tx)

    suspend fun updateTransaction(tx: Transaction) = transactionDao.update(tx)

    suspend fun deleteTransaction(id: Long) = transactionDao.delete(id)

    suspend fun getCategory(id: String): Category? = categoryDao.getById(id)

    fun getCategoriesByType(type: String): Flow<List<Category>> = categoryDao.getByType(type)

    suspend fun insertCategories(vararg categories: Category) = categoryDao.insert(*categories)
}
