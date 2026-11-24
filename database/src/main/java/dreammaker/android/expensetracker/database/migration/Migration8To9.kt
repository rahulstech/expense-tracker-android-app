package dreammaker.android.expensetracker.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration8To9: Migration(8,9) {

    override fun migrate(db: SupportSQLiteDatabase) {

        // accounts table
        db.execSQL("ALTER TABLE `accounts` RENAME TO `accounts_old`")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `accounts` 
            (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
            `name` TEXT NOT NULL,
            `balance` TEXT NOT NULL, 
            `lastUsed` TEXT, 
            `totalUsed` INTEGER)
        """)
        db.execSQL("""
            INSERT INTO `accounts` (`id`,`name`,`balance`, `lastUsed`, `totalUsed`)
            SELECT `id`,`name`, CAST(`balance` AS TEXT) AS `balance`, `lastUsed`, `totalUsed` FROM `accounts_old`;
        """)
        db.execSQL("DROP TABLE `accounts_old`")


        // groups table
        db.execSQL("ALTER TABLE `groups` RENAME TO `groups_old`;")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `groups` 
            (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 
            `name` TEXT NOT NULL,
            `balance` TEXT NOT NULL, 
            `lastUsed` TEXT, 
            `totalUsed` INTEGER);
        """)

        // TODO: why balance multiplied by -1
        db.execSQL("""
            INSERT INTO `groups` (`id`, `name`, `balance`, `lastUsed`, `totalUsed`) 
            SELECT `id`, `name`, CAST(-1 * `balance` AS TEXT) as `balance`, `lastUsed`, `totalUsed` FROM `groups_old`
        """)

        db.execSQL("DROP TABLE `groups_old`;")


        // histories
        db.execSQL("ALTER TABLE `histories` RENAME TO `histories_old`;")
        db.execSQL("""
            CREATE TABLE `histories` 
            (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, 
            `type` TEXT NOT NULL,
            `amount` TEXT NOT NULL,
            `date` TEXT NOT NULL,
            `primaryAccountId` INTEGER REFERENCES `accounts`(`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
            `secondaryAccountId` INTEGER REFERENCES `accounts`(`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
            `groupId` INTEGER REFERENCES `groups`(`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
            `note` TEXT);
        """)
        db.execSQL("""
            INSERT INTO `histories` (`id`, `type`,`date`, `amount`, `primaryAccountId`, `secondaryAccountId`,`groupId`,  `note`)
            SELECT `id`, `type`,`date`, CAST(`amount` AS TEXT) AS `amount`,`primaryAccountId`, `secondaryAccountId`,`groupId`, `note` 
                    FROM `histories_old`
        """)
        db.execSQL("DROP TABLE `histories_old`")

        // create indices of histories
        db.execSQL("CREATE INDEX `index_histories_primaryAccountId` ON `histories`(`primaryAccountId`)")
        db.execSQL("CREATE INDEX `index_histories_secondaryAccountId` ON `histories`(`secondaryAccountId`)")
        db.execSQL("CREATE INDEX `index_histories_groupId` ON `histories`(`groupId`)")
        db.execSQL("CREATE INDEX  `index_histories_type` ON `histories`(`type`)")
    }
}