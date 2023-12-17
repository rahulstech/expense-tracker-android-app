package dreammaker.android.expensetracker.database.dao;

import java.time.LocalDate;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import dreammaker.android.expensetracker.database.entity.Account;
import dreammaker.android.expensetracker.database.entity.Person;
import dreammaker.android.expensetracker.database.entity.TransactionHistory;
import dreammaker.android.expensetracker.database.model.TransactionHistoryModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;

@Dao
@SuppressWarnings("unused")
public abstract class TransactionHistoryDao {

    private static final String TAG = TransactionHistoryDao.class.getSimpleName();

    @Transaction
    public long addTransactionHistory(TransactionHistory history) {
        long id = insert_transactionHistory(history);
        TransactionType type = history.getType();
        Long payeeAccountId = history.getPayeeAccountId();
        Long payerAccountId = history.getPayerAccountId();
        Long payeePersonId = history.getPayeePersonId();
        Long payerPersonId = history.getPayerPersonId();
        Currency amount = history.getAmount();
        if (id > 0) {
            boolean updated;
            switch (type) {
                case INCOME: {
                    updated = updateAccountBalance(payeeAccountId,amount);
                }
                break;
                case EXPENSE: {
                    updated = updateAccountBalance(payerAccountId,amount.negate());
                }
                break;
                case DUE: {
                    updated = updateAccountBalance(payerAccountId,amount.negate()) && updatePersonDue(payeePersonId,amount);
                }
                break;
                case BORROW: {
                    updated = updateAccountBalance(payeeAccountId,amount) && updatePersonBorrow(payerPersonId,amount);
                }
                break;
                case PAY_DUE: {
                    updated = updateAccountBalance(payeeAccountId,amount) && updatePersonDue(payerPersonId,amount.negate());
                }
                break;
                case PAY_BORROW: {
                    updated = updateAccountBalance(payerAccountId,amount.negate()) &&  updatePersonBorrow(payeePersonId,amount.negate());
                }
                break;
                case MONEY_TRANSFER: {
                    updated = updateAccountBalance(payerAccountId,amount.negate()) && updateAccountBalance(payeeAccountId,amount);
                }
                break;
                case DUE_TRANSFER: {
                    updated = updatePersonDue(payerPersonId,amount.negate()) &&  updatePersonDue(payeePersonId,amount);
                }
                break;
                case BORROW_TRANSFER: {
                    updated = updatePersonBorrow(payerPersonId,amount.negate()) && updatePersonBorrow(payeePersonId,amount);
                }
                break;
                case BORROW_TO_DUE_TRANSFER: {
                    updated = updatePersonBorrow(payerAccountId,amount.negate()) &&  updatePersonDue(payeePersonId,amount.negate());
                }
                break;
                default: {
                    updated = false;
                }
            }
            if (updated) {
                return id;
            }
        }
        return 0;
    }

    @Transaction
    public int updateTransactionHistory(TransactionHistory history) {
        TransactionHistory old = findTransactionHistoryById(history.getId());
        TransactionType type = history.getType();
        Long payeeAccountId = history.getPayeeAccountId();
        Long payerAccountId = history.getPayerAccountId();
        Long payeePersonId = history.getPayeePersonId();
        Long payerPersonId = history.getPayerPersonId();
        Currency amount = history.getAmount();
        Currency old_amount = old.getAmount();
        if (1 == update_transactionHistory(history)) {
            boolean updated;
            switch (type) {
                case INCOME: {
                    Currency change = old_amount.negate().add(amount);
                    updated = updateAccountBalance(payeeAccountId,change);
                }
                break;
                case EXPENSE: {
                    Currency change = old_amount.add(amount.negate());
                    updated = updateAccountBalance(payerAccountId,change);
                }
                break;
                case DUE: {
                    Currency balanceChange = old_amount.add(amount.negate());
                    Currency dueChange = old_amount.negate().add(amount);
                    updated = updateAccountBalance(payerAccountId,balanceChange) && updatePersonDue(payeePersonId,dueChange);
                }
                break;
                case BORROW: {
                    Currency balanceChange = old_amount.negate().add(amount);
                    Currency borrowChange = old_amount.negate().add(amount);
                    updated = updateAccountBalance(payeeAccountId,balanceChange) && updatePersonBorrow(payerPersonId,borrowChange);
                }
                break;
                case PAY_DUE: {
                    Currency balanceChange = old_amount.negate().add(amount);
                    Currency dueChange = old_amount.add(amount.negate());
                    updated = updateAccountBalance(payeeAccountId,balanceChange) && updatePersonDue(payerPersonId,dueChange);
                }
                break;
                case PAY_BORROW: {
                    Currency balanceChange = old_amount.add(amount.negate());
                    Currency borrowChange = old_amount.add(amount.negate());
                    updated = updateAccountBalance(payerAccountId,balanceChange) &&  updatePersonBorrow(payeePersonId,borrowChange);
                }
                break;
                case MONEY_TRANSFER: {
                    Currency payeeBalanceChange = old_amount.negate().add(amount);
                    Currency payerBalanceChange = old_amount.add(amount.negate());
                    updated = updateAccountBalance(payerAccountId,payerBalanceChange) && updateAccountBalance(payeeAccountId,payeeBalanceChange);
                }
                break;
                case DUE_TRANSFER: {
                    Currency payeeDueChange = old_amount.negate().add(amount);
                    Currency payerDueChange = old_amount.add(amount.negate());
                    updated = updatePersonDue(payerPersonId,payerDueChange) &&  updatePersonDue(payeePersonId,payeeDueChange);
                }
                break;
                case BORROW_TRANSFER: {
                    Currency payeeBorrowChange = old_amount.negate().add(amount);
                    Currency payerBorrowChange = old_amount.add(amount.negate());
                    updated = updatePersonBorrow(payerPersonId,payerBorrowChange) &&  updatePersonBorrow(payeePersonId,payeeBorrowChange);
                }
                break;
                case BORROW_TO_DUE_TRANSFER: {
                    Currency payeeDueChange = old_amount.negate().add(amount);
                    Currency payerBorrowChange = old_amount.add(amount.negate());
                    updated = updatePersonBorrow(payerAccountId,payerBorrowChange) &&  updatePersonDue(payeePersonId,payeeDueChange);
                }
                break;
                default: {
                    updated = false;
                }
            }
            if (updated) {
                return 1;
            }
        }
        return 0;
    }

    @Transaction
    public int removeTransactionHistories(long[] ids) {
        int count = 0;
        for (long id : ids) {
            TransactionHistory history = findTransactionHistoryById(id);
            if (null == history) {
                continue;
            }
            count += removeTransactionHistory(history);
        }
        return count;
    }

    @Transaction
    public int removeTransactionHistory( TransactionHistory history) {
        TransactionType type = history.getType();
        Long payeeAccountId = history.getPayeeAccountId();
        Long payerAccountId = history.getPayerAccountId();
        Long payeePersonId = history.getPayeePersonId();
        Long payerPersonId = history.getPayerPersonId();
        Currency amount = history.getAmount();
        if (1 == delete_transactionHistory(history)) {
            boolean updated;
            switch (type) {
                case INCOME: {
                    updated = updateAccountBalance(payeeAccountId,amount.negate());
                }
                break;
                case EXPENSE: {
                    updated = updateAccountBalance(payerAccountId,amount);
                }
                break;
                case DUE: {
                    updated = updateAccountBalance(payerAccountId,amount) && updatePersonDue(payeePersonId,amount.negate());
                }
                break;
                case BORROW: {
                    updated = updateAccountBalance(payeeAccountId,amount.negate()) && updatePersonBorrow(payerPersonId,amount.negate());
                }
                break;
                case PAY_DUE: {
                    updated = updateAccountBalance(payeeAccountId,amount.negate()) && updatePersonDue(payerPersonId,amount);
                }
                break;
                case PAY_BORROW: {
                    updated = updateAccountBalance(payerAccountId,amount) &&  updatePersonBorrow(payeePersonId,amount);
                }
                break;
                case MONEY_TRANSFER: {
                    updated = updateAccountBalance(payerAccountId,amount) && updateAccountBalance(payeeAccountId,amount.negate());
                }
                break;
                case DUE_TRANSFER: {
                    updated = updatePersonDue(payerPersonId,amount) &&  updatePersonDue(payeePersonId,amount.negate());
                }
                break;
                case BORROW_TRANSFER: {
                    updated = updatePersonBorrow(payerPersonId,amount) && updatePersonBorrow(payeePersonId,amount.negate());
                }
                break;
                case BORROW_TO_DUE_TRANSFER: {
                    updated = updatePersonBorrow(payerAccountId,amount) &&  updatePersonDue(payeePersonId,amount);
                }
                break;
                default: {
                    updated = false;
                }
            }
            if (updated) {
                return 1;
            }
        }
        return 0;
    }

    @Query("SELECT * FROM `transaction_histories` WHERE `id` = :id")
    public abstract TransactionHistory findTransactionHistoryById(long id);

    @Query("SELECT * FROM `transaction_histories` WHERE `id` = :id")
    public abstract LiveData<TransactionHistoryModel> getTransactionHistoryByIdLive(long id);

    @Transaction
    @Query("SELECT * FROM `transaction_histories` WHERE DATE(`when`) IS :date ORDER BY `when` DESC")
    public abstract LiveData<List<TransactionHistoryModel>> getAllTransactionHistoriesForDateLive(LocalDate date);

    @Transaction
    @Query("SELECT * FROM `transaction_histories` WHERE DATE(`when`) BETWEEN DATE(:start) AND DATE(:end) AND (`payerAccountId` = :id OR `payeeAccountId` = :id) ORDER BY DATE(`when`) DESC")
    public abstract LiveData<List<TransactionHistoryModel>> getAllTransactionHistoriesForAccountsBetweenLive(long id, LocalDate start, LocalDate end);

    @Transaction
    @Query("SELECT * FROM `transaction_histories` WHERE DATE(`when`) BETWEEN DATE(:start) AND DATE(:end) AND (`payerPersonId` = :id OR `payeePersonId` = :id) ORDER BY DATE(`when`) DESC")
    public abstract LiveData<List<TransactionHistoryModel>> getAllTransactionHistoriesForPeopleBetweenLive(long id, LocalDate start, LocalDate end);

    private boolean updateAccountBalance(long accountId, @NonNull Currency change) {
        return 1 == query_updateAccountBalance(accountId,change);
    }

    private boolean updatePersonDue(long personId, @NonNull Currency change) {
        return 1 == query_updatePersonDue(personId,change);
    }

    private boolean updatePersonBorrow(long personId, @NonNull Currency change) {
        return 1 == query_updatePersonBorrow(personId,change);
    }

    @Query("UPDATE `accounts` SET `balance` = `balance` + :change WHERE `id` = :id" )
    protected abstract int query_updateAccountBalance(long id, Currency change);

    @Query("UPDATE `people` SET `due` = `due` + :change WHERE `id` = :id" )
    protected abstract int query_updatePersonDue(long id, Currency change);

    @Query("UPDATE `people` SET `borrow` = `borrow` + :change WHERE `id` = :id" )
    protected abstract int query_updatePersonBorrow(long id, Currency change);

    @Query("SELECT * FROM `accounts` WHERE `id` = :id")
    protected abstract Account findAccountById(long id);

    @Query("SELECT * FROM `people` WHERE `id` = :id")
    protected abstract Person findPersonById(long id);

    @Update
    protected abstract int updateAccount(Account account);

    @Update
    protected abstract int updatePerson(Person person);

    @Insert
    protected abstract long insert_transactionHistory(TransactionHistory history);

    @Update
    protected abstract int update_transactionHistory(TransactionHistory history);

    @Delete
    protected abstract int delete_transactionHistory(TransactionHistory history);
}
