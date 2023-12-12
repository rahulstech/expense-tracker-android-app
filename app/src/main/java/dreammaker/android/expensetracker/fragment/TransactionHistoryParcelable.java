package dreammaker.android.expensetracker.fragment;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import dreammaker.android.expensetracker.database.entity.TransactionHistory;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;

public class TransactionHistoryParcelable extends TransactionHistory implements Parcelable {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public TransactionHistoryParcelable() {
        super();
    }

    protected TransactionHistoryParcelable(Parcel in) {
        boolean hasId = readBoolean(in);
        long id = hasId ? in.readLong() : 0;
        boolean hasPayeeAccountId = readBoolean(in);
        Long payeeAccountId = hasPayeeAccountId ? in.readLong() : null;
        boolean hasPayerAccountId = readBoolean(in);
        Long payerAccountId = hasPayerAccountId ? in.readLong() : null;
        boolean hasPayeePersonId = readBoolean(in);
        Long payeePersonId = hasPayeePersonId ? in.readLong() : null;
        boolean hasPayerPersonId = readBoolean(in);
        Long payerPersonId = hasPayerPersonId ? in.readLong() : null;
        boolean hasType = readBoolean(in);
        TransactionType type = hasType ? TransactionType.valueOf(in.readString()) : null;
        boolean hasWhen = readBoolean(in);
        LocalDate when = hasWhen ? LocalDate.parse(in.readString(),FORMAT): null;
        boolean hasAmount = readBoolean(in);
        Currency amount = hasAmount ? Currency.valueOf(in.readString()) : null;
        boolean hasDescription = readBoolean(in);
        String description = hasDescription ? in.readString() : null;

        setId(id);
        setPayeeAccountId(payeeAccountId);
        setPayerAccountId(payerAccountId);
        setPayeePersonId(payeePersonId);
        setPayerPersonId(payerPersonId);
        setWhen(when);
        setAmount(amount);
        setType(type);
        setDescription(description);
    }

    public static final Creator<TransactionHistoryParcelable> CREATOR = new Creator<TransactionHistoryParcelable>() {
        @Override
        public TransactionHistoryParcelable createFromParcel(Parcel in) {
            return new TransactionHistoryParcelable(in);
        }

        @Override
        public TransactionHistoryParcelable[] newArray(int size) {
            return new TransactionHistoryParcelable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        long id = getId();
        Long payeeAccountId = getPayeeAccountId();
        Long payerAccountId = getPayerAccountId();
        Long payeePersonId = getPayeePersonId();
        Long payerPersonId = getPayerPersonId();
        LocalDate when = getWhen();
        Currency amount = getAmount();
        TransactionType type = getType();
        String description = getDescription();
        if (id > 0) {
            writeBoolean(dest,true);
            dest.writeLong(id);
        }
        else {
            writeBoolean(dest,false);
        }
        if (null != payeeAccountId) {
            writeBoolean(dest,true);
            dest.writeLong(payeeAccountId);
        }
        else {
            writeBoolean(dest,false);
        }
        if (null != payerAccountId) {
            writeBoolean(dest,true);
            dest.writeLong(payerAccountId);
        }
        else {
            writeBoolean(dest,false);
        }
        if (null != payeePersonId) {
            writeBoolean(dest,true);
            dest.writeLong(payeePersonId);
        }
        else {
            writeBoolean(dest,false);
        }
        if (null != payerPersonId) {
            writeBoolean(dest,true);
            dest.writeLong(payerPersonId);
        }
        else {
            writeBoolean(dest,false);
        }
        if (null != type) {
            writeBoolean(dest,true);
            dest.writeString(type.name());
        }
        else {
            writeBoolean(dest,false);
        }
        if (null != when) {
            writeBoolean(dest,true);
            dest.writeString(when.format(FORMAT));
        }
        else {
            writeBoolean(dest,false);
        }
        if (null != amount) {
            writeBoolean(dest,true);
            dest.writeString(amount.toString());
        }
        else {
            writeBoolean(dest,false);
        }
        if (null != description) {
            writeBoolean(dest,true);
            dest.writeString(description);
        }
        else {
            writeBoolean(dest,false);
        }
    }

    private boolean readBoolean(Parcel in) {
        return in.readInt() == 1;
    }

    private void writeBoolean(Parcel dest, boolean value) {
        dest.writeInt(value ? 1 : 0);
    }
}
