package id.almezi.simplemoneytracker_kt.data.backup

import id.almezi.simplemoneytracker_kt.data.Budget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Guards the import side of the backup round-trip.
 *
 * The export writes transactions, custom categories, budgets and accounts, so
 * the import has to read all four back. It used to parse only
 * `parsed.transactions`: restore a file after clearing app data and the money
 * came back while the budgets and the custom category names were gone, which is
 * the exact data loss the Cadangan screen warns about.
 */
class BackupRestoreTest {

    @Test
    fun `a backup category becomes a counting custom category`() {
        val category = BackupCategory(
            id = "exp_custom_1",
            name = "Kopi",
            type = "pengeluaran",
            colorHex = "#FF8800",
            hidden = false,
        ).toCategory()

        assertEquals("exp_custom_1", category.id)
        assertEquals("Kopi", category.customName)
        assertEquals("pengeluaran", category.type)
        assertEquals("#FF8800", category.colorHex)
        assertTrue(category.countsInTotals, "imported categories must count in totals")
        assertFalse(category.isDefault, "an imported category is a user category")
        assertFalse(category.isHidden)
    }

    @Test
    fun `a hidden backup category stays hidden`() {
        assertTrue(
            BackupCategory("exp_custom_1", "Kopi", "pengeluaran", "#FF8800", hidden = true)
                .toCategory().isHidden
        )
    }

    @Test
    fun `a backup budget keeps its limit and flag`() {
        val budget = BackupBudget(
            categoryId = "exp_daily_food",
            limit = 1_500_000,
            alertAt80 = false,
            createdAt = 111L,
            updatedAt = 222L,
        ).toBudget(currentTime = 999L)

        assertEquals(
            Budget("exp_daily_food", 1_500_000, alertAt80 = false, createdAt = 111L, updatedAt = 222L),
            budget,
        )
    }

    @Test
    fun `a backup budget with no timestamps gets the import time`() {
        val budget = BackupBudget("exp_daily_food", limit = 1_500_000).toBudget(currentTime = 999L)

        assertEquals(999L, budget.createdAt)
        assertEquals(999L, budget.updatedAt)
    }

    @Test
    fun `a backup account keeps its sort order`() {
        assertEquals(
            id.almezi.simplemoneytracker_kt.data.Account("acc_bca", "BCA", 1),
            BackupAccount("acc_bca", "BCA", 1).toAccount(),
        )
    }

    @Test
    fun `a transaction with a missing account falls back to the seeded default`() {
        val tx = BackupTransaction(
            type = "pemasukan",
            amount = 1_000,
            categoryId = "inc_salary",
            accountId = "",
            dateEpochMillis = 1_000L,
        ).toTransaction(currentTime = 55L)

        assertEquals(id.almezi.simplemoneytracker_kt.data.AccountSeed.DEFAULT_ACCOUNT_ID, tx.accountId)
        assertEquals(55L, tx.createdAt)
    }
}