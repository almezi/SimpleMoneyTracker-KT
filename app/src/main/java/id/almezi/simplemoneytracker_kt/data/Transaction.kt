package id.almezi.simplemoneytracker_kt.data

import androidx.room.Entity
import androidx.room.PrimaryKey

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
