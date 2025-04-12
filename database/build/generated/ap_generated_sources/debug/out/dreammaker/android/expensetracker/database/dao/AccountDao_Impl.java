package dreammaker.android.expensetracker.database.dao;

import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import dreammaker.android.expensetracker.database.Converters;
import dreammaker.android.expensetracker.database.entity.Account;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.database.type.Currency;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings({"unchecked", "deprecation"})
public final class AccountDao_Impl implements AccountDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Account> __insertionAdapterOfAccount;

  private final EntityDeletionOrUpdateAdapter<Account> __updateAdapterOfAccount;

  public AccountDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAccount = new EntityInsertionAdapter<Account>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `accounts` (`id`,`name`,`balance`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Account value) {
        stmt.bindLong(1, value.getId());
        if (value.getName() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getName());
        }
        final String _tmp = Converters.currencyToString(value.getBalance());
        if (_tmp == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, _tmp);
        }
      }
    };
    this.__updateAdapterOfAccount = new EntityDeletionOrUpdateAdapter<Account>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `accounts` SET `id` = ?,`name` = ?,`balance` = ? WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Account value) {
        stmt.bindLong(1, value.getId());
        if (value.getName() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getName());
        }
        final String _tmp = Converters.currencyToString(value.getBalance());
        if (_tmp == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, _tmp);
        }
        stmt.bindLong(4, value.getId());
      }
    };
  }

  @Override
  public long addAccount(final Account account) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      long _result = __insertionAdapterOfAccount.insertAndReturnId(account);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int updateAccount(final Account account) {
    __db.assertNotSuspendingTransaction();
    int _total = 0;
    __db.beginTransaction();
    try {
      _total +=__updateAdapterOfAccount.handle(account);
      __db.setTransactionSuccessful();
      return _total;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public LiveData<List<AccountModel>> getAllAccountsLive() {
    final String _sql = "SELECT `id`,`name`,`balance` FROM `accounts`";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"accounts"}, false, new Callable<List<AccountModel>>() {
      @Override
      public List<AccountModel> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = 0;
          final int _cursorIndexOfName = 1;
          final int _cursorIndexOfBalance = 2;
          final List<AccountModel> _result = new ArrayList<AccountModel>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final AccountModel _item;
            _item = new AccountModel();
            final Long _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getLong(_cursorIndexOfId);
            }
            _item.setId(_tmpId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            _item.setName(_tmpName);
            final Currency _tmpBalance;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfBalance)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfBalance);
            }
            _tmpBalance = Converters.stringToCurrency(_tmp);
            _item.setBalance(_tmpBalance);
            _result.add(_item);
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

  @Override
  public LiveData<List<AccountModel>> getAllAccountWithUsageCountLive() {
    final String _sql = "SELECT `id`,`name`,`balance`, (SELECT COUNT(`id`) FROM `transaction_histories` WHERE `payeeAccountId` = `accounts`.`id` OR `payerAccountId` = `accounts`.`id`) AS `usageCount` FROM `accounts`";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"transaction_histories","accounts"}, false, new Callable<List<AccountModel>>() {
      @Override
      public List<AccountModel> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = 0;
          final int _cursorIndexOfName = 1;
          final int _cursorIndexOfBalance = 2;
          final int _cursorIndexOfUsageCount = 3;
          final List<AccountModel> _result = new ArrayList<AccountModel>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final AccountModel _item;
            _item = new AccountModel();
            final Long _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getLong(_cursorIndexOfId);
            }
            _item.setId(_tmpId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            _item.setName(_tmpName);
            final Currency _tmpBalance;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfBalance)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfBalance);
            }
            _tmpBalance = Converters.stringToCurrency(_tmp);
            _item.setBalance(_tmpBalance);
            final Integer _tmpUsageCount;
            if (_cursor.isNull(_cursorIndexOfUsageCount)) {
              _tmpUsageCount = null;
            } else {
              _tmpUsageCount = _cursor.getInt(_cursorIndexOfUsageCount);
            }
            _item.setUsageCount(_tmpUsageCount);
            _result.add(_item);
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

  @Override
  public LiveData<AccountModel> getAccountByIdLive(final long id) {
    final String _sql = "SELECT * FROM `accounts` WHERE `id` = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return __db.getInvalidationTracker().createLiveData(new String[]{"accounts"}, false, new Callable<AccountModel>() {
      @Override
      public AccountModel call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfBalance = CursorUtil.getColumnIndexOrThrow(_cursor, "balance");
          final AccountModel _result;
          if(_cursor.moveToFirst()) {
            _result = new AccountModel();
            final Long _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getLong(_cursorIndexOfId);
            }
            _result.setId(_tmpId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            _result.setName(_tmpName);
            final Currency _tmpBalance;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfBalance)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfBalance);
            }
            _tmpBalance = Converters.stringToCurrency(_tmp);
            _result.setBalance(_tmpBalance);
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

  @Override
  public Account findAccountById(final long id) {
    final String _sql = "SELECT * FROM `accounts` WHERE `id` = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfBalance = CursorUtil.getColumnIndexOrThrow(_cursor, "balance");
      final Account _result;
      if(_cursor.moveToFirst()) {
        _result = new Account();
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        _result.setId(_tmpId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _result.setName(_tmpName);
        final Currency _tmpBalance;
        final String _tmp;
        if (_cursor.isNull(_cursorIndexOfBalance)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getString(_cursorIndexOfBalance);
        }
        _tmpBalance = Converters.stringToCurrency(_tmp);
        _result.setBalance(_tmpBalance);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int removeAccounts(final long[] ids) {
    __db.assertNotSuspendingTransaction();
    StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("DELETE FROM `accounts` WHERE `id` IN(");
    final int _inputSize = ids.length;
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
    int _argIndex = 1;
    for (long _item : ids) {
      _stmt.bindLong(_argIndex, _item);
      _argIndex ++;
    }
    __db.beginTransaction();
    try {
      final int _result = _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
