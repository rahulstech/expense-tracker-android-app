package dreammaker.android.expensetracker.database;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

@Dao
public interface RawDao {

    @RawQuery
    Cursor query(@NonNull SupportSQLiteQuery query);
}
