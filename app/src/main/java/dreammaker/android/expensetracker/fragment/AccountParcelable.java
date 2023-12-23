package dreammaker.android.expensetracker.fragment;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import androidx.core.os.ParcelCompat;
import dreammaker.android.expensetracker.database.entity.Account;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.database.type.Currency;

@SuppressWarnings("unused")
public class AccountParcelable extends Account implements Parcelable {

    public AccountParcelable(Account account) {
        Objects.requireNonNull(account,"given Account is null");
        setId(account.getId());
        setName(account.getName());
        setBalance(account.getBalance());
    }

    public AccountParcelable(AccountModel account) {
        Objects.requireNonNull(account,"given AccountModel is null");
        setId(null == account.getId() ? 0 : account.getId());
        setName(account.getName());
        setBalance(account.getBalance());
    }

    @SuppressWarnings("ConstantConditions")
    protected AccountParcelable(Parcel in) {
        long id = in.readLong();
        String name = in.readString();
        Currency balance;
        if (ParcelCompat.readBoolean(in)) {
            balance = Currency.valueOf(in.readString());
        }
        else {
            balance = null;
        }
        setId(id);
        setName(name);
        setBalance(balance);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        final long id = getId();
        final String name = getName();
        final Currency balance = getBalance();
        final boolean hasBalance = null != balance;
        dest.writeLong(id);
        dest.writeString(name);
        ParcelCompat.writeBoolean(dest,hasBalance);
        if (hasBalance) {
            dest.writeString(balance.toString());
        }
    }

    @Override
    public int describeContents() {return 0;}

    public AccountModel asAccountModel() {
        AccountModel model = new AccountModel();
        model.setId(getId());
        model.setBalance(getBalance());
        model.setName(getName());
        return model;
    }

    public static final Creator<AccountParcelable> CREATOR = new Creator<AccountParcelable>() {
        @Override
        public AccountParcelable createFromParcel(Parcel in) {
            return new AccountParcelable(in);
        }

        @Override
        public AccountParcelable[] newArray(int size) {
            return new AccountParcelable[size];
        }
    };
}
