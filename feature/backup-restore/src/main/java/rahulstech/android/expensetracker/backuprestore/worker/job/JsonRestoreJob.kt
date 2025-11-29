package rahulstech.android.expensetracker.backuprestore.worker.restore.job

import android.content.Context
import com.google.gson.stream.JsonToken
import dreammaker.android.expensetracker.database.IExpenseDatabase
import rahulstech.android.expensetracker.backuprestore.Constants
import rahulstech.android.expensetracker.backuprestore.VersionException
import rahulstech.android.expensetracker.backuprestore.util.AccountData
import rahulstech.android.expensetracker.backuprestore.util.GroupData
import rahulstech.android.expensetracker.backuprestore.util.HistoryData
import rahulstech.android.expensetracker.backuprestore.util.Progress
import rahulstech.android.expensetracker.backuprestore.util.newGson
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
                eoa = true
                job.endArray()
                break
            }
            val item = job.readNextObject(clazz)
            buffer.add(item)
        }
        return buffer
    }
}

abstract class JsonRestoreJob(
    val context: Context,
    val version: Int,
    val source: InputStream,
) {

    companion object {

        private val TAG = JsonRestoreJob::class.simpleName

        fun create(context: Context, db: IExpenseDatabase, sourceFactory: ()->InputStream): JsonRestoreJob {
            val version = sourceFactory().use { source ->
                readVersion(source)
            }

            return sourceFactory().use { source ->
                when (version) {
                    8 -> V8Impl(context,source, db)
                    else -> throw VersionException("can not create JsonRestoreJob for version $version")
                }
            }
        }

        internal fun readVersion(source: InputStream): Int {
            val reader = android.util.JsonReader(InputStreamReader(source))
            reader.beginObject()
            reader.nextName() // read
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
        notifyProgress(Progress.Infinite())
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
}

class V8Impl(
    context: Context,
    source: InputStream,
    val db: IExpenseDatabase,
): JsonRestoreJob(context,8,source) {

    override fun doRestore() {
        db.runInTransaction {
            while (hasNext()) {
                val name = readNextName()
                when(name) {
                    Constants.JSON_FIELD_ACCOUNTS -> restoreAccounts(readNextObjectArray(AccountData::class.java))
                    Constants.JSON_FIELD_GROUPS -> restoreGroups(readNextObjectArray(GroupData::class.java))
                    Constants.JSON_FIELD_HISTORIES -> restoreHistories(readNextObjectArrayInChunk(HistoryData::class.java))
                    else -> skipNext()
                }
            }
        }
    }

    private fun restoreAccounts(data: List<AccountData>) {
        val accounts = data.map { it.toAccountEntity() }
        db.accountDao.insertAccounts(accounts)
    }

    private fun restoreGroups(data: List<GroupData>) {
        val groups = data.map { it.toGroupEntity() }
        db.groupDao.insertGroups(groups)
    }

    private fun restoreHistories(reader: JsonArrayChunkReader<HistoryData>) {
        while (reader.hasNext()) {
            val chunk = reader.readNextChunk()
            val histories = chunk.map { it.toHistoryEntity() }
            db.historyDao.insertHistories(histories)
        }
    }
}