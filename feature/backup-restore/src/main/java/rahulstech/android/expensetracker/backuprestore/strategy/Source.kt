package rahulstech.android.expensetracker.backuprestore.strategy

interface Source {

    interface Input {

        fun create()

        fun get(): Any

        fun destroy()
    }

    val input: Input

    fun setup()

    fun cleanup()

    fun moveFirst(): Boolean

    fun moveNext(): Boolean

    fun nextName(): String

    fun <T> nextValue(): T?

    fun bufferValue(limit: Int, callback: (name: String, offset: Int, buffer: List<Any>)->Unit)

    fun <T> readSingle(name: String): T?
}