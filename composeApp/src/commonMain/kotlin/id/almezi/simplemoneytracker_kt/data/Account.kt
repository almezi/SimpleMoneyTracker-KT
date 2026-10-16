package id.almezi.simplemoneytracker_kt.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey val id: String,
    val name: String,
    val sortOrder: Int
)