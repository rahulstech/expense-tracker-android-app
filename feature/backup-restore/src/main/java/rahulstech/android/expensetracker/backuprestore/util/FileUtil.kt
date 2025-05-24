package rahulstech.android.expensetracker.backuprestore.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream


data class FileEntry(
    val displayName: String,
    val mimeType: String,
    val uri: Uri
) {
    fun isOfType(types: Array<String>) = mimeType in types
}

object FileUtil {

    private val TAG = FileUtil::class.simpleName

    private const val APP_DIRECTORY = "ExpenseTracker"

    private const val BACKUP_DIRECTORY = "backup"

    private val EXTENSION_MIME_TYPE_MAP = mapOf(
        "json" to "application/json",
        "gz" to "application/gzip"
    )

    fun getBackupFileDetails(context: Context, uri: Uri): FileEntry {
        val document = DocumentFile.fromSingleUri(context, uri)
        val displayName = document?.name ?: ""
        val extension = MimeTypeMap.getFileExtensionFromUrl(displayName)
        val mimeType = EXTENSION_MIME_TYPE_MAP[extension] ?: "application/octet-stream"
        return FileEntry(
            displayName,
            mimeType,
            uri
        )
    }

    fun openInputStream(context: Context, uri: Uri): InputStream {
        return when(uri.scheme) {
            "file" -> {
                val file = uri.toFile()
                FileInputStream(file)
            }
            "content" -> {
                context.applicationContext.contentResolver.openInputStream(uri) ?: throw IllegalStateException("can not openInputStream for uri $uri")
            }
            else -> throw IllegalStateException("unknown schema ${uri.scheme}; can not openInputStream for $uri")
        }
    }

    fun copy(src: InputStream, dest: OutputStream) {
        val buffer = ByteArray(1024)
        var readLength = 0
        while (true) {
            readLength = src.read(buffer)
            if (readLength == 0) {
                break
            }
            dest.write(buffer, 0, readLength)
            dest.flush()
        }
    }

    fun getPublicBackupDirectoryRelative(): File {
        return if (Build.VERSION.SDK_INT >= 29) {
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "$APP_DIRECTORY/$BACKUP_DIRECTORY")
        } else {
            File(APP_DIRECTORY, BACKUP_DIRECTORY)
        }
    }
}