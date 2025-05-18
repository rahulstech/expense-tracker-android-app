package rahulstech.android.expensetracker.backuprestore.strategy

import android.content.Context
import android.net.Uri
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.settings.SettingsProvider
import java.io.InputStream
import java.io.OutputStream

val NO_INPUT = object: Source.Input {
    override fun create() {}
    override fun destroy() {}
    override fun get() {}
}

val NO_OUTPUT = object: Destination.Output {
    override fun create() {}
    override fun destroy() {}
    override fun get() {}
}

class InputStreamInput(private val factory: ()-> InputStream): Source.Input {

    companion object {
        fun fromUri(context: Context, uri: Uri): InputStreamInput = InputStreamInput {
            context.applicationContext.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("can not open InputStream fro uri $uri")
        }
    }

    private var stream: InputStream? = null

    override fun create() {
        if (null != stream) {
            throw IllegalStateException("previous InputStream instance still open; close it using destroy() then create new instance")
        }
        stream = factory()
    }

    override fun get(): InputStream = stream!!

    override fun destroy() {
        stream?.close()
        stream = null
    }
}

class ExpenseDatabaseOutput(private val context: Context): Destination.Output {

    private var db: ExpensesDatabase? = null

    override fun create() {
        if (null != db) {
            throw IllegalStateException("previous db instance still open; close it using destroy() before creating new instance")
        }
        db = ExpensesDatabase.getInstance(context.applicationContext)
    }

    override fun get(): ExpensesDatabase = db!!

    override fun destroy() {
        db?.close()
        db = null
    }
}

class AppSettingsIO(private val context: Context): Source.Input, Destination.Output {
    override fun create() {}

    override fun destroy() {}

    override fun get(): SettingsProvider = SettingsProvider.get(context)
}

class AgentSettingsIO(private val context: Context): Source.Input, Destination.Output {
    override fun create() {}

    override fun destroy() {}

    override fun get(): Any {
        TODO("Not yet implemented")
    }
}

class OutputStreamOutput(private val factory: ()->OutputStream): Destination.Output {

    companion object {

        fun fromUri(context: Context, uri: Uri): OutputStreamOutput {
            return OutputStreamOutput {
                context.contentResolver.openOutputStream(uri)
                    ?: throw IllegalStateException("can not open OutputStream for uri=$uri")
            }
        }
    }

    private var stream: OutputStream? = null

    override fun create() {
        if (null != stream) {
            throw IllegalStateException("previous OutputStream is still open; close it using destroy() then create new instance")
        }
        stream = factory()
    }

    override fun get(): OutputStream = stream!!

    override fun destroy() { stream?.close() }
}