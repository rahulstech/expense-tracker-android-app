package dreammaker.android.expensetracker.database;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import static dreammaker.android.expensetracker.database.ExpensesContract.AboutAccountColumns.BALANCE;
import static dreammaker.android.expensetracker.database.ExpensesContract.AboutPersonColumns.DUE_PAYMENT;
import static dreammaker.android.expensetracker.database.ExpensesContract.Tables.ACCOUNTS_TABLE;
import static dreammaker.android.expensetracker.database.ExpensesContract.Tables.PERSONS_TABLE;
import static dreammaker.android.expensetracker.database.ExpensesContract.Tables.TRANSACTIONS_TABLE;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.ACCOUNT_ID;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.AMOUNT;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.PERSON_ID;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.TYPE;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.TYPE_CREDIT;
import static dreammaker.android.expensetracker.database.ExpensesContract.TransactionsColumns.TYPE_DEBIT;
import static dreammaker.android.expensetracker.database.ExpensesContract.Views.ABOUT_ACCOUNTS_VIEW;
import static dreammaker.android.expensetracker.database.ExpensesContract.Views.ABOUT_PERSONS_VIEW;
import static dreammaker.android.expensetracker.database.ExpensesContract.Views.TRANSACTION_DETAILS_VIEW;

@Database(entities = {Account.class, Person.class, Transaction.class,MoneyTransfer.class},
		version = ExpensesDatabase.DB_VERSION)
@TypeConverters({Converters.class})
public abstract class ExpensesDatabase extends RoomDatabase
{
    private static final String TAG = "ExpensesDatabase";

    private static final int VERSION_1 = 1;
    private static final int VERSION_2 = 2;
    private static final int VERSION_3 = 3;
    private static final int VERSION_4 = 4;
    private static final int VERSION_5 = 5;
    private static final int VERSION_6 = 6;

    static final String DB_NAME = "expenses.db3";
    static final int DB_VERSION = VERSION_6;
    
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
                    .addMigrations(MIGRATION_1_2,MIGRATION_2_3,MIGRATION_3_4,MIGRATION_4_5,MIGRATION_5_6)
					.build();
        }
        return mExpensesDB;
    }

    public abstract ExpensesDao getDao();

    public abstract ExpensesBackupDao getBackupDao();

    private static final Migration MIGRATION_1_2 = new Migration(VERSION_1,VERSION_2) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("DROP VIEW IF EXISTS "+TRANSACTION_DETAILS_VIEW);
        }
    };

    private static final Migration MIGRATION_2_3 =  new Migration(VERSION_2,VERSION_3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("DROP VIEW IF EXISTS "+ABOUT_ACCOUNTS_VIEW);
            db.execSQL("DROP VIEW IF EXISTS "+ABOUT_PERSONS_VIEW);
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(VERSION_3,VERSION_4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE "+ACCOUNTS_TABLE+" ADD COLUMN "+BALANCE+" REAL NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE "+PERSONS_TABLE+" ADD COLUMN "+DUE_PAYMENT+" REAL NOT NULL DEFAULT 0");
            db.execSQL("CREATE TRIGGER IF NOT EXISTS update_after_insert_transaction " +
                    "AFTER INSERT ON "+TRANSACTIONS_TABLE+
                    " BEGIN " +
                    "UPDATE accounts SET " +
                    BALANCE+" = "+BALANCE+" + (CASE new."+TYPE+" WHEN "+TYPE_CREDIT+" THEN -new."+AMOUNT+" WHEN "+TYPE_DEBIT+" THEN new."+AMOUNT+" ELSE 0 END) " +
                    "WHERE "+ACCOUNTS_TABLE+"."+ ExpensesContract.AccountsColumns._ID+" = new."+ACCOUNT_ID+";" +
                    "UPDATE "+PERSONS_TABLE+" SET " +
                    DUE_PAYMENT+" = "+DUE_PAYMENT+" + (CASE new."+TYPE+" WHEN "+TYPE_CREDIT+" THEN new."+AMOUNT+" WHEN "+TYPE_DEBIT+" THEN -new."+AMOUNT+" ELSE 0 END) " +
                    "WHERE "+PERSONS_TABLE+"."+ ExpensesContract.PersonsColumns._ID+" = new."+PERSON_ID+";" +
                    "END;");
            db.execSQL("CREATE TRIGGER IF NOT EXISTS update_after_update_transaction " +
                    "AFTER UPDATE ON " +TRANSACTIONS_TABLE+
                    " BEGIN " +
                    "UPDATE "+ACCOUNTS_TABLE+" SET "+
                    BALANCE+" = "+BALANCE+" + (CASE old."+TYPE+" WHEN "+TYPE_CREDIT+" THEN old."+AMOUNT+" WHEN "+TYPE_DEBIT+" THEN -old."+AMOUNT+" ELSE 0 END)  " +
                    "WHERE "+ACCOUNTS_TABLE+"."+ ExpensesContract.AccountsColumns._ID+" = old."+ACCOUNT_ID+";" +
                    "UPDATE " +PERSONS_TABLE+" SET "+
                    DUE_PAYMENT+" = "+DUE_PAYMENT+" + (CASE old."+TYPE+" WHEN "+TYPE_CREDIT+" THEN -old."+AMOUNT+" WHEN "+TYPE_DEBIT+" THEN old."+AMOUNT+" ELSE 0 END)" +
                    "WHERE "+PERSONS_TABLE+"."+ ExpensesContract.PersonsColumns._ID+" = old."+PERSON_ID+";" +
                    "UPDATE " +ACCOUNTS_TABLE+" SET " +
                    BALANCE+" = "+BALANCE+" + (CASE new."+TYPE+" WHEN "+TYPE_CREDIT+" THEN -new."+AMOUNT+" WHEN "+TYPE_DEBIT+" THEN new."+AMOUNT+" ELSE 0 END) " +
                    "WHERE "+ACCOUNTS_TABLE+"."+ ExpensesContract.AccountsColumns._ID+" = new."+ACCOUNT_ID+";" +
                    "UPDATE "+PERSONS_TABLE+" SET " +
                    DUE_PAYMENT+" = "+DUE_PAYMENT+" + (CASE new."+TYPE+" WHEN "+TYPE_CREDIT+" THEN new."+AMOUNT+" WHEN "+TYPE_DEBIT+" THEN -new."+AMOUNT+" ELSE 0 END) " +
                    "WHERE "+PERSONS_TABLE+"."+ ExpensesContract.PersonsColumns._ID+" = new."+PERSON_ID+";" +
                    "END; ");
            db.execSQL("CREATE TRIGGER IF NOT EXISTS update_after_delete_transaction " +
                    "AFTER DELETE ON " +TRANSACTIONS_TABLE+
                    " BEGIN " +
                    "UPDATE " +ACCOUNTS_TABLE+" SET "+
                    BALANCE+" = "+BALANCE+" + (CASE old."+TYPE+" WHEN "+TYPE_CREDIT+" THEN old."+AMOUNT+" WHEN "+TYPE_DEBIT+" THEN -old."+AMOUNT+" ELSE 0 END)  " +
                    "WHERE "+ACCOUNTS_TABLE+"."+ ExpensesContract.AccountsColumns._ID+" = old."+ACCOUNT_ID+";" +
                    "UPDATE " +PERSONS_TABLE+" SET "+
                    DUE_PAYMENT+" = "+DUE_PAYMENT+" + (CASE old."+TYPE+" WHEN "+TYPE_CREDIT+" THEN -old."+AMOUNT+" WHEN "+TYPE_DEBIT+" THEN old."+AMOUNT+" ELSE 0 END)" +
                    "WHERE "+PERSONS_TABLE+"."+ ExpensesContract.PersonsColumns._ID+" = old."+PERSON_ID+";" +
                    "END; ");
            recalculate_account_balance_and_person_due(db);
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(VERSION_4,VERSION_5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("DROP TRIGGER IF EXISTS update_after_insert_transaction;");
            db.execSQL("DROP TRIGGER IF EXISTS update_after_update_transaction;");
            db.execSQL("DROP TRIGGER IF EXISTS update_after_delete_transaction;");
            recalculate_account_balance_and_person_due(db);
        }
    };

    private static final Migration MIGRATION_5_6 = new Migration(VERSION_5,VERSION_6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `money_transfers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    " `amount` REAL NOT NULL DEFAULT 0, " +
                    "`when` TEXT NOT NULL, `payee_account_id` INTEGER NOT NULL," +
                    " `payer_account_id` INTEGER NOT NULL, `description` TEXT, " +
                    "FOREIGN KEY(`payee_account_id`) REFERENCES `accounts`(`_id`) ON UPDATE NO ACTION ON DELETE CASCADE," +
                    "FOREIGN KEY(`payer_account_id`) REFERENCES `accounts`(`_id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `money_transfer_payee_account_id_index` ON `money_transfers` (`payee_account_id`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `money_transfer_payer_account_id_index` ON `money_transfers` (`payer_account_id`)");
            db.execSQL("ALTER TABLE `"+TRANSACTIONS_TABLE+"` ADD COLUMN `deleted` INTEGER NOT NULL DEFAULT 0;");
        }
    };

    private static void recalculate_account_balance_and_person_due(@NonNull SupportSQLiteDatabase db) {
        final Cursor cBalances = db.query("SELECT "+ACCOUNT_ID +
                ", SUM(CASE "+TYPE+" WHEN "+TYPE_CREDIT+" THEN -"+AMOUNT+" WHEN "+TYPE_DEBIT+" THEN "+AMOUNT+" ELSE 0 END) AS "+BALANCE+
                " FROM "+TRANSACTIONS_TABLE+" GROUP BY "+ACCOUNT_ID);

        if (null != cBalances) {
            while (cBalances.moveToNext()) {
                final long _id = cBalances.getLong(cBalances.getColumnIndex(ACCOUNT_ID));
                final float balance = cBalances.getFloat(cBalances.getColumnIndex(BALANCE));
                db.execSQL("UPDATE "+ACCOUNTS_TABLE+" SET "+BALANCE+" = ? WHERE "+ ExpensesContract.AccountsColumns._ID+" = ?;",
                        new Object[]{balance,_id});
            }
        }

        final Cursor cDues = db.query("SELECT "+PERSON_ID+
                ", SUM(CASE "+TYPE+" WHEN "+TYPE_CREDIT+" THEN "+AMOUNT+" WHEN "+TYPE_DEBIT+" THEN -"+AMOUNT+" ELSE 0 END) AS "+DUE_PAYMENT+
                " FROM "+TRANSACTIONS_TABLE+" GROUP BY "+PERSON_ID);

        if (null != cDues) {
            while (cDues.moveToNext()) {
                final long _id = cDues.getLong(cDues.getColumnIndex(PERSON_ID));
                final float due = cDues.getFloat(cDues.getColumnIndex(DUE_PAYMENT));
                db.execSQL("UPDATE "+PERSONS_TABLE+" SET "+DUE_PAYMENT+" = ? WHERE "+ ExpensesContract.PersonsColumns._ID+" = ?;",
                        new Object[]{due,_id});
            }
        }
    }
}
