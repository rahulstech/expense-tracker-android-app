package dreammaker.android.expensetracker.database.migration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.util.FakeData;

@RunWith(AndroidJUnit4.class)
public class TestMigrations {

    final String TEST_DB_NAME = "expense-tracker-test.db3";

    @Rule
    @SuppressWarnings("deprecation")
    public MigrationTestHelper migrationHelper = new MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            ExpensesDatabase.class.getCanonicalName()
    );

    @Test
    public void migration6To7() throws Exception {
        SupportSQLiteDatabase db;

        db = migrationHelper.createDatabase(TEST_DB_NAME,6);
        FakeData.addDataForVersion(db,6);
        db.close();

        db = migrationHelper.runMigrationsAndValidate(TEST_DB_NAME,7,
                true, new MIGRATION_6_TO_7());
        db.close();
    }
}
