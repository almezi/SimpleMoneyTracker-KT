package id.almezi.simplemoneytracker_kt.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String,
    val nameRes: String,
    val groupId: String?,
    val countsInTotals: Boolean,
    val type: String,
    val colorHex: String? = null,
    val isDefault: Boolean = true,
    val isHidden: Boolean = false,
    val customName: String? = null
)