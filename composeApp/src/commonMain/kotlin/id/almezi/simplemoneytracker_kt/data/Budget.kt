package id.almezi.simplemoneytracker_kt.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val categoryId: String,
    val limitAmount: Long,
    val alertAt80: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)