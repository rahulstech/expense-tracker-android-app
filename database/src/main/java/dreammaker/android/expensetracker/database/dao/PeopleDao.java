package dreammaker.android.expensetracker.database.dao;

import java.math.BigDecimal;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import dreammaker.android.expensetracker.database.model.Person;
import dreammaker.android.expensetracker.database.model.PersonDisplayModel;

@Dao
public abstract class PeopleDao {

    @Insert
    public abstract long insert(Person person);

    @Update
    public abstract int update(Person person);

    public boolean addDue(long id, BigDecimal quantity) {
        Person person = getPersonById(id);
        if (null == person) return false;
        BigDecimal oldDue = person.getAmountDue();
        BigDecimal newDue = oldDue.add(quantity);
        person.setAmountDue(newDue);
        return 0 < update(person);
    }

    public boolean reduceDue(long id, BigDecimal quantity) {
        return addDue(id,quantity.negate());
    }

    public boolean addBorrow(long id, BigDecimal quantity) {
        Person person = getPersonById(id);
        if (null == person) return false;
        BigDecimal oldBorrow = person.getAmountBorrow();
        BigDecimal newBorrow = oldBorrow.add(quantity);
        person.setAmountBorrow(newBorrow);
        return 0 < update(person);
    }

    public boolean reduceBorrow(long id, BigDecimal quantity) {
        return addBorrow(id,quantity.negate());
    }

    @Delete
    public abstract int delete(Person person);

    @Query("DELETE FROM `people` WHERE `id` IN(:ids)")
    public abstract int deleteMultiple(List<Long> ids);

    @Query("SELECT * FROM `people` WHERE `id` = :id")
    public abstract LiveData<Person> findPersonById(long id);

    @Query("SELECT * FROM `people` WHERE `id` = :id")
    public abstract Person getPersonById(long id);

    @Query("SELECT `id`,`firstName`,`lastName`,`amountDue`,`amountBorrow` FROM `people` " +
            "ORDER BY CASE WHEN CAST(`amountDue` AS REAL) THEN 2 WHEN CAST(`amountBorrow` AS REAL) THEN 1 ELSE 0 END DESC")
    public abstract LiveData<List<PersonDisplayModel>> getAllPeopleForDisplay();
}
