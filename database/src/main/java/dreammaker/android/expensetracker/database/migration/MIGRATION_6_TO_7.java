package dreammaker.android.expensetracker.database.migration;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@SuppressWarnings("unused")
public class MIGRATION_6_TO_7 extends Migration {

    public MIGRATION_6_TO_7() {
        super(6, 7);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {

        // migrate accounts table
        db.execSQL("ALTER TABLE `accounts` RENAME TO `accounts_old`");
        db.execSQL("CREATE TABLE `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, " +
                "`balance` TEXT NOT NULL)");
        db.execSQL("INSERT INTO `accounts` (`id`,`name`,`balance`) SELECT `_id` AS `id`," +
                " `account_name` AS `name`, `balance` AS `balance`" +
                " FROM `accounts_old`");

        // migrate persons table
        db.execSQL("CREATE TABLE `people` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT, " +
                " `due` TEXT NOT NULL, `borrow` TEXT NOT NULL)");
        db.execSQL("INSERT INTO `people` (`id`,`firstName`,`due`,`borrow`) SELECT `_id` AS `id`, `person_name` AS `firstName`, " +
                " CASE WHEN `due` >= 0 THEN ABS(`due`) ELSE \"0.00\" END AS `due`, CASE WHEN `due` < 0 THEN ABS(`due`) ELSE \"0.00\" END AS `borrow` FROM `persons`");

        // migrate transactions table
        db.execSQL("CREATE TABLE `transaction_histories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " `payerPersonId` INTEGER,`payeePersonId` INTEGER, `payerAccountId` INTEGER, `payeeAccountId` INTEGER, " +
                "`amount` TEXT NOT NULL,`type` TEXT NOT NULL,`when` TEXT NOT NULL," +
                "`description` TEXT)");
        db.execSQL("INSERT INTO `transaction_histories` (`payeeAccountId`,`amount`,`type`,`when`,`description`)" +
                " SELECT `account_id` AS `payeeAccountId`, ('\"'||ABS(`amount`)||'\"') AS `amount`, " +
                " \"INCOME\" AS `type`, `date` AS `when`, `description` " +
                "FROM `transactions` WHERE `deleted` = 0 AND `person_id` IS NULL AND `type` = 1 ORDER BY `date` DESC");
        db.execSQL("INSERT INTO `transaction_histories` (`payerAccountId`,`amount`,`type`,`when`,`description`)" +
                " SELECT `account_id` AS `payeeAccountId`, ('\"'||ABS(`amount`)||'\"') AS `amount`," +
                " \"EXPENSE\" AS `type`, `date` AS `when`, `description` " +
                "FROM `transactions` WHERE `deleted` = 0 AND `person_id` IS NULL AND `type` = 0 ORDER BY `date` DESC");
        db.execSQL("INSERT INTO `transaction_histories` (`payerAccountId`, `payeePersonId`,`amount`,`type`,`when`,`description`)" +
                " SELECT `account_id` AS `payerAccountId`, `person_id` AS `payeePersonId`, ('\"'||ABS(`amount`)||'\"') AS `amount`, " +
                " \"DUE\" AS `type`, `date` AS `when`, `description` " +
                "FROM `transactions` WHERE `deleted` = 0 AND `person_id` IS NOT NULL AND `type` = 0 ORDER BY `date` DESC");
        db.execSQL("INSERT INTO `transaction_histories` (`payeeAccountId`, `payerPersonId`,`amount`,`type`,`when`,`description`)" +
                " SELECT `account_id` AS `payeeAccountId`, `person_id` AS `payerPersonId`, ABS(`amount`) AS `amount`, " +
                " \"PAY_DUE\" AS `type`, `date` AS `when`, `description` " +
                "FROM `transactions` WHERE `deleted` = 0 AND `person_id` IS NOT NULL AND `type` = 1 ORDER BY `date` DESC");

        // merge transactions and money_transfers table
        db.execSQL("INSERT INTO `transaction_histories` (`payeeAccountId`, `payerAccountId`,`amount`,`type`,`when`,`description`)" +
                " SELECT `payee_account_id` AS `payeeAccountId`, `payer_account_id` AS `payerAccountId`, ('\"' || ABS(`amount`) || '\"') AS `amount`, " +
                " \"MONEY_TRANSFER\" AS `type`, `when`, `description` " +
                "FROM `money_transfers` ORDER BY `when` DESC");

        // drop unnecessary tables
        db.execSQL("DROP TABLE `accounts_old`");
        db.execSQL("DROP TABLE `persons`");
        db.execSQL("DROP TABLE `transactions`");
        db.execSQL("DROP TABLE `money_transfers`");

        // create views
        db.execSQL("CREATE VIEW `accounts_summary_view` AS SELECT " +
                "SUM(CASE WHEN CAST(`balance` AS REAL) > 0 THEN 1 ELSE 0 END) AS `totalPositiveAccounts`, " +
                "SUM(CASE WHEN CAST(`balance` AS REAL) > 0 THEN `balance` ELSE 0 END) AS `totalPositiveBalance`,  " +
                "SUM(CASE WHEN CAST(`balance` AS REAL) < 0 THEN 1 ELSE 0 END) AS `totalNegativeAccounts`, " +
                "SUM(CASE WHEN CAST(`balance` AS REAL) < 0 THEN `balance` ELSE 0 END) AS `totalNegativeBalance` " +
                "FROM `accounts`");
        db.execSQL("CREATE VIEW `people_summary_view` AS SELECT "+
                " SUM(CASE WHEN CAST(`due` AS REAL) > 0 THEN 1 ELSE 0 END) AS `totalPositiveDuePeople`,"+
                " SUM(CASE WHEN CAST(`due` AS REAL) > 0 THEN `due` ELSE 0 END) AS `totalPositiveDue`,"+
                " SUM(CASE WHEN CAST(`due` AS REAL) < 0 THEN 1 ELSE 0 END) AS `totalNegativeDuePeople`,"+
                " SUM(CASE WHEN CAST(`due` AS REAL) < 0 THEN `due` ELSE 0 END) AS `totalNegativeDue`,"+
                " SUM(CASE WHEN CAST(`borrow` AS REAL) > 0 THEN 1 ELSE 0 END) AS `totalPositiveBorrowPeople`,"+
                " SUM(CASE WHEN CAST(`borrow` AS REAL) > 0 THEN `borrow` ELSE 0 END) AS `totalPositiveBorrow`, "+
                " SUM(CASE WHEN CAST(`borrow` AS REAL) < 0 THEN 1 ELSE 0 END) AS `totalNegativeBorrowPeople`, "+
                " SUM(CASE WHEN CAST(`borrow` AS REAL) < 0 THEN `borrow` ELSE 0 END) AS `totalNegativeBorrow`"+
                " FROM `people`");
        db.execSQL("CREATE VIEW `asset_liability_summary_view` AS SELECT * FROM (SELECT * FROM `accounts_summary_view`), (SELECT * FROM `people_summary_view`), " +
                "(SELECT COUNT(`id`) AS `totalAccounts` FROM `accounts`), (SELECT COUNT(`id`) AS `totalPeople` FROM `people`)");
    }
}
