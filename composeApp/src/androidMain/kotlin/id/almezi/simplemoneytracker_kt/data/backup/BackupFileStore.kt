package id.almezi.simplemoneytracker_kt.data.backup

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Android backup I/O: `FileProvider` for export, `SharedPreferences` for state.
 *
 * The `FileProvider` is the fix for the export bug recorded in docs/07 §7.7. The
 * old implementation wrote exports to `getExternalFilesDir`, which Android 11+
 * hides from the file picker, so a user could create a backup and then had no
 * way to reach the file. Sharing via `ACTION_SEND` hands it to the system file
 * picker or any share target.
 *
 * Constructed with an explicit `Context` and handed in by the caller — manual
 * DI, per `AGENTS.md`. There is no global context holder.
 */
class AndroidBackupFileStore(
    private val context: Context,
) : BackupFileStore {

    override suspend fun shareText(fileName: String, content: String, mimeType: String): String {
        val dir = File(context.cacheDir, "backups").apply { mkdirs() }
        val file = File(dir, fileName).apply { writeText(content, StandardCharsets.UTF_8) }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
        return file.absolutePath
    }

    override suspend fun writeText(fileName: String, content: String): String {
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        val file = File(dir, fileName)
        file.writeText(content, StandardCharsets.UTF_8)
        return file.absolutePath
    }
}

/** Android preferences, using the same `saku_backup` store as before the migration. */
class AndroidBackupPreferences(
    private val context: Context,
) : BackupPreferences {

    private val prefs
        get() = context.getSharedPreferences("saku_backup", Context.MODE_PRIVATE)

    override fun getLong(key: String, default: Long): Long = prefs.getLong(key, default)

    override fun putLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }
}