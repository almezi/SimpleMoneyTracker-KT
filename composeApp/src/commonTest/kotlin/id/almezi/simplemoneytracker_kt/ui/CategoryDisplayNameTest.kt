package id.almezi.simplemoneytracker_kt.ui

import kotlin.test.Test
import kotlin.test.assertEquals

// A renamed or user-created category has to be named by its own name wherever
// the money is shown: the Transaksi row, both delete sheets and the Ringkasan
// breakdown all used getCategoryName(id), which only knows the seeded ids, so a
// custom "Kopi" rendered as the literal word "Kategori".
class CategoryDisplayNameTest {

    private val names = mapOf(
        "exp_custom_1" to "Kopi",
        "inc_salary" to "Gajian"
    )

    @Test
    fun `a custom category shows its own name`() {
        assertEquals("Kopi", categoryDisplayName(names, "exp_custom_1"))
    }

    @Test
    fun `a renamed default shows the new name`() {
        assertEquals("Gajian", categoryDisplayName(names, "inc_salary"))
    }

    @Test
    fun `an unrenamed default still falls back to its seeded name`() {
        assertEquals(getCategoryName("exp_daily_food"), categoryDisplayName(emptyMap(), "exp_daily_food"))
    }

    @Test
    fun `an unknown id still falls back to the generic label`() {
        assertEquals(
            id.almezi.simplemoneytracker_kt.SakuStrings.tambah_category_label,
            categoryDisplayName(emptyMap(), "exp_gone"),
        )
    }

    @Test
    fun `displayNames maps every category id to its display name`() {
        val categories = listOf(
            id.almezi.simplemoneytracker_kt.data.Category("exp_custom_1", "", null, true, "pengeluaran", customName = "Kopi"),
            id.almezi.simplemoneytracker_kt.data.Category("inc_salary", "", null, true, "pemasukan", customName = "Gajian"),
        )

        assertEquals(mapOf("exp_custom_1" to "Kopi", "inc_salary" to "Gajian"), displayNames(categories))
    }
}