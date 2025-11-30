package rahulstech.android.expensetracker.backuprestore.worker.job

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import rahulstech.android.expensetracker.backuprestore.VersionException
import rahulstech.android.expensetracker.backuprestore.util.Progress
import rahulstech.android.expensetracker.backuprestore.util.newGson
import rahulstech.android.expensetracker.backuprestore.worker.job.impl.restore.JsonRestoreJobV8Impl
import rahulstech.android.expensetracker.domain.RestoreRepository
import java.io.Closeable
import java.io.InputStream
import java.io.InputStreamReader

class JsonArrayChunkReader<T>(
    val job: JsonRestoreJob,
    val clazz: Class<T>,
) {
    // end of array
    private var eoa: Boolean = true

    init {
        if (job.isNextArrayStart()) {
            job.beginArray()
            eoa = false
        }
    }

    fun hasNext(): Boolean = !eoa && job.hasNext()

    fun readNextChunk(limit: Int = 1000): List<T> {
        if (eoa) {
            throw IllegalStateException("already reached end of array")
        }
        val buffer = mutableListOf<T>()
        for (x in 1..limit) {
            if (job.isNextArrayEnd()) {
                markEOA()
                break
            }
            val item = job.readNextObject(clazz)
            // NOTE: let say limit is 5 and only 3 items remaining in the json array
            // so during 4th iteration it will return null,
            if (null == item) {
                markEOA()
                break
            }
            buffer.add(item)
        }
        return buffer
    }

    private fun markEOA() {
        eoa = true
        if (job.isNextArrayEnd()) {
            job.endArray()
        }
    }
}


abstract class JsonRestoreJob(
    val version: Int,
    val source: InputStream,
): Closeable {

    companion object {

        private val TAG = JsonRestoreJob::class.simpleName

        fun create(repo: RestoreRepository, sourceFactory: ()->InputStream): JsonRestoreJob {
            val version = sourceFactory().use { source ->
                readVersion(source)
            }
            val source = sourceFactory()
            return when (version) {
                8 -> JsonRestoreJobV8Impl(source, repo)
                else -> throw VersionException("can not create JsonRestoreJob for version $version")
            }
        }

        internal fun readVersion(source: InputStream): Int {
            val reader = JsonReader(InputStreamReader(source))
            reader.beginObject()
            reader.nextName()
            return reader.nextInt()
        }
    }

    private val gson = newGson()
    private val reader = gson.newJsonReader(InputStreamReader(source))

    // terminated will be set and get from different threads, always get and set this in threadsafe way
    private var terminated: Boolean = false
    private val lock = Any()

    var progressCallback: ((JsonRestoreJob, Progress)-> Unit)? = null

    fun restore() {
        beginObject()
        ensureNotTerminated()
        doRestore()
        endObject()
    }

    protected abstract fun doRestore()

    fun terminate() {
        synchronized(lock) {
            terminated = true
        }
    }

    protected fun ensureNotTerminated() {
        val terminated = synchronized(lock) { this.terminated }
        if (terminated) {
            throw IllegalStateException("JsonRestoreJob is terminated")
        }
    }

    fun hasNext(): Boolean {
        ensureNotTerminated()
        return reader.hasNext()
    }

    fun skipNext() {
        ensureNotTerminated()
        if (reader.peek() == JsonToken.NAME) {
            reader.nextName()
        }
        else {
            reader.skipValue()
        }
    }

    fun readNextName(): String {
        ensureNotTerminated()
        return reader.nextName()
    }

    fun <T> readNextObjectArray(clazz: Class<T>): List<T> {
        ensureNotTerminated()
        val value = mutableListOf<T>()
        reader.beginArray()
        while (reader.hasNext()) {
            val item = readNextObject(clazz)
            value.add(item)
        }
        reader.endArray()
        return value
    }

    fun <T> readNextObjectArrayInChunk(clazz: Class<T>): JsonArrayChunkReader<T> =
        JsonArrayChunkReader(
            job = this,
            clazz = clazz,
        )

    fun <T> readNextObject(clazz: Class<T>): T {
        ensureNotTerminated()
        return gson.fromJson<T>(reader, clazz)
    }

    fun beginArray() {
        ensureNotTerminated()
        reader.beginArray()
    }

    fun endArray() {
        ensureNotTerminated()
        reader.endArray()
    }

    fun beginObject() {
        ensureNotTerminated()
        reader.beginObject()
    }

    fun endObject() {
        ensureNotTerminated()
        reader.endObject()
    }

    fun isNextArrayStart(): Boolean = isNextToken(JsonToken.BEGIN_ARRAY)

    fun isNextArrayEnd(): Boolean = isNextToken(JsonToken.END_ARRAY)

    private fun isNextToken(token: JsonToken): Boolean {
        ensureNotTerminated()
        return reader.peek() == token
    }

    fun notifyProgress(progress: Progress) {
        progressCallback?.invoke(this,progress)
    }

    override fun close() {
        source.close()
    }
}