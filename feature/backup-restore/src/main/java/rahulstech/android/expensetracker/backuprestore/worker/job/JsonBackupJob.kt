package rahulstech.android.expensetracker.backuprestore.worker.job

import rahulstech.android.expensetracker.backuprestore.Constants
import rahulstech.android.expensetracker.backuprestore.VersionException
import rahulstech.android.expensetracker.backuprestore.util.Progress
import rahulstech.android.expensetracker.backuprestore.util.newGson
import rahulstech.android.expensetracker.backuprestore.worker.job.impl.backup.JsonBackupJobV8Impl
import rahulstech.android.expensetracker.domain.BackupRepository
import java.io.Closeable
import java.io.OutputStream
import java.io.OutputStreamWriter

/**
 *
 */
class JsonArrayChunkWriter<T>(val job: JsonBackupJob): Closeable {

    // end of array
    private var eoa: Boolean = true

    init {
        job.beginArray()
        eoa = false
    }

    fun writeNextChunk(chunk: List<T>) {
        if (eoa) {
            throw IllegalStateException("array ended")
        }
        for (value in chunk) {
            job.writeValue(value)
        }
    }

    override fun close() {
        job.endArray()
        eoa = true
    }
}

abstract class JsonBackupJob(
    val version: Int,
    val destination: OutputStream,
    val progressCallback: (Progress) -> Unit = {}
): Closeable {
    companion object {

        private val TAG = JsonBackupJob::class.simpleName

        fun create(version: Int,
                   repo: BackupRepository,
                   destFactory: ()->OutputStream,
                   progressCallback: (Progress)-> Unit = {}): JsonBackupJob {
            val destination = destFactory()
            return when (version) {
                8 -> JsonBackupJobV8Impl(destination,repo,progressCallback)
                else -> throw VersionException("unknow JsonBackupJob version $version")
            }
        }
    }

    internal val gson = newGson()
    internal val writer = gson.newJsonWriter(OutputStreamWriter(destination))

    private var terminated: Boolean = false
    private val lock = Any()

    fun backup() {
        beginObject()
        writeInt(Constants.JSON_FIELD_VERSION, version)
        doBackup()
        endObject()
    }

    protected abstract fun doBackup()

    fun terminate() {
        synchronized(lock) {
            terminated = true
        }
    }

    fun ensureNotTerminated() {
        val terminated = synchronized(lock) { this.terminated }
        if (terminated) {
            throw IllegalStateException("JsonBackupJob is terminated")
        }
    }

    fun writeInt(name: String, value: Int) {
        writeName(name)
        writeValue(value)
    }

    fun <T> writeArray(name: String, array: List<T>) {
        writeName(name)
        writeValue(array)
    }

    fun <T> writeArrayInChunk(name: String): JsonArrayChunkWriter<T> {
        writeName(name)
        return JsonArrayChunkWriter(this)
    }

    fun writeName(name: String) {
        ensureNotTerminated()
        writer
            .name(name)
            .flush()
    }

    fun writeValue(value: Any?) {
        ensureNotTerminated()
        when(value) {
            is Boolean -> writer.value(value)
            is Int, is Long -> writer.value(value.toLong())
            is Float -> writer.value(value)
            is Double -> writer.value(value)
            is Number -> writer.value(value)
            is String -> writer.value(value)
            null -> writer.nullValue()
            else -> {
                gson.toJson(value, value.javaClass, writer)
            }
        }
        writer.flush()
    }

    fun beginObject() {
        ensureNotTerminated()
        writer
            .beginObject()
            .flush()
    }

    fun endObject() {
        ensureNotTerminated()
        writer
            .endObject()
            .flush()
    }

    fun beginArray() {
        ensureNotTerminated()
        writer
            .beginArray()
            .flush()
    }

    fun endArray() {
        ensureNotTerminated()
        writer
            .endArray()
            .flush()
    }

    fun notifyProgress(progress: Progress) {
        progressCallback.invoke(progress)
    }

    override fun close() {
        destination.close()
    }
}