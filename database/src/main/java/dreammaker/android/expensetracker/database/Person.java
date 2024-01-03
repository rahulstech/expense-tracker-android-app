package dreammaker.android.expensetracker.database;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import static androidx.room.ColumnInfo.REAL;
import static androidx.room.ColumnInfo.TEXT;
import static dreammaker.android.expensetracker.database.ExpensesContract.AboutPersonColumns.DUE_PAYMENT;
import static dreammaker.android.expensetracker.database.ExpensesContract.PersonsColumns.PERSON_NAME;
import static dreammaker.android.expensetracker.database.ExpensesContract.PersonsColumns._ID;
import static dreammaker.android.expensetracker.database.ExpensesContract.Tables.PERSONS_TABLE;

@Entity(tableName = PERSONS_TABLE)
@Deprecated
@SuppressWarnings({"unused","deprecation"})
public class Person {

    @ColumnInfo(name = _ID, typeAffinity = ColumnInfo.INTEGER)
    @PrimaryKey(autoGenerate = true)
    @SerializedName(_ID)
    private long personId;

    @ColumnInfo(name = PERSON_NAME, typeAffinity = TEXT)
    @NonNull
    @SerializedName(PERSON_NAME)
    private String personName;


    @ColumnInfo(name = DUE_PAYMENT, typeAffinity = REAL, defaultValue = "0")
    @NonNull
    @SerializedName(DUE_PAYMENT)
    @TypeConverters(Converters.class)
    private BigDecimal totalDue;

    @Ignore
    @Deprecated
    public Person(long personId, @NonNull String personName, float due) {
        this(personId,personName,BigDecimal.valueOf(due));
    }

    public Person(long personId, @NonNull String personName, @NonNull BigDecimal totalDue) {
        this.personId = personId;
        this.personName = personName;
        this.totalDue = totalDue;
    }

    @Deprecated
    @Ignore
    public Person(long personId, String personName) {
        this(personId,personName,0);
    }

    @Ignore
    public Person() { this(0,"",BigDecimal.ZERO); }

    public long getPersonId() {
        return personId;
    }

    @NonNull
    public String getPersonName() {
        return personName;
    }

    public void setPersonId(long personId) {
        this.personId = personId;
    }

    public void setPersonName(@NonNull String personName) {
        this.personName = personName;
    }

    @Deprecated
    public float getDue() {
        return getTotalDue().floatValue();
    }

    @Deprecated
    public void setDue(float due) { setTotalDue(BigDecimal.valueOf(due)); }

    @NonNull
    public BigDecimal getTotalDue() {
        return totalDue;
    }

    public void setTotalDue(@NonNull BigDecimal totalDue) {
        this.totalDue = totalDue;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o instanceof Person){
            return personId == ((Person) o).personId;
        }
        return false;
    }

    public boolean equalContent(@Nullable Person p) {
        if (null != p) {
            return p.getPersonId() == this.getPersonId()
                    && Objects.deepEquals(personName,p.personName)
                    && 0 == totalDue.compareTo(p.totalDue);
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return "Person{" +
                "personId=" + personId +
                ", personName='" + personName + '\'' +
                ", due=" + totalDue.toPlainString() +
                '}';
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @NonNull
    @Override
    public Person clone() {
        return new Person(personId,personName,totalDue);
    }
}