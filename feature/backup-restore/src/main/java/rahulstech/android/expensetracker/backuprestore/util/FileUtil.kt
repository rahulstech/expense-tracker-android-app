package rahulstech.android.expensetracker.backuprestore.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.core.content.contentValuesOf
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileInputStream
import java.io.IOException
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
        var readLength: Int
        while (true) {
            readLength = src.read(buffer)
            if (readLength == -1) {
                break
            }
            dest.write(buffer, 0, readLength)
            dest.flush()
        }
    }

    private fun getPublicBackupDirectoryRelative(): String {
        return if (Build.VERSION.SDK_INT >= 29) {
            // media store relative path must end with / and must not start with /
            // relative path must start with one of the scoped directories.
            // for example: Documents, Downloads etc.
            // all Environment.DIRECTORY_* are valid for start of relative path
            "${Environment.DIRECTORY_DOCUMENTS}/$APP_DIRECTORY/$BACKUP_DIRECTORY/"
        } else {
            "$APP_DIRECTORY/$BACKUP_DIRECTORY/"
        }
    }

    fun openPublicBackupFileOutputStream(context: Context, filename: String): OutputStream {
        return if (Build.VERSION.SDK_INT >= 29) {
            openPublicBackupFileOutputStream29(context.applicationContext, filename)
        }
        else {
            openPublicBackupFileOutputStreamLegacy(filename)
        }
    }

    private fun openPublicBackupFileOutputStreamLegacy(filename: String): OutputStream {
        val dir = File(Environment.getExternalStorageDirectory(), getPublicBackupDirectoryRelative())
        val file = File(dir, filename)
        try {
            return file.outputStream()
        }
        catch (ex: IOException) {
            throw IllegalStateException("unable to open OutputStream for backup file ${file.canonicalPath} because ${ex.message}")
        }
    }

    @RequiresApi(29)
    private fun openPublicBackupFileOutputStream29(appContext: Context, filename: String): OutputStream {
        val contentResolver = appContext.contentResolver
        val extension = MimeTypeMap.getFileExtensionFromUrl(filename)
        val mimeType = EXTENSION_MIME_TYPE_MAP[extension] ?: "application/octet-stream"
        val relativePath = getPublicBackupDirectoryRelative()
        val values = contentValuesOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME to filename,
            MediaStore.Files.FileColumns.MIME_TYPE to mimeType,
            MediaStore.Files.FileColumns.RELATIVE_PATH to relativePath,
        )
        val uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val contentUri = contentResolver.insert(uri, values)
            ?: throw IllegalStateException("unable to create public backup file $filename in directory $relativePath")
        return contentResolver.openOutputStream(contentUri) ?: throw IllegalStateException("unable to open OutputStream for backup file $relativePath/$filename")
    }
}