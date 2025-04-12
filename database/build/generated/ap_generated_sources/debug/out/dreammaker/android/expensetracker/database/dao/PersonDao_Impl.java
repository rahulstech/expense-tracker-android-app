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
import dreammaker.android.expensetracker.database.entity.Person;
import dreammaker.android.expensetracker.database.model.PersonModel;
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
public final class PersonDao_Impl implements PersonDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Person> __insertionAdapterOfPerson;

  private final EntityDeletionOrUpdateAdapter<Person> __updateAdapterOfPerson;

  public PersonDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPerson = new EntityInsertionAdapter<Person>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `people` (`id`,`firstName`,`lastName`,`due`,`borrow`) VALUES (nullif(?, 0),?,?,?,?)";
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
  }

  @Override
  public long addPerson(final Person person) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      long _result = __insertionAdapterOfPerson.insertAndReturnId(person);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public int updatePerson(final Person person) {
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
  public LiveData<List<PersonModel>> getAllPeopleLive() {
    final String _sql = "SELECT `id`,`firstName`,`lastName`,`due`,`borrow` FROM `people`";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"people"}, false, new Callable<List<PersonModel>>() {
      @Override
      public List<PersonModel> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = 0;
          final int _cursorIndexOfFirstName = 1;
          final int _cursorIndexOfLastName = 2;
          final int _cursorIndexOfDue = 3;
          final int _cursorIndexOfBorrow = 4;
          final List<PersonModel> _result = new ArrayList<PersonModel>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final PersonModel _item;
            _item = new PersonModel();
            final Long _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getLong(_cursorIndexOfId);
            }
            _item.setId(_tmpId);
            final String _tmpFirstName;
            if (_cursor.isNull(_cursorIndexOfFirstName)) {
              _tmpFirstName = null;
            } else {
              _tmpFirstName = _cursor.getString(_cursorIndexOfFirstName);
            }
            _item.setFirstName(_tmpFirstName);
            final String _tmpLastName;
            if (_cursor.isNull(_cursorIndexOfLastName)) {
              _tmpLastName = null;
            } else {
              _tmpLastName = _cursor.getString(_cursorIndexOfLastName);
            }
            _item.setLastName(_tmpLastName);
            final Currency _tmpDue;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfDue)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfDue);
            }
            _tmpDue = Converters.stringToCurrency(_tmp);
            _item.setDue(_tmpDue);
            final Currency _tmpBorrow;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfBorrow)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfBorrow);
            }
            _tmpBorrow = Converters.stringToCurrency(_tmp_1);
            _item.setBorrow(_tmpBorrow);
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
  public LiveData<List<PersonModel>> getAllPeopleWithUsageCountLive() {
    final String _sql = "SELECT `id`,`firstName`,`lastName`,`due`,`borrow`, (SELECT COUNT(`id`) FROM `transaction_histories` WHERE `payeePersonId` = `people`.`id` OR `payerPersonId` = `people`.`id`) AS `usageCount` FROM `people`";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"transaction_histories","people"}, false, new Callable<List<PersonModel>>() {
      @Override
      public List<PersonModel> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = 0;
          final int _cursorIndexOfFirstName = 1;
          final int _cursorIndexOfLastName = 2;
          final int _cursorIndexOfDue = 3;
          final int _cursorIndexOfBorrow = 4;
          final int _cursorIndexOfUsageCount = 5;
          final List<PersonModel> _result = new ArrayList<PersonModel>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final PersonModel _item;
            _item = new PersonModel();
            final Long _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getLong(_cursorIndexOfId);
            }
            _item.setId(_tmpId);
            final String _tmpFirstName;
            if (_cursor.isNull(_cursorIndexOfFirstName)) {
              _tmpFirstName = null;
            } else {
              _tmpFirstName = _cursor.getString(_cursorIndexOfFirstName);
            }
            _item.setFirstName(_tmpFirstName);
            final String _tmpLastName;
            if (_cursor.isNull(_cursorIndexOfLastName)) {
              _tmpLastName = null;
            } else {
              _tmpLastName = _cursor.getString(_cursorIndexOfLastName);
            }
            _item.setLastName(_tmpLastName);
            final Currency _tmpDue;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfDue)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfDue);
            }
            _tmpDue = Converters.stringToCurrency(_tmp);
            _item.setDue(_tmpDue);
            final Currency _tmpBorrow;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfBorrow)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfBorrow);
            }
            _tmpBorrow = Converters.stringToCurrency(_tmp_1);
            _item.setBorrow(_tmpBorrow);
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
  public LiveData<PersonModel> getPersonByIdLive(final long id) {
    final String _sql = "SELECT * FROM `people` WHERE `id` = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return __db.getInvalidationTracker().createLiveData(new String[]{"people"}, false, new Callable<PersonModel>() {
      @Override
      public PersonModel call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFirstName = CursorUtil.getColumnIndexOrThrow(_cursor, "firstName");
          final int _cursorIndexOfLastName = CursorUtil.getColumnIndexOrThrow(_cursor, "lastName");
          final int _cursorIndexOfDue = CursorUtil.getColumnIndexOrThrow(_cursor, "due");
          final int _cursorIndexOfBorrow = CursorUtil.getColumnIndexOrThrow(_cursor, "borrow");
          final PersonModel _result;
          if(_cursor.moveToFirst()) {
            _result = new PersonModel();
            final Long _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getLong(_cursorIndexOfId);
            }
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
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Person findPersonById(final long id) {
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

  @Override
  public int removePeople(final long[] ids) {
    __db.assertNotSuspendingTransaction();
    StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("DELETE FROM `people` WHERE `id` IN(");
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
