package dreammaker.android.expensetracker.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import dreammaker.android.expensetracker.database.dao.AccountDao;
import dreammaker.android.expensetracker.database.dao.AnalyticsDao;
import dreammaker.android.expensetracker.database.dao.PeopleDao;
import dreammaker.android.expensetracker.database.dao.TransactionHistoryDao;
import dreammaker.android.expensetracker.database.migration.MIGRATION_6_TO_7;
import dreammaker.android.expensetracker.database.model.Account;
import dreammaker.android.expensetracker.database.model.Person;
import dreammaker.android.expensetracker.database.model.TransactionHistory;

@Database(
        entities = {Account.class, Person.class, TransactionHistory.class},
		version = ExpensesDatabase.DB_VERSION)
@TypeConverters({Converters.class})
public abstract class ExpensesDatabase extends RoomDatabase
{
    private static final String TAG = "ExpensesDatabase";

    private static final String DB_NAME = "expenses.db3";

    private static final int VERSION_6 = 6;
    private static final int VERSION_7 = 7;

    public static final int DB_VERSION = VERSION_7;
    
    private static ExpensesDatabase mExpensesDB;
    
    public synchronized static ExpensesDatabase getInstance(Context context){
        if(null == mExpensesDB){
            RoomDatabase.Builder<ExpensesDatabase> builder = Room.databaseBuilder(context.getApplicationContext(), ExpensesDatabase.class, DB_NAME);
            mExpensesDB = initialize(builder,null);
        }
        return mExpensesDB;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static ExpensesDatabase getTestInstance(@NonNull Context context, @Nullable Callback callback) {
        RoomDatabase.Builder<ExpensesDatabase> builder = Room.inMemoryDatabaseBuilder(context,ExpensesDatabase.class)
                .allowMainThreadQueries();
        return initialize(builder,callback);
    }

    private static ExpensesDatabase initialize(@NonNull RoomDatabase.Builder<ExpensesDatabase> builder, @Nullable Callback callback) {
        builder.addCallback(new Callback() {
            @Override
            public void onOpen(@NonNull SupportSQLiteDatabase db) {
                super.onOpen(db);
                db.setForeignKeyConstraintsEnabled(true);
            }
        });
        if (null != callback) builder.addCallback(callback);
        builder.addMigrations(new MIGRATION_6_TO_7());
        return builder.build();
    }

    public ExpensesDao getDao() {return null;}

    public ExpensesBackupDao getBackupDao() { return null; }

    public abstract AccountDao getAccountDao();

    public abstract PeopleDao getPeopleDao();

    public abstract TransactionHistoryDao getTransactionHistoryDao();

    public abstract AnalyticsDao getAnalyticsDao();
}
