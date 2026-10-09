package id.almezi.simplemoneytracker_kt.data

import kotlinx.coroutines.flow.Flow

class BudgetRepository(
    private val database: AppDatabase,
    private val budgetDao: BudgetDao,
    private val clock: Clock
) {
    fun getAllBudgets(): Flow<List<Budget>> = budgetDao.getAll()

    suspend fun getBudget(categoryId: String): Budget? = budgetDao.getByCategory(categoryId)

    suspend fun getAllBudgetsOnce(): List<Budget> = budgetDao.getAllOnce()

    suspend fun saveBudget(budget: Budget) {
        val now = clock.currentTimeMillis()
        val existing = budgetDao.getByCategory(budget.categoryId)
        budgetDao.upsert(
            if (existing == null) {
                budget.copy(createdAt = now, updatedAt = now)
            } else {
                budget.copy(
                    createdAt = existing.createdAt,
                    updatedAt = now
                )
            }
        )
    }

    suspend fun deleteBudget(categoryId: String) = budgetDao.delete(categoryId)

}