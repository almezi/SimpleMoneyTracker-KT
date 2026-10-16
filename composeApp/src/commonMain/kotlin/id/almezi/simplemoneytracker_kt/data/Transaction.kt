package id.almezi.simplemoneytracker_kt.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amount: Long,
    val categoryId: String,
    val accountId: String = AccountSeed.DEFAULT_ACCOUNT_ID,
    val dateEpochMillis: Long,
    val note: String?,
    val createdAt: Long
)
