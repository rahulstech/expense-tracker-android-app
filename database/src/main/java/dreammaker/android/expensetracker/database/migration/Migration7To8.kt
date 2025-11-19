package dreammaker.android.expensetracker.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration7To8: Migration(7,8) {

    override fun migrate(db: SupportSQLiteDatabase) {

        // create groups tables
        db.execSQL("CREATE TABLE IF NOT EXISTS `groups` (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `name` TEXT NOT NULL, `due` REAL NOT NULL, `lastUsed` TEXT, `totalUsed` INTEGER)")

        // insert all persons
        db.execSQL("INSERT INTO `groups` (`id`,`name`,`due`) SELECT `_id` AS `id`, `person_name` AS `name`, `due` FROM `persons`")

        // drop persons table
        db.execSQL("DROP TABLE IF EXISTS `persons`")

        // rename accounts table
        db.execSQL("ALTER TABLE `accounts` RENAME TO `accounts_old`")

        // create new accounts table
        db.execSQL("CREATE TABLE IF NOT EXISTS `accounts` (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,`name` TEXT NOT NULL,`balance` REAL NOT NULL, `lastUsed` TEXT, `totalUsed` INTEGER)")

        // insert all accounts
        db.execSQL("INSERT INTO `accounts` (`id`,`name`,`balance`) SELECT `_id` AS `id`, `account_name` AS `name`, `balance` FROM `accounts_old`")

        // drop old accounts table
        db.execSQL("DROP TABLE IF EXISTS `accounts_old`")

        // drop the histories view
        db.execSQL("DROP VIEW IF EXISTS `histories`")

        // create histories table
        db.execSQL("CREATE TABLE IF NOT EXISTS `histories` (" +
                "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "`type` TEXT NOT NULL," +
                "`amount` REAL NOT NULL, "+
                "`date` TEXT NOT NULL, " +
                "`primaryAccountId` INTEGER REFERENCES `accounts`(`id`) ON DELETE SET NULL ON UPDATE NO ACTION, " +
                "`secondaryAccountId` INTEGER REFERENCES `accounts`(`id`) ON DELETE SET NULL ON UPDATE NO ACTION, " +
                "`groupId` INTEGER REFERENCES `groups`(`id`) ON DELETE SET NULL ON UPDATE NO ACTION, " +
                "`note` TEXT" +
                ")")

        // create indices of histories
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_histories_primaryAccountId` ON `histories`(`primaryAccountId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_histories_secondaryAccountId` ON `histories`(`secondaryAccountId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_histories_groupId` ON `histories`(`groupId`)")

        // add transactions into histories
        db.execSQL("INSERT INTO `histories` (`type`,`date`,`primaryAccountId`,`groupId`, `amount`, `note`)" +
                " SELECT " +
                "CASE `type` WHEN 0 THEN 'DEBIT' ELSE 'CREDIT' END AS `type`, `date`, " +
                "`account_id` AS `primaryAccountId`, `person_id` AS `groupId`, `amount`, `description` AS `note`" +
                "FROM `transactions` WHERE `deleted` = 0 AND `person_id` IS NULL")

        // add money_transfers into histories
        db.execSQL("INSERT INTO `histories` (`type`,`date`,`primaryAccountId`,`secondaryAccountId`, `amount`, `note`) " +
                " SELECT 'TRANSFER' AS `type`, `when` AS `date`, " +
                " `payer_account_id` AS `primaryAccountId`, `payee_account_id` As `secondaryAccountId`," +
                " `amount`, `description` AS `note` FROM `money_transfers`")

        // drop transactions and money_transfers tables
        db.execSQL("DROP TABLE IF EXISTS `transactions`")
        db.execSQL("DROP TABLE IF EXISTS `money_transfers`")
    }
}