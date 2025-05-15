package rahulstech.android.expensetracker.backuprestore.strategy

import java.io.InputStream

class InputStreamInput(private val factory: ()->InputStream): Source.Input {

    private var stream: InputStream? = null

    override fun create() {
        if (null != stream) {
            throw IllegalStateException("previous InputStream instance still open; close it using destroy() before creating new instance")
        }
        stream = factory()
    }

    override fun get(): InputStream = stream!!

    override fun destroy() {
        stream?.close()
        stream = null
    }
}