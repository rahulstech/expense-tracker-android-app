package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import dreammaker.android.expensetracker.concurrent.Task;
import dreammaker.android.expensetracker.concurrent.TaskCallback;
import dreammaker.android.expensetracker.concurrent.TaskMaster;
import dreammaker.android.expensetracker.concurrent.TaskResult;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.dao.PeopleDao;
import dreammaker.android.expensetracker.database.model.Person;
import dreammaker.android.expensetracker.database.model.PersonDisplayModel;
import dreammaker.android.expensetracker.util.Constants;

public class PersonsViewModel extends AndroidViewModel {

    private static final String TAG = "PersonsViewModel";

    private PeopleDao peopleDao;

    private MutableLiveData<Long> mPersonIdLiveData;

    private LiveData<Person> mPersonLiveData;

    private LiveData<List<PersonDisplayModel>> mDisplayPeopleLiveData = null;

    private TaskMaster mTaskMaster;

    public PersonsViewModel(@NonNull Application application) {
        super(application);
        ExpensesDatabase db = ExpensesDatabase.getInstance(application);
        peopleDao = db.getPeopleDao();
        mPersonIdLiveData = new MutableLiveData<>();
        mPersonLiveData = Transformations.switchMap(mPersonIdLiveData,id -> peopleDao.findPersonById(id));
        mTaskMaster = new TaskMaster();
    }

    public TaskMaster getTaskMaster() {
        return mTaskMaster;
    }

    public void savePerson(@NonNull String key, @NonNull Person person, @NonNull TaskCallback callback) {
        Objects.requireNonNull(person, "null == person");
        if (person.getId() > 0) {
            editPerson(key, person, callback);
        }
        else {
            addPerson(key, person, callback);
        }
    }

    public void addPerson(@NonNull String key, @NonNull Person person, @NonNull TaskCallback callback) {
        Objects.requireNonNull(person,"null == person");
        Task task = new Task() {
            @NonNull
            @Override
            protected TaskResult execute() {
                TaskResult result = new TaskResult(Constants.DB_INSERT);
                result.parameter = person;
                try {
                    long id = peopleDao.insert(person);
                    result.result = id;
                    result.successful = id > 0;
                }
                catch (Exception ex) {
                    result.error = ex;
                    result.successful = false;
                }
                return result;
            }
        };
        task.addCallback(callback);
        mTaskMaster.execute(key,task);
    }

    public void editPerson(@NonNull String key, @NonNull Person person, @NonNull TaskCallback callback) {
        Objects.requireNonNull(person,"null == person");
        Task task = new Task() {
            @NonNull
            @Override
            protected TaskResult execute() {
                TaskResult result = new TaskResult(Constants.DB_UPDATE);
                result.parameter = person;
                try {
                    int changes = peopleDao.update(person);
                    result.result = changes;
                    result.successful = changes > 0;
                }
                catch (Exception ex) {
                    result.error = ex;
                    result.successful = false;
                }
                return result;
            }
        };
        task.addCallback(callback);
        mTaskMaster.execute(key,task);
    }

    public void findPersonById(String key, long id, @NonNull TaskCallback callback) {
        Task task = new Task() {
            @NonNull
            @Override
            protected TaskResult execute() {
                TaskResult result = new TaskResult(Constants.DB_QUERY);
                result.parameter = id;
                try {
                    Person person = peopleDao.getPersonById(id);
                    result.result = person;
                    result.successful = null != person;
                }
                catch (Exception ex) {
                    result.error = ex;
                    result.successful = false;
                }
                return result;
            }
        };
        task.addCallback(callback);
        mTaskMaster.execute(key,task);
    }

    public LiveData<List<PersonDisplayModel>> getAllPeopleForDisplay() {
        if (null == mDisplayPeopleLiveData) {
            mDisplayPeopleLiveData = peopleDao.getAllPeopleForDisplay();
        }
        return mDisplayPeopleLiveData;
    }

    public LiveData<Person> getPersonById(long id) {
        mPersonIdLiveData.postValue(id);
        return mPersonLiveData;
    }

    public void removeMultiplePerson(String key, List<Long> ids, @NonNull TaskCallback callback) {
        Task task = new Task() {
            @NonNull
            @Override
            protected TaskResult execute() {
                TaskResult result = new TaskResult(Constants.DB_DELETE);
                result.parameter = ids;
                try {
                    int changes = peopleDao.deleteMultiple(ids);
                    result.result = changes;
                    result.successful = changes == ids.size();
                }
                catch (Exception ex) {
                    result.error = ex;
                    result.successful = false;
                }
                return result;
            }
        };
        task.addCallback(callback);
        mTaskMaster.execute(key,task);
    }
 }
