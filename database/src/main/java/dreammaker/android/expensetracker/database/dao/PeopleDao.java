package dreammaker.android.expensetracker.database.dao;

import java.math.BigDecimal;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import dreammaker.android.expensetracker.database.model.Person;
import dreammaker.android.expensetracker.database.model.PersonDisplayModel;

@Dao
@Deprecated
public abstract class PeopleDao {

    //@Insert
    public long insert(Person person) { return 0; }

    //@Update
    public int update(Person person) { return 0; }

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

    //@Delete
    public int delete(Person person){return 0;}

    @Query("DELETE FROM `people` WHERE `id` IN(:ids)")
    public abstract int deleteMultiple(List<Long> ids);

    //@Query("SELECT * FROM `people` WHERE `id` = :id")
    public LiveData<Person> findPersonById(long id){ return new MutableLiveData<>(); }

    //@Query("SELECT * FROM `people` WHERE `id` = :id")
    public Person getPersonById(long id) { return null; }

    //@Query("SELECT `id`,`firstName`,`lastName`,`amountDue`,`amountBorrow` FROM `people` " +
    //        "ORDER BY CASE WHEN CAST(`amountDue` AS REAL) THEN 2 WHEN CAST(`amountBorrow` AS REAL) THEN 1 ELSE 0 END DESC")
    public LiveData<List<PersonDisplayModel>> getAllPeopleForDisplay() { return new MutableLiveData<>();}
}
