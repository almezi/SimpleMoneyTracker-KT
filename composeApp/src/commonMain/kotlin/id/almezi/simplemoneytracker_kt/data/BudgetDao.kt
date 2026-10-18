package id.almezi.simplemoneytracker_kt.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun getAll(): Flow<List<Budget>>

    @Query("SELECT * FROM budgets")
    suspend fun getAllOnce(): List<Budget>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId LIMIT 1")
    suspend fun getByCategory(categoryId: String): Budget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: Budget)

    // Import path: a budget already on the device is never overwritten, the same
    // IGNORE strategy CategoryDao.insert and AccountDao.insert use.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(vararg budgets: Budget)

    @Update
    suspend fun update(budget: Budget)

    @Query("DELETE FROM budgets WHERE categoryId = :categoryId")
    suspend fun delete(categoryId: String)
}