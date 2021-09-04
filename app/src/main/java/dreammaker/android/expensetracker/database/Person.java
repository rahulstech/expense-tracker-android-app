package dreammaker.android.expensetracker.database;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import dreammaker.android.expensetracker.util.Check;

import static androidx.room.ColumnInfo.REAL;
import static androidx.room.ColumnInfo.TEXT;
import static dreammaker.android.expensetracker.database.ExpensesContract.AboutPersonColumns.DUE_PAYMENT;
import static dreammaker.android.expensetracker.database.ExpensesContract.PersonsColumns.PERSON_NAME;
import static dreammaker.android.expensetracker.database.ExpensesContract.PersonsColumns._ID;
import static dreammaker.android.expensetracker.database.ExpensesContract.Tables.PERSONS_TABLE;

@Entity(tableName = PERSONS_TABLE)
public class Person {

    @ColumnInfo(name = _ID, typeAffinity = ColumnInfo.INTEGER)
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @SerializedName(_ID)
    private long personId;

    @ColumnInfo(name = PERSON_NAME, typeAffinity = TEXT)
    @NonNull
    @SerializedName(PERSON_NAME)
    private String personName;


    @ColumnInfo(name = DUE_PAYMENT, typeAffinity = REAL, defaultValue = "0")
    @NonNull
    @SerializedName(DUE_PAYMENT)
    private float due;

    public Person(long personId, @NonNull String personName, float due) {
        this.personId = personId;
        this.personName = personName;
        this.due = due;
    }

    @Deprecated
    @Ignore
    public Person(long personId, String personName) {
        this(personId,personName,0);
    }

    @Ignore
    @Deprecated
    public Person(String personName){
        this(0L, personName,0);
    }

    @Ignore
    public Person() { this(0,"",0); }

    public long getPersonId() {
        return personId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonId(long personId) {
        this.personId = personId;
    }

    public void setPersonName(@NonNull String personName) {
        this.personName = personName;
    }


    public float getDue() { return due; }

    public void setDue(float due) { this.due = due; }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o instanceof Person){
            return this.getPersonId() == ((Person) o).getPersonId();
        }
        return false;
    }

    public boolean equalContent(@Nullable Person p) {
        if (null != p) {
            return p.getPersonId() == this.getPersonId()
                    && Check.isEqualString(p.getPersonName(),this.getPersonName())
                    && 0 == Float.compare(this.due,p.due);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Person{" +
                "personId=" + personId +
                ", personName='" + personName + '\'' +
                ", due=" + due +
                '}';
    }

    @NonNull
    @Override
    public Person clone() {
        return new Person(personId,personName,due);
    }
}
