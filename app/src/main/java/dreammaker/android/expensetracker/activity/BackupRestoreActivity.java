package dreammaker.android.expensetracker.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.work.WorkInfo;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.backup.BackupRestoreHelper;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Date;
import dreammaker.android.expensetracker.view.BaseSpinnerAdapter;
import dreammaker.android.expensetracker.viewmodel.BackupRestoreViewModel;

import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.KEY_LAST_LOCAL_BACKUP_DATE;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.SCHEDULE_DAILY;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.SCHEDULE_MONTHLY;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.SCHEDULE_NEVER;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.SCHEDULE_WEEKLY;
import static dreammaker.android.expensetracker.backup.BackupRestoreHelper.setBackupAutoScheduleDuration;

public class BackupRestoreActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "BackupRestoreActivity";

    private Button backup;
    private TextView lastBackup;
    private Spinner backupScheduleSpinner;

    private BackupRestoreViewModel viewModel;
    private BackupScheduleSpinnerAdapter backupScheduleSpinnerAdapter;

    private AdapterView.OnItemSelectedListener onBackupScheduleSpinnerItemClick = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            int code = backupScheduleSpinnerAdapter.getCode(position);
            setBackupAutoScheduleDuration(BackupRestoreActivity.this,code);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = (pref, key) -> {
        if (KEY_LAST_LOCAL_BACKUP_DATE.equals(key)) {
            setLastBackupDate();
        }
    };

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    onStartLocalBackup();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        lastBackup = findViewById(R.id.last_backup);
        backup = findViewById(R.id.backup);
        backupScheduleSpinner = findViewById(R.id.backup_schedule_spinner);
        backupScheduleSpinnerAdapter = new BackupScheduleSpinnerAdapter(this);
        backupScheduleSpinner.setAdapter(backupScheduleSpinnerAdapter);
        backupScheduleSpinner.setSelection(
                backupScheduleSpinnerAdapter.getPositionForCode(
                        BackupRestoreHelper.getBackupAutoScheduleDuration(this)));

        setLastBackupDate();
        backup.setOnClickListener(this);
        backupScheduleSpinner.setOnItemSelectedListener(onBackupScheduleSpinnerItemClick);
        viewModel = new ViewModelProvider(this,
                new ViewModelProvider.AndroidViewModelFactory(getApplication()))
                .get(BackupRestoreViewModel.class);
        viewModel.getBackupWorkInfoLiveData().observe(this, infos -> onUpdateBackupProgress(infos));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public void onClick(View v) {
        if (backup == v) {
            onClickBackup();
        }
    }

    private void onClickBackup() {
        if (PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE))
            onStartLocalBackup();
        else if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
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
        else
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void onStartLocalBackup() {
        BackupRestoreHelper.backupNow(this);
    }

    private void hideProgressSections() {
        //backupProgressSection.setVisibility(View.GONE);
    }

    private void setLastBackupDate() {
        Date date = BackupRestoreHelper.getLastLocalBackDate(this);
        lastBackup.setText(Check.isNull(date) ? getString(R.string.backup_never) : date.format("dd-MMM-yyyy"));
    }

    private void onUpdateBackupProgress(@Nullable List<WorkInfo> infos) {
        if (null != infos && !infos.isEmpty()) {
            WorkInfo.State state = infos.get(0).getState();
            // TODO: handle disable
        }
    }

    /*
    private void updateBackupProgressSection(String message,
                                             int max, int current, boolean inderrminant,
                                             boolean showCancel) {
        //backupProgress.setIndeterminate(inderrminant);
        //backupProgress.setMax(max);
        //backupProgress.setProgress(current);
        //backupProgressMessage.setText(message);
        //btnBackupCancel.setVisibility(showCancel ? View.VISIBLE : View.GONE);
    }
    */

    private void changeEnableBackupButtonAndAutoSchedulesSpinner(boolean enable) {
        backup.setClickable(enable);
        backupScheduleSpinner.setClickable(enable);
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
