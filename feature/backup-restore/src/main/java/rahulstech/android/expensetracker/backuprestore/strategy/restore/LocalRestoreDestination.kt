package rahulstech.android.expensetracker.backuprestore.strategy.restore

import android.content.Context
import rahulstech.android.expensetracker.backuprestore.strategy.AppSettingsIO
import rahulstech.android.expensetracker.backuprestore.strategy.Destination
import rahulstech.android.expensetracker.backuprestore.strategy.ExpenseDatabaseOutput
import rahulstech.android.expensetracker.backuprestore.strategy.NO_OUTPUT

class LocalRestoreDestination(context: Context): Destination {

    override val output: Destination.Output = NO_OUTPUT

    private val applicationContext = context.applicationContext

    private val expenseDBDest = ExpenseDatabaseDestination(ExpenseDatabaseOutput(applicationContext))
    private val appSettingsDest = AppSettingsDestination(AppSettingsIO(applicationContext))

    override fun setup() {
        try {
            expenseDBDest.setup()
            appSettingsDest.setup()
        }
        catch(ignore: Exception) {
            cleanup()
        }
    }

    override fun cleanup() {
        runCatching { expenseDBDest.cleanup() }
        runCatching { appSettingsDest.cleanup() }
    }

    override fun canWrite(name: String): Boolean {
        return expenseDBDest.canWrite(name) || appSettingsDest.canWrite(name)
    }

    override fun writeSingle(name: String, entry: Any?) {
        if (appSettingsDest.canWrite(name)) {
            appSettingsDest.writeSingle(name,entry)
        }
    }

    override fun writeMultiple(name: String, entries: List<Any>) {}

    override fun beginAppendMultiple(name: String) {}

    override fun endAppendMultiple(name: String) {}

    override fun appendMultiple(name: String, entries: List<Any>) {
        if (expenseDBDest.canWrite(name)) {
            expenseDBDest.appendMultiple(name, entries)
        }
    }
}