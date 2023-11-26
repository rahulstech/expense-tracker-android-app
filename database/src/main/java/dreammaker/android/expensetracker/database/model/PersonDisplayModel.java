package dreammaker.android.expensetracker.database.model;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.Ignore;
import androidx.room.TypeConverters;
import dreammaker.android.expensetracker.database.Converters;

@Deprecated
public class PersonDisplayModel implements Cloneable {

    private long id;

    private String firstName;

    private String lastName;

    @TypeConverters(Converters.class)
    private BigDecimal amountDue;

    @TypeConverters(Converters.class)
    private BigDecimal amountBorrow;

    @Ignore
    PersonDisplayModel(long id, String firstName, String lastName, BigDecimal amountDue, BigDecimal amountBorrow) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        setAmountDue(amountDue);
        setAmountBorrow(amountBorrow);
    }

    public PersonDisplayModel() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        String name = "";
        if (!TextUtils.isEmpty(firstName)) {
            name += firstName+" ";
        }
        if (!TextUtils.isEmpty(lastName)) {
            name += lastName;
        }
        name = name.trim();
        return name;
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
        if (null == amountDue) return;
        this.amountDue = amountDue.setScale(2, RoundingMode.HALF_DOWN);
    }

    public BigDecimal getAmountBorrow() {
        return amountBorrow;
    }

    public void setAmountBorrow(BigDecimal amountBorrow) {
        if (null == amountBorrow) return;
        this.amountBorrow = amountBorrow.setScale(2, RoundingMode.HALF_DOWN);
    }

    @NonNull
    public BigDecimal calculateDue() {
        BigDecimal due = BigDecimal.ZERO;
        if (amountDue.compareTo(BigDecimal.ZERO) >= 0) {
            due = due.add(amountDue);
        }
        if (amountBorrow.compareTo(BigDecimal.ZERO) < 0) {
            due = due.add(amountBorrow.negate());
        }
        return due.setScale(2,RoundingMode.HALF_DOWN);
    }

    @NonNull
    public BigDecimal calculateBorrow() {
        BigDecimal borrow = BigDecimal.ZERO;
        if (amountDue.compareTo(BigDecimal.ZERO) < 0) {
            borrow = borrow.add(amountDue);
        }
        if (amountBorrow.compareTo(BigDecimal.ZERO) > 0) {
            borrow = borrow.add(amountBorrow);
        }
        return borrow.setScale(2,RoundingMode.HALF_DOWN);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonDisplayModel)) return false;
        PersonDisplayModel that = (PersonDisplayModel) o;
        return id == that.id && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(amountDue, that.amountDue) && Objects.equals(amountBorrow, that.amountBorrow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, amountDue, amountBorrow);
    }

    @Override
    public String toString() {
        return "PersonDisplayModel{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", amountDue=" + amountDue +
                ", amountBorrow=" + amountBorrow +
                '}';
    }

    @NonNull
    @Override
    public PersonDisplayModel clone() {
        return new PersonDisplayModel(id,firstName,lastName,new BigDecimal(amountDue.toString()),new BigDecimal(amountBorrow.toString()));
    }
}
