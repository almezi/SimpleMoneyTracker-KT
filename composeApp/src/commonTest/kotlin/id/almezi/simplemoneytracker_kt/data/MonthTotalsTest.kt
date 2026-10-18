package id.almezi.simplemoneytracker_kt.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Guards the product rule that categories flagged `countsInTotals = false`
 * (Transfer Masuk, Investasi as expense, Tabungan) are excluded from every
 * total — `UI_SPEC.md:16`, `CATEGORIES.md:12`.
 *
 * These are pure functions on purpose: the rule used to live in SQL that
 * nothing in this source set could execute, so a month holding nothing but a
 * transfer reported a full salary-sized balance on both screens.
 */
class MonthTotalsTest {

    private val octoberStart = 1_000L
    private val novemberStart = 2_000L

    private fun tx(
        type: String,
        amount: Long,
        categoryId: String,
        date: Long = octoberStart,
    ) = Transaction(
        id = 0,
        type = type,
        amount = amount,
        categoryId = categoryId,
        dateEpochMillis = date,
        note = null,
        createdAt = date,
    )

    @Test
    fun `excluded income category is not counted in the month totals`() {
        val totals = monthTotals(
            transactions = listOf(tx("pemasukan", 1_000_000, "inc_transfer_in")),
            startMillis = octoberStart,
            endMillis = novemberStart,
            categories = CategorySeed.all,
        )

        assertEquals(0L, totals.income)
        assertEquals(0L, totals.balance)
        assertTrue(totals.incomeByCategory.isEmpty(), "excluded category appeared in the breakdown")
    }

    @Test
    fun `excluded expense categories are not counted in the month totals`() {
        val totals = monthTotals(
            transactions = listOf(
                tx("pengeluaran", 500_000, "exp_finance_investment"),
                tx("pengeluaran", 250_000, "exp_finance_savings"),
                tx("pengeluaran", 40_000, "exp_daily_food"),
            ),
            startMillis = octoberStart,
            endMillis = novemberStart,
            categories = CategorySeed.all,
        )

        assertEquals(40_000L, totals.expense)
        assertEquals(listOf(CategoryTotal("exp_daily_food", 40_000L)), totals.expenseByCategory)
    }

    @Test
    fun `counted rows keep their totals and their share of the month`() {
        val totals = monthTotals(
            transactions = listOf(
                tx("pemasukan", 5_000_000, "inc_salary"),
                tx("pemasukan", 1_000_000, "inc_transfer_in"),
                tx("pengeluaran", 1_000_000, "exp_daily_food"),
                tx("pengeluaran", 500_000, "exp_finance_savings"),
            ),
            startMillis = octoberStart,
            endMillis = novemberStart,
            categories = CategorySeed.all,
        )

        assertEquals(5_000_000L, totals.income)
        assertEquals(1_000_000L, totals.expense)
        assertEquals(4_000_000L, totals.balance)
        assertEquals(listOf(CategoryTotal("inc_salary", 5_000_000L)), totals.incomeByCategory)
    }

    @Test
    fun `rows outside the month are never counted`() {
        val totals = monthTotals(
            transactions = listOf(
                tx("pemasukan", 5_000_000, "inc_salary", date = novemberStart),
                tx("pemasukan", 7_000_000, "inc_salary", date = octoberStart - 1),
            ),
            startMillis = octoberStart,
            endMillis = novemberStart,
            categories = CategorySeed.all,
        )

        assertEquals(0L, totals.income)
    }

    @Test
    fun `countedTransactions drops only the excluded categories`() {
        val counted = countedTransactions(
            transactions = listOf(
                tx("pemasukan", 5_000_000, "inc_salary"),
                tx("pemasukan", 1_000_000, "inc_transfer_in"),
                tx("pengeluaran", 500_000, "exp_finance_savings"),
            ),
            startMillis = octoberStart,
            endMillis = novemberStart,
            categories = CategorySeed.all,
        )

        assertEquals(listOf("inc_salary"), counted.map { it.categoryId })
    }

    @Test
    fun `a custom category always counts`() {
        val custom = Category(
            id = "exp_custom_1",
            nameRes = "",
            groupId = null,
            countsInTotals = true,
            type = "pengeluaran",
        )

        val totals = monthTotals(
            transactions = listOf(tx("pengeluaran", 15_000, "exp_custom_1")),
            startMillis = octoberStart,
            endMillis = novemberStart,
            categories = CategorySeed.all + custom,
        )

        assertEquals(15_000L, totals.expense)
    }
}