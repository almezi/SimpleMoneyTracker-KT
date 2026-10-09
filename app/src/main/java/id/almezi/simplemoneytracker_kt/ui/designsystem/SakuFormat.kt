package id.almezi.simplemoneytracker_kt.ui.designsystem

import android.content.Context
import id.almezi.simplemoneytracker_kt.R
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

private const val MINUS = "−"
private const val MAX_AMOUNT_DIGITS = 12
const val MAX_AMOUNT = 999_999_999_999L

fun formatRupiah(amount: Long): String =
    if (amount < 0L) {
        MINUS + "Rp " + groupDigits(abs(amount).toString())
    } else {
        "Rp " + groupDigits(amount.toString())
    }

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
    val rate = (income - expense).toDouble() / income.toDouble() * 100.0
    return formatPercent(rate, decimals = 1) + "%"
}

fun formatCategoryShare(categoryTotal: Long, typeTotal: Long): String {
    if (typeTotal <= 0L) return SAKU_NO_DATA
    val share = categoryTotal.toDouble() / typeTotal.toDouble() * 100.0
    return formatPercent(share, decimals = 0) + "%"
}

fun budgetRemainingAmount(limit: Long, spent: Long): Long = limit - spent

fun formatShortDate(context: Context, day: Int, month: Int, year: Int): String =
    context.getString(R.string.date_short_format, day, monthShort(context, month), year)

fun formatMonthYear(context: Context, month: Int, year: Int): String =
    context.getString(R.string.month_year_format, monthLong(context, month), year)

fun monthShort(context: Context, month: Int): String =
    context.resources.getStringArray(R.array.months_short)[monthIndex(month)]

fun monthLong(context: Context, month: Int): String =
    context.resources.getStringArray(R.array.months_long)[monthIndex(month)]

fun formatShortDate(context: Context, epochMillis: Long): String {
    val date = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(JAKARTA).date
    return formatShortDate(context, date.dayOfMonth, date.monthNumber, date.year)
}

fun jakartaYearMonth(epochMillis: Long): Pair<Int, Int> {
    val date = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(JAKARTA).date
    return date.monthNumber to date.year
}

val JAKARTA: TimeZone = TimeZone.of("Asia/Jakarta")

private fun monthIndex(month: Int): Int = (month - 1).coerceIn(0, 11)

private fun groupDigits(digits: String): String =
    digits.reversed().chunked(3).joinToString(".").reversed()

private fun formatPercent(value: Double, decimals: Int): String {
    val pattern = if (decimals == 0) "0" else "0." + "0".repeat(decimals)
    val symbols = DecimalFormatSymbols(Locale.US).apply { decimalSeparator = ',' }
    val factor = 10.0.pow(decimals)
    val rounded = (value * factor).roundToLong() / factor
    val normalized = if (rounded == 0.0) 0.0 else rounded
    val formatted = DecimalFormat(pattern, symbols).format(normalized)
    return if (formatted.startsWith("-")) MINUS + formatted.substring(1) else formatted
}
data class SakuMonth(val year: Int, val monthNumber: Int) : Comparable<SakuMonth> {
    override fun compareTo(other: SakuMonth): Int {
        if (year != other.year) return year.compareTo(other.year)
        return monthNumber.compareTo(other.monthNumber)
    }

    fun plusMonth(): SakuMonth = shifted(1)

    fun minusMonth(): SakuMonth = shifted(-1)

    private fun shifted(delta: Int): SakuMonth {
        val date = LocalDate(year, monthNumber, 1).plus(delta, DateTimeUnit.MONTH)
        return SakuMonth(date.year, date.monthNumber)
    }

    fun startMillis(): Long =
        LocalDate(year, monthNumber, 1).atStartOfDayIn(JAKARTA).toEpochMilliseconds()

    fun endMillisExclusive(): Long = shifted(1).startMillis()
}

data class SakuDate(val year: Int, val monthNumber: Int, val dayOfMonth: Int)

fun dateOf(epochMillis: Long): SakuDate {
    val date = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(JAKARTA).date
    return SakuDate(date.year, date.monthNumber, date.dayOfMonth)
}

fun monthOf(epochMillis: Long): SakuMonth {
    val date = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(JAKARTA).date
    return SakuMonth(date.year, date.monthNumber)
}

fun formatMonthLabel(context: Context, month: SakuMonth): String =
    formatMonthYear(context, month.monthNumber, month.year)

fun dayName(context: Context, epochMillis: Long): String {
    val date = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(JAKARTA).date
    return context.resources.getStringArray(R.array.days_short)[date.dayOfWeek.ordinal]
}
