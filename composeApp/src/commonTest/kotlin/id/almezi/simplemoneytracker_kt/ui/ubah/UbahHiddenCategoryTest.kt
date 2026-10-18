package id.almezi.simplemoneytracker_kt.ui.ubah

import id.almezi.simplemoneytracker_kt.data.Category
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals

// A hidden category keeps its history by design (`CATEGORIES.md:24`), so editing
// one of its transactions must not silently drop the category and force a
// re-categorise just to change the note.
class UbahHiddenCategoryTest {

    private val savings = Category("exp_finance_savings", "exp_finance_savings_name", "grp_finance", false, "pengeluaran", isHidden = true)
    private val food = Category("exp_daily_food", "exp_daily_food_name", "grp_daily", true, "pengeluaran")

    private fun state(
        selectedCategoryId: String?,
        originalCategoryId: String,
        originalType: String = "pengeluaran",
    ) = UbahUiState(
        loading = false,
        original = id.almezi.simplemoneytracker_kt.data.Transaction(
            id = 1,
            type = originalType,
            amount = 100_000,
            categoryId = originalCategoryId,
            dateEpochMillis = 0L,
            note = null,
            createdAt = 0L,
        ),
        type = TransactionType.Expense,
        selectedCategoryId = selectedCategoryId,
    )

    @Test
    fun `a hidden category of the same type is kept`() {
        val next = state(savings.id, savings.id).withCategories(
            categories = listOf(food),
            type = TransactionType.Expense,
        )

        assertEquals(savings.id, next.selectedCategoryId)
    }

    @Test
    fun `a visible category of the same type is kept`() {
        val next = state(food.id, food.id).withCategories(
            categories = listOf(food),
            type = TransactionType.Expense,
        )

        assertEquals(food.id, next.selectedCategoryId)
    }

    @Test
    fun `switching type clears even a hidden category of the old type`() {
        val next = state(savings.id, savings.id).withCategories(
            categories = emptyList(),
            type = TransactionType.Income,
        )

        assertEquals(null, next.selectedCategoryId)
    }
}