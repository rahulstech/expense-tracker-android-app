package dreammaker.android.expensetracker.database.dao;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Update;
import dreammaker.android.expensetracker.database.entity.Person;
import dreammaker.android.expensetracker.database.model.PersonModel;

@Dao
@SuppressWarnings("unused")
public interface PersonDao {

    @Insert
    long addPerson(Person person);

    @Update
    int updatePerson(Person person);

    @Query("SELECT `id`,`firstName`,`lastName`,`due`,`borrow` FROM `people`")
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    LiveData<List<PersonModel>> getAllPeopleLive();

    @Query("SELECT `id`,`firstName`,`lastName`,`due`,`borrow`, " +
            "(SELECT COUNT(`id`) FROM `transaction_histories` " +
            "WHERE `payeePersonId` = `people`.`id` OR `payerPersonId` = `people`.`id`) AS `usageCount` "+
            "FROM `people`")
    LiveData<List<PersonModel>> getAllPeopleWithUsageCountLive();

    @Query("SELECT * FROM `people` WHERE `id` = :id")
    LiveData<PersonModel> getPersonByIdLive(long id);

    @Query("SELECT * FROM `people` WHERE `id` = :id")
    Person findPersonById(long id);

    @Query("DELETE FROM `people` WHERE `id` IN(:ids)")
    int removePeople(long[] ids);
}
