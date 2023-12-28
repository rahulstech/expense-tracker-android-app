package dreammaker.android.expensetracker.fragment.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dreammaker.android.expensetracker.database.type.TransactionType;

public class HistoryFilterData implements Parcelable {

    private LocalDate rangeStart;

    private LocalDate rangeEnd;

    private ArrayList<AccountParcelable> accounts;

    private ArrayList<Long> accountIds;

    private ArrayList<PersonParcelable> people;

    private ArrayList<Long> personIds;

    private EnumSet<TransactionType> types;

    public HistoryFilterData() {
        this((HistoryFilterData) null);
    }

    public HistoryFilterData(@Nullable HistoryFilterData src) {
        if (null != src) {
            setRangeStart(src.rangeStart);
            setRangeEnd(src.rangeEnd);
            setAccounts(src.accounts);
            setPeople(src.people);
            setTypes(src.types);
        }
    }

    public LocalDate getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(LocalDate rangeStart) {
        this.rangeStart = rangeStart;
    }

    public LocalDate getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeEnd(LocalDate rangeEnd) {
        this.rangeEnd = rangeEnd;
    }

    public ArrayList<AccountParcelable> getAccounts() {
        return accounts;
    }

    public void setAccounts(ArrayList<AccountParcelable> accounts) {
        this.accounts = accounts;
        if (null != accountIds) {
            accountIds.clear();
            accountIds = null;
        }
    }

    public ArrayList<PersonParcelable> getPeople() {
        return people;
    }

    public void setPeople(ArrayList<PersonParcelable> people) {
        this.people = people;
        if (null != personIds) {
            personIds.clear();
            personIds = null;
        }
    }

    public EnumSet<TransactionType> getTypes() {
        return types;
    }

    public void setTypes(EnumSet<TransactionType> types) {
        this.types = types;
    }

    public ArrayList<Long> getAccountIds() {
        if (null != accounts && null == accountIds) {
            ArrayList<Long> ids = new ArrayList<>();
            for (AccountParcelable account : accounts) {
                ids.add(account.getId());
            }
            this.accountIds = ids;
        }
        return accountIds;
    }

    public ArrayList<Long> getPersonIds() {
        if (null != people && null == personIds) {
            ArrayList<Long> ids = new ArrayList<>();
            for (PersonParcelable person : people) {
                ids.add(person.getId());
            }
            this.personIds = ids;
        }
        return personIds;
    }

    protected HistoryFilterData(Parcel in) {
        String valRangeStart = in.readString();
        String valRangeEnd = in.readString();
        if (null == valRangeStart) {
            rangeStart = null;
        }
        else {
            rangeStart = LocalDate.parse(valRangeStart,DateTimeFormatter.ISO_LOCAL_DATE.withLocale(Locale.ENGLISH));
        }
        if (null == valRangeEnd) {
            rangeEnd = null;
        }
        else {
            rangeEnd = LocalDate.parse(valRangeEnd,DateTimeFormatter.ISO_LOCAL_DATE.withLocale(Locale.ENGLISH));
        }
        accounts = readParcelableArrayList(in,AccountParcelable.class);
        people = readParcelableArrayList(in,PersonParcelable.class);
        types = readEnumSet(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        final LocalDate rangeStart = this.rangeStart;
        final LocalDate rangeEnd = this.rangeEnd;
        final ArrayList<AccountParcelable> accounts = this.accounts;
        final ArrayList<PersonParcelable> people = this.people;
        final EnumSet<TransactionType> types = this.types;

        if (null == rangeStart) {
            dest.writeString(null);
        }
        else {
            dest.writeString(rangeStart.format(DateTimeFormatter.ISO_LOCAL_DATE.withLocale(Locale.ENGLISH)));
        }
        if (null == rangeEnd) {
            dest.writeString(null);
        }
        else {
            dest.writeString(rangeEnd.format(DateTimeFormatter.ISO_LOCAL_DATE.withLocale(Locale.ENGLISH)));
        }
        writeParcelableArraySet(dest,accounts);
        writeParcelableArraySet(dest,people);
        writeEnumSet(dest,types);
    }

    @Override
    public int describeContents() {return 0;}

    @SuppressWarnings("unchecked")
    private <T extends Parcelable> ArrayList<T> readParcelableArrayList(Parcel in, Class<T> clazz) {
        final int count = in.readInt();
        if (count == 0) {
            return null;
        }
        ArrayList<Parcelable> set = new ArrayList<>(count);
        for (int i=0; i<count; i++) {
            Parcelable p = in.readParcelable(clazz.getClassLoader());
            set.add(p);
        }
        return (ArrayList<T>) set;
    }

    private <T extends Parcelable> void writeParcelableArraySet(Parcel dest, @Nullable ArrayList<T> set) {
        int count;
        if (null == set) {
            count = 0;
        }
        else {
            count = set.size();
        }
        dest.writeInt(count);
        if (count > 0) {
            for (Parcelable p : set) {
                dest.writeParcelable(p,p.describeContents());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> EnumSet<T> readEnumSet(Parcel in) {
        final int count = in.readInt();
        if (count == 0) {
            return null;
        }
        EnumSet<T> set = EnumSet.noneOf((Class<T>) TransactionType.class);
        for (int i=0; i<count; i++) {
            String name = in.readString();
            T value = Enum.valueOf((Class<T>) TransactionType.class,name);
            set.add(value);
        }
        return EnumSet.copyOf(set);
    }

    private <T extends Enum<T>> void writeEnumSet(Parcel dest, @Nullable EnumSet<T> set) {
        int count;
        if (null == set) {
            count = 0;
        }
        else {
            count = set.size();
        }
        dest.writeInt(count);
        if (count > 0) {
            for (T value : set) {
                dest.writeString(value.name());
            }
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "HistoryFilterData{" +
                "rangeStart=" + rangeStart +
                ", rangeEnd=" + rangeEnd +
                ", accounts=" + accounts +
                ", people=" + people +
                ", types=" + types +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HistoryFilterData)) return false;
        HistoryFilterData data = (HistoryFilterData) o;
        return Objects.equals(rangeStart, data.rangeStart) && Objects.equals(rangeEnd, data.rangeEnd) && Objects.equals(accounts, data.accounts) && Objects.equals(accountIds, data.accountIds) && Objects.equals(people, data.people) && Objects.equals(personIds, data.personIds) && Objects.equals(types, data.types);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rangeStart, rangeEnd, accounts, accountIds, people, personIds, types);
    }

    public static final Creator<HistoryFilterData> CREATOR = new Creator<HistoryFilterData>() {
        @Override
        public HistoryFilterData createFromParcel(Parcel in) {
            return new HistoryFilterData(in);
        }

        @Override
        public HistoryFilterData[] newArray(int size) {
            return new HistoryFilterData[size];
        }
    };
}
