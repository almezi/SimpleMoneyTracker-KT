package id.almezi.simplemoneytracker_kt.data

import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType

/**
 * Month totals that honour `countsInTotals`.
 *
 * `UI_SPEC.md:16` says categories flagged `countsInTotals = false`
 * (Transfer Masuk, Investasi as expense, Tabungan) are excluded from every
 * total. The flag used to be written at seed time and never read, because the
 * totals were `SUM` queries nobody in this source set could execute; a month
 * holding nothing but a transfer reported a full balance on both screens.
 *
 * The rule now lives here, in plain Kotlin, so both screens share it and a test
 * can actually run it.
 */

/** One category's share of a month total. */
data class CategoryTotal(
    val categoryId: String,
    val total: Long
)

/** Income, expense and per-category totals for one month. */
data class MonthTotals(
    val income: Long,
    val expense: Long,
    val incomeByCategory: List<CategoryTotal>,
    val expenseByCategory: List<CategoryTotal>
) {
    val balance: Long get() = income - expense
}

/** Ids of the categories that must not be counted in any total. */
fun excludedCategoryIds(categories: List<Category>): Set<String> =
    categories.filterNot { it.countsInTotals }.mapTo(mutableSetOf()) { it.id }

/**
 * The rows of `[startMillis, endMillis)` that count towards totals: in the
 * month, and not flagged `countsInTotals = false`.
 *
 * Excluded rows stay in the list screens — the user recorded them, so they are
 * still visible — they just never move a number.
 */
fun countedTransactions(
    transactions: List<Transaction>,
    startMillis: Long,
    endMillis: Long,
    categories: List<Category>
): List<Transaction> {
    val excluded = excludedCategoryIds(categories)
    return transactions.filter {
        it.categoryId !in excluded && it.dateEpochMillis in startMillis until endMillis
    }
}

/** Totals for `[startMillis, endMillis)`, excluding every non-counting category. */
fun monthTotals(
    transactions: List<Transaction>,
    startMillis: Long,
    endMillis: Long,
    categories: List<Category>
): MonthTotals {
    val counted = countedTransactions(transactions, startMillis, endMillis, categories)
    val incomeType = TransactionType.Income.storageValue
    val expenseType = TransactionType.Expense.storageValue
    return MonthTotals(
        income = counted.filter { it.type == incomeType }.sumOf { it.amount },
        expense = counted.filter { it.type == expenseType }.sumOf { it.amount },
        incomeByCategory = counted.byCategory(incomeType),
        expenseByCategory = counted.byCategory(expenseType)
    )
}

private fun List<Transaction>.byCategory(type: String): List<CategoryTotal> =
    filter { it.type == type }
        .groupBy { it.categoryId }
        .map { (categoryId, rows) -> CategoryTotal(categoryId, rows.sumOf { it.amount }) }