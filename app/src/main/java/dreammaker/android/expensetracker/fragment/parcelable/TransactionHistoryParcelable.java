package dreammaker.android.expensetracker.fragment.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import dreammaker.android.expensetracker.database.entity.TransactionHistory;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;

@SuppressWarnings("unused")
public class TransactionHistoryParcelable extends TransactionHistory implements Parcelable {

    private static final int ACCOUNT = 2;

    private static final int PERSON = 4;

    private Parcelable payee;

    private Parcelable payer;

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public TransactionHistoryParcelable() {
        super();
    }


    public void setPayee(Parcelable payee) {
        this.payee = payee;
    }

    @SuppressWarnings("unchecked")
    public <T> T getPayee() {
        return (T) payee;
    }

    public void setPayer(Parcelable payer) {
        this.payer = payer;
    }

    @SuppressWarnings("unchecked")
    public <T> T getPayer() {
        return (T) payer;
    }

    @SuppressWarnings("ConstantConditions")
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

        boolean hasPayee = readBoolean(in);
        if (hasPayee) {
            int payeeType = in.readInt();
            if (payeeType == ACCOUNT) {
                payee = AccountParcelable.CREATOR.createFromParcel(in);
            }
            else if (payeeType == PERSON) {
                payee = PersonParcelable.CREATOR.createFromParcel(in);
            }
            else {
                payee = null;
            }
        }
        boolean hasPayer = readBoolean(in);
        if (hasPayer) {
            int payerType = in.readInt();
            if (payerType == ACCOUNT) {
                payer = AccountParcelable.CREATOR.createFromParcel(in);
            }
            else if (payerType == PERSON) {
                payer = PersonParcelable.CREATOR.createFromParcel(in);
            }
            else {
                payer = null;
            }
        }
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

    @SuppressWarnings("ConstantConditions")
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

        boolean hasPayee = null != payee;
        if (hasPayee) {
            writeBoolean(dest,true);
            if (payee instanceof AccountParcelable) {
                dest.writeInt(ACCOUNT);
                dest.writeParcelable(payee,0);
            }
            else if (payee instanceof PersonParcelable) {
                dest.writeInt(PERSON);
                dest.writeParcelable(payee,0);
            }
            else {
                throw new IllegalArgumentException("unknown payee parcelable "+payee.getClass().getName());
            }
        }

        boolean hasPayer = null != payer;
        if (hasPayer) {
            writeBoolean(dest,true);
            if (payer instanceof AccountParcelable) {
                dest.writeInt(ACCOUNT);
                dest.writeParcelable(payer,0);
            }
            else if (payer instanceof PersonParcelable) {
                dest.writeInt(PERSON);
                dest.writeParcelable(payer,0);
            }
            else {
                throw new IllegalArgumentException("unknown payer parcelable "+payee.getClass().getName());
            }
        }
    }

    private boolean readBoolean(Parcel in) {
        return in.readInt() == 1;
    }

    private void writeBoolean(Parcel dest, boolean value) {
        dest.writeInt(value ? 1 : 0);
    }
}
