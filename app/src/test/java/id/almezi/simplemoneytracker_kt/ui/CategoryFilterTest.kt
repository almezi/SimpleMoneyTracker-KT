package id.almezi.simplemoneytracker_kt.ui

import id.almezi.simplemoneytracker_kt.ui.components.SakuPickerOption
import id.almezi.simplemoneytracker_kt.ui.components.SakuPickerOptionGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryFilterTest {

    private val groups = listOf(
        SakuPickerOptionGroup(
            title = "Kebutuhan Sehari-hari",
            options = listOf(
                SakuPickerOption(id = "exp_daily_food", label = "Makanan & Minuman"),
                SakuPickerOption(id = "exp_daily_household", label = "Kebutuhan Rumah Tangga"),
            )
        ),
        SakuPickerOptionGroup(
            title = "Transportasi",
            options = listOf(
                SakuPickerOption(id = "exp_transport_fuel", label = "Bahan Bakar"),
                SakuPickerOption(id = "exp_transport_public", label = "Transportasi Umum"),
            )
        ),
    )

    @Test
    fun `empty query keeps every group and option`() {
        assertEquals(groups, filterCategoryGroups(groups, ""))
        assertEquals(groups, filterCategoryGroups(groups, "   "))
    }

    @Test
    fun `match is a like word search on any part of the label`() {
        assertTrue(matchesCategoryQuery("Makanan & Minuman", "makanan"))
        assertTrue(matchesCategoryQuery("Makanan & Minuman", "Minuman"))
        assertTrue(matchesCategoryQuery("Bahan Bakar", "an bak"))
        assertFalse(matchesCategoryQuery("Bahan Bakar", "gaji"))
    }

    @Test
    fun `match ignores letter case and surrounding spaces`() {
        assertTrue(matchesCategoryQuery("Transportasi Umum", "  TRANSPORT  "))
    }

    @Test
    fun `filter drops groups with no matching option`() {
        val result = filterCategoryGroups(groups, "bahan")
        assertEquals(1, result.size)
        assertEquals("Transportasi", result.first().title)
        assertEquals(listOf("exp_transport_fuel"), result.first().options.map { it.id })
    }

    @Test
    fun `filter keeps several groups matching the same word`() {
        val result = filterCategoryGroups(groups, "umum")
        assertEquals(1, result.size)
        assertEquals(listOf("exp_transport_public"), result.first().options.map { it.id })
    }

    @Test
    fun `filter returns nothing when no option matches`() {
        assertTrue(filterCategoryGroups(groups, "gaji").isEmpty())
    }
}