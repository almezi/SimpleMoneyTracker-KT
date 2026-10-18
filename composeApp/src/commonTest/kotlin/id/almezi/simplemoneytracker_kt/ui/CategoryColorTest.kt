package id.almezi.simplemoneytracker_kt.ui

import id.almezi.simplemoneytracker_kt.SakuStrings
import id.almezi.simplemoneytracker_kt.data.Category
import id.almezi.simplemoneytracker_kt.data.CategorySeed
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuCategoryPalette
import id.almezi.simplemoneytracker_kt.ui.kategori.fallbackCategoryId
import id.almezi.simplemoneytracker_kt.ui.kategori.isValidCategoryName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// The colour a user picks is stored in Category.colorHex but nothing ever read
// it: every mark and avatar was drawn with a hash of the category id, so
// choosing a swatch did nothing and the form preview hashed the hex text itself.
class CategoryColorTest {

    @Test
    fun `a stored colour wins over the id hash`() {
        assertEquals(
            SakuCategoryPalette.parseColor("#F6C453"),
            SakuCategoryPalette.colorFor("exp_custom_1", "#F6C453"),
        )
    }

    @Test
    fun `a category without a stored colour falls back to the id hash`() {
        assertEquals(
            SakuCategoryPalette.colorFor("exp_custom_1"),
            SakuCategoryPalette.colorFor("exp_custom_1", null),
        )
    }

    @Test
    fun `a blank stored colour falls back to the id hash`() {
        assertEquals(
            SakuCategoryPalette.colorFor("exp_custom_1"),
            SakuCategoryPalette.colorFor("exp_custom_1", ""),
        )
    }

    @Test
    fun `two categories sharing an id hash are told apart by their colour`() {
        val pink = SakuCategoryPalette.colorFor("exp_same", "#F0708A")
        val blue = SakuCategoryPalette.colorFor("exp_same", "#5FD3E6")

        assertFalse(pink == blue, "the stored colour must decide, not the id hash")
    }

    @Test
    fun `hex parsing accepts both rgb and argb forms and falls back on junk`() {
        assertEquals(SakuCategoryPalette.parseColor("#F6C453"), SakuCategoryPalette.parseColor("#FFF6C453"))
        assertEquals(
            SakuCategoryPalette.parseColor(SakuCategoryPalette.defaultHex),
            SakuCategoryPalette.parseColor("not a colour"),
        )
    }

    // Item 10: deleting a custom category moves its transactions to "Lainnya".
    // The fallback is a default category, so its customName is null and only
    // categoryDisplayName gives it a name.
    @Test
    fun `the delete fallback category has a name`() {
        val fallback = CategorySeed.all.first { it.id == fallbackCategoryId("pengeluaran") }

        assertEquals(SakuStrings.cat_exp_other, categoryDisplayName(fallback))
        assertTrue(categoryDisplayName(fallback).isNotBlank())
    }

    // Item 9: names are 1-20 characters.
    @Test
    fun `a category name must not be empty`() {
        assertFalse(isValidCategoryName(""))
        assertFalse(isValidCategoryName("   "))
        assertTrue(isValidCategoryName("Kopi"))
    }
}