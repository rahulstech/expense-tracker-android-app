package dreammaker.android.expensetracker.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.backup.BackupRestoreHelper;
import dreammaker.android.expensetracker.ui.main.MainActivity;

public class RestoreActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnRestore;
    private Button btnCancel;

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    onChooseRestoreFile();
                }
            });

    ActivityResultLauncher<Intent> restoreFileChooserLauncher =
    registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
            Uri uri = result.getData().getData();
            startRestore(uri);
        } else {
            Toast.makeText(RestoreActivity.this, R.string.message_restore_file_not_selected, Toast.LENGTH_SHORT).show();
        }
        startActivity(new Intent(RestoreActivity.this, MainActivity.class));
        finish();
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore);
        btnRestore = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        btnRestore.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (btnRestore == v) {
            onRestore();
        }
        else {
            onCancel();
        }
    }

    @Override
    public void onBackPressed() {}

    private void onRestore() {
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            onChooseRestoreFile();
        }
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
        else {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void onCancel() {
        BackupRestoreHelper.setFirstRestoreAsked(this, true);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void onChooseRestoreFile() {
        restoreFileChooserLauncher.launch(Intent.createChooser(new Intent(Intent.ACTION_GET_CONTENT)
                .setType("*/*"), getString(R.string.title_choose_restore_file)));
    }

    private void startRestore(@NonNull Uri restoreFrom) {
        BackupRestoreHelper.restore(this,restoreFrom);
    }
}