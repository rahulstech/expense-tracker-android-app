package dreammaker.android.expensetracker.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import dreammaker.android.expensetracker.database.migration.Migration7To8;
import dreammaker.android.expensetracker.database.model.AccountEntity;
import dreammaker.android.expensetracker.database.model.GroupEntity;
import dreammaker.android.expensetracker.database.model.HistoryEntity;

@Database(entities = {AccountEntity.class, GroupEntity.class, HistoryEntity.class},
		version = ExpensesDatabase.DB_VERSION)
@TypeConverters({Converters.class})
public abstract class ExpensesDatabase extends RoomDatabase implements IExpenseDatabase {

    private static final String TAG = "ExpensesDatabase";

    public static final String DB_NAME = "expenses.db3";
    public static final int DB_VERSION = 8;
    
    private static ExpensesDatabase mExpensesDB;
    private static final Object lock = new Object();
    
    public static ExpensesDatabase getInstance(Context context){
        synchronized (lock) {
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
                                Migrations.INSTANCE.getMIGRATION_6_7(),
                                new Migration7To8()
                        )
                        .build();
            }
            return mExpensesDB;
        }
    }

    @Override
    public void close() {
        super.close();
        synchronized (lock) {
            mExpensesDB = null;
        }
    }
}
