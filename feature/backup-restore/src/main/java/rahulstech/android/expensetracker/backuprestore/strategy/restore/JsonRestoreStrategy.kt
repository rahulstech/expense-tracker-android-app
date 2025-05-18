package rahulstech.android.expensetracker.backuprestore.strategy.restore

import android.content.Context
import rahulstech.android.expensetracker.backuprestore.util.AppSettingsData
import rahulstech.android.expensetracker.backuprestore.strategy.BaseStrategy
import rahulstech.android.expensetracker.backuprestore.worker.Constants
import rahulstech.android.expensetracker.backuprestore.strategy.Strategy

class JsonRestoreStrategy(context: Context,
                          override val source: JsonRestoreSource,
                          override val destination: LocalRestoreDestination): BaseStrategy(context) {

    companion object {
        private val TAG = JsonRestoreSource::class.simpleName
        private const val BUFFER_SIZE = 1000
    }

    override fun doPerform(params: Strategy.Parameter) {
        while (source.moveNext()) {
            val name = source.nextName()
            when(name) {
                Constants.JSON_FIELD_ACCOUNTS -> restoreAccounts()
                Constants.JSON_FIELD_GROUPS, Constants.JSON_FIELD_PEOPLE -> restoreGroups()
                Constants.JSON_FIELD_HISTORIES -> restoreHistories()
                Constants.APP_SETTINGS -> restoreAppSettings()
                Constants.AGENT_SETTINGS -> restoreAgentSettings()
            }
        }
    }

    private fun restoreAccounts() {
        restoreAppendMultiple(Constants.EXPENSE_DB_ACCOUNTS)
    }

    private fun restoreGroups() {
        restoreAppendMultiple(Constants.EXPENSE_DB_GROUPS)
    }

    private fun restoreHistories() {
        restoreAppendMultiple(Constants.EXPENSE_DB_HISTORIES)
    }

    private fun restoreAppendMultiple(name: String) {
        destination.beginAppendMultiple(name)
        source.bufferValue(BUFFER_SIZE) { _,_,buffer -> destination.appendMultiple(name, buffer)}
        destination.endAppendMultiple(name)
    }

    private fun restoreAppSettings() {
        val appSettings = source.nextValue<AppSettingsData>()?.toSettingsModel()
        appSettings?.let {
            destination.writeSingle(Constants.APP_SETTINGS, appSettings)
        }
    }

    private fun restoreAgentSettings() {

    }
}