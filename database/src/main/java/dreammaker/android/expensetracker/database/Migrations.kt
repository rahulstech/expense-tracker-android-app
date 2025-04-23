package dreammaker.android.expensetracker.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {

    val MIGRATION_6_7: Migration = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE VIEW IF NOT EXISTS `histories` AS" +
                    " SELECT `_id` AS `id`," +
                    " CASE WHEN `type` = 0 AND `person_id` IS NOT NULL THEN 'DEBIT'" +
                    " WHEN `type` = 0 AND `person_id` IS NULL THEN 'EXPENSE'" +
                    " WHEN `type` = 1 AND `person_id` IS NULL THEN 'INCOME'" +
                    " ELSE 'CREDIT' END AS `type`," +
                    " CASE `type` WHEN 0 THEN `account_id` ELSE NULL END AS `srcAccountId`," +
                    " CASE `type` WHEN 1 THEN `account_id` ELSE NULL END AS `destAccountId`," +
                    " CASE `type` WHEN 0 THEN `person_id` ELSE NULL END AS `destPersonId`," +
                    " CASE `type` WHEN 1 THEN `person_id` ELSE NULL END AS `srcPersonId`," +
                    " `amount`, `date`, `description` AS `note`" +
                    " FROM `transactions` WHERE `deleted` = 0" +
                    " UNION " +
                    " SELECT `id`, 'TRANSFER' AS `type`," +
                    " `payer_account_id` AS `srcAccountId`, `payee_account_id` As `destAccountId`," +
                    " NULL AS `srcPersonId`, NULL AS `destPersonId`, `amount`, `when` AS `date`, `description` AS `note` " +
                    " FROM `money_transfers`")
        }
    }
}

