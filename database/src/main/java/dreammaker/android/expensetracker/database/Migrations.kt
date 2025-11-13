package dreammaker.android.expensetracker.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {

    val MIGRATION_6_7: Migration = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE VIEW IF NOT EXISTS `histories` AS" +
                    " SELECT `_id` AS `id`," +
                    " CASE WHEN `type` = 0 THEN 'DEBIT'" +
                    " ELSE 'CREDIT' END AS `type`," +
                    " `account_id` AS `primaryAccountId`," +
                    " NULL AS `secondaryAccountId`," +
                    " `person_id` AS `groupId`,"+
                    " `amount`, `date`, `description` AS `note`" +
                    " FROM `transactions` WHERE `deleted` = 0" +
                    " UNION " +
                    " SELECT `id`, 'TRANSFER' AS `type`," +
                    " `payer_account_id` AS `primaryAccountId`, `payee_account_id` As `secondaryAccountId`," +
                    " NULL AS `groupId`, `amount`, `when` AS `date`, `description` AS `note` " +
                    " FROM `money_transfers`")
        }
    }

    val MIGRATION_7_8: Migration = object: Migration(7,8) {
        override fun migrate(db: SupportSQLiteDatabase) {

            // create groups tables
            db.execSQL("CREATE TABLE IF NOT EXISTS `groups` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT NOT NULL, `due` REAL NOT NULL)")

            // insert all persons
            db.execSQL("INSERT INTO `grups` (`id`,`name`,`due`) SELECT `_id` AS `id`, `person_name` AS `name`, `due` FROM `persons`")

            // drop persons table
            db.execSQL("DROP TABLE IF EXISTS `persons`")

            // rename accounts table
            db.execSQL("ALTER TABLE `accounts` RENAME TO `accounts_old`")

            // create new accounts table
            db.execSQL("CREATE TABLE IF NOT EXISTS `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `name` TEXT NOT NULL, `balance` REAL NOT NULL)")

            // insert all accounts
            db.execSQL("INSERT INTO `grups` (`id`,`name`,`balance`) SELECT `_id` AS `id`, `account_name` AS `name`, `due` FROM `accounts_old`")

            // drop old accounts table
            db.execSQL("DROP TABLE IF EXISTS `accounts_old`")

            // drop the histories view
            db.execSQL("DROP VIEW IF EXISTS `histories`")

            // create histories table
            db.execSQL("CREATE TABLE IF NOT EXISTS `histories` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "`type` TEXT NOT NULL," +
                    "`date` TEXT NOT NULL, " +
                    "`primaryAccountId` INTEGER REFERENCES `accounts`(`_id`), " +
                    "`secondaryAccountId` INTEGER REFERENCES `accounts`(`_id`), " +
                    "`groupId` INTEGER REFERENCES `groups`(`_id`), " +
                    "`note` TEXT" +
                    ")")

            // create group_histories table
            db.execSQL("CREATE TABLE IF NOT EXISTS `group_histories` (" +
                    "`groupid` INTEGER NOT NULL REFERENCES `groups`(`id`) ON DELETE CASCADE, " +
                    "`historyId` INTEGER NOT NULL REFERENCES `histories`(`id`), " +
                    "PRIMARY KEY(`groupId`,`historyId`))")

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
}

