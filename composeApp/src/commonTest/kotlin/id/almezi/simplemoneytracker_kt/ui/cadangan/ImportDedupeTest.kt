package id.almezi.simplemoneytracker_kt.ui.cadangan

import id.almezi.simplemoneytracker_kt.data.Transaction
import id.almezi.simplemoneytracker_kt.data.backup.BackupTransaction
import kotlin.test.Test
import kotlin.test.assertEquals

// A file that lists the same row twice must not store it twice. The dedupe only
// consulted the rows already in the database, so a hand-merged file doubled
// every repeat it contained.
class ImportDedupeTest {

    private fun backup(amount: Long, note: String? = null) = BackupTransaction(
        type = "pemasukan",
        amount = amount,
        categoryId = "inc_salary",
        accountId = "acc_tunai",
        dateEpochMillis = 1_000L,
        note = note,
    )

    private val stored = Transaction(
        id = 1,
        type = "pemasukan",
        amount = 500_000,
        categoryId = "inc_salary",
        accountId = "acc_tunai",
        dateEpochMillis = 1_000L,
        note = null,
        createdAt = 1L,
    )

    @Test
    fun `a row already in the database is skipped`() {
        val result = splitOnDuplicates(listOf(backup(500_000)), setOf(duplicateKeyOf(stored)), currentTime = 9L)

        assertEquals(emptyList(), result.first)
        assertEquals(1, result.second)
    }

    @Test
    fun `the same row twice in one file is inserted once`() {
        val result = splitOnDuplicates(
            listOf(backup(100_000), backup(100_000)),
            emptySet(),
            currentTime = 9L,
        )

        assertEquals(listOf(100_000L), result.first.map { it.amount })
        assertEquals(1, result.second)
    }

    @Test
    fun `rows that differ in any field are all inserted`() {
        val result = splitOnDuplicates(
            listOf(backup(100_000), backup(200_000), backup(100_000, "kopi")),
            emptySet(),
            currentTime = 9L,
        )

        assertEquals(3, result.first.size)
        assertEquals(0, result.second)
    }

    @Test
    fun `a repeated row already in the database is skipped once`() {
        val result = splitOnDuplicates(
            listOf(backup(500_000), backup(500_000)),
            setOf(duplicateKeyOf(stored)),
            currentTime = 9L,
        )

        assertEquals(emptyList(), result.first)
        assertEquals(2, result.second)
    }
}