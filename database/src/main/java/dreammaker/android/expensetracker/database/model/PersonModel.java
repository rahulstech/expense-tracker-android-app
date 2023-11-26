package dreammaker.android.expensetracker.database.model;

import java.time.LocalDate;
import java.util.Objects;

import androidx.annotation.NonNull;
import dreammaker.android.expensetracker.database.type.Currency;

@SuppressWarnings("unused")
public class PersonModel {

    private Long id;

    private String firstName;

    private String lastName;

    private Currency due;

    private Currency borrow;

    private Integer usageCount;

    public PersonModel() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Currency getDue() {
        return due;
    }

    public void setDue(Currency due) {
        this.due = due;
    }

    public Currency getBorrow() {
        return borrow;
    }

    public void setBorrow(Currency borrow) {
        this.borrow = borrow;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    @NonNull
    @Override
    public String toString() {
        return "PersonModel{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", due=" + due +
                ", borrow=" + borrow +
                ", usageCount=" + usageCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonModel)) return false;
        PersonModel that = (PersonModel) o;
        return Objects.equals(id, that.id) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(due, that.due) && Objects.equals(borrow, that.borrow) && Objects.equals(usageCount, that.usageCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, due, borrow, usageCount);
    }
}
