package rahulstech.android.expensetracker.backuprestore.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileInputStream
import java.io.InputStream


data class FileEntry(
    val displayName: String,
    val mimeType: String,
    val lastModifiedMillis: Long,
    val uri: Uri
)

object FileUtil {

    private val APP_DIRECTORY = "ExpenseTracker"

    private val BACKUP_DIRECTORY = "backup"

    suspend fun getBackupFilesFlow(context: Context, maxEntries: Int): Flow<List<FileEntry>> = flow {
            val entries = if (Build.VERSION.SDK_INT >= 29) {
                getBackupFiles29(context, maxEntries)
            } else {
                getBackupFilesLegacy(maxEntries)
            }
            emit(entries)
        }

    private fun getBackupFilesLegacy(maxEntries: Int): List<FileEntry> {
        val directory = File(Environment.getExternalStorageDirectory(), "$APP_DIRECTORY/$BACKUP_DIRECTORY")
        return directory
            .listFiles { file ->
                file.isFile && (
                        file.name.endsWith(".json", ignoreCase = true) ||
                                file.name.endsWith(".tar.gz", ignoreCase = true)
                        )
            }
            ?.sortedByDescending { it.lastModified() }
            ?.map { file ->
                val mimeType = when {
                    file.name.endsWith(".json", ignoreCase = true) -> "application/json"
                    file.name.endsWith(".tar.gz", ignoreCase = true) -> "application/gzip"
                    else -> throw IllegalStateException() // never throws
                }

                FileEntry(
                    file.name,
                    mimeType,
                    file.lastModified(),
                    Uri.fromFile(file)
                )
            }
            ?.take(maxEntries) ?: emptyList()
    }

    @RequiresApi(29)
    private fun getBackupFiles29(appContext: Context, maxEntries: Int): List<FileEntry> {
        val uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val projections = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_MODIFIED
        )
        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?" + " AND ${MediaStore.MediaColumns.MIME_TYPE} IN('application/json', 'application/gzip')"
        val selectionValues = arrayOf("${Environment.DIRECTORY_DOCUMENTS}/$APP_DIRECTORY/$BACKUP_DIRECTORY%")
        val sortOrder = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC LIMIT $maxEntries"

        val entries = appContext.contentResolver.query(uri,projections,selection,selectionValues,sortOrder)?.use { cursor ->
            val entries = mutableListOf<FileEntry>()
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val displayNameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val dateModifiedIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
            while (cursor.moveToNext()) {
                val entryUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL, cursor.getLong(idIndex))
                val entry = FileEntry(
                    cursor.getString(displayNameIndex),
                    cursor.getString(mimeTypeIndex),
                    cursor.getLong(dateModifiedIndex),
                    entryUri
                )
                entries.add(entry)
            }
            entries
        }
        return entries ?: emptyList()
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
            else -> throw IllegalStateException("unknown schema ${uri.scheme}; can not openInputStream for uri $uri")
        }
    }
}



fun getMimeTypeFromPath(path: String): String {
    val extension = MimeTypeMap.getFileExtensionFromUrl(path)
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
}

fun getExtensionNameFromMimeType(mimeType: String): String {
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    return ".$extension" ?: ""
}
//
//fun getAppFileUri(context: Context, directory: String, filename: String): Uri {
//    val mimeType = getMimeTypeFromPath(filename)
//    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//        val file = File("${Environment.getExternalStorageDirectory()}/$APP_DIRECTORY/$directory", filename)
//        try {
//            file.parentFile?.let { parent ->
//                if (!parent.exists()) {
//                    parent.mkdirs()
//                }
//            }
//        }
//        catch (ex: IOException) {
//            throw IOException("can not create file ${file.canonicalPath}")
//        }
//        return Uri.fromFile(file)
//    }
//    else {
//        val contentResolver = context.applicationContext.contentResolver
//        val file = File("${Environment.DIRECTORY_DOCUMENTS}/$APP_DIRECTORY/$BACKUP_DIRECTORY", filename)
//        val values = contentValuesOf(
//            MediaStore.Files.FileColumns.DISPLAY_NAME to file.name,
//            MediaStore.Files.FileColumns.MIME_TYPE to mimeType,
//            MediaStore.Files.FileColumns.RELATIVE_PATH to (file.parentFile?.canonicalPath ?: "")
//        )
//        return contentResolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),values)
//            ?: throw IOException("can not create file ${file.canonicalPath}")
//    }
//}
//
//fun getBackupFileUri(context: Context, filename: String): Uri = getAppFileUri(context, BACKUP_DIRECTORY, filename)


//fun moveBackupFileToExternalStorage(context: Context, target: File): Uri {
//    val mimeType = getMimeTypeFromPath(target.canonicalPath)
//    val extension = getExtensionNameFromMimeType(mimeType)
//    val filename = "backup_${System.currentTimeMillis()}${extension}"
//    val uri = getBackupFileUri(context, filename)
//
//}
