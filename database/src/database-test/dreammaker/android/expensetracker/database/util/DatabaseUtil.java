package dreammaker.android.expensetracker.database.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.entity.Account;
import dreammaker.android.expensetracker.database.entity.Person;
import dreammaker.android.expensetracker.database.entity.TransactionHistory;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.database.model.TransactionHistoryModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;

@SuppressWarnings("unused")
public class DatabaseUtil {

    @NonNull
    public static ExpensesDatabase createInMemoryExpenseDatabase(int version) {
        Context context = ApplicationProvider.getApplicationContext();
        RoomDatabase.Builder<ExpensesDatabase> builder = Room.inMemoryDatabaseBuilder(context,ExpensesDatabase.class)
                .allowMainThreadQueries();
        builder.addCallback(new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                FakeData.addDataForVersion(db,7);
            }
        });
        return builder.build();
    }

    public static void closeDBSilently(RoomDatabase db) {
        try {
            db.close();
        }
        catch (Throwable ignore) {}
    }

    @Nullable
    public static <T> T getValueFromLivedata(LiveData<T> liveData, long maxWait) throws Exception {
        Handler handler = new Handler(Looper.getMainLooper());
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> output = new AtomicReference<>(null);

        Observer<T> observer = (result) ->{
            output.set(result);
            latch.countDown();
        };
        handler.post(()->liveData.observeForever(observer));
        latch.await(maxWait, TimeUnit.MILLISECONDS);
        handler.post(()->liveData.removeObserver(observer));

        return output.get();
    }

    @NonNull
    public static Account createAccount(Object... values) {
        Account account = new Account();
        account.setId((long) values[0]);
        account.setName((String) values[1]);
        account.setBalance((Currency) values[2]);
        return account;
    }
    @NonNull
    public static AccountModel createAccountModel(Object... values) {
        AccountModel account = new AccountModel();
        if (values.length > 0) account.setId((Long) values[0]);
        if (values.length > 1) account.setName((String) values[1]);
        if (values.length > 2) account.setBalance((Currency) values[2]);
        if (values.length > 3) account.setUsageCount((Integer) values[3]);
        return account;
    }

    @NonNull
    public static Person createPerson(Object... values) {
        Person person = new Person();
        person.setId((long) values[0]);
        person.setFirstName((String) values[1]);
        person.setLastName((String) values[2]);
        person.setDue((Currency) values[3]);
        person.setBorrow((Currency) values[4]);
        return person;
    }

    @NonNull
    public static PersonModel createPersonModel(Object... values) {
        PersonModel person = new PersonModel();
        if (values.length > 0) person.setId((Long) values[0]);
        if (values.length > 1) person.setFirstName((String) values[1]);
        if (values.length > 2) person.setLastName((String) values[2]);
        if (values.length > 3) person.setDue((Currency) values[3]);
        if (values.length > 4) person.setBorrow((Currency) values[4]);
        if (values.length > 5) person.setUsageCount((Integer) values[5]);
        return person;
    }

    @NonNull
    public static TransactionHistory createTransactionHistory(Object... values) {
        TransactionHistory history = new TransactionHistory();
        history.setId((long) values[0]);
        history.setType((TransactionType) values[1]);
        history.setAmount((Currency) values[2]);
        history.setWhen((LocalDate) values[3]);
        history.setPayeeAccountId((Long) values[4]);
        history.setPayerAccountId((Long) values[5]);
        history.setPayeePersonId((Long) values[6]);
        history.setPayerPersonId((Long) values[7]);
        history.setDescription((String) values[8]);
        return history;
    }

    public static TransactionHistoryModel createTransactionHistoryMode(Object... values) {
        TransactionHistoryModel history = new TransactionHistoryModel();
        if (values.length > 0) history.setId((long) values[0]);
        if (values.length > 1) history.setType((TransactionType) values[1]);
        if (values.length > 2) history.setAmount((Currency) values[2]);
        if (values.length > 3) history.setWhen((LocalDate) values[3]);
        if (values.length > 4) history.setPayeeAccountId((Long) values[4]);
        if (values.length > 4) history.setPayerAccountId((Long) values[5]);
        if (values.length > 6) history.setPayeePersonId((Long) values[6]);
        if (values.length > 7) history.setPayerPersonId((Long) values[7]);
        if (values.length > 8) history.setDescription((String) values[8]);
        if (values.length > 9) history.setPayeeAccount((AccountModel) values[9]);
        if (values.length > 10) history.setPayerAccount((AccountModel) values[10]);
        if (values.length > 11) history.setPayeePerson((PersonModel) values[11]);
        if (values.length > 12) history.setPayerPerson((PersonModel) values[12]);
        return history;
    }
}
