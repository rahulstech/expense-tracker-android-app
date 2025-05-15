package rahulstech.android.expensetracker.backuprestore.strategy

import android.content.Context
import android.net.Uri

interface Strategy {

    enum class State {
        CREATED,
        RUNNING,
        SUCCEEDED,
        FAILED,
        CANCELED
        ;

        fun isFinished(): Boolean = this in arrayOf(SUCCEEDED, FAILED, CANCELED)
    }

    class Parameter {

        private val map = mutableMapOf<String,Any?>()

        fun put(key: String, value: Any?) {
            map.put(key,value)
        }

        @Suppress("UNCHECKED_CAST")
        fun <T> get(key: String, defaultValue: T? = null): T? {
            if (map.containsKey(key)) {
                return map[key] as T?
            }
            return defaultValue
        }

        fun getString(key: String, defaultValue: String? = null): String? = get(key, defaultValue)

        fun getUri(key: String, defaultValue: Uri? = null): Uri? = get(key,defaultValue)
    }

    data class Output(
        val state: State,
        val parameter: Parameter,
        val result: Any? = null,
        val error: Throwable? = null
    ) {
        fun isSucceeded() = state == State.SUCCEEDED

        fun isFailed() = state == State.FAILED
    }

    data class Progress(
        val current: Int,
        val max: Int,
        val message: CharSequence? = null
    )

    val applicationContext: Context

    fun getState(): State

    fun perform(parameter: Parameter): Output

    fun updateProgress(progress: Progress)

    fun registerProgressListener(listener: ProgressListener)

    fun unregisterProgressListener(listener: ProgressListener)

    fun cancel()

    fun isCanceled(): Boolean

    fun clean()

    interface ProgressListener {
        fun onProgressUpdated(work: Strategy, progress: Progress)
    }
}