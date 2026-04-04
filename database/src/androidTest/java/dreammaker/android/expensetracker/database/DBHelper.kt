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

fun createInMemoryDB(callback: RoomDatabase.Callback? = null): ExpensesDatabase {
    val context = ApplicationProvider.getApplicationContext<Application>()
    val builder = Room.inMemoryDatabaseBuilder(context, ExpensesDatabase::class.java)
        .allowMainThreadQueries()
    callback?.let { builder.addCallback(callback) }
    return builder.build()
}