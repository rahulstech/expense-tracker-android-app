package dreammaker.android.expensetracker.database.fake

import dreammaker.android.expensetracker.database.SQLiteStatementExecutorWrapper

class FakeData9: FakeData {
    override fun addFakeData(db: SQLiteStatementExecutorWrapper) {
        db.execSQL("INSERT INTO `accounts` (`id`, `name`, `balance`,`lastUsed`,`totalUsed`) VALUES (1,'Account 1','150.00','2026-10-15 15:56:20',1);")
        db.execSQL("INSERT INTO `accounts` (`id`, `name`, `balance`) VALUES (2,'Account 2','2000.00');")

        db.execSQL("INSERT INTO `groups` (`id`, `name`,`balance`) VALUES (1,'Group 1','100.00');")
        db.execSQL("INSERT INTO `groups` (`id`, `name`,`balance`,`lastUsed`, `totalUsed`) VALUES (2,'Group 2','-200.00', '2026-11-01 13:19:56',3);")

        db.execSQL("INSERT INTO `histories` (`id`, `type`, `primaryAccountId`, `groupId`, `amount`, `date`, `note`) VALUES (1, 'DEBIT', 1, 1, '50.00', '2026-02-16', 'transaction 1');")
        db.execSQL("INSERT INTO `histories` (`id`, `type`, `primaryAccountId`, `groupId`, `amount`, `date`, `note`) VALUES (2, 'CREDIT', 2, 1, '150.00', '2026-02-26', 'transaction 2');")
        db.execSQL("INSERT INTO `histories` (`id`, `type`, `primaryAccountId`, `groupId`, `amount`, `date`, `note`) VALUES (3, 'DEBIT', 1, NULL, '20.00', '2026-01-16', 'transaction 3');")
        db.execSQL("INSERT INTO `histories` (`id`, `type`, `primaryAccountId`, `groupId`, `amount`, `date`, `note`) VALUES (4, 'DEBIT', 1, 2, '120.00', '2026-01-20', 'transaction 4');")
        db.execSQL("INSERT INTO `histories` (`id`, `type`, `primaryAccountId`, `secondaryAccountId`, `amount`, `date`, `note`) VALUES (5, 'TRANSFER', 1, 2, '50.00', '2026-03-06', 'transfer 1');")
        db.execSQL("INSERT INTO `histories` (`id`, `type`, `primaryAccountId`, `groupId`, `amount`, `date`, `note`) VALUES (6, 'CREDIT', 2, NULL, '51.56', '2026-02-03', 'transaction 5');")
    }
}