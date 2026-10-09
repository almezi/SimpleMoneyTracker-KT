package id.almezi.simplemoneytracker_kt.data

import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val database: AppDatabase,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {
    fun getAll(): Flow<List<Category>> = categoryDao.getAll()

    fun getByType(type: String): Flow<List<Category>> = categoryDao.getByType(type)

    suspend fun getByTypeOnce(type: String): List<Category> = categoryDao.getByTypeOnce(type)

    suspend fun get(id: String): Category? = categoryDao.getById(id)

    suspend fun usageCount(categoryId: String): Int = transactionDao.countForCategory(categoryId)

    suspend fun setHidden(id: String, hidden: Boolean) = categoryDao.setHidden(id, hidden)

    suspend fun rename(id: String, name: String) = categoryDao.rename(id, name.trim())

    suspend fun create(
        id: String,
        type: String,
        name: String,
        colorHex: String,
        createdAt: Long
    ): Category {
        val category = Category(
            id = id,
            nameRes = "",
            groupId = null,
            countsInTotals = true,
            type = type,
            colorHex = colorHex,
            isDefault = false,
            isHidden = false,
            customName = name.trim()
        )
        categoryDao.insert(category)
        return category
    }

    suspend fun countWithName(type: String, name: String, excludeId: String): Int =
        categoryDao.countWithName(type, name.trim(), excludeId)

    suspend fun deleteCustomCategoryAtomically(categoryId: String, fallbackCategoryId: String) =
        database.deleteCustomCategoryAtomically(categoryId, fallbackCategoryId)

    suspend fun assignColorIfMissing(id: String, hex: String) =
        categoryDao.assignColorIfMissing(id, hex)
}