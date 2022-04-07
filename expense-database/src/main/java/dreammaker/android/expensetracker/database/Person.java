package dreammaker.android.expensetracker.database;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import dreammaker.android.expensetracker.util.Check;

@Entity(tableName = "persons")
public class Person {

    @ColumnInfo(name = "_id")
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @SerializedName("_id")
    private long personId;

    @ColumnInfo(name = "person_name")
    @NonNull
    @SerializedName("person_name")
    private String personName;

    @ColumnInfo(defaultValue = "0")
    @NonNull
    private float due;

    @ColumnInfo(defaultValue = "1")
    @NonNull
    private boolean included;

    public Person(long personId, String personName, float due, boolean included) {
        this.personId = personId;
        this.personName = personName;
        this.due = due;
        this.included = included;
    }

    @Deprecated
    @Ignore
    public Person(long personId, @NonNull String personName, float due) {
        this(personId,personName,due,false);
    }

    @Deprecated
    @Ignore
    public Person(long personId, String personName) {
        this(personId,personName,0, false);
    }

    @Ignore
    public Person() { this(0,"",0, true); }

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

    public boolean isIncluded() {
        return included;
    }

    public void setIncluded(boolean included) {
        this.included = included;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (null != o && o instanceof Person){
            return personId == ((Person) o).personId;
        }
        return false;
    }

    public boolean equalContent(@Nullable Person p) {
        if (null != p) {
            return p.personId == personId
                    && Check.isEqualString(p.personName,personName)
                    && 0 == Float.compare(p.due,due)
                    && p.included == included;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Person{" +
                "personId=" + personId +
                ", personName='" + personName + '\'' +
                ", due=" + due +
                ", included="+ included +
                '}';
    }

    @NonNull
    @Override
    public Person clone() {
        return new Person(personId,personName,due, included);
    }
}
