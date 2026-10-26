package id.almezi.simplemoneytracker_kt.data.backup

import androidx.compose.runtime.Composable
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.memcpy
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIActivityViewController
import platform.UIKit.popoverPresentationController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene
import platform.UniformTypeIdentifiers.UTTypeJSON
import platform.UniformTypeIdentifiers.UTTypePlainText
import kotlin.coroutines.resume

/**
 * iOS backup I/O: `UIActivityViewController` for export,
 * `UIDocumentPickerViewController` for import, `NSUserDefaults` for state.
 *
 * Each method is the direct counterpart of the Android implementation, so the
 * backup a user produces is the same on both platforms (docs/09 Stage 5.6).
 */
class IosBackupFileStore : BackupFileStore {

    override suspend fun shareText(fileName: String, content: String, mimeType: String): String {
        val path = writeText(fileName, content)
        // Fail loudly instead of reporting a backup that was never shown to the
        // user; the caller turns this into the export-failed toast.
        val presenter = keyViewController()
            ?: error("No view controller available to present the share sheet")
        val activity = UIActivityViewController(
            activityItems = listOf(NSURL.fileURLWithPath(path)),
            applicationActivities = null,
        )
        // Mandatory on iPad: a popover without an anchor is a hard crash.
        activity.popoverPresentationController?.sourceView = presenter.view
        presenter.presentViewController(activity, animated = true, completion = null)
        return path
    }

    private suspend fun writeText(fileName: String, content: String): String {
        val path = documentsDirectory() + "/" + fileName
        // Kotlin/Native has no Dispatchers.IO; Default is the off-main one. The
        // UIActivityViewController presentation in shareText resumes on the
        // caller's dispatcher, where UIKit requires it.
        val wrote = withContext(Dispatchers.Default) {
            NSFileManager.defaultManager.createFileAtPath(
                path = path,
                contents = content.toNSData(),
                attributes = null,
            )
        }
        if (!wrote) error("Could not write backup file: $path")
        return path
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun documentsDirectory(): String {
        val url: NSURL = requireNotNull(
            NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
        ) { "Could not resolve NSDocumentDirectory" }
        return requireNotNull(url.path) { "NSDocumentDirectory has no path" }
    }
}

/**
 * iOS document picker.
 *
 * Modally presented rather than driven by a suspending call, because
 * `UIDocumentPickerViewController` needs a presenting view controller and a
 * delegate object; that is the whole reason `FilePicker` is a separate seam from
 * `BackupFileStore` (see that interface's docs).
 */
class IosFilePicker : FilePicker {

    override fun present(onPicked: (String?) -> Unit) {
        val presenter = keyViewController()
        if (presenter == null) {
            onPicked(null)
            return
        }
        // JSON import only (`UI_SPEC.md` §6.7): plain text covers files whose
        // editor saved a .json with a text mime, and keeps the picker from
        // offering the CSV export that the app cannot re-import.
        val picker = UIDocumentPickerViewController(
            forOpeningContentTypes = listOf(UTTypeJSON, UTTypePlainText),
        )
        picker.delegate = PickerDelegate(onPicked)
        presenter.presentViewController(picker, animated = true, completion = null)
    }

    /**
     * Bridges the picker's two delegate callbacks back to the caller.
     *
     * Exactly one of `didPickDocumentsAtURLs` and `documentPickerWasCancelled`
     * must fire the callback, so the one-shot guard stops a double delivery if
     * UIKit ever reports both.
     */
    private class PickerDelegate(
        private val onPicked: (String?) -> Unit,
    ) : platform.darwin.NSObject(), UIDocumentPickerDelegateProtocol {

        private var delivered = false

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        if (delivered) return
        delivered = true
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        onPicked(url?.let(::readSecurityScoped))
    }

    /**
     * Document-picker URLs are security-scoped: outside the sandbox the read
     * fails with no error unless access is taken explicitly (simulators are
     * lax about this, physical devices are not).
     */
    @OptIn(ExperimentalForeignApi::class)
    private fun readSecurityScoped(url: NSURL): String? {
        val scoped = url.startAccessingSecurityScopedResource()
        try {
            return url.path?.let { readUtf8File(it) }
        } finally {
            if (scoped) url.stopAccessingSecurityScopedResource()
        }
    }

        override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
            if (delivered) return
            delivered = true
            onPicked(null)
        }

    }
}

/** iOS preferences. Mirrors the Android `saku_backup` store with the same key. */
class IosBackupPreferences : BackupPreferences {

    override fun getLong(key: String, default: Long): Long {
        val defaults = NSUserDefaults.standardUserDefaults
        return if (defaults.objectForKey(key) == null) default else defaults.integerForKey(key)
    }

    override fun putLong(key: String, value: Long) {
        NSUserDefaults.standardUserDefaults.setInteger(value, forKey = key)
    }
}

/** iOS needs no arguments; Android's counterpart takes a `Context`. */
fun createBackupPreferences(): BackupPreferences = IosBackupPreferences()

fun createBackupFileStore(): BackupFileStore = IosBackupFileStore()

@Composable
actual fun rememberFilePicker(): FilePicker = androidx.compose.runtime.remember { IosFilePicker() }

@Composable
actual fun rememberBackupFileStore(): BackupFileStore = androidx.compose.runtime.remember { IosBackupFileStore() }

/**
 * UTF-8 bytes as `NSData`, which is what Foundation's file APIs expect.
 *
 * Goes via `NSString` rather than `NSData.create(bytes = …, length = …)`: that
 * factory needs a pinned C pointer, which adds unsafe pointer arithmetic for no
 * benefit when the source is already a Kotlin `String`.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun String.toNSData(): NSData {
    val bytes = this.encodeToByteArray()
    return bytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
    val result = ByteArray(this.length.toInt())
    if (result.isNotEmpty()) {
        result.usePinned { pinned ->
            memcpy(pinned.addressOf(0), this.bytes, this.length)
        }
    }
    return result
}

/** Reads a UTF-8 file, or an empty string if it is missing or not valid UTF-8. */
internal fun readUtf8File(path: String): String {
    val data = NSFileManager.defaultManager.contentsAtPath(path) ?: return ""
    return data.toByteArray().decodeToString()
}

/**
 * The scene's key window's top-most view controller, needed to present any UIKit
 * sheet.
 *
 * `keyWindow` is deprecated in iOS 15 in favour of comparing `windowScene` and
 * `isKeyWindow`, but it stays the shortest correct way to reach a presenter from
 * code that holds no scene reference of its own. Top-level rather than a member
 * because both the store and the picker need it.
 *
 * The walk down `presentedViewController` matters: UIKit refuses to present from
 * the root while anything else is presented, so the sheet must come from the
 * top of the stack.
 */
private fun keyViewController(): UIViewController? =
    UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstOrNull()
        ?.keyWindow
        ?.rootViewController
        ?.let { root -> generateSequence(root) { it.presentedViewController }.last() }
