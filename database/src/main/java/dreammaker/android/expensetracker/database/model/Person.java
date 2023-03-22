package dreammaker.android.expensetracker.database.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import dreammaker.android.expensetracker.database.Converters;

import static androidx.room.ColumnInfo.TEXT;

@Entity(tableName = "people")
public class Person implements Cloneable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private long id;

    @NonNull
    private String firstName;

    private String lastName;

    @ColumnInfo(typeAffinity = TEXT, defaultValue = "0")
    @NonNull
    @TypeConverters(Converters.class)
    private BigDecimal amountDue;

    @ColumnInfo(typeAffinity = TEXT, defaultValue = "0")
    @NonNull
    @TypeConverters(Converters.class)
    private BigDecimal amountBorrow;

    @Ignore
    public Person(long id, String firstName, String lastName, BigDecimal amountDue, BigDecimal amountBorrow) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        setAmountDue(amountDue);
        setAmountBorrow(amountBorrow);
    }

    public Person() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public BigDecimal getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(BigDecimal amountDue) {
        this.amountDue = amountDue.setScale(2, RoundingMode.HALF_DOWN);
    }

    public BigDecimal getAmountBorrow() {
        return amountBorrow;
    }

    public void setAmountBorrow(BigDecimal amountBorrow) {
        this.amountBorrow = amountBorrow.setScale(2, RoundingMode.HALF_DOWN);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person person = (Person) o;
        return id == person.id && firstName.equals(person.firstName) && Objects.equals(lastName, person.lastName) && amountDue.equals(person.amountDue) && amountBorrow.equals(person.amountBorrow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, amountDue, amountBorrow);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", amountDue=" + amountDue +
                ", amountBorrow=" + amountBorrow +
                '}';
    }

    @NonNull
    @Override
    public Person clone() {
        return new Person(id,firstName,lastName, new BigDecimal(amountDue.toString()),new BigDecimal(amountBorrow.toString()));
    }
}
