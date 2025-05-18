package rahulstech.android.expensetracker.backuprestore.strategy

interface Destination {

    interface Output {
        fun create()

        fun get(): Any

        fun destroy()
    }

    val output: Output

    fun setup()

    fun cleanup()

    fun canWrite(name: String): Boolean

    fun writeSingle(name: String, entry: Any?)

    fun writeMultiple(name: String, entries: List<Any>)

    fun beginAppendMultiple(name: String)

    fun appendMultiple(name: String, entries: List<Any>)

    fun endAppendMultiple(name: String)
}