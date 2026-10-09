package id.almezi.simplemoneytracker_kt.ui.designsystem

import org.junit.Assert.assertEquals
import org.junit.Test

class SakuFormatTest {

    @Test
    fun `formatRupiah groups thousands with dots`() {
        assertEquals("Rp 212.121", formatRupiah(212121))
        assertEquals("Rp 1.000", formatRupiah(1000))
        assertEquals("Rp 999.999.999.999", formatRupiah(999999999999L))
    }

    @Test
    fun `formatRupiah renders zero and small values`() {
        assertEquals("Rp 0", formatRupiah(0))
        assertEquals("Rp 7", formatRupiah(7))
    }

    @Test
    fun `formatRupiah uses unicode minus for negatives`() {
        assertEquals("−Rp 123", formatRupiah(-123))
        assertEquals("−Rp 1.500", formatRupiah(-1500))
    }

    @Test
    fun `formatRupiahFromDigits shows placeholder when empty`() {
        assertEquals("Rp 0", formatRupiahFromDigits(""))
    }

    @Test
    fun `formatRupiahFromDigits groups typed digits`() {
        assertEquals("Rp 212.121", formatRupiahFromDigits("212121"))
    }

    @Test
    fun `sanitizeAmountInput strips non digits`() {
        assertEquals("212121", sanitizeAmountInput("212.121 rupiah"))
        assertEquals("", sanitizeAmountInput("abc"))
    }

    @Test
    fun `sanitizeAmountInput caps at twelve digits`() {
        assertEquals("123456789012", sanitizeAmountInput("12345678901234"))
    }

    @Test
    fun `parseAmountDigits returns zero for empty input`() {
        assertEquals(0L, parseAmountDigits(""))
        assertEquals(0L, parseAmountDigits("abc"))
        assertEquals(212121L, parseAmountDigits("212121"))
    }

    @Test
    fun `isAmountInRange requires one to max amount`() {
        assertEquals(false, isAmountInRange(""))
        assertEquals(false, isAmountInRange("0"))
        assertEquals(true, isAmountInRange("1"))
        assertEquals(true, isAmountInRange("999999999999"))
    }

    @Test
    fun `formatSavingsRate uses comma decimal and one decimal`() {
        assertEquals("99,9%", formatSavingsRate(212121, 123))
        assertEquals("100,0%", formatSavingsRate(212121, 0))
        assertEquals("50,0%", formatSavingsRate(100000, 50000))
    }

    @Test
    fun `formatSavingsRate shows dash when income is zero`() {
        assertEquals("—", formatSavingsRate(0, 123))
        assertEquals("—", formatSavingsRate(0, 0))
    }

    @Test
    fun `formatSavingsRate shows minus for negative rate`() {
        assertEquals("−50,0%", formatSavingsRate(100000, 150000))
    }

    @Test
    fun `formatSavingsRate avoids negative zero`() {
        assertEquals("0,0%", formatSavingsRate(100000, 100000))
    }

    @Test
    fun `formatCategoryShare rounds to whole percent`() {
        assertEquals("25%", formatCategoryShare(25000, 100000))
        assertEquals("100%", formatCategoryShare(100000, 100000))
        assertEquals("—", formatCategoryShare(1000, 0))
    }

    @Test
    fun `budgetRemainingAmount is limit minus spent`() {
        assertEquals(50000L, budgetRemainingAmount(500000, 450000))
        assertEquals(-45000L, budgetRemainingAmount(500000, 545000))
    }
}

class BudgetStatusTest {

    @Test
    fun `under seventy five percent is safe`() {
        assertEquals(BudgetStatus.Safe, BudgetStatus.of(374999, 500000))
        assertEquals(BudgetStatus.Safe, BudgetStatus.of(0, 500000))
    }

    @Test
    fun `seventy five percent up to under full is almost gone`() {
        assertEquals(BudgetStatus.AlmostGone, BudgetStatus.of(375000, 500000))
        assertEquals(BudgetStatus.AlmostGone, BudgetStatus.of(499999, 500000))
    }

    @Test
    fun `full and above is over limit`() {
        assertEquals(BudgetStatus.OverLimit, BudgetStatus.of(500000, 500000))
        assertEquals(BudgetStatus.OverLimit, BudgetStatus.of(600000, 500000))
    }

    @Test
    fun `zero limit is safe`() {
        assertEquals(BudgetStatus.Safe, BudgetStatus.of(1000, 0))
    }
}