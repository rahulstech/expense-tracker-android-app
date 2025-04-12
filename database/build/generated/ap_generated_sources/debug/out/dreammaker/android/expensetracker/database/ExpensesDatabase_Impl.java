package dreammaker.android.expensetracker.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenHelper;
import androidx.room.RoomOpenHelper.Delegate;
import androidx.room.RoomOpenHelper.ValidationResult;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.room.util.TableInfo.Column;
import androidx.room.util.TableInfo.ForeignKey;
import androidx.room.util.TableInfo.Index;
import androidx.room.util.ViewInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Callback;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration;
import dreammaker.android.expensetracker.database.dao.AccountDao;
import dreammaker.android.expensetracker.database.dao.AccountDao_Impl;
import dreammaker.android.expensetracker.database.dao.AnalyticsDao;
import dreammaker.android.expensetracker.database.dao.AnalyticsDao_Impl;
import dreammaker.android.expensetracker.database.dao.PersonDao;
import dreammaker.android.expensetracker.database.dao.PersonDao_Impl;
import dreammaker.android.expensetracker.database.dao.TransactionHistoryDao;
import dreammaker.android.expensetracker.database.dao.TransactionHistoryDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "deprecation"})
public final class ExpensesDatabase_Impl extends ExpensesDatabase {
  private volatile AccountDao _accountDao;

  private volatile PersonDao _personDao;

  private volatile TransactionHistoryDao _transactionHistoryDao;

  private volatile AnalyticsDao _analyticsDao;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(7) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `balance` TEXT NOT NULL)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `people` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT, `due` TEXT NOT NULL, `borrow` TEXT NOT NULL)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `transaction_histories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `payeeAccountId` INTEGER, `payerAccountId` INTEGER, `payeePersonId` INTEGER, `payerPersonId` INTEGER, `type` TEXT NOT NULL, `amount` TEXT NOT NULL, `when` TEXT NOT NULL, `description` TEXT)");
        _db.execSQL("CREATE VIEW `accounts_summary_view` AS SELECT SUM(CASE WHEN CAST(`balance` AS REAL) > 0 THEN 1 ELSE 0 END) AS `totalPositiveAccounts`, SUM(CASE WHEN CAST(`balance` AS REAL) > 0 THEN `balance` ELSE 0 END) AS `totalPositiveBalance`,  SUM(CASE WHEN CAST(`balance` AS REAL) < 0 THEN 1 ELSE 0 END) AS `totalNegativeAccounts`, SUM(CASE WHEN CAST(`balance` AS REAL) < 0 THEN `balance` ELSE 0 END) AS `totalNegativeBalance` FROM `accounts`");
        _db.execSQL("CREATE VIEW `people_summary_view` AS SELECT  SUM(CASE WHEN CAST(`due` AS REAL) > 0 THEN 1 ELSE 0 END) AS `totalPositiveDuePeople`, SUM(CASE WHEN CAST(`due` AS REAL) > 0 THEN `due` ELSE 0 END) AS `totalPositiveDue`, SUM(CASE WHEN CAST(`due` AS REAL) < 0 THEN 1 ELSE 0 END) AS `totalNegativeDuePeople`, SUM(CASE WHEN CAST(`due` AS REAL) < 0 THEN `due` ELSE 0 END) AS `totalNegativeDue`, SUM(CASE WHEN CAST(`borrow` AS REAL) > 0 THEN 1 ELSE 0 END) AS `totalPositiveBorrowPeople`, SUM(CASE WHEN CAST(`borrow` AS REAL) > 0 THEN `borrow` ELSE 0 END) AS `totalPositiveBorrow`,  SUM(CASE WHEN CAST(`borrow` AS REAL) < 0 THEN 1 ELSE 0 END) AS `totalNegativeBorrowPeople`,  SUM(CASE WHEN CAST(`borrow` AS REAL) < 0 THEN `borrow` ELSE 0 END) AS `totalNegativeBorrow` FROM `people`");
        _db.execSQL("CREATE VIEW `daily_expense_view` AS SELECT DATE(`when`) AS `date`,SUM(`amount`) AS `amount` FROM `transaction_histories` GROUP BY DATE(`when`) ORDER BY `when`");
        _db.execSQL("CREATE VIEW `asset_liability_summary_view` AS SELECT * FROM (SELECT * FROM `accounts_summary_view`), (SELECT * FROM `people_summary_view`), (SELECT COUNT(`id`) AS `totalAccounts` FROM `accounts`), (SELECT COUNT(`id`) AS `totalPeople` FROM `people`)");
        _db.execSQL("CREATE VIEW `monthly_expense_view` AS SELECT DATE(`date`,'start of month') AS `monthYear`,SUM(`amount`) AS `amount` FROM `daily_expense_view` GROUP BY STRFTIME(`date`,'%m-%Y') ORDER BY date");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '85e24f250f1d8b0ebb6bc3c4ba7a2778')");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `accounts`");
        _db.execSQL("DROP TABLE IF EXISTS `people`");
        _db.execSQL("DROP TABLE IF EXISTS `transaction_histories`");
        _db.execSQL("DROP VIEW IF EXISTS `accounts_summary_view`");
        _db.execSQL("DROP VIEW IF EXISTS `people_summary_view`");
        _db.execSQL("DROP VIEW IF EXISTS `daily_expense_view`");
        _db.execSQL("DROP VIEW IF EXISTS `asset_liability_summary_view`");
        _db.execSQL("DROP VIEW IF EXISTS `monthly_expense_view`");
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onDestructiveMigration(_db);
          }
        }
      }

      @Override
      protected void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      @Override
      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      @Override
      public void onPreMigrate(SupportSQLiteDatabase _db) {
        DBUtil.dropFtsSyncTriggers(_db);
      }

      @Override
      public void onPostMigrate(SupportSQLiteDatabase _db) {
      }

      @Override
      protected RoomOpenHelper.ValidationResult onValidateSchema(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsAccounts = new HashMap<String, TableInfo.Column>(3);
        _columnsAccounts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAccounts.put("balance", new TableInfo.Column("balance", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAccounts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAccounts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAccounts = new TableInfo("accounts", _columnsAccounts, _foreignKeysAccounts, _indicesAccounts);
        final TableInfo _existingAccounts = TableInfo.read(_db, "accounts");
        if (! _infoAccounts.equals(_existingAccounts)) {
          return new RoomOpenHelper.ValidationResult(false, "accounts(dreammaker.android.expensetracker.database.entity.Account).\n"
                  + " Expected:\n" + _infoAccounts + "\n"
                  + " Found:\n" + _existingAccounts);
        }
        final HashMap<String, TableInfo.Column> _columnsPeople = new HashMap<String, TableInfo.Column>(5);
        _columnsPeople.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("firstName", new TableInfo.Column("firstName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("lastName", new TableInfo.Column("lastName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("due", new TableInfo.Column("due", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPeople.put("borrow", new TableInfo.Column("borrow", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPeople = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPeople = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPeople = new TableInfo("people", _columnsPeople, _foreignKeysPeople, _indicesPeople);
        final TableInfo _existingPeople = TableInfo.read(_db, "people");
        if (! _infoPeople.equals(_existingPeople)) {
          return new RoomOpenHelper.ValidationResult(false, "people(dreammaker.android.expensetracker.database.entity.Person).\n"
                  + " Expected:\n" + _infoPeople + "\n"
                  + " Found:\n" + _existingPeople);
        }
        final HashMap<String, TableInfo.Column> _columnsTransactionHistories = new HashMap<String, TableInfo.Column>(9);
        _columnsTransactionHistories.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionHistories.put("payeeAccountId", new TableInfo.Column("payeeAccountId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionHistories.put("payerAccountId", new TableInfo.Column("payerAccountId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionHistories.put("payeePersonId", new TableInfo.Column("payeePersonId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionHistories.put("payerPersonId", new TableInfo.Column("payerPersonId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionHistories.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionHistories.put("amount", new TableInfo.Column("amount", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionHistories.put("when", new TableInfo.Column("when", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactionHistories.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTransactionHistories = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTransactionHistories = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTransactionHistories = new TableInfo("transaction_histories", _columnsTransactionHistories, _foreignKeysTransactionHistories, _indicesTransactionHistories);
        final TableInfo _existingTransactionHistories = TableInfo.read(_db, "transaction_histories");
        if (! _infoTransactionHistories.equals(_existingTransactionHistories)) {
          return new RoomOpenHelper.ValidationResult(false, "transaction_histories(dreammaker.android.expensetracker.database.entity.TransactionHistory).\n"
                  + " Expected:\n" + _infoTransactionHistories + "\n"
                  + " Found:\n" + _existingTransactionHistories);
        }
        final ViewInfo _infoAccountsSummaryView = new ViewInfo("accounts_summary_view", "CREATE VIEW `accounts_summary_view` AS SELECT SUM(CASE WHEN CAST(`balance` AS REAL) > 0 THEN 1 ELSE 0 END) AS `totalPositiveAccounts`, SUM(CASE WHEN CAST(`balance` AS REAL) > 0 THEN `balance` ELSE 0 END) AS `totalPositiveBalance`,  SUM(CASE WHEN CAST(`balance` AS REAL) < 0 THEN 1 ELSE 0 END) AS `totalNegativeAccounts`, SUM(CASE WHEN CAST(`balance` AS REAL) < 0 THEN `balance` ELSE 0 END) AS `totalNegativeBalance` FROM `accounts`");
        final ViewInfo _existingAccountsSummaryView = ViewInfo.read(_db, "accounts_summary_view");
        if (! _infoAccountsSummaryView.equals(_existingAccountsSummaryView)) {
          return new RoomOpenHelper.ValidationResult(false, "accounts_summary_view(dreammaker.android.expensetracker.database.view.AccountsSummaryView).\n"
                  + " Expected:\n" + _infoAccountsSummaryView + "\n"
                  + " Found:\n" + _existingAccountsSummaryView);
        }
        final ViewInfo _infoPeopleSummaryView = new ViewInfo("people_summary_view", "CREATE VIEW `people_summary_view` AS SELECT  SUM(CASE WHEN CAST(`due` AS REAL) > 0 THEN 1 ELSE 0 END) AS `totalPositiveDuePeople`, SUM(CASE WHEN CAST(`due` AS REAL) > 0 THEN `due` ELSE 0 END) AS `totalPositiveDue`, SUM(CASE WHEN CAST(`due` AS REAL) < 0 THEN 1 ELSE 0 END) AS `totalNegativeDuePeople`, SUM(CASE WHEN CAST(`due` AS REAL) < 0 THEN `due` ELSE 0 END) AS `totalNegativeDue`, SUM(CASE WHEN CAST(`borrow` AS REAL) > 0 THEN 1 ELSE 0 END) AS `totalPositiveBorrowPeople`, SUM(CASE WHEN CAST(`borrow` AS REAL) > 0 THEN `borrow` ELSE 0 END) AS `totalPositiveBorrow`,  SUM(CASE WHEN CAST(`borrow` AS REAL) < 0 THEN 1 ELSE 0 END) AS `totalNegativeBorrowPeople`,  SUM(CASE WHEN CAST(`borrow` AS REAL) < 0 THEN `borrow` ELSE 0 END) AS `totalNegativeBorrow` FROM `people`");
        final ViewInfo _existingPeopleSummaryView = ViewInfo.read(_db, "people_summary_view");
        if (! _infoPeopleSummaryView.equals(_existingPeopleSummaryView)) {
          return new RoomOpenHelper.ValidationResult(false, "people_summary_view(dreammaker.android.expensetracker.database.view.PeopleSummaryView).\n"
                  + " Expected:\n" + _infoPeopleSummaryView + "\n"
                  + " Found:\n" + _existingPeopleSummaryView);
        }
        final ViewInfo _infoDailyExpenseView = new ViewInfo("daily_expense_view", "CREATE VIEW `daily_expense_view` AS SELECT DATE(`when`) AS `date`,SUM(`amount`) AS `amount` FROM `transaction_histories` GROUP BY DATE(`when`) ORDER BY `when`");
        final ViewInfo _existingDailyExpenseView = ViewInfo.read(_db, "daily_expense_view");
        if (! _infoDailyExpenseView.equals(_existingDailyExpenseView)) {
          return new RoomOpenHelper.ValidationResult(false, "daily_expense_view(dreammaker.android.expensetracker.database.view.DailyExpenseView).\n"
                  + " Expected:\n" + _infoDailyExpenseView + "\n"
                  + " Found:\n" + _existingDailyExpenseView);
        }
        final ViewInfo _infoAssetLiabilitySummaryView = new ViewInfo("asset_liability_summary_view", "CREATE VIEW `asset_liability_summary_view` AS SELECT * FROM (SELECT * FROM `accounts_summary_view`), (SELECT * FROM `people_summary_view`), (SELECT COUNT(`id`) AS `totalAccounts` FROM `accounts`), (SELECT COUNT(`id`) AS `totalPeople` FROM `people`)");
        final ViewInfo _existingAssetLiabilitySummaryView = ViewInfo.read(_db, "asset_liability_summary_view");
        if (! _infoAssetLiabilitySummaryView.equals(_existingAssetLiabilitySummaryView)) {
          return new RoomOpenHelper.ValidationResult(false, "asset_liability_summary_view(dreammaker.android.expensetracker.database.view.AssetLiabilitySummary).\n"
                  + " Expected:\n" + _infoAssetLiabilitySummaryView + "\n"
                  + " Found:\n" + _existingAssetLiabilitySummaryView);
        }
        final ViewInfo _infoMonthlyExpenseView = new ViewInfo("monthly_expense_view", "CREATE VIEW `monthly_expense_view` AS SELECT DATE(`date`,'start of month') AS `monthYear`,SUM(`amount`) AS `amount` FROM `daily_expense_view` GROUP BY STRFTIME(`date`,'%m-%Y') ORDER BY date");
        final ViewInfo _existingMonthlyExpenseView = ViewInfo.read(_db, "monthly_expense_view");
        if (! _infoMonthlyExpenseView.equals(_existingMonthlyExpenseView)) {
          return new RoomOpenHelper.ValidationResult(false, "monthly_expense_view(dreammaker.android.expensetracker.database.view.MonthlyExpenseView).\n"
                  + " Expected:\n" + _infoMonthlyExpenseView + "\n"
                  + " Found:\n" + _existingMonthlyExpenseView);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "85e24f250f1d8b0ebb6bc3c4ba7a2778", "b0b78bf3f2701d1b7efe6beee888e25d");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(5);
    HashSet<String> _tables = new HashSet<String>(1);
    _tables.add("accounts");
    _viewTables.put("accounts_summary_view", _tables);
    HashSet<String> _tables_1 = new HashSet<String>(1);
    _tables_1.add("people");
    _viewTables.put("people_summary_view", _tables_1);
    HashSet<String> _tables_2 = new HashSet<String>(1);
    _tables_2.add("transaction_histories");
    _viewTables.put("daily_expense_view", _tables_2);
    HashSet<String> _tables_3 = new HashSet<String>(2);
    _tables_3.add("accounts");
    _tables_3.add("people");
    _viewTables.put("asset_liability_summary_view", _tables_3);
    HashSet<String> _tables_4 = new HashSet<String>(1);
    _tables_4.add("transaction_histories");
    _viewTables.put("monthly_expense_view", _tables_4);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "accounts","people","transaction_histories");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `accounts`");
      _db.execSQL("DELETE FROM `people`");
      _db.execSQL("DELETE FROM `transaction_histories`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(AccountDao.class, AccountDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PersonDao.class, PersonDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TransactionHistoryDao.class, TransactionHistoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AnalyticsDao.class, AnalyticsDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  public List<Migration> getAutoMigrations(
      @NonNull Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecsMap) {
    return Arrays.asList();
  }

  @Override
  public AccountDao getAccountDao() {
    if (_accountDao != null) {
      return _accountDao;
    } else {
      synchronized(this) {
        if(_accountDao == null) {
          _accountDao = new AccountDao_Impl(this);
        }
        return _accountDao;
      }
    }
  }

  @Override
  public PersonDao getPersonDao() {
    if (_personDao != null) {
      return _personDao;
    } else {
      synchronized(this) {
        if(_personDao == null) {
          _personDao = new PersonDao_Impl(this);
        }
        return _personDao;
      }
    }
  }

  @Override
  public TransactionHistoryDao getTransactionHistoryDao() {
    if (_transactionHistoryDao != null) {
      return _transactionHistoryDao;
    } else {
      synchronized(this) {
        if(_transactionHistoryDao == null) {
          _transactionHistoryDao = new TransactionHistoryDao_Impl(this);
        }
        return _transactionHistoryDao;
      }
    }
  }

  @Override
  public AnalyticsDao getAnalyticsDao() {
    if (_analyticsDao != null) {
      return _analyticsDao;
    } else {
      synchronized(this) {
        if(_analyticsDao == null) {
          _analyticsDao = new AnalyticsDao_Impl(this);
        }
        return _analyticsDao;
      }
    }
  }
}
