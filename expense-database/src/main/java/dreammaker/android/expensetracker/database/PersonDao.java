package dreammaker.android.expensetracker.database;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public abstract class PersonDao {

    @Insert
    public abstract long insertPerson(Person person);

    @Query("SELECT * FROM `persons`")
    public abstract LiveData<List<Person>> getAllPeople();

    @Query("SELECT * FROM `persons` WHERE included = :included")
    public abstract LiveData<List<Person>> getAllPeople(boolean included);

    @Update
    public abstract int updatePerson(Person person);

    @Query("UPDATE `persons` SET `due` = `due` + :amount WHERE `_id` = :personId")
    public abstract int addPersonDue(long personId, float amount);

    @Delete
    public abstract int deletePeople(Person... persons);

    @Query("DELETE FROM `persons`")
    public abstract void clearPeople();
}
