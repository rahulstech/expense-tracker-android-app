package rahulstech.android.expensetracker.backuprestore.strategy

import android.content.Context
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.settings.SettingsProvider

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

class SettingsProviderOutput(private val context: Context): Destination.Output {
    override fun create() {}

    override fun get(): SettingsProvider = SettingsProvider.get(context)

    override fun destroy() {}
}