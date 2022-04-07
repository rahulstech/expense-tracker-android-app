package dreammaker.android.expensetracker.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class FakeValueProvider {

    public static ExpensesDatabase createExpenseDbAndFakeDate(@NonNull Context context) {
        return Room.inMemoryDatabaseBuilder(context,ExpensesDatabase.class)
                .allowMainThreadQueries()
                .addCallback(new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                addExpenseDbFakeValues(db);
            }

            @Override
            public void onOpen(@NonNull SupportSQLiteDatabase db) {
                db.setForeignKeyConstraintsEnabled(true);
            }
        }).build();
    }

    public static void addExpenseDbFakeValues(@NonNull SupportSQLiteDatabase db) {
        addAccounts(db);
        addPeople(db);
        addTransactions(db);
        addMoneyTransfers(db);
        addBudgets(db);
    }

    public static void addAccounts(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("INSERT INTO accounts (_id,account_name,balance) VALUES (1,\"acc 1\",0)");
        db.execSQL("INSERT INTO accounts (_id,account_name,balance) VALUES (2,\"acc 2\",3500)");
        db.execSQL("INSERT INTO accounts (_id,account_name,balance) VALUES (3,\"acc 3\",25000)");
    }

    public static void addPeople(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("INSERT INTO persons (_id,person_name,due) VALUES (1,\"person 1\",0);");
        db.execSQL("INSERT INTO persons (_id,person_name,due) VALUES (2,\"person 2\",-1000);");
        db.execSQL("INSERT INTO persons (_id,person_name,due) VALUES (3,\"person 3\",5000);");
        db.execSQL("INSERT INTO persons (_id,person_name,due) VALUES (4,\"person 4\",-9000);");
        db.execSQL("INSERT INTO persons (_id,person_name,due,included) VALUES (5,\"person 5\",-200,0);");
        db.execSQL("INSERT INTO persons (_id,person_name,due,included) VALUES (6,\"person 6\",1000,0);");
    }

    public static void addTransactions(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("INSERT INTO transactions (_id,account_id,person_id,amount,date) VALUES (1,1,1,-200,\"2021-01-03\");");
        db.execSQL("INSERT INTO transactions (_id,account_id,person_id,amount,date) VALUES (2,3,1,-500,\"2021-01-03\");");
        db.execSQL("INSERT INTO transactions (_id,account_id,person_id,amount,date) VALUES (3,2,1,355,\"2021-01-06\");");
        db.execSQL("INSERT INTO transactions (_id,account_id,person_id,amount,date) VALUES (4,1,1,+500,\"2021-01-05\");");
        db.execSQL("INSERT INTO transactions (_id,account_id,person_id,amount,date) VALUES (5,3,1,-700,\"2021-01-05\");");
        db.execSQL("INSERT INTO transactions (_id,account_id,person_id,amount,date) VALUES (6,1,4,-156,\"2021-01-06\");");
        db.execSQL("INSERT INTO transactions (_id,account_id,person_id,amount,date) VALUES (7,1,3,+799,\"2021-01-03\");");
    }

    public static void addMoneyTransfers(@NonNull SupportSQLiteDatabase db) {

    }

    public static void addBudgets(@NonNull SupportSQLiteDatabase db) {

    }
}
