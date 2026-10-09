package id.almezi.simplemoneytracker_kt.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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

    @Query(
        "SELECT COALESCE(SUM(CASE WHEN type = 'pemasukan' THEN amount ELSE -amount END), 0) " +
            "FROM transactions WHERE dateEpochMillis >= :startMillis AND dateEpochMillis < :endMillis"
    )
    suspend fun sumNetBetween(startMillis: Long, endMillis: Long): Long

    @Query(
        "SELECT COALESCE(SUM(CASE WHEN type = 'pemasukan' THEN amount ELSE 0 END), 0) " +
            "FROM transactions WHERE dateEpochMillis >= :startMillis AND dateEpochMillis < :endMillis"
    )
    suspend fun sumIncomeBetween(startMillis: Long, endMillis: Long): Long

    @Query(
        "SELECT COALESCE(SUM(CASE WHEN type = 'pengeluaran' THEN amount ELSE 0 END), 0) " +
            "FROM transactions WHERE dateEpochMillis >= :startMillis AND dateEpochMillis < :endMillis"
    )
    suspend fun sumExpenseBetween(startMillis: Long, endMillis: Long): Long

    @Query(
        "SELECT categoryId, COALESCE(SUM(amount), 0) AS total FROM transactions " +
            "WHERE type = :type AND dateEpochMillis >= :startMillis AND dateEpochMillis < :endMillis " +
            "GROUP BY categoryId"
    )
    suspend fun totalsByCategory(
        type: String,
        startMillis: Long,
        endMillis: Long
    ): List<CategoryTotal>

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

data class CategoryTotal(
    val categoryId: String,
    val total: Long
)