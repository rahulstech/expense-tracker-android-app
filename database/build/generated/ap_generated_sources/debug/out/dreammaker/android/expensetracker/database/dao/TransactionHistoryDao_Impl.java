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
import dreammaker.android.expensetracker.database.entity.Person;
import dreammaker.android.expensetracker.database.entity.TransactionHistory;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.database.model.TransactionHistoryModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

@SuppressWarnings({"unchecked", "deprecation"})
public final class TransactionHistoryDao_Impl extends TransactionHistoryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TransactionHistory> __insertionAdapterOfTransactionHistory;

  private final EntityDeletionOrUpdateAdapter<TransactionHistory> __deletionAdapterOfTransactionHistory;

  private final EntityDeletionOrUpdateAdapter<Account> __updateAdapterOfAccount;

  private final EntityDeletionOrUpdateAdapter<Person> __updateAdapterOfPerson;

  private final EntityDeletionOrUpdateAdapter<TransactionHistory> __updateAdapterOfTransactionHistory;

  public TransactionHistoryDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTransactionHistory = new EntityInsertionAdapter<TransactionHistory>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `transaction_histories` (`id`,`payeeAccountId`,`payerAccountId`,`payeePersonId`,`payerPersonId`,`type`,`amount`,`when`,`description`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, TransactionHistory value) {
        stmt.bindLong(1, value.getId());
        if (value.getPayeeAccountId() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindLong(2, value.getPayeeAccountId());
        }
        if (value.getPayerAccountId() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindLong(3, value.getPayerAccountId());
        }
        if (value.getPayeePersonId() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindLong(4, value.getPayeePersonId());
        }
        if (value.getPayerPersonId() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindLong(5, value.getPayerPersonId());
        }
        if (value.getType() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, __TransactionType_enumToString(value.getType()));
        }
        final String _tmp = Converters.currencyToString(value.getAmount());
        if (_tmp == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, _tmp);
        }
        final String _tmp_1 = Converters.localDateToString(value.getWhen());
        if (_tmp_1 == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, _tmp_1);
        }
        if (value.getDescription() == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindString(9, value.getDescription());
        }
      }
    };
    this.__deletionAdapterOfTransactionHistory = new EntityDeletionOrUpdateAdapter<TransactionHistory>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `transaction_histories` WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, TransactionHistory value) {
        stmt.bindLong(1, value.getId());
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
    this.__updateAdapterOfPerson = new EntityDeletionOrUpdateAdapter<Person>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `people` SET `id` = ?,`firstName` = ?,`lastName` = ?,`due` = ?,`borrow` = ? WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Person value) {
        stmt.bindLong(1, value.getId());
        if (value.getFirstName() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getFirstName());
        }
        if (value.getLastName() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getLastName());
        }
        final String _tmp = Converters.currencyToString(value.getDue());
        if (_tmp == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, _tmp);
        }
        final String _tmp_1 = Converters.currencyToString(value.getBorrow());
        if (_tmp_1 == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, _tmp_1);
        }
        stmt.bindLong(6, value.getId());
      }
    };
    this.__updateAdapterOfTransactionHistory = new EntityDeletionOrUpdateAdapter<TransactionHistory>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `transaction_histories` SET `id` = ?,`payeeAccountId` = ?,`payerAccountId` = ?,`payeePersonId` = ?,`payerPersonId` = ?,`type` = ?,`amount` = ?,`when` = ?,`description` = ? WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, TransactionHistory value) {
        stmt.bindLong(1, value.getId());
        if (value.getPayeeAccountId() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindLong(2, value.getPayeeAccountId());
        }
        if (value.getPayerAccountId() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindLong(3, value.getPayerAccountId());
        }
        if (value.getPayeePersonId() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindLong(4, value.getPayeePersonId());
        }
        if (value.getPayerPersonId() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindLong(5, value.getPayerPersonId());
        }
        if (value.getType() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, __TransactionType_enumToString(value.getType()));
        }
        final String _tmp = Converters.currencyToString(value.getAmount());
        if (_tmp == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, _tmp);
        }
        final String _tmp_1 = Converters.localDateToString(value.getWhen());
        if (_tmp_1 == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, _tmp_1);
        }
        if (value.getDescription() == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindString(9, value.getDescription());
        }
        stmt.bindLong(10, value.getId());
      }
    };
  }

  @Override
  protected long insert_transactionHistory(final TransactionHistory history) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      long _result = __insertionAdapterOfTransactionHistory.insertAndReturnId(history);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  protected int delete_transactionHistory(final TransactionHistory history) {
    __db.assertNotSuspendingTransaction();
    int _total = 0;
    __db.beginTransaction();
    try {
      _total +=__deletionAdapterOfTransactionHistory.handle(history);
      __db.setTransactionSuccessful();
      return _total;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  protected int updateAccount(final Account account) {
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
  protected int updatePerson(final Person person) {
    __db.assertNotSuspendingTransaction();
    int _total = 0;
    __db.beginTransaction();
    try {
      _total +=__updateAdapterOfPerson.handle(person);
      __db.setTransactionSuccessful();
      return _total;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  protected int update_transactionHistory(final TransactionHistory history) {
    __db.assertNotSuspendingTransaction();
    int _total = 0;
    __db.beginTransaction();
    try {
      _total +=__updateAdapterOfTransactionHistory.handle(history);
      __db.setTransactionSuccessful();
      return _total;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public long addTransactionHistory(final TransactionHistory history) {
    __db.beginTransaction();
    try {
      long _result = TransactionHistoryDao_Impl.super.addTransactionHistory(history);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int updateTransactionHistory(final TransactionHistory history) {
    __db.beginTransaction();
    try {
      int _result = TransactionHistoryDao_Impl.super.updateTransactionHistory(history);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int removeTransactionHistories(final long[] ids) {
    __db.beginTransaction();
    try {
      int _result = TransactionHistoryDao_Impl.super.removeTransactionHistories(ids);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int removeTransactionHistory(final TransactionHistory history) {
    __db.beginTransaction();
    try {
      int _result = TransactionHistoryDao_Impl.super.removeTransactionHistory(history);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public TransactionHistory findTransactionHistoryById(final long id) {
    final String _sql = "SELECT * FROM `transaction_histories` WHERE `id` = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfPayeeAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "payeeAccountId");
      final int _cursorIndexOfPayerAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "payerAccountId");
      final int _cursorIndexOfPayeePersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "payeePersonId");
      final int _cursorIndexOfPayerPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "payerPersonId");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
      final int _cursorIndexOfWhen = CursorUtil.getColumnIndexOrThrow(_cursor, "when");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final TransactionHistory _result;
      if(_cursor.moveToFirst()) {
        _result = new TransactionHistory();
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        _result.setId(_tmpId);
        final Long _tmpPayeeAccountId;
        if (_cursor.isNull(_cursorIndexOfPayeeAccountId)) {
          _tmpPayeeAccountId = null;
        } else {
          _tmpPayeeAccountId = _cursor.getLong(_cursorIndexOfPayeeAccountId);
        }
        _result.setPayeeAccountId(_tmpPayeeAccountId);
        final Long _tmpPayerAccountId;
        if (_cursor.isNull(_cursorIndexOfPayerAccountId)) {
          _tmpPayerAccountId = null;
        } else {
          _tmpPayerAccountId = _cursor.getLong(_cursorIndexOfPayerAccountId);
        }
        _result.setPayerAccountId(_tmpPayerAccountId);
        final Long _tmpPayeePersonId;
        if (_cursor.isNull(_cursorIndexOfPayeePersonId)) {
          _tmpPayeePersonId = null;
        } else {
          _tmpPayeePersonId = _cursor.getLong(_cursorIndexOfPayeePersonId);
        }
        _result.setPayeePersonId(_tmpPayeePersonId);
        final Long _tmpPayerPersonId;
        if (_cursor.isNull(_cursorIndexOfPayerPersonId)) {
          _tmpPayerPersonId = null;
        } else {
          _tmpPayerPersonId = _cursor.getLong(_cursorIndexOfPayerPersonId);
        }
        _result.setPayerPersonId(_tmpPayerPersonId);
        final TransactionType _tmpType;
        _tmpType = __TransactionType_stringToEnum(_cursor.getString(_cursorIndexOfType));
        _result.setType(_tmpType);
        final Currency _tmpAmount;
        final String _tmp;
        if (_cursor.isNull(_cursorIndexOfAmount)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getString(_cursorIndexOfAmount);
        }
        _tmpAmount = Converters.stringToCurrency(_tmp);
        _result.setAmount(_tmpAmount);
        final LocalDate _tmpWhen;
        final String _tmp_1;
        if (_cursor.isNull(_cursorIndexOfWhen)) {
          _tmp_1 = null;
        } else {
          _tmp_1 = _cursor.getString(_cursorIndexOfWhen);
        }
        _tmpWhen = Converters.stringToLocalDate(_tmp_1);
        _result.setWhen(_tmpWhen);
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        _result.setDescription(_tmpDescription);
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
  public LiveData<TransactionHistoryModel> getTransactionHistoryByIdLive(final long id) {
    final String _sql = "SELECT * FROM `transaction_histories` WHERE `id` = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return __db.getInvalidationTracker().createLiveData(new String[]{"accounts","people","transaction_histories"}, false, new Callable<TransactionHistoryModel>() {
      @Override
      public TransactionHistoryModel call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPayeeAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "payeeAccountId");
          final int _cursorIndexOfPayerAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "payerAccountId");
          final int _cursorIndexOfPayeePersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "payeePersonId");
          final int _cursorIndexOfPayerPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "payerPersonId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfWhen = CursorUtil.getColumnIndexOrThrow(_cursor, "when");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final HashMap<Long, AccountModel> _collectionPayeeAccount = new HashMap<Long, AccountModel>();
          final HashMap<Long, AccountModel> _collectionPayerAccount = new HashMap<Long, AccountModel>();
          final HashMap<Long, PersonModel> _collectionPayeePerson = new HashMap<Long, PersonModel>();
          final HashMap<Long, PersonModel> _collectionPayerPerson = new HashMap<Long, PersonModel>();
          while (_cursor.moveToNext()) {
            if (!_cursor.isNull(_cursorIndexOfPayeeAccountId)) {
              final Long _tmpKey = _cursor.getLong(_cursorIndexOfPayeeAccountId);
              _collectionPayeeAccount.put(_tmpKey, null);
            }
            if (!_cursor.isNull(_cursorIndexOfPayerAccountId)) {
              final Long _tmpKey_1 = _cursor.getLong(_cursorIndexOfPayerAccountId);
              _collectionPayerAccount.put(_tmpKey_1, null);
            }
            if (!_cursor.isNull(_cursorIndexOfPayeePersonId)) {
              final Long _tmpKey_2 = _cursor.getLong(_cursorIndexOfPayeePersonId);
              _collectionPayeePerson.put(_tmpKey_2, null);
            }
            if (!_cursor.isNull(_cursorIndexOfPayerPersonId)) {
              final Long _tmpKey_3 = _cursor.getLong(_cursorIndexOfPayerPersonId);
              _collectionPayerPerson.put(_tmpKey_3, null);
            }
          }
          _cursor.moveToPosition(-1);
          __fetchRelationshipaccountsAsdreammakerAndroidExpensetrackerDatabaseModelAccountModel(_collectionPayeeAccount);
          __fetchRelationshipaccountsAsdreammakerAndroidExpensetrackerDatabaseModelAccountModel(_collectionPayerAccount);
          __fetchRelationshippeopleAsdreammakerAndroidExpensetrackerDatabaseModelPersonModel(_collectionPayeePerson);
          __fetchRelationshippeopleAsdreammakerAndroidExpensetrackerDatabaseModelPersonModel(_collectionPayerPerson);
          final TransactionHistoryModel _result;
          if(_cursor.moveToFirst()) {
            AccountModel _tmpPayeeAccount = null;
            if (!_cursor.isNull(_cursorIndexOfPayeeAccountId)) {
              final Long _tmpKey_4 = _cursor.getLong(_cursorIndexOfPayeeAccountId);
              _tmpPayeeAccount = _collectionPayeeAccount.get(_tmpKey_4);
            }
            AccountModel _tmpPayerAccount = null;
            if (!_cursor.isNull(_cursorIndexOfPayerAccountId)) {
              final Long _tmpKey_5 = _cursor.getLong(_cursorIndexOfPayerAccountId);
              _tmpPayerAccount = _collectionPayerAccount.get(_tmpKey_5);
            }
            PersonModel _tmpPayeePerson = null;
            if (!_cursor.isNull(_cursorIndexOfPayeePersonId)) {
              final Long _tmpKey_6 = _cursor.getLong(_cursorIndexOfPayeePersonId);
              _tmpPayeePerson = _collectionPayeePerson.get(_tmpKey_6);
            }
            PersonModel _tmpPayerPerson = null;
            if (!_cursor.isNull(_cursorIndexOfPayerPersonId)) {
              final Long _tmpKey_7 = _cursor.getLong(_cursorIndexOfPayerPersonId);
              _tmpPayerPerson = _collectionPayerPerson.get(_tmpKey_7);
            }
            _result = new TransactionHistoryModel();
            final Long _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getLong(_cursorIndexOfId);
            }
            _result.setId(_tmpId);
            final Long _tmpPayeeAccountId;
            if (_cursor.isNull(_cursorIndexOfPayeeAccountId)) {
              _tmpPayeeAccountId = null;
            } else {
              _tmpPayeeAccountId = _cursor.getLong(_cursorIndexOfPayeeAccountId);
            }
            _result.setPayeeAccountId(_tmpPayeeAccountId);
            final Long _tmpPayerAccountId;
            if (_cursor.isNull(_cursorIndexOfPayerAccountId)) {
              _tmpPayerAccountId = null;
            } else {
              _tmpPayerAccountId = _cursor.getLong(_cursorIndexOfPayerAccountId);
            }
            _result.setPayerAccountId(_tmpPayerAccountId);
            final Long _tmpPayeePersonId;
            if (_cursor.isNull(_cursorIndexOfPayeePersonId)) {
              _tmpPayeePersonId = null;
            } else {
              _tmpPayeePersonId = _cursor.getLong(_cursorIndexOfPayeePersonId);
            }
            _result.setPayeePersonId(_tmpPayeePersonId);
            final Long _tmpPayerPersonId;
            if (_cursor.isNull(_cursorIndexOfPayerPersonId)) {
              _tmpPayerPersonId = null;
            } else {
              _tmpPayerPersonId = _cursor.getLong(_cursorIndexOfPayerPersonId);
            }
            _result.setPayerPersonId(_tmpPayerPersonId);
            final TransactionType _tmpType;
            _tmpType = __TransactionType_stringToEnum(_cursor.getString(_cursorIndexOfType));
            _result.setType(_tmpType);
            final Currency _tmpAmount;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfAmount)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfAmount);
            }
            _tmpAmount = Converters.stringToCurrency(_tmp);
            _result.setAmount(_tmpAmount);
            final LocalDate _tmpWhen;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfWhen)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfWhen);
            }
            _tmpWhen = Converters.stringToLocalDate(_tmp_1);
            _result.setWhen(_tmpWhen);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            _result.setDescription(_tmpDescription);
            _result.setPayeeAccount(_tmpPayeeAccount);
            _result.setPayerAccount(_tmpPayerAccount);
            _result.setPayeePerson(_tmpPayeePerson);
            _result.setPayerPerson(_tmpPayerPerson);
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
  public LiveData<List<TransactionHistoryModel>> getAllTransactionHistoriesForDateLive(
      final LocalDate date) {
    final String _sql = "SELECT * FROM `transaction_histories` WHERE DATE(`when`) IS ? ORDER BY `when` DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final String _tmp = Converters.localDateToString(date);
    if (_tmp == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, _tmp);
    }
    return __db.getInvalidationTracker().createLiveData(new String[]{"accounts","people","transaction_histories"}, true, new Callable<List<TransactionHistoryModel>>() {
      @Override
      public List<TransactionHistoryModel> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfPayeeAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "payeeAccountId");
            final int _cursorIndexOfPayerAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "payerAccountId");
            final int _cursorIndexOfPayeePersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "payeePersonId");
            final int _cursorIndexOfPayerPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "payerPersonId");
            final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
            final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
            final int _cursorIndexOfWhen = CursorUtil.getColumnIndexOrThrow(_cursor, "when");
            final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
            final HashMap<Long, AccountModel> _collectionPayeeAccount = new HashMap<Long, AccountModel>();
            final HashMap<Long, AccountModel> _collectionPayerAccount = new HashMap<Long, AccountModel>();
            final HashMap<Long, PersonModel> _collectionPayeePerson = new HashMap<Long, PersonModel>();
            final HashMap<Long, PersonModel> _collectionPayerPerson = new HashMap<Long, PersonModel>();
            while (_cursor.moveToNext()) {
              if (!_cursor.isNull(_cursorIndexOfPayeeAccountId)) {
                final Long _tmpKey = _cursor.getLong(_cursorIndexOfPayeeAccountId);
                _collectionPayeeAccount.put(_tmpKey, null);
              }
              if (!_cursor.isNull(_cursorIndexOfPayerAccountId)) {
                final Long _tmpKey_1 = _cursor.getLong(_cursorIndexOfPayerAccountId);
                _collectionPayerAccount.put(_tmpKey_1, null);
              }
              if (!_cursor.isNull(_cursorIndexOfPayeePersonId)) {
                final Long _tmpKey_2 = _cursor.getLong(_cursorIndexOfPayeePersonId);
                _collectionPayeePerson.put(_tmpKey_2, null);
              }
              if (!_cursor.isNull(_cursorIndexOfPayerPersonId)) {
                final Long _tmpKey_3 = _cursor.getLong(_cursorIndexOfPayerPersonId);
                _collectionPayerPerson.put(_tmpKey_3, null);
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipaccountsAsdreammakerAndroidExpensetrackerDatabaseModelAccountModel(_collectionPayeeAccount);
            __fetchRelationshipaccountsAsdreammakerAndroidExpensetrackerDatabaseModelAccountModel(_collectionPayerAccount);
            __fetchRelationshippeopleAsdreammakerAndroidExpensetrackerDatabaseModelPersonModel(_collectionPayeePerson);
            __fetchRelationshippeopleAsdreammakerAndroidExpensetrackerDatabaseModelPersonModel(_collectionPayerPerson);
            final List<TransactionHistoryModel> _result = new ArrayList<TransactionHistoryModel>(_cursor.getCount());
            while(_cursor.moveToNext()) {
              final TransactionHistoryModel _item;
              AccountModel _tmpPayeeAccount = null;
              if (!_cursor.isNull(_cursorIndexOfPayeeAccountId)) {
                final Long _tmpKey_4 = _cursor.getLong(_cursorIndexOfPayeeAccountId);
                _tmpPayeeAccount = _collectionPayeeAccount.get(_tmpKey_4);
              }
              AccountModel _tmpPayerAccount = null;
              if (!_cursor.isNull(_cursorIndexOfPayerAccountId)) {
                final Long _tmpKey_5 = _cursor.getLong(_cursorIndexOfPayerAccountId);
                _tmpPayerAccount = _collectionPayerAccount.get(_tmpKey_5);
              }
              PersonModel _tmpPayeePerson = null;
              if (!_cursor.isNull(_cursorIndexOfPayeePersonId)) {
                final Long _tmpKey_6 = _cursor.getLong(_cursorIndexOfPayeePersonId);
                _tmpPayeePerson = _collectionPayeePerson.get(_tmpKey_6);
              }
              PersonModel _tmpPayerPerson = null;
              if (!_cursor.isNull(_cursorIndexOfPayerPersonId)) {
                final Long _tmpKey_7 = _cursor.getLong(_cursorIndexOfPayerPersonId);
                _tmpPayerPerson = _collectionPayerPerson.get(_tmpKey_7);
              }
              _item = new TransactionHistoryModel();
              final Long _tmpId;
              if (_cursor.isNull(_cursorIndexOfId)) {
                _tmpId = null;
              } else {
                _tmpId = _cursor.getLong(_cursorIndexOfId);
              }
              _item.setId(_tmpId);
              final Long _tmpPayeeAccountId;
              if (_cursor.isNull(_cursorIndexOfPayeeAccountId)) {
                _tmpPayeeAccountId = null;
              } else {
                _tmpPayeeAccountId = _cursor.getLong(_cursorIndexOfPayeeAccountId);
              }
              _item.setPayeeAccountId(_tmpPayeeAccountId);
              final Long _tmpPayerAccountId;
              if (_cursor.isNull(_cursorIndexOfPayerAccountId)) {
                _tmpPayerAccountId = null;
              } else {
                _tmpPayerAccountId = _cursor.getLong(_cursorIndexOfPayerAccountId);
              }
              _item.setPayerAccountId(_tmpPayerAccountId);
              final Long _tmpPayeePersonId;
              if (_cursor.isNull(_cursorIndexOfPayeePersonId)) {
                _tmpPayeePersonId = null;
              } else {
                _tmpPayeePersonId = _cursor.getLong(_cursorIndexOfPayeePersonId);
              }
              _item.setPayeePersonId(_tmpPayeePersonId);
              final Long _tmpPayerPersonId;
              if (_cursor.isNull(_cursorIndexOfPayerPersonId)) {
                _tmpPayerPersonId = null;
              } else {
                _tmpPayerPersonId = _cursor.getLong(_cursorIndexOfPayerPersonId);
              }
              _item.setPayerPersonId(_tmpPayerPersonId);
              final TransactionType _tmpType;
              _tmpType = __TransactionType_stringToEnum(_cursor.getString(_cursorIndexOfType));
              _item.setType(_tmpType);
              final Currency _tmpAmount;
              final String _tmp_1;
              if (_cursor.isNull(_cursorIndexOfAmount)) {
                _tmp_1 = null;
              } else {
                _tmp_1 = _cursor.getString(_cursorIndexOfAmount);
              }
              _tmpAmount = Converters.stringToCurrency(_tmp_1);
              _item.setAmount(_tmpAmount);
              final LocalDate _tmpWhen;
              final String _tmp_2;
              if (_cursor.isNull(_cursorIndexOfWhen)) {
                _tmp_2 = null;
              } else {
                _tmp_2 = _cursor.getString(_cursorIndexOfWhen);
              }
              _tmpWhen = Converters.stringToLocalDate(_tmp_2);
              _item.setWhen(_tmpWhen);
              final String _tmpDescription;
              if (_cursor.isNull(_cursorIndexOfDescription)) {
                _tmpDescription = null;
              } else {
                _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
              }
              _item.setDescription(_tmpDescription);
              _item.setPayeeAccount(_tmpPayeeAccount);
              _item.setPayerAccount(_tmpPayerAccount);
              _item.setPayeePerson(_tmpPayeePerson);
              _item.setPayerPerson(_tmpPayerPerson);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<TransactionHistoryModel>> getAllTransactionHistoriesBetweenLive(
      final LocalDate start, final LocalDate end) {
    final String _sql = "SELECT * FROM `transaction_histories` WHERE DATE(`when`) BETWEEN ? AND ? ORDER BY `when` DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    final String _tmp = Converters.localDateToString(start);
    if (_tmp == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, _tmp);
    }
    _argIndex = 2;
    final String _tmp_1 = Converters.localDateToString(end);
    if (_tmp_1 == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, _tmp_1);
    }
    return __db.getInvalidationTracker().createLiveData(new String[]{"accounts","people","transaction_histories"}, true, new Callable<List<TransactionHistoryModel>>() {
      @Override
      public List<TransactionHistoryModel> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfPayeeAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "payeeAccountId");
            final int _cursorIndexOfPayerAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "payerAccountId");
            final int _cursorIndexOfPayeePersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "payeePersonId");
            final int _cursorIndexOfPayerPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "payerPersonId");
            final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
            final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
            final int _cursorIndexOfWhen = CursorUtil.getColumnIndexOrThrow(_cursor, "when");
            final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
            final HashMap<Long, AccountModel> _collectionPayeeAccount = new HashMap<Long, AccountModel>();
            final HashMap<Long, AccountModel> _collectionPayerAccount = new HashMap<Long, AccountModel>();
            final HashMap<Long, PersonModel> _collectionPayeePerson = new HashMap<Long, PersonModel>();
            final HashMap<Long, PersonModel> _collectionPayerPerson = new HashMap<Long, PersonModel>();
            while (_cursor.moveToNext()) {
              if (!_cursor.isNull(_cursorIndexOfPayeeAccountId)) {
                final Long _tmpKey = _cursor.getLong(_cursorIndexOfPayeeAccountId);
                _collectionPayeeAccount.put(_tmpKey, null);
              }
              if (!_cursor.isNull(_cursorIndexOfPayerAccountId)) {
                final Long _tmpKey_1 = _cursor.getLong(_cursorIndexOfPayerAccountId);
                _collectionPayerAccount.put(_tmpKey_1, null);
              }
              if (!_cursor.isNull(_cursorIndexOfPayeePersonId)) {
                final Long _tmpKey_2 = _cursor.getLong(_cursorIndexOfPayeePersonId);
                _collectionPayeePerson.put(_tmpKey_2, null);
              }
              if (!_cursor.isNull(_cursorIndexOfPayerPersonId)) {
                final Long _tmpKey_3 = _cursor.getLong(_cursorIndexOfPayerPersonId);
                _collectionPayerPerson.put(_tmpKey_3, null);
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipaccountsAsdreammakerAndroidExpensetrackerDatabaseModelAccountModel(_collectionPayeeAccount);
            __fetchRelationshipaccountsAsdreammakerAndroidExpensetrackerDatabaseModelAccountModel(_collectionPayerAccount);
            __fetchRelationshippeopleAsdreammakerAndroidExpensetrackerDatabaseModelPersonModel(_collectionPayeePerson);
            __fetchRelationshippeopleAsdreammakerAndroidExpensetrackerDatabaseModelPersonModel(_collectionPayerPerson);
            final List<TransactionHistoryModel> _result = new ArrayList<TransactionHistoryModel>(_cursor.getCount());
            while(_cursor.moveToNext()) {
              final TransactionHistoryModel _item;
              AccountModel _tmpPayeeAccount = null;
              if (!_cursor.isNull(_cursorIndexOfPayeeAccountId)) {
                final Long _tmpKey_4 = _cursor.getLong(_cursorIndexOfPayeeAccountId);
                _tmpPayeeAccount = _collectionPayeeAccount.get(_tmpKey_4);
              }
              AccountModel _tmpPayerAccount = null;
              if (!_cursor.isNull(_cursorIndexOfPayerAccountId)) {
                final Long _tmpKey_5 = _cursor.getLong(_cursorIndexOfPayerAccountId);
                _tmpPayerAccount = _collectionPayerAccount.get(_tmpKey_5);
              }
              PersonModel _tmpPayeePerson = null;
              if (!_cursor.isNull(_cursorIndexOfPayeePersonId)) {
                final Long _tmpKey_6 = _cursor.getLong(_cursorIndexOfPayeePersonId);
                _tmpPayeePerson = _collectionPayeePerson.get(_tmpKey_6);
              }
              PersonModel _tmpPayerPerson = null;
              if (!_cursor.isNull(_cursorIndexOfPayerPersonId)) {
                final Long _tmpKey_7 = _cursor.getLong(_cursorIndexOfPayerPersonId);
                _tmpPayerPerson = _collectionPayerPerson.get(_tmpKey_7);
              }
              _item = new TransactionHistoryModel();
              final Long _tmpId;
              if (_cursor.isNull(_cursorIndexOfId)) {
                _tmpId = null;
              } else {
                _tmpId = _cursor.getLong(_cursorIndexOfId);
              }
              _item.setId(_tmpId);
              final Long _tmpPayeeAccountId;
              if (_cursor.isNull(_cursorIndexOfPayeeAccountId)) {
                _tmpPayeeAccountId = null;
              } else {
                _tmpPayeeAccountId = _cursor.getLong(_cursorIndexOfPayeeAccountId);
              }
              _item.setPayeeAccountId(_tmpPayeeAccountId);
              final Long _tmpPayerAccountId;
              if (_cursor.isNull(_cursorIndexOfPayerAccountId)) {
                _tmpPayerAccountId = null;
              } else {
                _tmpPayerAccountId = _cursor.getLong(_cursorIndexOfPayerAccountId);
              }
              _item.setPayerAccountId(_tmpPayerAccountId);
              final Long _tmpPayeePersonId;
              if (_cursor.isNull(_cursorIndexOfPayeePersonId)) {
                _tmpPayeePersonId = null;
              } else {
                _tmpPayeePersonId = _cursor.getLong(_cursorIndexOfPayeePersonId);
              }
              _item.setPayeePersonId(_tmpPayeePersonId);
              final Long _tmpPayerPersonId;
              if (_cursor.isNull(_cursorIndexOfPayerPersonId)) {
                _tmpPayerPersonId = null;
              } else {
                _tmpPayerPersonId = _cursor.getLong(_cursorIndexOfPayerPersonId);
              }
              _item.setPayerPersonId(_tmpPayerPersonId);
              final TransactionType _tmpType;
              _tmpType = __TransactionType_stringToEnum(_cursor.getString(_cursorIndexOfType));
              _item.setType(_tmpType);
              final Currency _tmpAmount;
              final String _tmp_2;
              if (_cursor.isNull(_cursorIndexOfAmount)) {
                _tmp_2 = null;
              } else {
                _tmp_2 = _cursor.getString(_cursorIndexOfAmount);
              }
              _tmpAmount = Converters.stringToCurrency(_tmp_2);
              _item.setAmount(_tmpAmount);
              final LocalDate _tmpWhen;
              final String _tmp_3;
              if (_cursor.isNull(_cursorIndexOfWhen)) {
                _tmp_3 = null;
              } else {
                _tmp_3 = _cursor.getString(_cursorIndexOfWhen);
              }
              _tmpWhen = Converters.stringToLocalDate(_tmp_3);
              _item.setWhen(_tmpWhen);
              final String _tmpDescription;
              if (_cursor.isNull(_cursorIndexOfDescription)) {
                _tmpDescription = null;
              } else {
                _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
              }
              _item.setDescription(_tmpDescription);
              _item.setPayeeAccount(_tmpPayeeAccount);
              _item.setPayerAccount(_tmpPayerAccount);
              _item.setPayeePerson(_tmpPayeePerson);
              _item.setPayerPerson(_tmpPayerPerson);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<TransactionHistoryModel>> getAllTransactionHistoriesForAccountsBetweenLive(
      final long id, final LocalDate start, final LocalDate end) {
    final String _sql = "SELECT * FROM `transaction_histories` WHERE DATE(`when`) BETWEEN DATE(?) AND DATE(?) AND (`payerAccountId` = ? OR `payeeAccountId` = ?) ORDER BY DATE(`when`) DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    final String _tmp = Converters.localDateToString(start);
    if (_tmp == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, _tmp);
    }
    _argIndex = 2;
    final String _tmp_1 = Converters.localDateToString(end);
    if (_tmp_1 == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, _tmp_1);
    }
    _argIndex = 3;
    _statement.bindLong(_argIndex, id);
    _argIndex = 4;
    _statement.bindLong(_argIndex, id);
    return __db.getInvalidationTracker().createLiveData(new String[]{"accounts","people","transaction_histories"}, true, new Callable<List<TransactionHistoryModel>>() {
      @Override
      public List<TransactionHistoryModel> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfPayeeAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "payeeAccountId");
            final int _cursorIndexOfPayerAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "payerAccountId");
            final int _cursorIndexOfPayeePersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "payeePersonId");
            final int _cursorIndexOfPayerPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "payerPersonId");
            final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
            final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
            final int _cursorIndexOfWhen = CursorUtil.getColumnIndexOrThrow(_cursor, "when");
            final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
            final HashMap<Long, AccountModel> _collectionPayeeAccount = new HashMap<Long, AccountModel>();
            final HashMap<Long, AccountModel> _collectionPayerAccount = new HashMap<Long, AccountModel>();
            final HashMap<Long, PersonModel> _collectionPayeePerson = new HashMap<Long, PersonModel>();
            final HashMap<Long, PersonModel> _collectionPayerPerson = new HashMap<Long, PersonModel>();
            while (_cursor.moveToNext()) {
              if (!_cursor.isNull(_cursorIndexOfPayeeAccountId)) {
                final Long _tmpKey = _cursor.getLong(_cursorIndexOfPayeeAccountId);
                _collectionPayeeAccount.put(_tmpKey, null);
              }
              if (!_cursor.isNull(_cursorIndexOfPayerAccountId)) {
                final Long _tmpKey_1 = _cursor.getLong(_cursorIndexOfPayerAccountId);
                _collectionPayerAccount.put(_tmpKey_1, null);
              }
              if (!_cursor.isNull(_cursorIndexOfPayeePersonId)) {
                final Long _tmpKey_2 = _cursor.getLong(_cursorIndexOfPayeePersonId);
                _collectionPayeePerson.put(_tmpKey_2, null);
              }
              if (!_cursor.isNull(_cursorIndexOfPayerPersonId)) {
                final Long _tmpKey_3 = _cursor.getLong(_cursorIndexOfPayerPersonId);
                _collectionPayerPerson.put(_tmpKey_3, null);
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipaccountsAsdreammakerAndroidExpensetrackerDatabaseModelAccountModel(_collectionPayeeAccount);
            __fetchRelationshipaccountsAsdreammakerAndroidExpensetrackerDatabaseModelAccountModel(_collectionPayerAccount);
            __fetchRelationshippeopleAsdreammakerAndroidExpensetrackerDatabaseModelPersonModel(_collectionPayeePerson);
            __fetchRelationshippeopleAsdreammakerAndroidExpensetrackerDatabaseModelPersonModel(_collectionPayerPerson);
            final List<TransactionHistoryModel> _result = new ArrayList<TransactionHistoryModel>(_cursor.getCount());
            while(_cursor.moveToNext()) {
              final TransactionHistoryModel _item;
              AccountModel _tmpPayeeAccount = null;
              if (!_cursor.isNull(_cursorIndexOfPayeeAccountId)) {
                final Long _tmpKey_4 = _cursor.getLong(_cursorIndexOfPayeeAccountId);
                _tmpPayeeAccount = _collectionPayeeAccount.get(_tmpKey_4);
              }
              AccountModel _tmpPayerAccount = null;
              if (!_cursor.isNull(_cursorIndexOfPayerAccountId)) {
                final Long _tmpKey_5 = _cursor.getLong(_cursorIndexOfPayerAccountId);
                _tmpPayerAccount = _collectionPayerAccount.get(_tmpKey_5);
              }
              PersonModel _tmpPayeePerson = null;
              if (!_cursor.isNull(_cursorIndexOfPayeePersonId)) {
                final Long _tmpKey_6 = _cursor.getLong(_cursorIndexOfPayeePersonId);
                _tmpPayeePerson = _collectionPayeePerson.get(_tmpKey_6);
              }
              PersonModel _tmpPayerPerson = null;
              if (!_cursor.isNull(_cursorIndexOfPayerPersonId)) {
                final Long _tmpKey_7 = _cursor.getLong(_cursorIndexOfPayerPersonId);
                _tmpPayerPerson = _collectionPayerPerson.get(_tmpKey_7);
              }
              _item = new TransactionHistoryModel();
              final Long _tmpId;
              if (_cursor.isNull(_cursorIndexOfId)) {
                _tmpId = null;
              } else {
                _tmpId = _cursor.getLong(_cursorIndexOfId);
              }
              _item.setId(_tmpId);
              final Long _tmpPayeeAccountId;
              if (_cursor.isNull(_cursorIndexOfPayeeAccountId)) {
                _tmpPayeeAccountId = null;
              } else {
                _tmpPayeeAccountId = _cursor.getLong(_cursorIndexOfPayeeAccountId);
              }
              _item.setPayeeAccountId(_tmpPayeeAccountId);
              final Long _tmpPayerAccountId;
              if (_cursor.isNull(_cursorIndexOfPayerAccountId)) {
                _tmpPayerAccountId = null;
              } else {
                _tmpPayerAccountId = _cursor.getLong(_cursorIndexOfPayerAccountId);
              }
              _item.setPayerAccountId(_tmpPayerAccountId);
              final Long _tmpPayeePersonId;
              if (_cursor.isNull(_cursorIndexOfPayeePersonId)) {
                _tmpPayeePersonId = null;
              } else {
                _tmpPayeePersonId = _cursor.getLong(_cursorIndexOfPayeePersonId);
              }
              _item.setPayeePersonId(_tmpPayeePersonId);
              final Long _tmpPayerPersonId;
              if (_cursor.isNull(_cursorIndexOfPayerPersonId)) {
                _tmpPayerPersonId = null;
              } else {
                _tmpPayerPersonId = _cursor.getLong(_cursorIndexOfPayerPersonId);
              }
              _item.setPayerPersonId(_tmpPayerPersonId);
              final TransactionType _tmpType;
              _tmpType = __TransactionType_stringToEnum(_cursor.getString(_cursorIndexOfType));
              _item.setType(_tmpType);
              final Currency _tmpAmount;
              final String _tmp_2;
              if (_cursor.isNull(_cursorIndexOfAmount)) {
                _tmp_2 = null;
              } else {
                _tmp_2 = _cursor.getString(_cursorIndexOfAmount);
              }
              _tmpAmount = Converters.stringToCurrency(_tmp_2);
              _item.setAmount(_tmpAmount);
              final LocalDate _tmpWhen;
              final String _tmp_3;
              if (_cursor.isNull(_cursorIndexOfWhen)) {
                _tmp_3 = null;
              } else {
                _tmp_3 = _cursor.getString(_cursorIndexOfWhen);
              }
              _tmpWhen = Converters.stringToLocalDate(_tmp_3);
              _item.setWhen(_tmpWhen);
              final String _tmpDescription;
              if (_cursor.isNull(_cursorIndexOfDescription)) {
                _tmpDescription = null;
              } else {
                _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
              }
              _item.setDescription(_tmpDescription);
              _item.setPayeeAccount(_tmpPayeeAccount);
              _item.setPayerAccount(_tmpPayerAccount);
              _item.setPayeePerson(_tmpPayeePerson);
              _item.setPayerPerson(_tmpPayerPerson);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<TransactionHistoryModel>> getAllTransactionHistoriesForPeopleBetweenLive(
      final long id, final LocalDate start, final LocalDate end) {
    final String _sql = "SELECT * FROM `transaction_histories` WHERE DATE(`when`) BETWEEN DATE(?) AND DATE(?) AND (`payerPersonId` = ? OR `payeePersonId` = ?) ORDER BY DATE(`when`) DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    final String _tmp = Converters.localDateToString(start);
    if (_tmp == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, _tmp);
    }
    _argIndex = 2;
    final String _tmp_1 = Converters.localDateToString(end);
    if (_tmp_1 == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, _tmp_1);
    }
    _argIndex = 3;
    _statement.bindLong(_argIndex, id);
    _argIndex = 4;
    _statement.bindLong(_argIndex, id);
    return __db.getInvalidationTracker().createLiveData(new String[]{"accounts","people","transaction_histories"}, true, new Callable<List<TransactionHistoryModel>>() {
      @Override
      public List<TransactionHistoryModel> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfPayeeAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "payeeAccountId");
            final int _cursorIndexOfPayerAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "payerAccountId");
            final int _cursorIndexOfPayeePersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "payeePersonId");
            final int _cursorIndexOfPayerPersonId = CursorUtil.getColumnIndexOrThrow(_cursor, "payerPersonId");
            final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
            final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
            final int _cursorIndexOfWhen = CursorUtil.getColumnIndexOrThrow(_cursor, "when");
            final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
            final HashMap<Long, AccountModel> _collectionPayeeAccount = new HashMap<Long, AccountModel>();
            final HashMap<Long, AccountModel> _collectionPayerAccount = new HashMap<Long, AccountModel>();
            final HashMap<Long, PersonModel> _collectionPayeePerson = new HashMap<Long, PersonModel>();
            final HashMap<Long, PersonModel> _collectionPayerPerson = new HashMap<Long, PersonModel>();
            while (_cursor.moveToNext()) {
              if (!_cursor.isNull(_cursorIndexOfPayeeAccountId)) {
                final Long _tmpKey = _cursor.getLong(_cursorIndexOfPayeeAccountId);
                _collectionPayeeAccount.put(_tmpKey, null);
              }
              if (!_cursor.isNull(_cursorIndexOfPayerAccountId)) {
                final Long _tmpKey_1 = _cursor.getLong(_cursorIndexOfPayerAccountId);
                _collectionPayerAccount.put(_tmpKey_1, null);
              }
              if (!_cursor.isNull(_cursorIndexOfPayeePersonId)) {
                final Long _tmpKey_2 = _cursor.getLong(_cursorIndexOfPayeePersonId);
                _collectionPayeePerson.put(_tmpKey_2, null);
              }
              if (!_cursor.isNull(_cursorIndexOfPayerPersonId)) {
                final Long _tmpKey_3 = _cursor.getLong(_cursorIndexOfPayerPersonId);
                _collectionPayerPerson.put(_tmpKey_3, null);
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipaccountsAsdreammakerAndroidExpensetrackerDatabaseModelAccountModel(_collectionPayeeAccount);
            __fetchRelationshipaccountsAsdreammakerAndroidExpensetrackerDatabaseModelAccountModel(_collectionPayerAccount);
            __fetchRelationshippeopleAsdreammakerAndroidExpensetrackerDatabaseModelPersonModel(_collectionPayeePerson);
            __fetchRelationshippeopleAsdreammakerAndroidExpensetrackerDatabaseModelPersonModel(_collectionPayerPerson);
            final List<TransactionHistoryModel> _result = new ArrayList<TransactionHistoryModel>(_cursor.getCount());
            while(_cursor.moveToNext()) {
              final TransactionHistoryModel _item;
              AccountModel _tmpPayeeAccount = null;
              if (!_cursor.isNull(_cursorIndexOfPayeeAccountId)) {
                final Long _tmpKey_4 = _cursor.getLong(_cursorIndexOfPayeeAccountId);
                _tmpPayeeAccount = _collectionPayeeAccount.get(_tmpKey_4);
              }
              AccountModel _tmpPayerAccount = null;
              if (!_cursor.isNull(_cursorIndexOfPayerAccountId)) {
                final Long _tmpKey_5 = _cursor.getLong(_cursorIndexOfPayerAccountId);
                _tmpPayerAccount = _collectionPayerAccount.get(_tmpKey_5);
              }
              PersonModel _tmpPayeePerson = null;
              if (!_cursor.isNull(_cursorIndexOfPayeePersonId)) {
                final Long _tmpKey_6 = _cursor.getLong(_cursorIndexOfPayeePersonId);
                _tmpPayeePerson = _collectionPayeePerson.get(_tmpKey_6);
              }
              PersonModel _tmpPayerPerson = null;
              if (!_cursor.isNull(_cursorIndexOfPayerPersonId)) {
                final Long _tmpKey_7 = _cursor.getLong(_cursorIndexOfPayerPersonId);
                _tmpPayerPerson = _collectionPayerPerson.get(_tmpKey_7);
              }
              _item = new TransactionHistoryModel();
              final Long _tmpId;
              if (_cursor.isNull(_cursorIndexOfId)) {
                _tmpId = null;
              } else {
                _tmpId = _cursor.getLong(_cursorIndexOfId);
              }
              _item.setId(_tmpId);
              final Long _tmpPayeeAccountId;
              if (_cursor.isNull(_cursorIndexOfPayeeAccountId)) {
                _tmpPayeeAccountId = null;
              } else {
                _tmpPayeeAccountId = _cursor.getLong(_cursorIndexOfPayeeAccountId);
              }
              _item.setPayeeAccountId(_tmpPayeeAccountId);
              final Long _tmpPayerAccountId;
              if (_cursor.isNull(_cursorIndexOfPayerAccountId)) {
                _tmpPayerAccountId = null;
              } else {
                _tmpPayerAccountId = _cursor.getLong(_cursorIndexOfPayerAccountId);
              }
              _item.setPayerAccountId(_tmpPayerAccountId);
              final Long _tmpPayeePersonId;
              if (_cursor.isNull(_cursorIndexOfPayeePersonId)) {
                _tmpPayeePersonId = null;
              } else {
                _tmpPayeePersonId = _cursor.getLong(_cursorIndexOfPayeePersonId);
              }
              _item.setPayeePersonId(_tmpPayeePersonId);
              final Long _tmpPayerPersonId;
              if (_cursor.isNull(_cursorIndexOfPayerPersonId)) {
                _tmpPayerPersonId = null;
              } else {
                _tmpPayerPersonId = _cursor.getLong(_cursorIndexOfPayerPersonId);
              }
              _item.setPayerPersonId(_tmpPayerPersonId);
              final TransactionType _tmpType;
              _tmpType = __TransactionType_stringToEnum(_cursor.getString(_cursorIndexOfType));
              _item.setType(_tmpType);
              final Currency _tmpAmount;
              final String _tmp_2;
              if (_cursor.isNull(_cursorIndexOfAmount)) {
                _tmp_2 = null;
              } else {
                _tmp_2 = _cursor.getString(_cursorIndexOfAmount);
              }
              _tmpAmount = Converters.stringToCurrency(_tmp_2);
              _item.setAmount(_tmpAmount);
              final LocalDate _tmpWhen;
              final String _tmp_3;
              if (_cursor.isNull(_cursorIndexOfWhen)) {
                _tmp_3 = null;
              } else {
                _tmp_3 = _cursor.getString(_cursorIndexOfWhen);
              }
              _tmpWhen = Converters.stringToLocalDate(_tmp_3);
              _item.setWhen(_tmpWhen);
              final String _tmpDescription;
              if (_cursor.isNull(_cursorIndexOfDescription)) {
                _tmpDescription = null;
              } else {
                _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
              }
              _item.setDescription(_tmpDescription);
              _item.setPayeeAccount(_tmpPayeeAccount);
              _item.setPayerAccount(_tmpPayerAccount);
              _item.setPayeePerson(_tmpPayeePerson);
              _item.setPayerPerson(_tmpPayerPerson);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  protected Account findAccountById(final long id) {
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
  protected Person findPersonById(final long id) {
    final String _sql = "SELECT * FROM `people` WHERE `id` = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfFirstName = CursorUtil.getColumnIndexOrThrow(_cursor, "firstName");
      final int _cursorIndexOfLastName = CursorUtil.getColumnIndexOrThrow(_cursor, "lastName");
      final int _cursorIndexOfDue = CursorUtil.getColumnIndexOrThrow(_cursor, "due");
      final int _cursorIndexOfBorrow = CursorUtil.getColumnIndexOrThrow(_cursor, "borrow");
      final Person _result;
      if(_cursor.moveToFirst()) {
        _result = new Person();
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        _result.setId(_tmpId);
        final String _tmpFirstName;
        if (_cursor.isNull(_cursorIndexOfFirstName)) {
          _tmpFirstName = null;
        } else {
          _tmpFirstName = _cursor.getString(_cursorIndexOfFirstName);
        }
        _result.setFirstName(_tmpFirstName);
        final String _tmpLastName;
        if (_cursor.isNull(_cursorIndexOfLastName)) {
          _tmpLastName = null;
        } else {
          _tmpLastName = _cursor.getString(_cursorIndexOfLastName);
        }
        _result.setLastName(_tmpLastName);
        final Currency _tmpDue;
        final String _tmp;
        if (_cursor.isNull(_cursorIndexOfDue)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getString(_cursorIndexOfDue);
        }
        _tmpDue = Converters.stringToCurrency(_tmp);
        _result.setDue(_tmpDue);
        final Currency _tmpBorrow;
        final String _tmp_1;
        if (_cursor.isNull(_cursorIndexOfBorrow)) {
          _tmp_1 = null;
        } else {
          _tmp_1 = _cursor.getString(_cursorIndexOfBorrow);
        }
        _tmpBorrow = Converters.stringToCurrency(_tmp_1);
        _result.setBorrow(_tmpBorrow);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __TransactionType_enumToString(final TransactionType _value) {
    if (_value == null) {
      return null;
    } switch (_value) {
      case INCOME: return "INCOME";
      case EXPENSE: return "EXPENSE";
      case DUE: return "DUE";
      case BORROW: return "BORROW";
      case PAY_BORROW: return "PAY_BORROW";
      case PAY_DUE: return "PAY_DUE";
      case MONEY_TRANSFER: return "MONEY_TRANSFER";
      case DUE_TRANSFER: return "DUE_TRANSFER";
      case BORROW_TRANSFER: return "BORROW_TRANSFER";
      case BORROW_TO_DUE_TRANSFER: return "BORROW_TO_DUE_TRANSFER";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private TransactionType __TransactionType_stringToEnum(final String _value) {
    if (_value == null) {
      return null;
    } switch (_value) {
      case "INCOME": return TransactionType.INCOME;
      case "EXPENSE": return TransactionType.EXPENSE;
      case "DUE": return TransactionType.DUE;
      case "BORROW": return TransactionType.BORROW;
      case "PAY_BORROW": return TransactionType.PAY_BORROW;
      case "PAY_DUE": return TransactionType.PAY_DUE;
      case "MONEY_TRANSFER": return TransactionType.MONEY_TRANSFER;
      case "DUE_TRANSFER": return TransactionType.DUE_TRANSFER;
      case "BORROW_TRANSFER": return TransactionType.BORROW_TRANSFER;
      case "BORROW_TO_DUE_TRANSFER": return TransactionType.BORROW_TO_DUE_TRANSFER;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }

  private void __fetchRelationshipaccountsAsdreammakerAndroidExpensetrackerDatabaseModelAccountModel(
      final HashMap<Long, AccountModel> _map) {
    final Set<Long> __mapKeySet = _map.keySet();
    if (__mapKeySet.isEmpty()) {
      return;
    }
    // check if the size is too big, if so divide;
    if(_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      HashMap<Long, AccountModel> _tmpInnerMap = new HashMap<Long, AccountModel>(androidx.room.RoomDatabase.MAX_BIND_PARAMETER_CNT);
      int _tmpIndex = 0;
      for(Long _mapKey : __mapKeySet) {
        _tmpInnerMap.put(_mapKey, null);
        _tmpIndex++;
        if(_tmpIndex == RoomDatabase.MAX_BIND_PARAMETER_CNT) {
          __fetchRelationshipaccountsAsdreammakerAndroidExpensetrackerDatabaseModelAccountModel(_tmpInnerMap);
          _map.putAll(_tmpInnerMap);
          _tmpInnerMap = new HashMap<Long, AccountModel>(RoomDatabase.MAX_BIND_PARAMETER_CNT);
          _tmpIndex = 0;
        }
      }
      if(_tmpIndex > 0) {
        __fetchRelationshipaccountsAsdreammakerAndroidExpensetrackerDatabaseModelAccountModel(_tmpInnerMap);
        _map.putAll(_tmpInnerMap);
      }
      return;
    }
    StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`name` FROM `accounts` WHERE `id` IN (");
    final int _inputSize = __mapKeySet.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (Long _item : __mapKeySet) {
      if (_item == null) {
        _stmt.bindNull(_argIndex);
      } else {
        _stmt.bindLong(_argIndex, _item);
      }
      _argIndex ++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "id");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfName = 1;
      while(_cursor.moveToNext()) {
        if (!_cursor.isNull(_itemKeyIndex)) {
          final Long _tmpKey = _cursor.getLong(_itemKeyIndex);
          if (_map.containsKey(_tmpKey)) {
            final AccountModel _item_1;
            _item_1 = new AccountModel();
            final Long _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getLong(_cursorIndexOfId);
            }
            _item_1.setId(_tmpId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            _item_1.setName(_tmpName);
            _map.put(_tmpKey, _item_1);
          }
        }
      }
    } finally {
      _cursor.close();
    }
  }

  private void __fetchRelationshippeopleAsdreammakerAndroidExpensetrackerDatabaseModelPersonModel(
      final HashMap<Long, PersonModel> _map) {
    final Set<Long> __mapKeySet = _map.keySet();
    if (__mapKeySet.isEmpty()) {
      return;
    }
    // check if the size is too big, if so divide;
    if(_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      HashMap<Long, PersonModel> _tmpInnerMap = new HashMap<Long, PersonModel>(androidx.room.RoomDatabase.MAX_BIND_PARAMETER_CNT);
      int _tmpIndex = 0;
      for(Long _mapKey : __mapKeySet) {
        _tmpInnerMap.put(_mapKey, null);
        _tmpIndex++;
        if(_tmpIndex == RoomDatabase.MAX_BIND_PARAMETER_CNT) {
          __fetchRelationshippeopleAsdreammakerAndroidExpensetrackerDatabaseModelPersonModel(_tmpInnerMap);
          _map.putAll(_tmpInnerMap);
          _tmpInnerMap = new HashMap<Long, PersonModel>(RoomDatabase.MAX_BIND_PARAMETER_CNT);
          _tmpIndex = 0;
        }
      }
      if(_tmpIndex > 0) {
        __fetchRelationshippeopleAsdreammakerAndroidExpensetrackerDatabaseModelPersonModel(_tmpInnerMap);
        _map.putAll(_tmpInnerMap);
      }
      return;
    }
    StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`firstName`,`lastName` FROM `people` WHERE `id` IN (");
    final int _inputSize = __mapKeySet.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (Long _item : __mapKeySet) {
      if (_item == null) {
        _stmt.bindNull(_argIndex);
      } else {
        _stmt.bindLong(_argIndex, _item);
      }
      _argIndex ++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "id");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfFirstName = 1;
      final int _cursorIndexOfLastName = 2;
      while(_cursor.moveToNext()) {
        if (!_cursor.isNull(_itemKeyIndex)) {
          final Long _tmpKey = _cursor.getLong(_itemKeyIndex);
          if (_map.containsKey(_tmpKey)) {
            final PersonModel _item_1;
            _item_1 = new PersonModel();
            final Long _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getLong(_cursorIndexOfId);
            }
            _item_1.setId(_tmpId);
            final String _tmpFirstName;
            if (_cursor.isNull(_cursorIndexOfFirstName)) {
              _tmpFirstName = null;
            } else {
              _tmpFirstName = _cursor.getString(_cursorIndexOfFirstName);
            }
            _item_1.setFirstName(_tmpFirstName);
            final String _tmpLastName;
            if (_cursor.isNull(_cursorIndexOfLastName)) {
              _tmpLastName = null;
            } else {
              _tmpLastName = _cursor.getString(_cursorIndexOfLastName);
            }
            _item_1.setLastName(_tmpLastName);
            _map.put(_tmpKey, _item_1);
          }
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
