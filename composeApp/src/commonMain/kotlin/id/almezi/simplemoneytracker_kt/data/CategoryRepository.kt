package id.almezi.simplemoneytracker_kt.data

import kotlinx.coroutines.flow.Flow

/** Outcome of [CategoryRepository.rename]. */
enum class RenameResult { Renamed, EmptyName, DuplicateName, Missing }

/** How many transactions reference one category. */
data class CategoryCount(val categoryId: String, val count: Int)

class CategoryRepository(
    private val database: AppDatabase,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {
    fun getAll(): Flow<List<Category>> = categoryDao.getAll()

    suspend fun getAllOnce(): List<Category> = categoryDao.getAllOnce()

    suspend fun getByTypeOnce(type: String): List<Category> = categoryDao.getByTypeOnce(type)

    /** Same rows as [getByTypeOnce], observed — hidden rows included. */
    fun getByType(type: String): Flow<List<Category>> = categoryDao.getByType(type)

    suspend fun get(id: String): Category? = categoryDao.getById(id)

    suspend fun usageCount(categoryId: String): Int = transactionDao.countForCategory(categoryId)

    /** Every category's usage count in one query. Categories with none are absent. */
    suspend fun usageCounts(): Map<String, Int> =
        transactionDao.countsByCategory().associate { it.categoryId to it.count }

    suspend fun setHidden(id: String, hidden: Boolean) = categoryDao.setHidden(id, hidden)

    /**
     * Renames a category, or refuses and reports why.
     *
     * Refusing an empty name and a name already used in the same type is what
     * keeps `categoryDisplayName` able to name every category and keeps the
     * "referenced by ID" assumption readable.
     */
    suspend fun rename(id: String, name: String): RenameResult {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return RenameResult.EmptyName
        val category = categoryDao.getById(id) ?: return RenameResult.Missing
        if (countWithName(category.type, trimmed, id) > 0) return RenameResult.DuplicateName
        categoryDao.rename(id, trimmed)
        return RenameResult.Renamed
    }

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