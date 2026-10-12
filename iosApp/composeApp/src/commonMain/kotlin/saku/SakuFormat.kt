// Adapted from the Android app's ui/designsystem/SakuFormat.kt.
//
// Key change, and the one Stage 3 of the iOS plan recommends anyway: every
// function that took an Android `Context` purely to read a month/day string
// array now takes the strings directly. That makes them pure, unit-testable
// without a Context, and portable.

package saku

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

private const val MINUS = "−"
private const val MAX_AMOUNT_DIGITS = 12
const val MAX_AMOUNT = 999_999_999_999L

val JAKARTA: TimeZone = TimeZone.of("Asia/Jakarta")

fun formatRupiah(amount: Long): String =
    if (amount < 0L) MINUS + "Rp " + groupDigits(abs(amount).toString())
    else "Rp " + groupDigits(amount.toString())

fun formatRupiahFromDigits(digits: String): String {
    val clean = sanitizeAmountInput(digits)
    if (clean.isEmpty()) return SAKU_AMOUNT_PLACEHOLDER
    return "Rp " + groupDigits(clean)
}

fun sanitizeAmountInput(value: String): String =
    value.filter { it.isDigit() }.take(MAX_AMOUNT_DIGITS)

fun parseAmountDigits(value: String): Long =
    sanitizeAmountInput(value).toLongOrNull() ?: 0L

fun isAmountInRange(value: String): Boolean = parseAmountDigits(value) in 1L..MAX_AMOUNT

fun formatSavingsRate(income: Long, expense: Long): String {
    if (income <= 0L) return SAKU_NO_DATA
    return formatPercent((income - expense).toDouble() / income.toDouble() * 100.0, 1) + "%"
}

private fun groupDigits(digits: String): String =
    digits.reversed().chunked(3).joinToString(".").reversed()

private fun formatPercent(value: Double, decimals: Int): String {
    val pattern = if (decimals == 0) "0" else "0." + "0".repeat(decimals)
    val factor = 10.0.pow(decimals)
    val rounded = (value * factor).roundToLong() / factor
    val normalized = if (rounded == 0.0) 0.0 else rounded
    val formatted = formatFixed(pattern, normalized)
    return if (formatted.startsWith("-")) MINUS + formatted.substring(1) else formatted
}

/**
 * Minimal fixed-decimal formatter. Replaces java.text.DecimalFormat, which is
 * JVM-only and would not compile in commonMain.
 */
private fun formatFixed(pattern: String, value: Double): String {
    val decimals = pattern.substringAfter('.', "").length
    val negative = value < 0
    val scaled = abs(value).let { (it * (10.0).pow(decimals)).roundToLong() }
    val div = longPowOfTen(decimals)
    val intPart = scaled / div
    val fracPart = scaled % div
    val intDigits = intPart.toString()
    val grouped = intDigits.reversed().chunked(3).joinToString(".").reversed()
    val frac = if (decimals == 0) "" else "." + fracPart.toString().padStart(decimals, '0')
    return (if (negative) "-" else "") + grouped + frac
}

private fun longPowOfTen(n: Int): Long {
    var r = 1L
    repeat(n) { r *= 10L }
    return r
}

data class SakuMonth(val year: Int, val monthNumber: Int) : Comparable<SakuMonth> {
    override fun compareTo(other: SakuMonth): Int {
        if (year != other.year) return year.compareTo(other.year)
        return monthNumber.compareTo(other.monthNumber)
    }

    fun startMillis(): Long = LocalDate(year, monthNumber, 1).atStartOfDayIn(JAKARTA).toEpochMilliseconds()
    fun endMillisExclusive(): Long =
        LocalDate(year, monthNumber, 1).plus(1, DateTimeUnit.MONTH).atStartOfDayIn(JAKARTA)
            .toEpochMilliseconds()

    fun labelLong(months: List<String>): String {
        val idx = (monthNumber - 1).coerceIn(0, 11)
        // Guard for the frame where the string array has not resolved yet.
        val name = months.getOrNull(idx) ?: ""
        return "$name $year"
    }
}

data class SakuDate(val year: Int, val monthNumber: Int, val dayOfMonth: Int)

fun monthOf(epochMillis: Long): SakuMonth {
    val d = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(JAKARTA).date
    return SakuMonth(d.year, d.monthNumber)
}

fun dayNameOf(epochMillis: Long, days: List<String>): String {
    val d = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(JAKARTA).date
    return days.getOrElse(d.dayOfWeek.ordinal) { "" }
}