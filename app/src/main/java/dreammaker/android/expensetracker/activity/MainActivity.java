package dreammaker.android.expensetracker.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.databinding.MainBinding;

public class MainActivity extends AppCompatActivity implements ActivityModelProvider {

    private ActivityModel mActivityModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityModel = new ActivityModel(this);
        setContentView(R.layout.main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActivityModel.setSupportToolbar(toolbar);
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
        if (shouldPopBack()) {
            super.onBackPressed();
        }
    }

    private boolean shouldPopBack() {
        return !mActivityModel.onBackPressed();
    }

    @Override
    public ActivityModel getActivityModel() {
        return mActivityModel;
    }

    private boolean onClickLeftDrawerItem(MenuItem item) {
        //mBinding.drawerLayout.closeDrawer(mBinding.drawer);
        int id = item.getItemId();
        if (R.id.backup_restore == id) {
            //startActivity(new Intent(requireContext(), BackupRestoreActivity.class));
            return true;
        }
        else if (R.id.settings == id) {
            //startActivity(new Intent(requireContext(), SettingsActivity.class));
            return true;
        }
        return false;
    }
}
