package id.almezi.simplemoneytracker_kt.data

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection

import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val database: AppDatabase,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
    private val budgetDao: BudgetDao
) {
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAll()

    suspend fun getAllTransactionsOnce(): List<Transaction> = transactionDao.getAllOnce()

    suspend fun getTransaction(id: Long): Transaction? = transactionDao.getById(id)

    suspend fun countTransactions(): Int = transactionDao.count()

    suspend fun insertTransaction(tx: Transaction): Long = transactionDao.insert(tx)

    /** @return false when the id was taken while the row was deleted; nothing was restored. */
    suspend fun restoreTransaction(tx: Transaction): Boolean =
        transactionDao.insertIfAbsent(tx) != -1L

    suspend fun updateTransaction(tx: Transaction) = transactionDao.update(tx)

    suspend fun deleteTransaction(id: Long) = transactionDao.delete(id)

    fun getVisibleCategoriesByType(type: String): Flow<List<Category>> =
        categoryDao.getVisibleByType(type)

    fun getAccounts(): Flow<List<Account>> = accountDao.getAll()

    // One import: categories, accounts and budgets first, then the transactions
    // that reference them. All IGNORE (`UI_SPEC.md:119`, import never deletes or
    // overwrites); order matters, since a transaction whose category is missing
    // would land in a list that cannot name it.
    suspend fun restoreImport(
        categories: List<Category>,
        accounts: List<Account>,
        budgets: List<Budget>,
        transactions: List<Transaction>
    ) {
        // KMP: `withTransaction { }` is Android-only. The equivalent is
        // useWriterConnection { it.immediateTransaction { } }.
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                categoryDao.insert(*categories.toTypedArray())
                accountDao.insert(*accounts.toTypedArray())
                budgetDao.insertIfAbsent(*budgets.toTypedArray())
                transactionDao.insertAll(transactions)
            }
        }
    }

    suspend fun spentForCategory(categoryId: String, startMillis: Long, endMillis: Long): Long =
        transactionDao.sumAmountForCategoryBetween(categoryId, startMillis, endMillis)
}