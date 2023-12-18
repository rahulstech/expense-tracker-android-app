package dreammaker.android.expensetracker.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import dreammaker.android.expensetracker.R;

public class MainActivity extends AppCompatActivity implements ActivityModelProvider {

    private ActivityModel mActivityModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityModel = new ActivityModel(this);
        setContentView(R.layout.main);
        //getSupportFragmentManager()
        //        .findFragmentById(R.id.nav_host_fragment_container);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mActivityModel.setSupportToolbar(toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        checkFirstRestoreRequired();
        //checkForAutoDelete();
    }

    private void checkFirstRestoreRequired() {
        /*BackupRestoreHelper.checkAppFirstRestoreRequired(this, isRequired -> {
            if (isRequired)
                startActivity(new Intent(MainActivity.this,RestoreActivity.class));
        });*/
    }

    @Override
    public void onBackPressed() {
        if (!mActivityModel.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public ActivityModel getActivityModel() {
        return mActivityModel;
    }
}
