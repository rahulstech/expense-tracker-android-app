package dreammaker.android.expensetracker.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Account.class, Person.class, Transaction.class,MoneyTransfer.class},
        views = { History.class },
		version = ExpensesDatabase.DB_VERSION)
@TypeConverters({Converters.class})
public abstract class ExpensesDatabase extends RoomDatabase
{
    private static final String TAG = "ExpensesDatabase";

    static final String DB_NAME = "expenses.db3";
    static final int DB_VERSION = 7;
    
    private static ExpensesDatabase mExpensesDB;
    
    public synchronized static ExpensesDatabase getInstance(Context context){
        if(null == mExpensesDB){
            mExpensesDB = Room.databaseBuilder(context.getApplicationContext(), ExpensesDatabase.class, DB_NAME)
                    .addCallback(new Callback() {
                        @Override
                        public void onOpen(@NonNull SupportSQLiteDatabase db) {
                            super.onOpen(db);
                            db.setForeignKeyConstraintsEnabled(true);
                        }
                    })
                    .addMigrations(
                            Migrations.INSTANCE.getMIGRATION_6_7()
                    )
					.build();
        }
        return mExpensesDB;
    }

    public abstract ExpensesDao getDao();

    public abstract ExpensesBackupDao getBackupDao();

    public abstract HistoryDao getHistoryDao();

    public abstract AccountDao getAccountDao();

    public abstract GroupDao getGroupDao();
}
