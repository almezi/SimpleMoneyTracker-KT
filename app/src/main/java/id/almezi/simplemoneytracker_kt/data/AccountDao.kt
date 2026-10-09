package id.almezi.simplemoneytracker_kt.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY sortOrder ASC")
    fun getAll(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: String): Account?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vararg accounts: Account)
}