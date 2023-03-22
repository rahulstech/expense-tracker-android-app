package dreammaker.android.expensetracker.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import dreammaker.android.expensetracker.database.ExpensesDatabase;

public class FakeData {
    
    private Context context = null;
    
    public Context getContext() {
        if (null == context) {
            context = ApplicationProvider.getApplicationContext();
        }
        return context;
    }
    
    public ExpensesDatabase getTestDatabase(@Nullable RoomDatabase.Callback callback) {
        return ExpensesDatabase.getTestInstance(getContext(), callback);
    }
    
    public void addFakeData_v6(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("INSERT INTO accounts (_id,account_name,balance) VALUES (1,\"acc 1\",0)");
        db.execSQL("INSERT INTO accounts (_id,account_name,balance) VALUES (2,\"acc 2\",3500)");
        db.execSQL("INSERT INTO accounts (_id,account_name,balance) VALUES (3,\"acc 3\",25000)");

        db.execSQL("INSERT INTO persons (_id,person_name,due) VALUES (1,\"person 1\",0);");
        db.execSQL("INSERT INTO persons (_id,person_name,due) VALUES (2,\"person 2\",-1000);");
        db.execSQL("INSERT INTO persons (_id,person_name,due) VALUES (3,\"person 3\",5000);");
        db.execSQL("INSERT INTO persons (_id,person_name,due) VALUES (4,\"person 4\",-9000);");

        db.execSQL("INSERT INTO transactions (_id,account_id,person_id,amount,type,date,deleted) VALUES (1,1,1,-200,0,\"2021-01-03\",0);");
        db.execSQL("INSERT INTO transactions (_id,account_id,person_id,amount,type,date,deleted) VALUES (2,3,1,-500,0,\"2021-01-03\",0);");
        db.execSQL("INSERT INTO transactions (_id,account_id,person_id,amount,type,date,deleted) VALUES (3,2,1,355,1,\"2021-01-06\",0);");
        db.execSQL("INSERT INTO transactions (_id,account_id,person_id,amount,type,date,deleted) VALUES (4,1,1,500,1,\"2021-01-05\",0);");
        db.execSQL("INSERT INTO transactions (_id,account_id,person_id,amount,type,date,deleted) VALUES (5,3,1,-700,0,\"2021-01-05\",0);");
        db.execSQL("INSERT INTO transactions (_id,account_id,person_id,amount,type,date,deleted) VALUES (6,1,4,-156,0,\"2021-01-06\",0);");
        db.execSQL("INSERT INTO transactions (_id,account_id,person_id,amount,type,date,deleted) VALUES (7,1,3,799,1,\"2021-01-03\",0);");
        db.execSQL("INSERT INTO transactions (_id,account_id,person_id,amount,type,date,deleted) VALUES (8,3,NULL,200,1,\"2021-08-05\",0);");
        db.execSQL("INSERT INTO transactions (_id,account_id,person_id,amount,type,date,deleted) VALUES (9,1,4,800.5,1,\"2021-01-03\",1);");
        db.execSQL("INSERT INTO transactions (_id,account_id,person_id,amount,type,date,deleted) VALUES (10,3,NULL,-300,0,\"2021-08-14\",1);");

        db.execSQL("INSERT INTO money_transfers (id,payer_account_id,payee_account_id,amount,`when`) VALUES (1,3,2,500,\"2021-07-15\");");
        db.execSQL("INSERT INTO money_transfers (id,payer_account_id,payee_account_id,amount,`when`) VALUES (2,2,1,649,\"2021-04-21\");");
    }

    public void addFakeData_v7(@NonNull SupportSQLiteDatabase db) {
        db.execSQL("INSERT INTO accounts (id,name,balance) VALUES (1,\"acc 1\",\"0\")");
        db.execSQL("INSERT INTO accounts (id,name,balance) VALUES (2,\"acc 2\",\"3500\")");
        db.execSQL("INSERT INTO accounts (id,name,balance) VALUES (3,\"acc 3\",\"25000\")");
        db.execSQL("INSERT INTO accounts (id,name,balance) VALUES (4,\"acc 4\",\"-2600\")");

        db.execSQL("INSERT INTO people (id,firstName,amountDue) VALUES (1,\"person 1\",\"200\");");
        db.execSQL("INSERT INTO people (id,firstName,amountBorrow) VALUES (2,\"person 2\",\"1000\");");
        db.execSQL("INSERT INTO people (id,firstName,lastName) VALUES (3,\"person 3 FN\",\"person 3 LN\");");
        db.execSQL("INSERT INTO people (id,firstName,lastName,amountDue) VALUES (4,\"person 4 FN\",\"person 4 LN\",\"-9000\");");
        db.execSQL("INSERT INTO people (id,firstName,amountBorrow) VALUES (5,\"person 5 FN\",\"-150\");");

        db.execSQL("INSERT INTO transaction_histories (id,payerAccountId,payeePersonId,amount,type,date) VALUES (1,1,1,\"200\",\"DUE\",\"2021-01-03\");");
        db.execSQL("INSERT INTO transaction_histories (id,payerAccountId,payeePersonId,amount,type,date) VALUES (2,3,1,\"500\",\"DUE\",\"2021-01-03\");");
        db.execSQL("INSERT INTO transaction_histories (id,payeeAccountId,payerPersonId,amount,type,date) VALUES (3,2,1,\"355\",\"PAY_DUE\",\"2021-01-06\");");
        db.execSQL("INSERT INTO transaction_histories (id,payeeAccountId,payerPersonId,amount,type,date) VALUES (4,1,1,\"500\",\"PAY_DUE\",\"2021-01-05\");");
        db.execSQL("INSERT INTO transaction_histories (id,payerAccountId,payeePersonId,amount,type,date) VALUES (5,3,1,\"700\",\"DUE\",\"2021-01-05\");");
        db.execSQL("INSERT INTO transaction_histories (id,payerAccountId,payeePersonId,amount,type,date) VALUES (6,1,4,\"156\",\"DUE\",\"2021-01-06\");");
        db.execSQL("INSERT INTO transaction_histories (id,payeeAccountId,payerPersonId,amount,type,date) VALUES (7,1,3,\"799\",\"PAY_DUE\",\"2021-01-03\");");
        db.execSQL("INSERT INTO transaction_histories (id,payeeAccountId,amount,type,date) VALUES (8,3,\"200\",\"INCOME\",\"2021-08-05\");");
        db.execSQL("INSERT INTO transaction_histories (id,payeeAccountId,payerPersonId,amount,type,date) VALUES (9,1,4,\"800.5\",\"PAY_DUE\",\"2021-01-03\");");
        db.execSQL("INSERT INTO transaction_histories (id,payerAccountId,amount,type,date) VALUES (10,3,\"300\",\"EXPENSE\",\"2021-08-14\");");
        db.execSQL("INSERT INTO transaction_histories (id,payeeAccountId,payerPersonId,amount,type,date) VALUES (11,1,3,\"500\",\"BORROW\",\"2021-09-06\");");
        db.execSQL("INSERT INTO transaction_histories (id,payerAccountId,payeePersonId,amount,type,date) VALUES (12,1,3,\"500\",\"PAY_BORROW\",\"2021-10-03\");");
        db.execSQL("INSERT INTO transaction_histories (id,payerAccountId,payeeAccountId,amount,type,date) VALUES (13,3,2,500,\"MONEY_TRANSFER\",\"2021-07-15\");");
        db.execSQL("INSERT INTO transaction_histories (id,payerAccountId,payeeAccountId,amount,type,date) VALUES (14,2,1,649,\"MONEY_TRANSFER\",\"2021-04-21\");");
    }
    
}
