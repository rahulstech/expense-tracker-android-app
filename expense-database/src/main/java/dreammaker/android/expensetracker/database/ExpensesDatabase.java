package dreammaker.android.expensetracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.room.BuiltInTypeConverters;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import dreammaker.android.expensetracker.util.Date;

@Database(entities = {Account.class, Person.class, Transaction.class,MoneyTransfer.class,Budget.class},
		version = ExpensesDatabase.DB_VERSION)
@TypeConverters(value = {Converters.class}, builtInTypeConverters = @BuiltInTypeConverters(enums = BuiltInTypeConverters.State.ENABLED))
public abstract class ExpensesDatabase extends RoomDatabase
{
    private static final String TAG = "ExpensesDatabase";

    private static final int VERSION_1 = 1;
    private static final int VERSION_2 = 2;
    private static final int VERSION_3 = 3;
    private static final int VERSION_4 = 4;
    private static final int VERSION_5 = 5;
    private static final int VERSION_6 = 6;
    private static final int VERSION_7 = 7;

    public static final String DB_NAME = "expenses.db3";
    public static final int DB_VERSION = VERSION_7;
    
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
                    .addMigrations(MIGRATION_1_2,MIGRATION_2_3,MIGRATION_3_4,MIGRATION_4_5,MIGRATION_5_6,MIGRATION_6_7)
					.build();
        }
        return mExpensesDB;
    }

    public abstract ExpensesDao getDao();

    public abstract ExpensesBackupDao getBackupDao();

    public abstract AccountDao getAccountDao();

    public abstract PersonDao getPersonDao();

    public abstract TransactionsDao getTransactionDao();

    public abstract MoneyTransferDao getMoneyTransferDao();

    public abstract RawDao getRawDao();

    public abstract ReportDao getReportDao();

    private static final Migration MIGRATION_1_2 = new Migration(VERSION_1,VERSION_2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("DROP VIEW IF EXISTS `transaction_details`");
        }
    };

    private static final Migration MIGRATION_2_3 =  new Migration(VERSION_2,VERSION_3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("DROP VIEW IF EXISTS about_accounts");
            db.execSQL("DROP VIEW IF EXISTS about_persons");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(VERSION_3,VERSION_4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE `accounts` ADD COLUMN `balance` REAL NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE `persons` ADD COLUMN `due` REAL NOT NULL DEFAULT 0");
            db.execSQL("CREATE TRIGGER IF NOT EXISTS update_after_insert_transaction " +
                    "AFTER INSERT ON `transactions` BEGIN" +
                    " UPDATE accounts SET" +
                    " `balance` = `balance` + (CASE new.`type` WHEN 0 THEN -new.`amount` ELSE new.`amount` END)" +
                    " WHERE `accounts`.`_id` = new.`account_id`;" +
                    " UPDATE `persons` SET " +
                    " `due` = `due` + (CASE new.`type` WHEN 0 THEN new.`amount` ELSE -new.`amount` END)" +
                    " WHERE `persons`.`_id` = new.`person_id`;" +
                    "END;");
            db.execSQL("CREATE TRIGGER IF NOT EXISTS update_after_update_transaction " +
                    "AFTER UPDATE ON `transactions` BEGIN " +
                    "UPDATE `accounts` SET "+
                    "`balance` = `balance` + (CASE old.`type` WHEN 0 THEN old.`amount` ELSE -old.`amount` END)  " +
                    "WHERE `accounts`.`_id` = old.`account_id`;" +
                    "UPDATE `persons` SET "+
                    "`due` = `due` + (CASE old.`type` WHEN 0 THEN -old.`amount` ELSE old.`amount` END)" +
                    "WHERE `persons`.`_id` = old.`person_id`;" +
                    "UPDATE `accounts` SET " +
                    "`balance` = `balance` + (CASE new.`type` WHEN 0 THEN -new.`amount` ELSE new.`amount` END) " +
                    "WHERE `accounts`.`_id` = new.`account_id`;" +
                    "UPDATE `persons` SET " +
                    "`due` = `due` + (CASE new.`type` WHEN 0 THEN new.`amount` ELSE -new.`amount` END) " +
                    "WHERE `persons`.`_id` = new.`person_id`;" +
                    "END; ");
            db.execSQL("CREATE TRIGGER IF NOT EXISTS update_after_delete_transaction " +
                    "AFTER DELETE ON `transactions` BEGIN " +
                    "UPDATE `accounts` SET "+
                    "`balance` = `balance` + (CASE old.`type` WHEN 0 THEN old.`amount` ELSE -old.`amount` END)  " +
                    "WHERE `accounts`.`_id` = old.`account_id`;" +
                    "UPDATE `persons` SET "+
                    "`due` = `due` + (CASE old.`type` WHEN 0 THEN -old.`amount` ELSE old.`amount` END)" +
                    "WHERE `persons`.`_id` = old.`person_id`;" +
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
                    " `payer_account_id` INTEGER NOT NULL, `description` TEXT," +
                    " FOREIGN KEY(`payee_account_id`) REFERENCES `accounts`(`_id`) ON UPDATE NO ACTION ON DELETE CASCADE," +
                    " FOREIGN KEY(`payer_account_id`) REFERENCES `accounts`(`_id`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `money_transfer_payee_account_id_index` ON `money_transfers` (`payee_account_id`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `money_transfer_payer_account_id_index` ON `money_transfers` (`payer_account_id`)");
            db.execSQL("ALTER TABLE `transactions` ADD COLUMN `deleted` INTEGER NOT NULL DEFAULT 0;");
        }
    };

    private static final Migration MIGRATION_6_7 = new Migration(VERSION_6,VERSION_7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE `persons` ADD COLUMN `included` INTEGER NOT NULL DEFAULT 1;");

            ContentValues values1 = new ContentValues();
            values1.put("person_name","None");
            values1.put("included","0");
            final long personId = db.insert("persons",SQLiteDatabase.CONFLICT_ROLLBACK,values1);

            db.execSQL("CREATE TABLE IF NOT EXISTS tmp_transactions (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    " account_id INTEGER NOT NULL REFERENCES accounts(_id) ON DELETE CASCADE," +
                    " person_id INTEGER NOT NULL REFERENCES persons(_id) ON DELETE CASCADE," +
                    " amount REAL NOT NULL DEFAULT 0, date TEXT NOT NULL," +
                    " deleted INTEGER NOT NULL DEFAULT 0," +
                    " description TEXT);");

            db.execSQL("INSERT INTO tmp_transactions SELECT " +
                    "_id, account_id, date, (CASE type WHEN 0 THEN -1*amount ELSE amount END) AS amount, description" +
                    " CASE WHEN person_id IS NULL THEN "+personId+" ELSE person_id END AS person_id" +
                    " FROM transactions WHERE deleted = 0;");

            db.execSQL("DROP TABLE IF EXISTS transactions;");

            db.execSQL("ALTER TABLE tmp_transactions RENAME TO transactions;");

            db.execSQL("CREATE INDEX IF NOT EXISTS `transactions_account_id_index` ON `transactions` (`account_id`)");

            db.execSQL("CREATE INDEX IF NOT EXISTS `transactions_person_id_index` ON `transactions` (`person_id`)");

            db.execSQL("CREATE TABLE IF NOT EXISTS budgets (budgetId INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "dateStart TEXT NOT NULL, dateEnd TEXT NOT NULL, " +
                    "targetAmount REAL NOT NULL DEFAULT 0, achievedAmount REAL NOT NULL DEFAULT 0, " +
                    "entityId INTEGER NOT NULL, entityType TEXT NOT NULL, remark TEXT);");
        }
    };

    private static void recalculate_account_balance_and_person_due(@NonNull SupportSQLiteDatabase db) {
        // TODO: optimize recalculate_account_balance_and_person_due
        final Cursor cBalances = db.query("SELECT account_id, SUM(`amount`) AS balance FROM transactions GROUP BY account_id");

        if (null != cBalances) {
            while (cBalances.moveToNext()) {
                final long _id = cBalances.getLong(0);
                final float balance = cBalances.getFloat(1);
                db.execSQL("UPDATE `accounts` SET `balance` = ? WHERE `_id` = ?;",
                        new Object[]{balance,_id});
            }
        }

        final Cursor cDues = db.query("SELECT person_id, SUM(`amount`) AS due FROM transactions GROUP BY person_id");

        if (null != cDues) {
            while (cDues.moveToNext()) {
                final long _id = cDues.getLong(0);
                final float due = cDues.getFloat(1);
                db.execSQL("UPDATE `persons` SET `due` = ? WHERE `_id` = ?;",
                        new Object[]{due,_id});
            }
        }
    }
}
