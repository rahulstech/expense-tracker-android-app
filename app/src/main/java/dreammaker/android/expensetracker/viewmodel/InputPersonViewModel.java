package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import dreammaker.android.expensetracker.database.dao.PersonDao;
import dreammaker.android.expensetracker.database.entity.Person;
import dreammaker.android.expensetracker.database.model.PersonModel;

@SuppressWarnings("unused")
public class InputPersonViewModel extends DBViewModel {

    private static final String TAG = InputPersonViewModel.class.getSimpleName();

    public static final int SAVE_PERSON = 1;

    private LiveData<PersonModel> mPerson;

    public InputPersonViewModel(@NonNull Application application) {
        super(application);
    }

    @NonNull
    protected PersonDao getPersonDao() {
        return getExpenseDatabase().getPersonDao();
    }

    @NonNull
    public LiveData<AsyncQueryResult> savePerson(@NonNull Person person) {
        return getOrCreate(SAVE_PERSON,()->{
            boolean editing = person.getId() > 0;
            if (editing) {
                int changes = getPersonDao().updatePerson(person);
                if (changes != 1) {
                    return null;
                }
            }
            else {
                long id = getPersonDao().addPerson(person);
                if (id <= 0) {
                    return null;
                }
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
}
