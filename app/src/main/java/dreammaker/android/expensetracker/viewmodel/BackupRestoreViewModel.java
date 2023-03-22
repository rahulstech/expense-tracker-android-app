package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;

import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.BACKUP_WORK_TAG;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.RESTORE_WORK_TAG;

public class BackupRestoreViewModel extends BaseViewModel {

    private LiveData<List<WorkInfo>> backupWorkInfoLiveData;
    private WorkManager workManager;

    public BackupRestoreViewModel(@NonNull Application application) {
        super(application);
        workManager = WorkManager.getInstance(application.getApplicationContext());
        backupWorkInfoLiveData = workManager.getWorkInfosForUniqueWorkLiveData(BACKUP_WORK_TAG);
    }

    public LiveData<List<WorkInfo>> getBackupWorkInfoLiveData() {
        return backupWorkInfoLiveData;
    }
}
