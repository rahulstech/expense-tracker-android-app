package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import dreammaker.android.expensetracker.database.dao.PersonDao;
import dreammaker.android.expensetracker.database.model.PersonModel;

@SuppressWarnings("unused")
public class PeopleListViewModel extends DBViewModel {

    private LiveData<List<PersonModel>> mPeople;

    public PeopleListViewModel(@NonNull Application application) {
        super(application);
    }

    @NonNull
    public PersonDao getPersonDao() {
        return getExpenseDatabase().getPersonDao();
    }

    @NonNull
    public LiveData<List<PersonModel>> getAllPeople() {
        if (null == mPeople) {
            mPeople = getPersonDao().getAllPeopleLive();
        }
        return mPeople;
    }
}
