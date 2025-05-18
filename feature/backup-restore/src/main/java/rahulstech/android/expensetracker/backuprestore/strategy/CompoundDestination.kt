package rahulstech.android.expensetracker.backuprestore.strategy

class CompoundDestination() : Destination {

    override val output: Destination.Output = NO_OUTPUT

    private var destionations: MutableMap<String, Destination>? = null

    fun addDestination(name: String, destination: Destination) {
        val destinations = destionations ?: HashMap()
        destinations[name] = destination
    }

    fun removeDestination(name: String): Destination? {
        if (null == destionations) return null
        val removed = destionations?.remove(name)
        if (destionations?.isEmpty() == true) {
            destionations = null
        }
        return removed
    }

    fun getDestination(name: String): Destination? {
        return destionations?.let {  it[name] }
    }

    override fun setup() {
        destionations?.let {
            it.values.forEach { d -> d.setup() }
        }
    }

    override fun cleanup() {
        destionations?.let {
            it.values.forEach { d -> d.cleanup() }
            it.clear()
        }
        destionations = null
    }

    override fun canWrite(name: String): Boolean {
        return getDestinationCanWrite(name) != null
    }

    private fun getDestinationCanWrite(name: String): Destination? {
        return destionations?.let { it.values.find { d -> d.canWrite(name) } }
    }

    override fun writeSingle(name: String, entry: Any?) {
        getDestinationCanWrite(name)?.writeSingle(name,entry)
    }

    override fun writeMultiple(name: String, entries: List<Any>) {
        getDestinationCanWrite(name)?.writeMultiple(name,entries)
    }

    override fun beginAppendMultiple(name: String) {
        getDestinationCanWrite(name)?.beginAppendMultiple(name)
    }

    override fun endAppendMultiple(name: String) {
        getDestinationCanWrite(name)?.endAppendMultiple(name)
    }

    override fun appendMultiple(name: String, entries: List<Any>) {
        getDestinationCanWrite(name)?.appendMultiple(name,entries)
    }
}