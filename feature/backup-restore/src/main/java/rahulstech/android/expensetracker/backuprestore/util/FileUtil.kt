package rahulstech.android.expensetracker.backuprestore.util

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.content.contentValuesOf
import java.io.File
import java.io.IOException

private val APP_DIRECTORY = "ExpenseTracker"
private val BACKUP_DIRECTORY = "backup"


fun getMimeTypeFromPath(path: String): String {
    val extension = MimeTypeMap.getFileExtensionFromUrl(path)
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
}

fun getExtensionNameFromMimeType(mimeType: String): String {
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    return ".$extension" ?: ""
}

fun getAppFileUri(context: Context, directory: String, filename: String): Uri {
    val mimeType = getMimeTypeFromPath(filename)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        val file = File("${Environment.getExternalStorageDirectory()}/$APP_DIRECTORY/$directory", filename)
        try {
            file.parentFile?.let { parent ->
                if (!parent.exists()) {
                    parent.mkdirs()
                }
            }
        }
        catch (ex: IOException) {
            throw IOException("can not create file ${file.canonicalPath}")
        }
        return Uri.fromFile(file)
    }
    else {
        val contentResolver = context.applicationContext.contentResolver
        val file = File("${Environment.DIRECTORY_DOCUMENTS}/$APP_DIRECTORY/$BACKUP_DIRECTORY", filename)
        val values = contentValuesOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME to file.name,
            MediaStore.Files.FileColumns.MIME_TYPE to mimeType,
            MediaStore.Files.FileColumns.RELATIVE_PATH to (file.parentFile?.canonicalPath ?: "")
        )
        return contentResolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),values)
            ?: throw IOException("can not create file ${file.canonicalPath}")
    }
}

fun getBackupFileUri(context: Context, filename: String): Uri = getAppFileUri(context, BACKUP_DIRECTORY, filename)


//fun moveBackupFileToExternalStorage(context: Context, target: File): Uri {
//    val mimeType = getMimeTypeFromPath(target.canonicalPath)
//    val extension = getExtensionNameFromMimeType(mimeType)
//    val filename = "backup_${System.currentTimeMillis()}${extension}"
//    val uri = getBackupFileUri(context, filename)
//
//}
