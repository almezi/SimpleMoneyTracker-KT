package id.almezi.simplemoneytracker_kt.ui.kategori

import id.almezi.simplemoneytracker_kt.data.Category
import kotlin.test.Test
import kotlin.test.assertEquals

// The manage screen counted transactions one query per row, about 36 per reload.
// It now takes one grouped count and assembles the rows from that.
class CategoryRowsTest {

    private fun category(id: String) = Category(id, "", null, true, "pengeluaran")

    @Test
    fun `every category gets a row`() {
        val rows = categoryRows(
            listOf(category("exp_a"), category("exp_b")),
            mapOf("exp_a" to 3),
        )

        assertEquals(listOf("exp_a", "exp_b"), rows.map { it.category.id })
        assertEquals(listOf(3, 0), rows.map { it.usageCount })
    }

    @Test
    fun `counts come from the grouped query`() {
        val rows = categoryRows(listOf(category("exp_a")), mapOf("exp_a" to 12))

        assertEquals(12, rows.first().usageCount)
    }
}