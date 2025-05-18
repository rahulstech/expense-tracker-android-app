package rahulstech.android.expensetracker.backuprestore.strategy.restore

import rahulstech.android.expensetracker.backuprestore.util.AppSettingsData
import rahulstech.android.expensetracker.backuprestore.strategy.AppSettingsIO
import rahulstech.android.expensetracker.backuprestore.worker.Constants
import rahulstech.android.expensetracker.backuprestore.strategy.Destination

class AppSettingsDestination(override val output: AppSettingsIO): Destination {
    private val TAG = AppSettingsDestination::class.simpleName

    override fun setup() {}

    override fun cleanup() {}

    override fun canWrite(name: String): Boolean = name == Constants.APP_SETTINGS

    override fun writeSingle(name: String, entry: Any?) {
        if (name == Constants.APP_SETTINGS && entry is AppSettingsData) {
            val model = entry.toSettingsModel()
            output.get().restore(model)
        }
    }

    override fun writeMultiple(name: String, entries: List<Any>) {}

    override fun beginAppendMultiple(name: String) {}

    override fun endAppendMultiple(name: String) {}

    override fun appendMultiple(name: String, entries: List<Any>) {}

}