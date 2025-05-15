package rahulstech.android.expensetracker.backuprestore.strategy.restore

import android.content.Context
import rahulstech.android.expensetracker.backuprestore.strategy.BaseStrategy
import rahulstech.android.expensetracker.backuprestore.strategy.Strategy
import rahulstech.android.expensetracker.backuprestore.strategy.Constants

class JsonRestoreStrategy(context: Context): BaseStrategy(context) {

    companion object {
        const val KEY_SOURCE_FILE = "key_source_file"
    }

    private val target = JsonRestoreDestination(context)
    private var source: JsonRestoreSource? = null

    override fun doPerform(params: Strategy.Parameter) {
        val uri = params.getUri(KEY_SOURCE_FILE)
        requireNotNull(uri) { "$KEY_SOURCE_FILE not found" }

//        val input = UriInput(applicationContext.contentResolver, uri)
//        source = JsonRestoreSource(input)

        source?.apply {
            val version = readSingle<Int>(Constants.JSON_FIELD_VERSION)

        }
    }

    override fun clean() {
        super.clean()
        target.cleanup()
        source?.cleanup()
    }

//    private class UriInput(val contentResolver: ContentResolver, val uri: Uri): InputStreamInput {
//
//        private var stream: InputStream? = null
//
//        override fun get(): InputStream = stream!!
//
//        override fun create() {
//            stream = contentResolver.openInputStream(uri)
//        }
//
//        override fun destroy() {
//            stream?.close()
//            stream = null
//        }
//    }
}