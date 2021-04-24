package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import dreammaker.android.expensetracker.database.ExpensesDao;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.util.AppExecutor;
import dreammaker.android.expensetracker.util.Check;

import static dreammaker.android.expensetracker.util.Helper.ACTION_DELETE;
import static dreammaker.android.expensetracker.util.Helper.ACTION_EDIT;
import static dreammaker.android.expensetracker.util.Helper.ACTION_INSERT;

public abstract class BaseViewModel extends AndroidViewModel {

    private static final String TAG = "BaseViewModel";

    private ExpensesDatabase db;
    private ExpensesDao dao;
    private OperationCallback callback;
    private SparseArray<Object> savedData;

    protected BaseViewModel(@NonNull Application application) {
        super(application);
        db = ExpensesDatabase.getInstance(application.getApplicationContext());
        dao = db.getDao();
        savedData = new SparseArray<>();
    }

    protected ExpensesDao getDao(){
        return dao;
    }

    protected ExpensesDatabase getDatabase(){
        return db;
    }

    protected  void notifyOperationCallback(int action, boolean success) {
        notifyOperationCallback(action, success, null);
    }

    protected void notifyOperationCallback(int action, boolean success, Object extra) {
        AppExecutor.getMainThreadExecutor().execute(()->{
            if (hasOperationCallback()) {
                getOperationCallback().setExtra(extra);
                switch (action) {
                    case ACTION_INSERT: getOperationCallback().onCompleteInsert(success);
                    break;
                    case ACTION_EDIT: getOperationCallback().onCompleteUpdate(success);
                    break;
                    case ACTION_DELETE: getOperationCallback().onCompleteDelete(success);
                }
            }
        });
    }

    public void setOperationCallback(OperationCallback callback){
        this.callback = callback;
    }

    public OperationCallback getOperationCallback(){
        return callback;
    }

    public boolean hasOperationCallback(){
        return Check.isNonNull(callback);
    }

    public void putSavedData(int id, Object data) {
        savedData.put(id, data);
    }

    public <D> D getSavedData(int id, D ifNotExists) {
        return (D) savedData.get(id, ifNotExists);
    }

    public void removeSavedData(int... ids) {
        if (Check.isNonNull(ids)) {
            for (int id : ids) {
                savedData.remove(id);
            }
        }
    }
}
