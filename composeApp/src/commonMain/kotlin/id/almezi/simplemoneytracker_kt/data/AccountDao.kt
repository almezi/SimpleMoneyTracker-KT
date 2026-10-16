package id.almezi.simplemoneytracker_kt.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY sortOrder ASC")
    fun getAll(): Flow<List<Account>>

    @Query("SELECT * FROM accounts ORDER BY sortOrder ASC")
    suspend fun getAllOnce(): List<Account>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: String): Account?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vararg accounts: Account)
}