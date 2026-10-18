package id.almezi.simplemoneytracker_kt.data

import id.almezi.simplemoneytracker_kt.ui.designsystem.TransactionType

// Month totals that honour `countsInTotals` (`UI_SPEC.md:16`).
//
// The flag was written at seed time and never read, because the totals were
// `SUM` queries no test in this source set could execute: a month holding
// nothing but a transfer reported a full balance on both screens.

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
private fun excludedCategoryIds(categories: List<Category>): Set<String> =
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
    val incomeByCategory = counted.byCategory(TransactionType.Income.storageValue)
    val expenseByCategory = counted.byCategory(TransactionType.Expense.storageValue)
    return MonthTotals(
        income = incomeByCategory.sumOf { it.total },
        expense = expenseByCategory.sumOf { it.total },
        incomeByCategory = incomeByCategory,
        expenseByCategory = expenseByCategory
    )
}

private fun List<Transaction>.byCategory(type: String): List<CategoryTotal> =
    filter { it.type == type }
        .groupBy { it.categoryId }
        .map { (categoryId, rows) -> CategoryTotal(categoryId, rows.sumOf { it.amount }) }