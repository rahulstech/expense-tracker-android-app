package dreammaker.android.expensetracker.database.dao;

import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import dreammaker.android.expensetracker.database.Converters;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.view.AssetLiabilitySummary;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings({"unchecked", "deprecation"})
public final class AnalyticsDao_Impl implements AnalyticsDao {
  private final RoomDatabase __db;

  public AnalyticsDao_Impl(RoomDatabase __db) {
    this.__db = __db;
  }

  @Override
  public LiveData<AssetLiabilitySummary> getTotalAssetLiability() {
    final String _sql = "SELECT * FROM `asset_liability_summary_view`";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"asset_liability_summary_view"}, false, new Callable<AssetLiabilitySummary>() {
      @Override
      public AssetLiabilitySummary call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfTotalPositiveAccounts = CursorUtil.getColumnIndexOrThrow(_cursor, "totalPositiveAccounts");
          final int _cursorIndexOfTotalPositiveBalance = CursorUtil.getColumnIndexOrThrow(_cursor, "totalPositiveBalance");
          final int _cursorIndexOfTotalNegativeAccounts = CursorUtil.getColumnIndexOrThrow(_cursor, "totalNegativeAccounts");
          final int _cursorIndexOfTotalNegativeBalance = CursorUtil.getColumnIndexOrThrow(_cursor, "totalNegativeBalance");
          final int _cursorIndexOfTotalPositiveDuePeople = CursorUtil.getColumnIndexOrThrow(_cursor, "totalPositiveDuePeople");
          final int _cursorIndexOfTotalPositiveDue = CursorUtil.getColumnIndexOrThrow(_cursor, "totalPositiveDue");
          final int _cursorIndexOfTotalNegativeDuePeople = CursorUtil.getColumnIndexOrThrow(_cursor, "totalNegativeDuePeople");
          final int _cursorIndexOfTotalNegativeDue = CursorUtil.getColumnIndexOrThrow(_cursor, "totalNegativeDue");
          final int _cursorIndexOfTotalPositiveBorrowPeople = CursorUtil.getColumnIndexOrThrow(_cursor, "totalPositiveBorrowPeople");
          final int _cursorIndexOfTotalPositiveBorrow = CursorUtil.getColumnIndexOrThrow(_cursor, "totalPositiveBorrow");
          final int _cursorIndexOfTotalNegativeBorrowPeople = CursorUtil.getColumnIndexOrThrow(_cursor, "totalNegativeBorrowPeople");
          final int _cursorIndexOfTotalNegativeBorrow = CursorUtil.getColumnIndexOrThrow(_cursor, "totalNegativeBorrow");
          final int _cursorIndexOfTotalAccounts = CursorUtil.getColumnIndexOrThrow(_cursor, "totalAccounts");
          final int _cursorIndexOfTotalPeople = CursorUtil.getColumnIndexOrThrow(_cursor, "totalPeople");
          final AssetLiabilitySummary _result;
          if(_cursor.moveToFirst()) {
            _result = new AssetLiabilitySummary();
            final int _tmpTotalPositiveAccounts;
            _tmpTotalPositiveAccounts = _cursor.getInt(_cursorIndexOfTotalPositiveAccounts);
            _result.setTotalPositiveAccounts(_tmpTotalPositiveAccounts);
            final Currency _tmpTotalPositiveBalance;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfTotalPositiveBalance)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfTotalPositiveBalance);
            }
            _tmpTotalPositiveBalance = Converters.stringToCurrency(_tmp);
            _result.setTotalPositiveBalance(_tmpTotalPositiveBalance);
            final int _tmpTotalNegativeAccounts;
            _tmpTotalNegativeAccounts = _cursor.getInt(_cursorIndexOfTotalNegativeAccounts);
            _result.setTotalNegativeAccounts(_tmpTotalNegativeAccounts);
            final Currency _tmpTotalNegativeBalance;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfTotalNegativeBalance)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfTotalNegativeBalance);
            }
            _tmpTotalNegativeBalance = Converters.stringToCurrency(_tmp_1);
            _result.setTotalNegativeBalance(_tmpTotalNegativeBalance);
            final int _tmpTotalPositiveDuePeople;
            _tmpTotalPositiveDuePeople = _cursor.getInt(_cursorIndexOfTotalPositiveDuePeople);
            _result.setTotalPositiveDuePeople(_tmpTotalPositiveDuePeople);
            final Currency _tmpTotalPositiveDue;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfTotalPositiveDue)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfTotalPositiveDue);
            }
            _tmpTotalPositiveDue = Converters.stringToCurrency(_tmp_2);
            _result.setTotalPositiveDue(_tmpTotalPositiveDue);
            final int _tmpTotalNegativeDuePeople;
            _tmpTotalNegativeDuePeople = _cursor.getInt(_cursorIndexOfTotalNegativeDuePeople);
            _result.setTotalNegativeDuePeople(_tmpTotalNegativeDuePeople);
            final Currency _tmpTotalNegativeDue;
            final String _tmp_3;
            if (_cursor.isNull(_cursorIndexOfTotalNegativeDue)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getString(_cursorIndexOfTotalNegativeDue);
            }
            _tmpTotalNegativeDue = Converters.stringToCurrency(_tmp_3);
            _result.setTotalNegativeDue(_tmpTotalNegativeDue);
            final int _tmpTotalPositiveBorrowPeople;
            _tmpTotalPositiveBorrowPeople = _cursor.getInt(_cursorIndexOfTotalPositiveBorrowPeople);
            _result.setTotalPositiveBorrowPeople(_tmpTotalPositiveBorrowPeople);
            final Currency _tmpTotalPositiveBorrow;
            final String _tmp_4;
            if (_cursor.isNull(_cursorIndexOfTotalPositiveBorrow)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getString(_cursorIndexOfTotalPositiveBorrow);
            }
            _tmpTotalPositiveBorrow = Converters.stringToCurrency(_tmp_4);
            _result.setTotalPositiveBorrow(_tmpTotalPositiveBorrow);
            final int _tmpTotalNegativeBorrowPeople;
            _tmpTotalNegativeBorrowPeople = _cursor.getInt(_cursorIndexOfTotalNegativeBorrowPeople);
            _result.setTotalNegativeBorrowPeople(_tmpTotalNegativeBorrowPeople);
            final Currency _tmpTotalNegativeBorrow;
            final String _tmp_5;
            if (_cursor.isNull(_cursorIndexOfTotalNegativeBorrow)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getString(_cursorIndexOfTotalNegativeBorrow);
            }
            _tmpTotalNegativeBorrow = Converters.stringToCurrency(_tmp_5);
            _result.setTotalNegativeBorrow(_tmpTotalNegativeBorrow);
            final int _tmpTotalAccounts;
            _tmpTotalAccounts = _cursor.getInt(_cursorIndexOfTotalAccounts);
            _result.setTotalAccounts(_tmpTotalAccounts);
            final int _tmpTotalPeople;
            _tmpTotalPeople = _cursor.getInt(_cursorIndexOfTotalPeople);
            _result.setTotalPeople(_tmpTotalPeople);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
