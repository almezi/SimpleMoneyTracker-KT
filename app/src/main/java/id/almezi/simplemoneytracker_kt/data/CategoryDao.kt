package id.almezi.simplemoneytracker_kt.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAll(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY isDefault DESC, id ASC")
    fun getByType(type: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY isDefault DESC, id ASC")
    suspend fun getByTypeOnce(type: String): List<Category>

    @Query("SELECT * FROM categories WHERE type = :type AND isHidden = 0 ORDER BY isDefault DESC, id ASC")
    fun getVisibleByType(type: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): Category?

    @Query("SELECT COUNT(*) FROM categories WHERE type = :type AND LOWER(customName) = LOWER(:name) AND id != :excludeId")
    suspend fun countWithName(type: String, name: String, excludeId: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vararg categories: Category)

    @Query("UPDATE categories SET colorHex = :hex WHERE id = :id AND colorHex IS NULL")
    suspend fun assignColorIfMissing(id: String, hex: String)

    @Query("UPDATE categories SET isHidden = :hidden WHERE id = :id")
    suspend fun setHidden(id: String, hidden: Boolean)

    @Query("UPDATE categories SET customName = :name WHERE id = :id")
    suspend fun rename(id: String, name: String?)

    @Query("UPDATE categories SET colorHex = :hex WHERE id = :id")
    suspend fun setColor(id: String, hex: String)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun delete(id: String)
}