package id.almezi.simplemoneytracker_kt.data

import androidx.room3.Dao
import androidx.room3.Insert
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

    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
            "WHERE categoryId = :categoryId AND type = 'pengeluaran' " +
            "AND dateEpochMillis >= :startMillis AND dateEpochMillis < :endMillis"
    )
    suspend fun sumAmountForCategoryBetween(
        categoryId: String,
        startMillis: Long,
        endMillis: Long
    ): Long

    @Query("UPDATE transactions SET categoryId = :toCategoryId WHERE categoryId = :fromCategoryId")
    suspend fun moveCategory(fromCategoryId: String, toCategoryId: String)

    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Insert
    suspend fun insertAll(transactions: List<Transaction>)

    @Update
    suspend fun update(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long)
}