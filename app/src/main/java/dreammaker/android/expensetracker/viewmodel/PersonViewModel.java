package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import dreammaker.android.expensetracker.database.dao.PersonDao;
import dreammaker.android.expensetracker.database.entity.Person;
import dreammaker.android.expensetracker.database.model.PersonModel;

@SuppressWarnings("unused")
public class PersonViewModel extends DBViewModel{

    public static final int SAVE_PERSON = 1;

    public static final int DELETE_PEOPLE = 2;

    private LiveData<PersonModel> mPerson;

    private LiveData<List<PersonModel>> mPeople;

    public PersonViewModel(@NonNull Application application) {
        super(application);
    }

    private PersonDao getPersonDao() {
        return getExpenseDatabase().getPersonDao();
    }

    @NonNull
    public LiveData<AsyncQueryResult> savePerson(@NonNull Person person) {
        return execute(SAVE_PERSON,()->{
            boolean editing = person.getId() > 0;
            if (editing) {
                if (getPersonDao().updatePerson(person) != 1) {
                    return null;
                }
            }
            else {
                long id = getPersonDao().addPerson(person);
                if (id <= 0) {
                    return null;
                }
                person.setId(id);
            }
            return person;
        });
    }

    @NonNull
    public LiveData<PersonModel> getPersonById(long id) {
        if (null == mPerson) {
            mPerson = getPersonDao().getPersonByIdLive(id);
        }
        return mPerson;
    }

    @NonNull
    public LiveData<List<PersonModel>> getAllPeopleWithUsageCount() {
        if (null == mPeople) {
            mPeople = getPersonDao().getAllPeopleWithUsageCountLive();
        }
        return mPeople;
    }

    @NonNull
    public LiveData<List<PersonModel>> getAllPeople() {
        if (null == mPeople) {
            mPeople = getPersonDao().getAllPeopleLive();
        }
        return mPeople;
    }

    @NonNull
    public LiveData<AsyncQueryResult> removePeople(final long[] ids) {
        Objects.requireNonNull(ids,"array of person ids is null");
        return execute(DELETE_PEOPLE,()->{
            if (ids.length == 0) {
                return 0;
            }
            return getPersonDao().removePeople(ids);
        });
    }
}
