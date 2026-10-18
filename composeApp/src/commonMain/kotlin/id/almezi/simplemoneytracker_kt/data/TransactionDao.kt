package id.almezi.simplemoneytracker_kt.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateEpochMillis DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY dateEpochMillis DESC, createdAt DESC")
    suspend fun getAllOnce(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun countForCategory(categoryId: String): Int

    /** One query for every row's usage count, instead of one query per category. */
    @Query("SELECT categoryId, COUNT(*) AS count FROM transactions GROUP BY categoryId")
    suspend fun countsByCategory(): List<CategoryCount>

    /**
     * The month's expense spend for every category in one query.
     *
     * Budget spend deliberately ignores `countsInTotals`: a budget is set on one
     * category, so what matters is that category's own spending.
     */
    @Query(
        "SELECT categoryId, COALESCE(SUM(amount), 0) AS total FROM transactions " +
            "WHERE type = 'pengeluaran' " +
            "AND dateEpochMillis >= :startMillis AND dateEpochMillis < :endMillis " +
            "GROUP BY categoryId"
    )
    suspend fun expenseTotalsByCategory(startMillis: Long, endMillis: Long): List<CategoryTotal>

    @Query("UPDATE transactions SET categoryId = :toCategoryId WHERE categoryId = :fromCategoryId")
    suspend fun moveCategory(fromCategoryId: String, toCategoryId: String)

    @Insert
    suspend fun insert(transaction: Transaction): Long

    /**
     * Undo path. IGNORE, not ABORT: SQLite hands the deleted row's id straight
     * to the next insert, so undoing a delete after adding a new transaction
     * would raise a constraint violation inside `viewModelScope` and crash.
     * Returns -1 when the row is gone for good, because its id was taken.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(transaction: Transaction): Long

    @Insert
    suspend fun insertAll(transactions: List<Transaction>)

    @Update
    suspend fun update(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long)
}