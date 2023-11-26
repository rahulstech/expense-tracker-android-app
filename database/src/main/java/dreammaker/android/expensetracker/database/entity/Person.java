package dreammaker.android.expensetracker.database.entity;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import dreammaker.android.expensetracker.database.type.Currency;

@Entity(tableName = "people")
@SuppressWarnings("unused")
public class Person {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String firstName;

    private String lastName;

    @NonNull
    private Currency due;

    @NonNull
    private Currency borrow;

    public Person() {
        this.due = Currency.ZERO;
        this.borrow = Currency.ZERO;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(@NonNull String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @NonNull
    public Currency getDue() {
        return due;
    }

    public void setDue(@NonNull Currency due) {
        this.due = due;
    }

    @NonNull
    public Currency getBorrow() {
        return borrow;
    }

    public void setBorrow(@NonNull Currency borrow) {
        this.borrow = borrow;
    }

    @NonNull
    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", due=" + due +
                ", borrow=" + borrow +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person person = (Person) o;
        return id == person.id && firstName.equals(person.firstName) && Objects.equals(lastName, person.lastName) && due.equals(person.due) && borrow.equals(person.borrow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, due, borrow);
    }

    @NonNull
    public Person copy() {
        Person copy = new Person();
        copy.id = id;
        copy.firstName = firstName;
        copy.lastName = lastName;
        copy.due = due;
        copy.borrow = borrow;
        return copy;
    }
}
