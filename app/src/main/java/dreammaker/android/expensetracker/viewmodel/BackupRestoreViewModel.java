package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

public class BackupRestoreViewModel extends BaseViewModel {

    //private LiveData<List<WorkInfo>> backupWorkInfoLiveData;
    //private WorkManager workManager;

    public BackupRestoreViewModel(@NonNull Application application) {
        super(application);
        //workManager = WorkManager.getInstance(application.getApplicationContext());
        //backupWorkInfoLiveData = workManager.getWorkInfosForUniqueWorkLiveData(BACKUP_WORK_TAG);
    }

    //public LiveData<List<WorkInfo>> getBackupWorkInfoLiveData() {
    //    return backupWorkInfoLiveData;
    //}
}
