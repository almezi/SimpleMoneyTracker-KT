package id.almezi.simplemoneytracker_kt.data.backup

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene
import platform.UniformTypeIdentifiers.UTTypeCommaSeparatedText
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
        val presenter = keyViewController()
        if (presenter != null) {
            val activity = UIActivityViewController(
                activityItems = listOf(NSURL.fileURLWithPath(path)),
                applicationActivities = null,
            )
            // Mandatory on iPad: a popover without an anchor is a hard crash.
            activity.popoverPresentationController?.sourceView = presenter.view
            presenter.presentViewController(activity, animated = true, completion = null)
        }
        return path
    }

    override suspend fun writeText(fileName: String, content: String): String {
        val path = documentsDirectory() + "/" + fileName
        NSFileManager.defaultManager.createFileAtPath(
            path = path,
            contents = content.toNSData(),
            attributes = null,
        )
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
        val picker = UIDocumentPickerViewController(
            listOf(UTTypeJSON, UTTypeCommaSeparatedText, UTTypePlainText),
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
            val path = url?.path
            onPicked(path?.let { readTextAtPath(it) })
        }

        override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
            if (delivered) return
            delivered = true
            onPicked(null)
        }

        private fun readTextAtPath(path: String): String =
            NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)?.toString() ?: ""
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

/** UTF-8 bytes as `NSData`, which is what Foundation's file APIs expect. */
@OptIn(ExperimentalForeignApi::class)
internal fun String.toNSData(): NSData =
    encodeToByteArray().usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = pinned.size.toULong())
    }

/**
 * The scene's key window's root view controller, needed to present any UIKit
 * sheet.
 *
 * `keyWindow` is deprecated in iOS 15 in favour of comparing `windowScene` and
 * `isKeyWindow`, but it stays the shortest correct way to reach a presenter from
 * code that holds no scene reference of its own. Top-level rather than a member
 * because both the store and the picker need it.
 */
private fun keyViewController(): UIViewController? =
    UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstOrNull()
        ?.keyWindow
        ?.rootViewController
