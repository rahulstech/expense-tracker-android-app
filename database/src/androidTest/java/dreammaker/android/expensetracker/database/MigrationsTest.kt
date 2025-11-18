package dreammaker.android.expensetracker.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dreammaker.android.expensetracker.database.Migrations.MIGRATION_6_7
import dreammaker.android.expensetracker.database.Migrations.MIGRATION_7_8
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationsTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpensesDatabase::class.java.canonicalName!!,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun testMigration6To7() {
        helper.createDatabase(ExpensesDatabase.DB_NAME, 6).use {
            val wrapper = SQLiteStatementExecutorWrapperBuilder().buildFromSupportSQLiteDatabase(it)
            FAKE_DATA_6.addFakeData(wrapper)
        }

        helper.runMigrationsAndValidate(ExpensesDatabase.DB_NAME, 7, true, MIGRATION_6_7).use {
            it.query("SELECT COUNT(*) FROM `sqlite_master` WHERE `type` = 'view' AND `name` = 'histories'").use { cursor ->
                cursor.moveToFirst()
                assert(cursor.getInt(0) == 1)
            }
        }
    }

    @Test
    fun testMigration7to8() {
        helper.createDatabase(ExpensesDatabase.DB_NAME, 7).use {
            val wrapper = SQLiteStatementExecutorWrapperBuilder().buildFromSupportSQLiteDatabase(it)
            FAKE_DATA_7.addFakeData(wrapper)
        }

        helper.runMigrationsAndValidate(ExpensesDatabase.DB_NAME, 8, true, MIGRATION_7_8).use { db ->
            db.query("SELECT COUNT(*) FROM `sqlite_master` WHERE `type` = 'table' AND `name` = 'histories'").use { cursor ->
                cursor.moveToFirst()
                assert(cursor.getInt(0) == 1)
            }
        }
    }
}