package dreammaker.android.expensetracker.database.migration;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class MIGRATION_6_TO_7 extends Migration {

    public MIGRATION_6_TO_7() {
        super(6, 7);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase db) {

        // migrate accounts table

        db.execSQL("ALTER TABLE `accounts` RENAME TO `accounts_old`");

        db.execSQL("CREATE TABLE `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, " +
                "`balance` TEXT NOT NULL DEFAULT '0')");

        db.execSQL("INSERT INTO `accounts` (`id`,`name`,`balance`) SELECT `_id` AS `id`," +
                " `account_name` AS `name`, `balance` AS `balance`" +
                " FROM `accounts_old`");

        // migrate persons table

        db.execSQL("CREATE TABLE `people` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT, " +
                " `amountDue` TEXT NOT NULL DEFAULT '0', `amountBorrow` TEXT NOT NULL DEFAULT '0')");

        db.execSQL("INSERT INTO `people` (`id`,`firstName`,`amountDue`,`amountBorrow`) SELECT `_id` AS `id`, `person_name` AS `firstName`, " +
                " CASE WHEN `due` >= 0 THEN ABS(`due`) ELSE 0 END AS `amountDue`, CASE WHEN `due` < 0 THEN ABS(`due`) ELSE 0 END AS `amountBorrow` FROM `persons`");

        // migrate transactions table

        db.execSQL("CREATE TABLE `transaction_histories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " `payerPersonId` INTEGER,`payeePersonId` INTEGER, `payerAccountId` INTEGER, `payeeAccountId` INTEGER, " +
                "`amount` TEXT NOT NULL DEFAULT '0',`type` TEXT NOT NULL,`date` TEXT NOT NULL," +
                "`description` TEXT," +
                "FOREIGN KEY(`payerPersonId`) REFERENCES `people`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE," +
                "FOREIGN KEY(`payeePersonId`) REFERENCES `people`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE," +
                "FOREIGN KEY(`payerAccountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE," +
                "FOREIGN KEY(`payeeAccountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)");

        db.execSQL("CREATE INDEX IF NOT EXISTS `transaction_history_payeePersonId_index` ON `transaction_histories` (`payeePersonId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `transaction_history_payerPersonId_index` ON `transaction_histories` (`payerPersonId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `transaction_history_payeeAccountId_index` ON `transaction_histories` (`payeeAccountId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `transaction_history_payerAccountId_index` ON `transaction_histories` (`payerAccountId`)");


        db.execSQL("INSERT INTO `transaction_histories` (`payeeAccountId`,`amount`,`type`,`date`,`description`)" +
                " SELECT `account_id` AS `payeeAccountId`, ABS(`amount`) AS `amount`, " +
                " \"INCOME\" AS `type`, `date`, `description` " +
                "FROM `transactions` WHERE `deleted` = 0 AND `person_id` IS NULL AND `type` = 1 ORDER BY `date` DESC");

        db.execSQL("INSERT INTO `transaction_histories` (`payerAccountId`,`amount`,`type`,`date`,`description`)" +
                " SELECT `account_id` AS `payeeAccountId`, ABS(`amount`) AS `amount`," +
                " \"EXPENSE\" AS `type`, `date`, `description` " +
                "FROM `transactions` WHERE `deleted` = 0 AND `person_id` IS NULL AND `type` = 0 ORDER BY `date` DESC");

        db.execSQL("INSERT INTO `transaction_histories` (`payerAccountId`, `payeePersonId`,`amount`,`type`,`date`,`description`)" +
                " SELECT `account_id` AS `payerAccountId`, `person_id` AS `payeePersonId`, ABS(`amount`) AS `amount`, " +
                " \"DUE\" AS `type`, `date`, `description` " +
                "FROM `transactions` WHERE `deleted` = 0 AND `person_id` IS NOT NULL AND `type` = 0 ORDER BY `date` DESC");

        db.execSQL("INSERT INTO `transaction_histories` (`payeeAccountId`, `payerPersonId`,`amount`,`type`,`date`,`description`)" +
                " SELECT `account_id` AS `payeeAccountId`, `person_id` AS `payerPersonId`, ABS(`amount`) AS `amount`, " +
                " \"PAY_DUE\" AS `type`, `date`, `description` " +
                "FROM `transactions` WHERE `deleted` = 0 AND `person_id` IS NOT NULL AND `type` = 1 ORDER BY `date` DESC");

        // merge transactions and money_transfers table

        db.execSQL("INSERT INTO `transaction_histories` (`payeeAccountId`, `payerAccountId`,`amount`,`type`,`date`,`description`)" +
                " SELECT `payee_account_id` AS `payeeAccountId`, `payer_account_id` AS `payerAccountId`, ABS(`amount`) AS `amount`, " +
                " \"MONEY_TRANSFER\" AS `type`, `when` AS `date`, `description` " +
                "FROM `money_transfers` ORDER BY `date` DESC");

        // drop unnecessary tables

        db.execSQL("DROP TABLE `accounts_old`");
        db.execSQL("DROP TABLE `persons`");
        db.execSQL("DROP TABLE `transactions`");
        db.execSQL("DROP TABLE `money_transfers`");
    }
}
