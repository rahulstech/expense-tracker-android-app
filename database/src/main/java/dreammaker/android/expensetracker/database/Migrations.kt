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
}

