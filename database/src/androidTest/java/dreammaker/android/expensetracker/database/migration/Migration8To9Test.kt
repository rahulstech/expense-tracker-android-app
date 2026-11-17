package dreammaker.android.expensetracker.database.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dreammaker.android.expensetracker.database.ExpensesDatabase
import dreammaker.android.expensetracker.database.SQLiteStatementExecutorWrapperBuilder
import dreammaker.android.expensetracker.database.fake.FakeData8
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration8To9Test {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpensesDatabase::class.java,
    )

    @Test
    fun testMigration7to8() {
        helper.createDatabase(ExpensesDatabase.DB_NAME, 8).use {
            val wrapper = SQLiteStatementExecutorWrapperBuilder().buildFromSupportSQLiteDatabase(it)
            FakeData8().addFakeData(wrapper)
        }

        helper.runMigrationsAndValidate(
            ExpensesDatabase.DB_NAME,
            9,
            true,
            Migration8To9()
        )
    }
}