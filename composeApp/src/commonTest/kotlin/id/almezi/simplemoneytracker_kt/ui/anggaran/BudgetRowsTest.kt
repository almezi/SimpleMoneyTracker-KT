package id.almezi.simplemoneytracker_kt.ui.anggaran

import id.almezi.simplemoneytracker_kt.data.Budget
import id.almezi.simplemoneytracker_kt.data.Category
import id.almezi.simplemoneytracker_kt.ui.designsystem.BudgetStatus
import kotlin.test.Test
import kotlin.test.assertEquals

// The screen used to load the category and the month's spend with two queries per
// budget row. Both are now one query each, keyed by category id, and this covers
// the assembly that replaced the per-row loop.
class BudgetRowsTest {

    private fun category(id: String) = Category(id, "", null, true, "pengeluaran")

    @Test
    fun `a budget takes its category and its spend from the maps`() {
        val rows = budgetRows(
            budgets = listOf(Budget("exp_daily_food", 500_000, true, 1L, 1L)),
            categoriesById = mapOf("exp_daily_food" to category("exp_daily_food")),
            spentByCategory = mapOf("exp_daily_food" to 200_000L),
        )

        assertEquals(1, rows.size)
        assertEquals(200_000L, rows.first().spent)
        assertEquals(BudgetStatus.Safe, rows.first().status)
    }

    @Test
    fun `a budget whose category is gone is dropped`() {
        val rows = budgetRows(
            budgets = listOf(Budget("exp_gone", 500_000, true, 1L, 1L)),
            categoriesById = mapOf("exp_daily_food" to category("exp_daily_food")),
            spentByCategory = emptyMap(),
        )

        assertEquals(emptyList(), rows)
    }

    @Test
    fun `a category with no spend counts as zero`() {
        val rows = budgetRows(
            budgets = listOf(Budget("exp_daily_food", 500_000, true, 1L, 1L)),
            categoriesById = mapOf("exp_daily_food" to category("exp_daily_food")),
            spentByCategory = emptyMap(),
        )

        assertEquals(0L, rows.first().spent)
    }

    @Test
    fun `rows are ordered by how much of the budget is used`() {
        val rows = budgetRows(
            budgets = listOf(
                Budget("exp_a", 1_000_000, true, 1L, 1L),
                Budget("exp_b", 1_000_000, true, 1L, 1L),
                Budget("exp_c", 1_000_000, true, 1L, 1L),
            ),
            categoriesById = mapOf(
                "exp_a" to category("exp_a"),
                "exp_b" to category("exp_b"),
                "exp_c" to category("exp_c"),
            ),
            spentByCategory = mapOf("exp_a" to 100_000L, "exp_b" to 1_200_000L, "exp_c" to 0L),
        )

        assertEquals(listOf("exp_b", "exp_a", "exp_c"), rows.map { it.category.id })
        assertEquals(BudgetStatus.OverLimit, rows.first().status)
    }
}