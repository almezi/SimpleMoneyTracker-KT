package id.almezi.simplemoneytracker_kt.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Transaction::class, Category::class, Account::class, Budget::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `accounts` (" +
                        "`id` TEXT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`sortOrder` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "ALTER TABLE `transactions` ADD COLUMN `accountId` TEXT NOT NULL DEFAULT '" +
                        AccountSeed.DEFAULT_ACCOUNT_ID + "'"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `categories` ADD COLUMN `colorHex` TEXT")
                db.execSQL("ALTER TABLE `categories` ADD COLUMN `isDefault` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `categories` ADD COLUMN `isHidden` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `categories` ADD COLUMN `customName` TEXT")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `budgets` (" +
                        "`categoryId` TEXT NOT NULL, " +
                        "`limitAmount` INTEGER NOT NULL, " +
                        "`alertAt80` INTEGER NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`categoryId`))"
                )
            }
        }

        val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
    }
}

suspend fun AppDatabase.deleteCustomCategoryAtomically(
    categoryId: String,
    fallbackCategoryId: String
) {
    withTransaction {
        transactionDao().moveCategory(categoryId, fallbackCategoryId)
        budgetDao().delete(categoryId)
        categoryDao().delete(categoryId)
    }
}