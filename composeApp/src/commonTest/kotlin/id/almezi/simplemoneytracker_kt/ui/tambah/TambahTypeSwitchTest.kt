package id.almezi.simplemoneytracker_kt.ui.tambah

import id.almezi.simplemoneytracker_kt.data.AccountSeed
import id.almezi.simplemoneytracker_kt.data.Category
import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Guards `UI_SPEC.md:14`: switching the type toggle keeps the amount and the
 * note, and clears only a category that does not belong to the new type.
 *
 * The rule lives in [switchedTo] rather than inline in `updateType` so this
 * test drives the production transition. The old test in `iosTest` reimplemented
 * it against a local state holder, so it passed while the real screen wiped the
 * amount.
 */
class TambahTypeSwitchTest {

    private val salary = Category("inc_salary", "inc_salary_name", null, true, "pemasukan")
    private val food = Category("exp_daily_food", "exp_daily_food_name", "grp_daily", true, "pengeluaran")

    private fun state(
        type: TransactionType = TransactionType.Income,
        amountDigits: String = "5000",
        categoryId: String? = "inc_salary",
        note: String = "kopi",
    ) = TambahUiState(
        type = type,
        amountDigits = amountDigits,
        selectedCategoryId = categoryId,
        note = note,
    )

    @Test
    fun `switching type keeps the amount`() {
        val next = state().switchedTo(TransactionType.Expense, listOf(salary, food))
        assertEquals("5000", next.amountDigits)
        assertEquals(5_000L, next.amount)
    }

    @Test
    fun `switching type keeps the note`() {
        val next = state().switchedTo(TransactionType.Expense, listOf(salary, food))
        assertEquals("kopi", next.note)
    }

    @Test
    fun `switching type clears a category that does not belong to the new type`() {
        val next = state().switchedTo(TransactionType.Expense, listOf(salary, food))
        assertNull(next.selectedCategoryId)
    }

    @Test
    fun `switching type keeps a category that belongs to the new type`() {
        val next = state(TransactionType.Income, "5000", "inc_salary").switchedTo(
            TransactionType.Expense,
            listOf(Category("inc_salary", "inc_salary_name", null, true, "pengeluaran")),
        )
        assertEquals("inc_salary", next.selectedCategoryId)
    }

    @Test
    fun `switching type changes the type and the account stays selected`() {
        val next = state().switchedTo(TransactionType.Expense, listOf(salary, food))
        assertEquals(TransactionType.Expense, next.type)
        assertEquals(AccountSeed.DEFAULT_ACCOUNT_ID, next.selectedAccountId)
    }
}