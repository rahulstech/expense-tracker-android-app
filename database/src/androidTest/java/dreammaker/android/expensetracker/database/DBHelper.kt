package dreammaker.android.expensetracker.database

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.CountDownLatch

fun <T> runOnLiveDataResultReceived(source: LiveData<T>, onReceive: (T) -> Unit) {
    val handler = Handler(Looper.getMainLooper())
    val latch = CountDownLatch(1)
    val observer = Observer<T> {
        try {
            onReceive(it)
        }
        finally {
            latch.countDown()
        }
    }
    handler.post { source.observeForever(observer) }
    latch.await()
    handler.post{ source.removeObserver(observer) }
}

interface SQLiteStatementExecutorWrapper {
    fun execSQL(sql: String)
}

class SQLiteStatementExecutorWrapperBuilder {

    fun buildFromSupportSQLiteDatabase(db: SupportSQLiteDatabase): SQLiteStatementExecutorWrapper
    = object : SQLiteStatementExecutorWrapper {
        override fun execSQL(sql: String) {
            db.execSQL(sql)
        }
    }
}

interface FakeData {
    fun addFakeData(db: SQLiteStatementExecutorWrapper)
}

val FAKE_DATA_6 = object : FakeData {
    override fun addFakeData(db: SQLiteStatementExecutorWrapper) {
        db.execSQL("INSERT INTO `accounts` (`_id`, `account_name`, `balance`) VALUES (1,'Account 1',150.00);")
        db.execSQL("INSERT INTO `accounts` (`_id`, `account_name`, `balance`) VALUES (2,'Account 2',2000.00);")

        db.execSQL("INSERT INTO `persons` (`_id`, `person_name`,`due`) VALUES (1,'Person 1',100);")

        db.execSQL("INSERT INTO `transactions` (`_id`, `type`, `account_id`, `person_id`, `amount`, `date`, `description`, `deleted`) VALUES (1, 0, 1, 1, 50.00, '2025-02-16', 'transaction 1',0);")
        db.execSQL("INSERT INTO `transactions` (`_id`, `type`, `account_id`, `person_id`, `amount`, `date`, `description`, `deleted`) VALUES (2, 1, 2, 1, 150.00, '2025-02-26', 'transaction 2',0);")
        db.execSQL("INSERT INTO `transactions` (`_id`, `type`, `account_id`, `person_id`, `amount`, `date`, `description`, `deleted`) VALUES (3, 0, 1, NULL, 20.00, '2025-01-16', 'transaction 3',0);")

        db.execSQL("INSERT INTO `money_transfers` (`id`, `payee_account_id`, `payer_account_id`, `amount`, `when`, `description`) VALUES (1, 1, 2, 50.00, '2025-03-06', 'transfer 1');")
    }
}

val FAKE_DATA_7 = object : FakeData {
    override fun addFakeData(db: SQLiteStatementExecutorWrapper) {
        db.execSQL("INSERT INTO `accounts` (`_id`, `account_name`, `balance`) VALUES (1,'Account 1',150.00);")
        db.execSQL("INSERT INTO `accounts` (`_id`, `account_name`, `balance`) VALUES (2,'Account 2',2000.00);")

        db.execSQL("INSERT INTO `persons` (`_id`, `person_name`,`due`) VALUES (1,'Person 1',100);")

        db.execSQL("INSERT INTO `transactions` (`_id`, `type`, `account_id`, `person_id`, `amount`, `date`, `description`, `deleted`) VALUES (1, 0, 1, 1, 50.00, '2025-02-16', 'transaction 1',0);")
        db.execSQL("INSERT INTO `transactions` (`_id`, `type`, `account_id`, `person_id`, `amount`, `date`, `description`, `deleted`) VALUES (2, 1, 2, 1, 150.00, '2025-02-26', 'transaction 2',0);")
        db.execSQL("INSERT INTO `transactions` (`_id`, `type`, `account_id`, `person_id`, `amount`, `date`, `description`, `deleted`) VALUES (3, 0, 1, NULL, 20.00, '2025-01-16', 'transaction 3',0);")

        db.execSQL("INSERT INTO `money_transfers` (`id`, `payee_account_id`, `payer_account_id`, `amount`, `when`, `description`) VALUES (1, 1, 2, 50.00, '2025-03-06', 'transfer 1');")
    }
}