package id.almezi.simplemoneytracker_kt.data.backup

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

/**
 * Android's factory takes a `Context`; iOS's takes none. Deliberately not
 * expect/actual for the same reason as `createAppContainer`, see
 * `AppContainerFactory.kt`.
 */
fun createBackupPreferences(context: Context): BackupPreferences =
    AndroidBackupPreferences(context.applicationContext)

fun createBackupFileStore(context: Context): BackupFileStore =
    AndroidBackupFileStore(context.applicationContext)

/** Storage Access Framework picker (`OpenDocument`) for import. */
class AndroidFilePicker : FilePicker {

    private var onPicked: ((String?) -> Unit)? = null
    private var launch: ((Array<String>) -> Unit)? = null

    fun bind(launch: (Array<String>) -> Unit) {
        this.launch = launch
    }

    override fun present(onPicked: (String?) -> Unit) {
        this.onPicked = onPicked
        launch?.invoke(arrayOf("application/json", "text/csv", "*/*"))
    }

    fun onUriText(text: String?) {
        val callback = onPicked
        onPicked = null
        callback?.invoke(text)
    }
}

@Composable
actual fun rememberFilePicker(): FilePicker {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val picker = remember { AndroidFilePicker() }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            picker.onUriText(null)
        } else {
            scope.launch {
                val text = try {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            stream.bufferedReader(StandardCharsets.UTF_8).readText()
                        }
                    }
                } catch (_: Exception) {
                    null
                }
                picker.onUriText(text)
            }
        }
    }

    SideEffect {
        picker.bind { mimeTypes -> launcher.launch(mimeTypes) }
    }

    return picker
}

@Composable
actual fun rememberBackupFileStore(): BackupFileStore {
    val context = LocalContext.current
    return remember(context) { AndroidBackupFileStore(context) }
}
