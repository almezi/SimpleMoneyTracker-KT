package id.almezi.simplemoneytracker_kt.ui.designsystem

enum class BudgetStatus {
    Safe,
    AlmostGone,
    OverLimit;

    companion object {
        const val ALMOST_GONE_THRESHOLD = 0.75

        fun of(spent: Long, limit: Long): BudgetStatus {
            if (limit <= 0L) return Safe
            val ratio = spent.toDouble() / limit.toDouble()
            return when {
                ratio >= 1.0 -> OverLimit
                ratio >= ALMOST_GONE_THRESHOLD -> AlmostGone
                else -> Safe
            }
        }
    }
}