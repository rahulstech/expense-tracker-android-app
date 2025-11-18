package dreammaker.android.expensetracker.database

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CountDownLatch

fun <T> runOnLiveDataResultReceived(source: LiveData<T>, onReceive: (T) -> Unit) {
    val handler = Handler(Looper.getMainLooper())
    val latch = CountDownLatch(1)
    var error: Throwable? = null
    val observer = Observer<T> {
        try {
            onReceive(it)
        }
        catch (ex: Throwable) {
            error = ex
        }
        finally {
            latch.countDown()
        }
    }
    handler.post { source.observeForever(observer) }
    latch.await()
    handler.post{ source.removeObserver(observer) }

    error?.let { throw it }
}

fun parseDate(dateText: String, pattern: String = "yyyy-MM-dd"): LocalDate =
    LocalDate.parse(dateText, DateTimeFormatter.ofPattern(pattern))

fun parseDateTime(datetimeText: String, pattern: String = "yyyy-MM-dd HH:mm:ss"): LocalDateTime =
    LocalDateTime.parse(datetimeText, DateTimeFormatter.ofPattern(pattern))

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

fun <T: RoomDatabase> createInMemoryDB(clazz: Class<T>, callback: RoomDatabase.Callback? = null): T {
    val context = ApplicationProvider.getApplicationContext<Application>()
    val builder = Room.inMemoryDatabaseBuilder(context,clazz)
        .allowMainThreadQueries()
    callback?.let { builder.addCallback(callback) }
    return builder.build()
}

class FakeDataCallback(private val source: FakeData): RoomDatabase.Callback() {
    override fun onOpen(db: SupportSQLiteDatabase) {
        val wrapper = SQLiteStatementExecutorWrapperBuilder().buildFromSupportSQLiteDatabase(db)
        source.addFakeData(wrapper)
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
        db.execSQL("INSERT INTO `persons` (`_id`, `person_name`,`due`) VALUES (2,'Person 2',-200);")

        db.execSQL("INSERT INTO `transactions` (`_id`, `type`, `account_id`, `person_id`, `amount`, `date`, `description`, `deleted`) VALUES (1, 0, 1, 1, 50.00, '2025-02-16', 'transaction 1',0);")
        db.execSQL("INSERT INTO `transactions` (`_id`, `type`, `account_id`, `person_id`, `amount`, `date`, `description`, `deleted`) VALUES (2, 1, 2, 1, 150.00, '2025-02-26', 'transaction 2',0);")
        db.execSQL("INSERT INTO `transactions` (`_id`, `type`, `account_id`, `person_id`, `amount`, `date`, `description`, `deleted`) VALUES (3, 0, 1, NULL, 20.00, '2025-01-16', 'transaction 3',0);")
        db.execSQL("INSERT INTO `transactions` (`_id`, `type`, `account_id`, `person_id`, `amount`, `date`, `description`, `deleted`) VALUES (4, 0, 1, 2, 120.00, '2025-01-20', 'transaction 4',0);")

        db.execSQL("INSERT INTO `money_transfers` (`id`, `payee_account_id`, `payer_account_id`, `amount`, `when`, `description`) VALUES (1, 1, 2, 50.00, '2025-03-06', 'transfer 1');")
    }
}

val FAKE_DATA_8 = object : FakeData {
    override fun addFakeData(db: SQLiteStatementExecutorWrapper) {
        db.execSQL("INSERT INTO `accounts` (`id`, `name`, `balance`,`lastUsed`,`totalUsed`) VALUES (1,'Account 1',150.00,'2025-10-15 15:56:20',1);")
        db.execSQL("INSERT INTO `accounts` (`id`, `name`, `balance`) VALUES (2,'Account 2',2000.00);")

        db.execSQL("INSERT INTO `groups` (`id`, `name`,`due`) VALUES (1,'Group 1',100);")
        db.execSQL("INSERT INTO `groups` (`id`, `name`,`due`,`lastUsed`, `totalUsed`) VALUES (2,'Group 2',-200, '2025-11-01 13:19:56',3);")

        db.execSQL("INSERT INTO `histories` (`id`, `type`, `primaryAccountId`, `groupId`, `amount`, `date`, `note`) VALUES (1, 'DEBIT', 1, 1, 50.00, '2025-02-16', 'transaction 1');")
        db.execSQL("INSERT INTO `histories` (`id`, `type`, `primaryAccountId`, `groupId`, `amount`, `date`, `note`) VALUES (2, 'CREDIT', 2, 1, 150.00, '2025-02-26', 'transaction 2');")
        db.execSQL("INSERT INTO `histories` (`id`, `type`, `primaryAccountId`, `groupId`, `amount`, `date`, `note`) VALUES (3, 'DEBIT', 1, NULL, 20.00, '2025-01-16', 'transaction 3');")
        db.execSQL("INSERT INTO `histories` (`id`, `type`, `primaryAccountId`, `groupId`, `amount`, `date`, `note`) VALUES (4, 'DEBIT', 1, 2, 120.00, '2025-01-20', 'transaction 4');")

        db.execSQL("INSERT INTO `histories` (`id`, `type`, `primaryAccountId`, `secondaryAccountId`, `amount`, `date`, `note`) VALUES (5, 'TRANSFER', 1, 2, 50.00, '2025-03-06', 'transfer 1');")
    }
}