package dreammaker.android.expensetracker.fragment;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import androidx.core.os.ParcelCompat;
import dreammaker.android.expensetracker.database.entity.Person;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.database.type.Currency;

@SuppressWarnings("unused")
public class PersonParcelable extends Person implements Parcelable {

    public PersonParcelable(Person person) {
        Objects.requireNonNull(person,"given Person is null");
        setId(person.getId());
        setFirstName(person.getFirstName());
        setLastName(person.getLastName());
        setDue(person.getDue());
        setBorrow(person.getBorrow());
    }

    public PersonParcelable(PersonModel person) {
        Objects.requireNonNull(person,"given PersonModel is null");
        setId(null == person.getId() ? 0 : person.getId());
        setFirstName(person.getFirstName());
        setLastName(person.getLastName());
        setDue(person.getDue());
        setBorrow(person.getBorrow());
    }

    @SuppressWarnings("ConstantConditions")
    protected PersonParcelable(Parcel in) {
        long id = in.readLong();
        String firstName = in.readString();
        String lastName = in.readString();
        Currency due, borrow;
        if (ParcelCompat.readBoolean(in)) {
            due = Currency.valueOf(in.readString());
        }
        else {
            due = null;
        }
        if (ParcelCompat.readBoolean(in)) {
            borrow = Currency.valueOf(in.readString());
        }
        else {
            borrow = null;
        }
        setId(id);
        setFirstName(firstName);
        setLastName(lastName);
        setDue(due);
        setBorrow(borrow);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        final long id = getId();
        final String firstName = getFirstName();
        final String lasName = getLastName();
        final Currency due = getDue();
        final Currency borrow = getBorrow();
        final boolean hasDue = null != due;
        final boolean hasBorrow = null != borrow;

        dest.writeLong(id);
        dest.writeString(firstName);
        dest.writeString(lasName);
        ParcelCompat.writeBoolean(dest,hasDue);
        if (hasDue){
            dest.writeString(due.toString());
        }
        ParcelCompat.writeBoolean(dest,hasBorrow);
        if (hasBorrow) {
            dest.writeString(borrow.toString());
        }
    }

    @Override
    public int describeContents() {return 0;}

    public static final Creator<PersonParcelable> CREATOR = new Creator<PersonParcelable>() {
        @Override
        public PersonParcelable createFromParcel(Parcel in) {
            return new PersonParcelable(in);
        }

        @Override
        public PersonParcelable[] newArray(int size) {
            return new PersonParcelable[size];
        }
    };
}
