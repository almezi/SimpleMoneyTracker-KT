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

    suspend fun restoreTransaction(tx: Transaction): Long = transactionDao.insert(tx)

    suspend fun updateTransaction(tx: Transaction) = transactionDao.update(tx)

    suspend fun deleteTransaction(id: Long) = transactionDao.delete(id)

    fun getVisibleCategoriesByType(type: String): Flow<List<Category>> =
        categoryDao.getVisibleByType(type)

    fun getAccounts(): Flow<List<Account>> = accountDao.getAll()

    // KMP: `withTransaction { }` is Android-only. The equivalent is
    // useWriterConnection { it.immediateTransaction { } }.
    suspend fun insertTransactions(transactions: List<Transaction>) {
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction { transactionDao.insertAll(transactions) }
        }
    }

    /**
     * Writes one import: custom categories, accounts and budgets first, then the
     * transactions that reference them, all in a single transaction.
     *
     * `UI_SPEC.md:119` says importing never deletes anything, so every insert is
     * an IGNORE: rows already on the device win, and a category or budget that
     * exists only in the file is added. Order matters, since a transaction whose
     * category is missing would otherwise land in a list that cannot name it.
     */
    suspend fun restoreImport(
        categories: List<Category>,
        accounts: List<Account>,
        budgets: List<Budget>,
        transactions: List<Transaction>
    ) {
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                if (categories.isNotEmpty()) categoryDao.insert(*categories.toTypedArray())
                if (accounts.isNotEmpty()) accountDao.insert(*accounts.toTypedArray())
                if (budgets.isNotEmpty()) budgetDao.insertIfAbsent(*budgets.toTypedArray())
                if (transactions.isNotEmpty()) transactionDao.insertAll(transactions)
            }
        }
    }

    suspend fun spentForCategory(categoryId: String, startMillis: Long, endMillis: Long): Long =
        transactionDao.sumAmountForCategoryBetween(categoryId, startMillis, endMillis)
}