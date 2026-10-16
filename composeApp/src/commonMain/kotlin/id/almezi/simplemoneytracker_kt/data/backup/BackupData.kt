package id.almezi.simplemoneytracker_kt.data.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Bumped whenever the shape of [BackupData] changes in a way that an older
 * build cannot read. Kept in sync with the Android-only original.
 */
const val BACKUP_VERSION = 1

/**
 * The on-disk backup shape.
 *
 * Lives in commonMain so **both platforms serialise it with the same code**.
 * Stage 5 item 5.6 requires CSV and JSON output to be byte-identical on Android
 * and iOS; the only way to guarantee that is for there to be exactly one
 * implementation. The previous Android-only version used `org.json`,
 * `SimpleDateFormat`, `Calendar` and `DecimalFormat`, all of which are JVM-only
 * and unavailable on Kotlin/Native.
 */
@Serializable
data class BackupData(
    val version: Int = BACKUP_VERSION,
    val transactions: List<BackupTransaction> = emptyList(),
    val categories: List<BackupCategory> = emptyList(),
    val budgets: List<BackupBudget> = emptyList(),
    val accounts: List<BackupAccount> = emptyList(),
)

@Serializable
data class BackupTransaction(
    val type: String,
    val amount: Long,
    val categoryId: String,
    val accountId: String,
    val dateEpochMillis: Long,
    val note: String? = null,
    val createdAt: Long = 0L,
)

@Serializable
data class BackupCategory(
    val id: String,
    val name: String,
    val type: String,
    @SerialName("color") val colorHex: String,
    val hidden: Boolean = false,
)

@Serializable
data class BackupBudget(
    val categoryId: String,
    val limit: Long,
    val alertAt80: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

@Serializable
data class BackupAccount(
    val id: String,
    val name: String,
    val sortOrder: Int = 0,
)
