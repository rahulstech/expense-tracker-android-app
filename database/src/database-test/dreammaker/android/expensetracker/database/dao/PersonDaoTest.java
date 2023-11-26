package dreammaker.android.expensetracker.database.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.entity.Person;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.util.DatabaseUtil;

import static dreammaker.android.expensetracker.database.util.DatabaseUtil.createPerson;
import static dreammaker.android.expensetracker.database.util.DatabaseUtil.createPersonModel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PersonDaoTest {

    ExpensesDatabase db;

    PersonDao dao;

    @Before
    public void openDb() {
        db = DatabaseUtil.createInMemoryExpenseDatabase(7);
        dao = db.getPersonDao();
    }

    @After
    public void closeDB() {
        DatabaseUtil.closeDBSilently(db);
    }

    @Test
    public void addPerson() {
        Person person = createPerson(0L,"Person 5 FN","Person 5 LN",Currency.valueOf("200"),Currency.valueOf("100"));
        long id = dao.addPerson(person);
        assertTrue(id > 0);
    }

    @Test
    public void updatePerson() {
        Person person = createPerson(1L,"Person 1 FN","Person 1 LN",Currency.valueOf("250"),Currency.ZERO);
        int changes = dao.updatePerson(person);
        assertEquals(1,changes);
    }

    @Test
    public void getAllPeopleLive() throws Exception {
        LiveData<List<PersonModel>> liveData = dao.getAllPeopleLive();
        List<PersonModel> actual = DatabaseUtil.getValueFromLivedata(liveData, 5000);

        PersonModel person1 = createPersonModel(1L,"person 1 FN",null,Currency.valueOf("200"),Currency.valueOf("-100"));
        PersonModel person2 = createPersonModel(2L,"person 2 FN",null,Currency.valueOf("-500"),Currency.valueOf("1000"));
        PersonModel person3 = createPersonModel(3L,"person 3 FN","person 3 LN",Currency.ZERO,Currency.ZERO);
        List<PersonModel> expected = Arrays.asList(person1,person2,person3);

        assertEquals(expected,actual);
    }

    @Test
    public void getAllPeopleWithUsageCountLive() throws Exception {
        LiveData<List<PersonModel>> liveData = dao.getAllPeopleWithUsageCountLive();
        List<PersonModel> actual = DatabaseUtil.getValueFromLivedata(liveData,5000);

        PersonModel person1 = createPersonModel(1L,"person 1 FN",null,Currency.valueOf("200"),Currency.valueOf("-100"), 5);
        PersonModel person2 = createPersonModel(2L,"person 2 FN",null,Currency.valueOf("-500"),Currency.valueOf("1000"), 2);
        PersonModel person3 = createPersonModel(3L,"person 3 FN","person 3 LN",Currency.ZERO,Currency.ZERO,3);
        List<PersonModel> expected = Arrays.asList(person1,person2,person3);

        assertEquals(expected,actual);
    }

    @Test
    public void findPersonById() {
        Person expected = createPerson(1L,"person 1 FN",null,Currency.valueOf("200"),Currency.valueOf("-100"));
        Person actual = dao.findPersonById(1);
        assertEquals(expected,actual);
    }

    @Test
    public void removePeople() {
        int changes = dao.removePeople(new long[]{1,3});
        assertEquals(2,changes);
    }

}
