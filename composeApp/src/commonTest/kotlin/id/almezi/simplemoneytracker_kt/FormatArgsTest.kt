package id.almezi.simplemoneytracker_kt

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Covers the portable replacement for `java.lang.String.format`.
 *
 * Every one of Saku's 163 strings is Indonesian copy, and the ones with
 * placeholders use the positional form (`%1$s`). The JVM version is gone, so
 * this helper is now the only thing standing between a typo in the XML and a
 * screen that renders `%1$s` to the user verbatim.
 */
class FormatArgsTest {

    @Test
    fun `substitutes a single positional placeholder`() {
        assertEquals("Transaksi 12 disimpan", formatArgs("Transaksi %1\$s disimpan", "12"))
    }

    @Test
    fun `substitutes repeated and out-of-order positionals`() {
        // Order of appearance must not matter: that is the point of %N$.
        assertEquals("Rp 500 untuk Makan", formatArgs("%1\$s untuk %2\$s", "Rp 500", "Makan"))
        assertEquals("Makan untuk Rp 500", formatArgs("%2\$s untuk %1\$s", "Rp 500", "Makan"))
        assertEquals("a a", formatArgs("%1\$s %1\$s", "a"))
    }

    @Test
    fun `substitutes plain and numeric placeholders left to right`() {
        assertEquals("Rendahnya 12", formatArgs("Rendahnya %s", 12))
        assertEquals("12 dan 34", formatArgs("%d dan %d", 12, 34))
    }

    @Test
    fun `renders a literal percent as a doubled one`() {
        assertEquals("100%", formatArgs("100%%"))
        assertEquals("100% selesai", formatArgs("100%% selesai"))
        assertEquals("100% dari total", formatArgs("100%% dari %1\$s", "total"))
    }

    @Test
    fun `leaves text without placeholders untouched`() {
        assertEquals("Ringkasan bulanan", formatArgs("Ringkasan bulanan"))
        assertEquals("", formatArgs(""))
    }

    @Test
    fun `yields an empty string when an argument is missing`() {
        // Better a gap than a crash or a visible "%1$s" in the UI.
        assertEquals("Transaksi  disimpan", formatArgs("Transaksi %1\$s disimpan"))
    }

    @Test
    fun `yields an empty string when there are more placeholders than arguments`() {
        assertEquals("a ", formatArgs("%1\$s %2\$s", "a"))
    }

    @Test
    fun `treats a trailing percent as literal text`() {
        assertEquals("diskon 50%", formatArgs("diskon 50%"))
    }

    @Test
    fun `leaves an unknown conversion type untouched`() {
        assertEquals("nilai %q", formatArgs("nilai %q", 1))
    }

    @Test
    fun `works as a String extension`() {
        assertEquals("Hai Budi", "Hai %1\$s".format("Budi"))
        assertEquals("Hai ", "Hai %1\$s".format())
        assertEquals("a-b", "%1\$s-%2\$s".format("a", "b"))
    }

    @Test
    fun `formats null arguments as empty text`() {
        assertEquals("Total: ", formatArgs("Total: %1\$s", null))
    }
}