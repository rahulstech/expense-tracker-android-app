package dreammaker.android.expensetracker.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.BuiltInTypeConverters;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import dreammaker.android.expensetracker.database.dao.AccountDao;
import dreammaker.android.expensetracker.database.dao.AnalyticsDao;
import dreammaker.android.expensetracker.database.dao.PersonDao;
import dreammaker.android.expensetracker.database.dao.TransactionHistoryDao;
import dreammaker.android.expensetracker.database.entity.Account;
import dreammaker.android.expensetracker.database.entity.Person;
import dreammaker.android.expensetracker.database.entity.TransactionHistory;
import dreammaker.android.expensetracker.database.migration.MIGRATION_6_TO_7;
import dreammaker.android.expensetracker.database.view.AssetLiabilitySummary;
import dreammaker.android.expensetracker.database.view.AccountsSummaryView;
import dreammaker.android.expensetracker.database.view.DailyExpenseView;
import dreammaker.android.expensetracker.database.view.MonthlyExpenseView;
import dreammaker.android.expensetracker.database.view.PeopleSummaryView;

@Database(
        entities = {Account.class, Person.class, TransactionHistory.class},
        views = {AccountsSummaryView.class, PeopleSummaryView.class, AssetLiabilitySummary.class,
                DailyExpenseView.class, MonthlyExpenseView.class},
		version = ExpensesDatabase.DB_VERSION)
@TypeConverters(value = Converters.class, builtInTypeConverters = @BuiltInTypeConverters(enums = BuiltInTypeConverters.State.ENABLED))
@SuppressWarnings({"unused"})
public abstract class ExpensesDatabase extends RoomDatabase
{
    private static final String TAG = "ExpensesDatabase";

    private static final String DB_NAME = "expenses.db3";

    public static final int DB_VERSION = 7;
    
    private static ExpensesDatabase mExpensesDB;
    
    public synchronized static ExpensesDatabase getInstance(Context context){
        if(null == mExpensesDB){
            RoomDatabase.Builder<ExpensesDatabase> builder = Room.databaseBuilder(context.getApplicationContext(), ExpensesDatabase.class, DB_NAME);
            mExpensesDB = initialize(builder,null);
        }
        return mExpensesDB;
    }

    @Deprecated
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

    public abstract AccountDao getAccountDao();

    public abstract PersonDao getPersonDao();

    public abstract TransactionHistoryDao getTransactionHistoryDao();

    public abstract AnalyticsDao getAnalyticsDao();
}
