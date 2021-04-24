package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;
import android.util.Log;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import dreammaker.android.expensetracker.database.AboutPerson;
import dreammaker.android.expensetracker.database.Person;
import dreammaker.android.expensetracker.util.AppExecutor;
import dreammaker.android.expensetracker.util.Check;

import static dreammaker.android.expensetracker.util.Helper.ACTION_DELETE;
import static dreammaker.android.expensetracker.util.Helper.ACTION_EDIT;
import static dreammaker.android.expensetracker.util.Helper.ACTION_INSERT;

public class PersonsViewModel extends BaseViewModel {

    private static final String TAG = "PersonsViewModel";

    private LiveData<List<AboutPerson>> persons;
    private MutableLiveData<Person> selectedPersonLiveData;

    public PersonsViewModel(@NonNull Application application) {
        super(application);
        persons = getDao().getAllPersonsForListOfPersons();
        selectedPersonLiveData = new MutableLiveData<>();
    }

    public void setSelectedPerson(Person person) {
        Check.isNonNull(person, "selected person must be non null");
        selectedPersonLiveData.setValue(person.clone());
    }

    public LiveData<Person> getSelectedPersonLiveData() {
        return selectedPersonLiveData;
    }

    public Person getSelectedPerson() { return selectedPersonLiveData.getValue(); }

    public void insertPerson(final Person person){
        AppExecutor.getDiskOperationsExecutor().execute(() -> {
            boolean success = false;
            try{
                success = getDao().insertPerson(person) > 0;
            }
            catch (Exception e){
                Log.e(TAG, "error while inserting new person with message: "+e.getMessage());
            }
            finally {
                notifyOperationCallback(ACTION_INSERT, success);
            }
        });
    }

    public void updatePerson(final Person person){
        AppExecutor.getDiskOperationsExecutor().execute(() -> {
            boolean success = false;
            try {
                success = getDao().updatePerson(person) > 0;
            }
            catch (Exception e){
                Log.e(TAG, "error while updating person with message: "+e.getMessage());
            }
            finally {
                notifyOperationCallback(ACTION_EDIT, success);
            }
        });
    }

    public void deletePerson(final Person... people){
        if (Check.isNonNull(people)) {
            AppExecutor.getDiskOperationsExecutor().execute(() -> {
                int changes = 0;
                try{
                    changes = getDao().deletePersons(people);
                }
                catch (Exception e){
                    Log.e(TAG, "error while deleting person(s) with message: "+e.getMessage());
                }
                finally {
                    notifyOperationCallback(ACTION_DELETE, changes > 0, changes);
                }
            });
        }
    }

    public LiveData<List<AboutPerson>> getPersons() {
        return persons;
    }
}
