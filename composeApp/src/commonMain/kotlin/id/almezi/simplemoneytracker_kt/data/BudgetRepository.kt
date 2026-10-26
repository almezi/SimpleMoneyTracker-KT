package id.almezi.simplemoneytracker_kt.data

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import kotlinx.coroutines.flow.Flow

class BudgetRepository(
    private val database: AppDatabase,
    private val budgetDao: BudgetDao,
    private val clock: Clock
) {
    fun getAllBudgets(): Flow<List<Budget>> = budgetDao.getAll()

    suspend fun getBudget(categoryId: String): Budget? = budgetDao.getByCategory(categoryId)

    suspend fun getAllBudgetsOnce(): List<Budget> = budgetDao.getAllOnce()

    /**
     * Upserts one budget. The repository owns the timestamps completely —
     * `createdAt` survives an update, `updatedAt` is always now — so callers
     * pass only user data and cannot get the two stories wrong.
     *
     * The read-then-write runs in one transaction: two writers racing used to
     * be able to overwrite a just-preserved `createdAt`.
     */
    suspend fun saveBudget(categoryId: String, limitAmount: Long, alertAt80: Boolean) {
        val now = clock.currentTimeMillis()
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                val existing = budgetDao.getByCategory(categoryId)
                budgetDao.upsert(
                    if (existing == null) {
                        Budget(
                            categoryId = categoryId,
                            limitAmount = limitAmount,
                            alertAt80 = alertAt80,
                            createdAt = now,
                            updatedAt = now
                        )
                    } else {
                        existing.copy(limitAmount = limitAmount, alertAt80 = alertAt80, updatedAt = now)
                    }
                )
            }
        }
    }

    suspend fun deleteBudget(categoryId: String) = budgetDao.delete(categoryId)

}
