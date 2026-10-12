package id.almezi.simplemoneytracker_kt.data.backup

/**
 * The platform capabilities backup needs, reduced to what Saku actually uses
 * (docs/09 Stage 5).
 *
 * Everything above this interface is shared code. Nothing below it is, which is
 * why the same screen and the same ViewModel run on both platforms unchanged.
 *
 * Split deliberately into two seams, because they have different shapes:
 *
 *  - [BackupFileStore] is plain Kotlin and can run anywhere.
 *  - [FilePicker] shows **UI**, so it cannot be a `suspend fun`: Android's SAF
 *    picker has to be registered as an `ActivityResultLauncher` before
 *    composition happens, while iOS's `UIDocumentPickerViewController` needs a
 *    presenting view controller. Neither fits a suspending call, so the screen
 *    takes the picker as a slot instead.
 */
interface BackupFileStore {
    /**
     * Hands a generated file to the user and returns what to show them.
     *
     * This is the path that actually works for a user. The old Android code
     * wrote exports to `getExternalFilesDir`, which Android 11+ hides from the
     * file picker, so a backup could be created but never retrieved. Android
     * now goes through `FileProvider` + `ACTION_SEND`; iOS uses
     * `UIActivityViewController`.
     */
    suspend fun shareText(fileName: String, content: String, mimeType: String): String

    /** Writes [content] somewhere durable, returning a displayable path. */
    suspend fun writeText(fileName: String, content: String): String
}

/**
 * Shows the platform file picker.
 *
 * [present] must be called from a composable, and delivers the chosen file's
 * text to [onPicked], or null if the user cancelled.
 */
interface FilePicker {
    fun present(onPicked: (String?) -> Unit)
}

/**
 * The last-backup timestamp.
 *
 * Android: `SharedPreferences("saku_backup")`. iOS: `NSUserDefaults`. The
 * interface is deliberately narrow — a backup only stores one `Long`, so a
 * general key-value store would be more abstraction than the app needs.
 */
interface BackupPreferences {
    fun getLong(key: String, default: Long = 0L): Long
    fun putLong(key: String, value: Long)
}

/** The key the last successful backup is stored under. */
const val KEY_LAST_BACKUP = "last_backup_millis"
