package dreammaker.android.expensetracker.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.WorkInfo;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.backup.BackupRestoreHelper;
import dreammaker.android.expensetracker.backup.WorkActionService;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Date;
import dreammaker.android.expensetracker.view.AbsSelectionListAdapter;
import dreammaker.android.expensetracker.view.BaseSpinnerAdapter;
import dreammaker.android.expensetracker.viewmodel.BackupRestoreViewModel;

import static dreammaker.android.expensetracker.BuildConfig.DEBUG;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.FAIL_FILE_ACCESS;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.FAIL_NON_EMPTY;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.FAIL_NO_DATA;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.FAIL_NO_FILE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.FAIL_UNKNOWN;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_FAILURE_CODE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_MESSAGE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_PROGRESS_CURRENT;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_PROGRESS_MAX;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.SCHEDULE_DAILY;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.SCHEDULE_MONTHLY;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.SCHEDULE_NEVER;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.SCHEDULE_WEEKLY;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.setBackupAutoScheduleDuration;
import static dreammaker.android.expensetracker.backup.WorkActionService.ACTION_CANCEL_BACKUP;
import static dreammaker.android.expensetracker.backup.WorkActionService.ACTION_CANCEL_RESTORE;
import static dreammaker.android.expensetracker.backup.WorkActionService.ACTION_RETRY_BACKUP;
import static dreammaker.android.expensetracker.backup.WorkActionService.ACTION_RETRY_RESTORE;

public class BackupRestoreActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "BackupRestoreActivity";

    private static final int RC_CHOOSE_FILE_RESTORE = 100;
    private static final int RC_PERMISSION = 101;

    private Button backup;
    private TextView lastBackup;
    private Button restore;
    private View backupProgressSection;
    private View restoreProgressSection;
    private TextView backupProgressMessage;
    private ProgressBar backupProgress;
    private ImageButton btnBackupCancel;
    private TextView restoreProgressMessage;
    private ProgressBar restoreProgress;
    private ImageButton btnRestoreCancel;
    private Spinner backupScheduleSpinner;

    private BackupRestoreViewModel viewModel;
    private BackupScheduleSpinnerAdapter backupScheduleSpinnerAdapter;
    private AdapterView.OnItemSelectedListener onBackupScheduleSpinnerItemClick = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            int code = backupScheduleSpinnerAdapter.getCode(position);
            setBackupAutoScheduleDuration(BackupRestoreActivity.this,code);
            BackupRestoreHelper.backupNext(BackupRestoreActivity.this);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        lastBackup = findViewById(R.id.last_backup);
        backup = findViewById(R.id.backup);
        restore = findViewById(R.id.restore);
        backupProgressSection = findViewById(R.id.backup_progress_section);
        backupProgressMessage = findViewById(R.id.backup_progress_message);
        backupProgress = findViewById(R.id.backup_progress);
        btnBackupCancel = findViewById(R.id.btn_cancel_backup);
        restoreProgressSection = findViewById(R.id.restore_progress_section);
        restoreProgressMessage = findViewById(R.id.restore_progress_message);
        restoreProgress = findViewById(R.id.restore_progress);
        btnRestoreCancel = findViewById(R.id.btn_cancel_restore);
        backupScheduleSpinner = findViewById(R.id.backup_schedule_spinner);
        backupScheduleSpinnerAdapter = new BackupScheduleSpinnerAdapter(this);
        backupScheduleSpinner.setAdapter(backupScheduleSpinnerAdapter);
        backupScheduleSpinner.setSelection(
                backupScheduleSpinnerAdapter.getPositionForCode(
                        BackupRestoreHelper.getBackupAutoScheduleDuration(this)));

        setLastBackupDate();
        backup.setOnClickListener(this);
        restore.setOnClickListener(this);
        btnBackupCancel.setOnClickListener(this);
        btnRestoreCancel.setOnClickListener(this);
        backupScheduleSpinner.setOnItemSelectedListener(onBackupScheduleSpinnerItemClick);
        viewModel = new ViewModelProvider(this,
                new ViewModelProvider.AndroidViewModelFactory(getApplication()))
                .get(BackupRestoreViewModel.class);
        viewModel.getBackupWorkInfoLiveData().observe(this, infos -> onUpdateBackupProgress(infos));
        viewModel.getRestoreWorkInfoLiveData().observe(this, infos -> onUpdateRestoreProgress(infos));
    }

    @Override
    public void onClick(View v) {
        if (checkPermissionOrRequest()) {
            if (backup == v) {
                onClickBackup();
            } else if (restore == v) {
                onClickRestore();
            }
        }
        if (v == btnBackupCancel) {
            startService(new Intent(BackupRestoreActivity.this, WorkActionService.class)
                    .setAction(ACTION_CANCEL_BACKUP));
        }
        else if (v == btnRestoreCancel) {
            startService(new Intent(BackupRestoreActivity.this, WorkActionService.class)
                    .setAction(ACTION_CANCEL_RESTORE));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RC_CHOOSE_FILE_RESTORE == requestCode && RESULT_OK == resultCode) {
            if (Check.isNull(data) || Check.isNull(data.getData())) {
                Toast.makeText(this, R.string.message_restore_file_not_selected, Toast.LENGTH_SHORT).show();
                return;
            }
            onRestore(data.getData());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (RC_PERMISSION == requestCode && PackageManager.PERMISSION_DENIED == grantResults[0]
                && !ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.description_open_settings_allow_permission)
                    .setPositiveButton(R.string.open_settings, (dialogInterface, i) ->
                            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .setData(Uri.fromParts("package", getPackageName(), null))
                                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    private void onClickBackup() {
        hideProgressSections();
        BackupRestoreHelper.backupNow(this);
    }

    private void onClickRestore() {
        hideProgressSections();
        startActivityForResult(Intent.createChooser(new Intent(Intent.ACTION_GET_CONTENT)
                .setType("*/*"), getString(R.string.title_choose_restore_file)), RC_CHOOSE_FILE_RESTORE);
    }

    private void onRestore(@NonNull Uri uri) {
        BackupRestoreHelper.restore(this,uri);
    }

    private void hideProgressSections() {
        backupProgressSection.setVisibility(View.GONE);
        restoreProgressSection.setVisibility(View.GONE);
    }

    private void setLastBackupDate() {
        Date date = BackupRestoreHelper.getLastLocalBackDate(this);
        lastBackup.setText(Check.isNull(date) ? getString(R.string.backup_never) : date.format("dd-MMM-yyyy"));
    }

    private boolean checkPermissionOrRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && PackageManager.PERMISSION_GRANTED !=
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RC_PERMISSION);
            return false;
        }
        return true;
    }

    private void onUpdateBackupProgress(@Nullable List<WorkInfo> infos) {
        if (null != infos && !infos.isEmpty()) {
            WorkInfo info = infos.get(0);
            if (DEBUG) Log.d(TAG,info.toString());
            WorkInfo.State state = info.getState();
            if (WorkInfo.State.RUNNING == state) {
                backupProgressSection.setVisibility(View.VISIBLE);
                Data progress = info.getProgress();
                String message = progress.getString(KEY_MESSAGE);
                int max = progress.getInt(KEY_PROGRESS_MAX,100);
                int current = progress.getInt(KEY_PROGRESS_CURRENT,0);
                updateBackupProgressSection(message,max,current,false,true);
            }
            else if (WorkInfo.State.SUCCEEDED == state) {
                setLastBackupDate();
                backupProgressSection.setVisibility(View.GONE);
            }
            else if (WorkInfo.State.CANCELLED == state) {
                updateBackupProgressSection(getString(R.string.message_backup_canceled),
                        100,0,false,true);
            }
            else if (WorkInfo.State.FAILED == state) {
                Data output = info.getOutputData();
                int failureCode = output.getInt(KEY_FAILURE_CODE,-1);
                switch (failureCode) {
                    case FAIL_NO_DATA: {
                        updateBackupProgressSection(getString(R.string.message_fail_no_data),
                                100,0,false,true);
                    }
                    break;
                    case FAIL_UNKNOWN: {
                        updateBackupProgressSection(getString(R.string.message_backup_fail_unknown),
                                100,0,false,false);
                    }
                    break;
                    case FAIL_FILE_ACCESS: {
                        updateBackupProgressSection(getString(R.string.message_error_file_access),
                                100,0,false,true);
                    }
                }
            }
            else {
                backupProgressSection.setVisibility(View.GONE);
            }
        }
    }

    private void onUpdateRestoreProgress(@Nullable List<WorkInfo> infos) {
        if (null != infos && !infos.isEmpty()) {
            WorkInfo info = infos.get(0);
            if (DEBUG) Log.d(TAG,info.toString());
            WorkInfo.State state = info.getState();
            if (WorkInfo.State.RUNNING == state) {
                restoreProgressSection.setVisibility(View.VISIBLE);
                Data progress = info.getProgress();
                String message = progress.getString(KEY_MESSAGE);
                int max = progress.getInt(KEY_PROGRESS_MAX,100);
                int current = progress.getInt(KEY_PROGRESS_CURRENT,0);
                updateRestoreProgressSection(message,max,current,true,true);
            }
            else if (WorkInfo.State.CANCELLED == state) {
                updateBackupProgressSection(getString(R.string.message_restore_canceled),
                        100,0,false,true);
            }
            else if (WorkInfo.State.FAILED == state) {
                Data output = info.getOutputData();
                int failureCode = output.getInt(KEY_FAILURE_CODE,-1);
                switch (failureCode) {
                    case FAIL_UNKNOWN: {
                        updateRestoreProgressSection(getString(R.string.message_restore_fail_unknown),
                                100,0,false,false);
                    }
                    break;
                    case FAIL_FILE_ACCESS: {
                        updateBackupProgressSection(getString(R.string.message_error_file_access),
                                100,0,false,true);
                    }
                    break;
                    case FAIL_NON_EMPTY: {
                        updateRestoreProgressSection(getString(R.string.message_restore_non_empty_database),
                                100,0,false,
                                true);
                    }
                    break;
                    case FAIL_NO_FILE: {
                        updateRestoreProgressSection(getString(R.string.message_restore_file_not_selected),
                                100,0,false,true);
                    }
                }
            }
            else {
                restoreProgressSection.setVisibility(View.GONE);
            }
        }
    }

    private void updateBackupProgressSection(String message,
                                             int max, int current, boolean inderrminant,
                                             boolean showCancel) {
        backupProgress.setIndeterminate(inderrminant);
        backupProgress.setMax(max);
        backupProgress.setProgress(current);
        backupProgressMessage.setText(message);
        btnBackupCancel.setVisibility(showCancel ? View.VISIBLE : View.GONE);
    }

    private void updateRestoreProgressSection(String message,
                                              int max, int current, boolean indeterminant,
                                              boolean showCancel) {
        restoreProgress.setIndeterminate(indeterminant);
        restoreProgress.setMax(max);
        restoreProgress.setProgress(current);
        restoreProgressMessage.setText(message);
        btnRestoreCancel.setVisibility(showCancel ? View.VISIBLE : View.GONE);
    }

    private void changeEnableBackupButtonAndAutoSchedulesSpinner(boolean enable) {
        backup.setClickable(enable);
        if (enable)
            backupScheduleSpinner.setOnItemSelectedListener(onBackupScheduleSpinnerItemClick);
        else
            backupScheduleSpinner.setOnItemSelectedListener(null);
    }

    private static class BackupScheduleSpinnerAdapter extends BaseSpinnerAdapter<String> {

        protected BackupScheduleSpinnerAdapter(Context context) {
            super(context);
            ArrayList<String> list = new ArrayList<>();
            list.add(context.getString(R.string.schedule_daily));
            list.add(context.getString(R.string.schedule_weekly));
            list.add(context.getString(R.string.schedule_monthly));
            list.add(context.getString(R.string.schedule_never));
            super.changeList(list);
        }

        @Override
        protected long getItemId(@NonNull String item) {
            return item.hashCode();
        }

        public int getCode(int position) {
            switch (position) {
                case 0: return SCHEDULE_DAILY;
                case 1: return SCHEDULE_WEEKLY;
                case 2: return SCHEDULE_MONTHLY;
                case 3: return SCHEDULE_NEVER;
                default:
                    throw new IndexOutOfBoundsException("invalid position="+position);
            }
        }

        public int getPositionForCode(int code) {
            switch (code) {
                case SCHEDULE_DAILY: return 0;
                case SCHEDULE_WEEKLY: return 1;
                case SCHEDULE_MONTHLY: return 2;
                case SCHEDULE_NEVER: return 3;
                default:
                    throw new RuntimeException("invalid code="+code);
            }
        }

        @Override
        protected void onBindViewHolder(SpinnerViewHolder vh, int position) {
            vh.setContentText(getItem(position));
        }
    }
}
