package id.almezi.simplemoneytracker_kt.data

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Room KMP database, schema version 3.
 *
 * Adapted for Kotlin Multiplatform:
 *  - `@ConstructedBy` + the `expect object` constructor below. Room's compiler
 *    generates the `actual`; the `@Suppress` silences the IDE warning about a
 *    missing actual.
 *  - Migrations take a `SQLiteConnection` instead of the Android-only
 *    `SupportSQLiteDatabase`. The SQL itself is unchanged.
 */
@Database(
    entities = [Transaction::class, Category::class, Account::class, Budget::class],
    version = 3,
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `accounts` (" +
                        "`id` TEXT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`sortOrder` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
                connection.execSQL(
                    "ALTER TABLE `transactions` ADD COLUMN `accountId` TEXT NOT NULL DEFAULT '" +
                        AccountSeed.DEFAULT_ACCOUNT_ID + "'"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE `categories` ADD COLUMN `colorHex` TEXT")
                connection.execSQL("ALTER TABLE `categories` ADD COLUMN `isDefault` INTEGER NOT NULL DEFAULT 1")
                connection.execSQL("ALTER TABLE `categories` ADD COLUMN `isHidden` INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE `categories` ADD COLUMN `customName` TEXT")
                connection.execSQL(
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

// Room's compiler generates the `actual` implementation for every target.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/**
 * Deletes a custom category and moves its transactions to the fallback, all in
 * one transaction.
 *
 * Adapted for KMP: `withTransaction { }` is Android-only. The KMP equivalent is
 * `useWriterConnection { it.immediateTransaction { } }`. `immediateTransaction`
 * is the recommended choice under WAL, which is the default.
 */
suspend fun AppDatabase.deleteCustomCategoryAtomically(
    categoryId: String,
    fallbackCategoryId: String
) {
    useWriterConnection { transactor ->
        transactor.immediateTransaction {
            transactionDao().moveCategory(categoryId, fallbackCategoryId)
            budgetDao().delete(categoryId)
            categoryDao().delete(categoryId)
        }
    }
}